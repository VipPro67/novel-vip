from psycopg2.extras import execute_values
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
import unicodedata
import os
import bleach
from dotenv import load_dotenv
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type, before_sleep_log
import aiohttp


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
        self.rate_limit_delay = 0.1
        self.max_concurrency = 5
        self.db_pool = SimpleConnectionPool(1, 10, **db_config)
        self.checkpoint_list_file = 'checkpoint_list.txt'
        self.checkpoint_details_file = 'checkpoint_details.txt'

    def __del__(self):
        if self.db_pool:
            self.db_pool.closeall()

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=3, max=10),
        retry=retry_if_exception_type(
            (aiohttp.ClientError, aiohttp.http_exceptions.HttpProcessingError)),
        before_sleep=before_sleep_log(logging, logging.WARNING)
    )
    async def fetch_page(self, session, url):
        timeout = aiohttp.ClientTimeout(total=15)
        async with session.get(url, headers=self.headers, timeout=timeout) as response:
            response.raise_for_status()
            return await response.text()
    # Load environment variables

    def normalize_title(self, title: str):
        nfkd = unicodedata.normalize('NFKD', title)
        no_accent = ''.join(c for c in nfkd if not unicodedata.combining(c))
        s = no_accent.lower()
        s = re.sub(r'\[.*?\]|\(.*?\)', '', s)
        s = re.sub(
            r'\b(phần|phan|tap|tập|edit|fanfic|hệ thống|đồng nhân|dịch|truyện sắc|xuyên nhanh|trùng sinh|hệ thống)\b\s*\d*', '', s)
        s = re.sub(r'[^a-z0-9\s]', '', s)
        return re.sub(r'\s+', ' ', s).strip()

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
                        'title_normalized': self.normalize_title(title),
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

    async def get_novel_details(self, session, novel):
        try:
            detail_url = f"{self.base_url}/{novel['slug']}"
            html = await self.fetch_page(session, detail_url)
            soup = BeautifulSoup(html, 'html.parser')
            description_tag = soup.select_one('.desc-text')
            description = bleach.clean(
                description_tag.get_text(strip=True) if description_tag else '', strip=True)
            # Genre tags
            genre_div = soup.find("h3", string="Thể loại:").find_parent("div")
            genres = [a.text.strip() for a in genre_div.find_all("a")]
            novel['genres'] = genres
            # Extract rating
            rating_value = soup.find(
                "span", itemprop="ratingValue").text.strip()
            # Update novel details
            cover_image = soup.select_one('.book img')
            novel['cover_image'] = cover_image['src'] if cover_image and 'src' in cover_image.attrs else novel['cover_image']
            novel['description'] = description
            novel['rating'] = round(
                float(rating_value)/2, 2) if rating_value else 0.0
            novel['status'] = 'active'
            novel['updated_at'] = datetime.now(timezone.utc)
            logging.debug(f"Fetched details for novel: {novel['title']}")
        except Exception as e:
            logging.error(f"Failed to fetch details for {novel['title']}: {e}")

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

    def save_categories_tags_geners(self, cursor, novel_list, genre_cache):
        genre_pairs = []
        new_genres = []

        for novel in novel_list:
            genres = novel.get('genres', [])
            for genre in genres:
                genre = genre.strip()
                if not genre:
                    continue

                genre_lower = genre.lower()

                if genre_lower in genre_cache:
                    genre_id = genre_cache[genre_lower]
                else:
                    cursor.execute(
                        "SELECT id FROM genres WHERE unaccent(lower(name)) = unaccent(lower(%s)) LIMIT 1",
                        (genre,)
                    )
                    result = cursor.fetchone()
                    if result:
                        genre_id = result[0]
                    else:
                        genre_id = str(uuid.uuid4())
                        new_genres.append((genre_id, genre))
                    genre_cache[genre_lower] = genre_id

                genre_pairs.append((novel['id'], genre_cache[genre_lower]))

        if new_genres:
            execute_values(
                cursor,
                "INSERT INTO genres (id, name) VALUES %s ON CONFLICT DO NOTHING",
                new_genres
            )

        if genre_pairs:
            execute_values(
                cursor,
                "INSERT INTO novel_genres (novel_id, genre_id) VALUES %s ON CONFLICT DO NOTHING",
                genre_pairs
            )

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
                if novel_info_list is None:
                    logging.error(
                        f"Failed to fetch novels from page {page}, skipping")
                    continue

                async def delayed_task(session, novel, delay):
                    await asyncio.sleep(delay)
                    return await self.get_novel_details(session, novel)

                tasks = [
                    delayed_task(session, novel, i * 0.075)
                    for i, novel in enumerate(novel_info_list)
                ]
                await asyncio.gather(*tasks)
                novel_ids = self.save_to_postgresql(novel_info_list)
                total_novels_saved += len(novel_ids)
                conn = self.db_pool.getconn()
                genre_cache = {}
                try:
                    cursor = conn.cursor()
                    valid_novels = [
                        novel for novel in novel_info_list
                        if isinstance(novel, dict) and novel.get('id') in novel_ids
                    ]

                    self.save_categories_tags_geners(
                        cursor, valid_novels, genre_cache)
                    total_details_updated += len(valid_novels)

                    conn.commit()
                    cursor.close()
                finally:
                    self.db_pool.putconn(conn)

                self.save_checkpoint(self.checkpoint_list_file, page)
                await asyncio.sleep(self.rate_limit_delay)

        elapsed_time = time.time() - start_time
        logging.info(
            f"Crawling completed: {total_novels_saved} novels saved, {total_details_updated} details updated in {elapsed_time:.2f} seconds")
        return total_novels_saved, total_details_updated


async def main():
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

    total_novels, total_details = await crawler.crawl_novels(max_pages=1341)
    print(f"Total novels saved: {total_novels}")
    print(f"Total novel details updated: {total_details}")


if __name__ == "__main__":
    asyncio.run(main())
# This code is designed to crawl novels from a specific website, extract their details, and save them to a PostgreSQL database.
