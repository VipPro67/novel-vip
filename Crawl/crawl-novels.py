import re
import uuid
import logging
import time
import asyncio
import aiohttp
from datetime import datetime, timezone
from bs4 import BeautifulSoup
import psycopg2
from psycopg2.pool import SimpleConnectionPool
from unidecode import unidecode
import os
import bleach
from dotenv import load_dotenv
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s - %(levelname)s - %(message)s')


class NovelCrawler:
    def __init__(self, base_url, db_config):
        self.base_url = base_url
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5'
        }
        self.rate_limit_delay = 0.2
        self.max_concurrency = 5
        self.db_pool = SimpleConnectionPool(1, 10, **db_config)
        self.checkpoint_list_file = 'checkpoint_list.txt'
        self.checkpoint_details_file = 'checkpoint_details.txt'

    def __del__(self):
        if self.db_pool:
            self.db_pool.closeall()

    def normalize_title(self, title):
        return unidecode(title).lower()

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type(
            (aiohttp.ClientError, aiohttp.http_exceptions.HttpProcessingError))
    )
    async def fetch_page(self, session, url):
        logging.debug(f"Fetching URL: {url}")
        timeout = aiohttp.ClientTimeout(total=15)
        async with session.get(url, headers=self.headers, timeout=timeout) as response:
            response.raise_for_status()
            text = await response.text()
            logging.debug(
                f"Response status code: {response.status}, content length: {len(text)}")
            return text

    async def get_novel_info(self, session, list_url):
        try:
            html = await self.fetch_page(session, list_url)
            soup = BeautifulSoup(html, 'html.parser')
            novel_items = soup.find_all(
                'div', class_='row', itemtype='https://schema.org/Book')
            logging.debug(
                f"Found {len(novel_items)} novel items on {list_url}")

            novel_data_list = []
            for item in novel_items:
                try:
                    title_tag = item.select_one('.truyen-title a')
                    if not title_tag:
                        continue
                    title = title_tag.get_text(strip=True)
                    slug = title_tag['href'].strip('/').split('/')[-1]

                    author_tag = item.select_one('.author')
                    author = (author_tag.get_text(strip=True).replace('✍', '').strip()
                              if author_tag and author_tag.get_text(strip=True) else 'Unknown')

                    cover_div = item.select_one('.lazyimg')
                    cover_image = cover_div['data-image'] if cover_div and 'data-image' in cover_div.attrs else 'https://cdn.apptruyen.lol/images/public/default-image.jpg'

                    chapter_link = item.select_one('.chapter-text')
                    total_chapters = 0
                    if chapter_link:
                        chapter_text = chapter_link.find_parent(
                            'a').get_text(strip=True)
                        match = re.search(r'Chương\s*(\d+)', chapter_text)
                        if match:
                            total_chapters = int(match.group(1))

                    novel_data = {
                        'id': str(uuid.uuid4()),
                        'title': title,
                        'title_normalized': self.normalize_title(title).replace('[dich] ', ''),
                        'author': author,
                        'slug': slug,
                        'cover_image': cover_image,
                        'description': '',
                        'status': 'crawling',
                        'total_chapters': total_chapters,
                        'rating': 0.0,
                        'views': 0,
                        'created_at': datetime.now(timezone.utc),
                        'updated_at': datetime.now(timezone.utc)
                    }

                    novel_data_list.append(novel_data)
                    logging.debug(f"Added novel: {title}")

                except Exception as e:
                    logging.error(f"Error processing novel item: {e}")
                    continue

            return novel_data_list if novel_data_list else None

        except Exception as e:
            logging.error(f"Failed to process page {list_url}: {e}")
            return None

    def insert_file_metadata(self, cursor, novel):
        # Check if file metadata with the same URL already exists
        select_query = "SELECT id FROM file_metadata WHERE file_url = %s LIMIT 1"
        cursor.execute(select_query, (novel['cover_image'],))
        existing = cursor.fetchone()

        if existing:
            return existing[0]  # Return existing file ID

        # Insert new metadata
        file_id = str(uuid.uuid4())
        now = datetime.now(timezone.utc)
        insert_file_query = """
            INSERT INTO file_metadata (
                id, file_name, file_url, content_type, type,
                public_id, uploaded_at, last_modified_at, size
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        file_values = (
            file_id,
            f"{novel['slug']}.jpg",
            novel['cover_image'],
            'image/jpeg',
            'image',
            novel['slug'],
            now,
            now,
            0
        )
        cursor.execute(insert_file_query, file_values)
        return file_id

    def save_to_postgresql(self, novel_info_list):
        if not novel_info_list:
            logging.warning("No novels to save")
            return []

        conn = None
        cursor = None
        try:
            conn = self.db_pool.getconn()
            cursor = conn.cursor()

            # Step 1: Insert file metadata and get cover image IDs
            for novel in novel_info_list:
                file_id = self.insert_file_metadata(cursor, novel)
                novel['cover_image_id'] = file_id

            # Step 2: Prepare SQL insert query for novels
            insert_query = """
                INSERT INTO novels (
                    id,
                    author,
                    cover_image_id,
                    created_at,
                    description,
                    rating,
                    slug,
                    status,
                    title,
                    title_normalized,
                    total_chapters,
                    updated_at,
                    views,
                    is_public
                )
                VALUES %s
                ON CONFLICT (slug) DO NOTHING
                RETURNING id
            """

            # Step 3: Prepare values for batch insert
            values = [
                (
                    novel['id'],
                    novel['author'],
                    novel['cover_image_id'],
                    novel['created_at'],
                    novel['description'],
                    novel['rating'],
                    novel['slug'],
                    novel['status'],
                    novel['title'],
                    novel['title_normalized'],
                    novel['total_chapters'],
                    novel['updated_at'],
                    novel['views'],
                    True  # is_public
                )
                for novel in novel_info_list
            ]

            # Step 4: Execute batch insert
            from psycopg2.extras import execute_values
            execute_values(cursor, insert_query, values)

            # Step 5: Fetch inserted novel IDs
            novel_ids = [row[0]
                         for row in cursor.fetchall()] if cursor.description else []
            conn.commit()

            # Step 6: Logging
            logging.info(f"Saved {len(novel_ids)} novels to database")
            for novel, novel_id in zip(novel_info_list, novel_ids):
                logging.debug(
                    f"Saved novel: {novel['title']} with ID: {novel_id}")
            logging.warning(
                f"Skipped {len(novel_info_list) - len(novel_ids)} novels (possible duplicates)")

            return novel_ids

        except psycopg2.OperationalError as e:
            logging.error(f"Database connection failed: {e}")
            return []
        except psycopg2.IntegrityError as e:
            logging.error(f"Database integrity error: {e}")
            if conn:
                conn.rollback()
            return []
        except Exception as e:
            logging.error(f"Failed to save novels to database: {e}")
            if conn:
                conn.rollback()
            return []
        finally:
            if cursor:
                cursor.close()
            if conn:
                self.db_pool.putconn(conn)

    def load_checkpoint(self, checkpoint_file):
        if os.path.exists(checkpoint_file):
            with open(checkpoint_file, 'r') as f:
                try:
                    return f.read().strip()
                except ValueError:
                    logging.warning(
                        f"Invalid checkpoint file {checkpoint_file}, starting from beginning")
        return None

    def save_checkpoint(self, checkpoint_file, item):
        with open(checkpoint_file, 'w') as f:
            f.write(str(item))
        logging.debug(f"Saved checkpoint to {checkpoint_file}: {item}")

    async def crawl_novels(self, start_page=1, max_pages=10):
        total_novels_saved = 0
        total_details_updated = 0
        start_time = time.time()

        last_page = self.load_checkpoint(self.checkpoint_list_file)
        start_page = int(last_page) + \
            1 if last_page and last_page.isdigit() else start_page

        async with aiohttp.ClientSession() as session:
            for page in range(start_page, max_pages + 1):
                list_url = f"{self.base_url}/danh-sach/truyen-hot/trang-{page}"
                logging.info(f"Processing page {page}/{max_pages}")

                novel_info_list = await self.get_novel_info(session, list_url)
                if not novel_info_list:
                    logging.warning(
                        f"No novels found on page {page}, stopping")
                    break
                novel_ids = self.save_to_postgresql(novel_info_list)
                total_novels_saved += len(novel_ids)

                self.save_checkpoint(self.checkpoint_list_file, page)
                await asyncio.sleep(self.rate_limit_delay)

        elapsed_time = time.time() - start_time
        logging.info(
            f"Crawling completed: {total_novels_saved} novels saved, {total_details_updated} details updated in {elapsed_time:.2f} seconds")
        return total_novels_saved, total_details_updated


def main():
    db_config = {
        'dbname': 'postgres',
        'user': 'postgres.ijdorxaikoovmezfpdrz',
        'password': 'qZPAgRYvQM5z4TR1',
        'host': 'aws-0-ap-southeast-1.pooler.supabase.com',
        'port': 5432,
        'sslmode': 'require'
    }

    base_url = "https://truyenfull.vision"
    crawler = NovelCrawler(base_url, db_config)

    total_novels, total_details = asyncio.run(
        crawler.crawl_novels(max_pages=1341))
    print(f"Total novels saved: {total_novels}")
    print(f"Total novel details updated: {total_details}")


if __name__ == "__main__":
    main()
