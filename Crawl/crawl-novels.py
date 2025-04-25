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
from dotenv import load_dotenv
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')

class NovelCrawler:
    def __init__(self, base_url, db_config):
        self.base_url = base_url
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5'
        }
        self.rate_limit_delay = 0.2  # Delay between requests (seconds)
        self.max_concurrency = 5  # Maximum concurrent HTTP requests
        self.db_pool = SimpleConnectionPool(1, 10, **db_config)
        self.checkpoint_list_file = 'checkpoint_list.txt'
        self.checkpoint_details_file = 'checkpoint_details.txt'

    def clean_slug(self, title):
        """Generate a clean slug from the title."""
        slug = unidecode(title).lower()
        slug = re.sub(r'[^a-z0-9]+', '-', slug).strip('-')
        return slug

    def normalize_title(self, title):
        """Normalize title by removing diacritics and converting to lowercase."""
        return unidecode(title).lower()

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type((aiohttp.ClientError, aiohttp.http_exceptions.HttpProcessingError))
    )
    async def fetch_page(self, session, url):
        """Fetch a single page with retry logic."""
        logging.debug(f"Fetching URL: {url}")
        async with session.get(url, headers=self.headers, timeout=15) as response:
            response.raise_for_status()
            text = await response.text()
            logging.debug(f"Response status code: {response.status}, content length: {len(text)}")
            return text

    async def get_novel_info(self, session, list_url):
        """Extract novel information from a category page."""
        try:
            html = await self.fetch_page(session, list_url)
            soup = BeautifulSoup(html, 'html.parser')
            novel_items = soup.find_all('div', class_='py-4 px-4 flex items-center gap-4 hover:bg-gray-50')
            logging.debug(f"Found {len(novel_items)} novel items on {list_url}")

            novel_data_list = []
            for item in novel_items:
                try:
                    # Extract title and slug
                    title_link = item.find('a', class_='text-[#14394d] font-medium hover:underline line-clamp-2')
                    if not title_link:
                        logging.warning("Title link not found in item")
                        continue
                    title = title_link.get_text(strip=True).split('<span')[0].strip()
                    if not title:
                        logging.warning("Title is empty")
                        continue
                    title = title.replace('Hot', '').strip()
                    title = re.sub(r'\s+', ' ', title)
                    title = title.replace('Truyện', '').strip()
                    title = title.replace('Full', '').strip()
                    title = title.replace('[Dịch]', '').strip()
                    slug = title_link['href'].strip('/').split('/')[-1]
                    author = item.find('span', class_='italic text-sm').get_text(strip=True) if item.find('span', class_='italic text-sm') else 'Unknown'
                    base_slug = self.clean_slug(title)

                    # Extract cover image
                    cover_img = item.find('img', class_='w-full h-full object-cover')
                    cover_image = cover_img['src'] if cover_img else 'https://cdn.apptruyen.lol/images/public/default-image.jpg'

                    # Extract total chapters
                    chapter_link = item.find('a', class_='text-[#14394d] hover:underline')
                    total_chapters = 0
                    if chapter_link:
                        chapter_text = chapter_link.get_text(strip=True)
                        match = re.search(r'Chương\s+(\d+)', chapter_text)
                        if match:
                            total_chapters = int(match.group(1))

                    # Extract status
                    status_span = item.find('span', class_='bg-green-50 text-green-600')
                    status = 'completed' if status_span and status_span.get_text(strip=True) == 'Full' else 'updating'

                    novel_data = {
                        'id': str(uuid.uuid4()),
                        'title': title,
                        'title_nomalized': self.normalize_title(title),
                        'author': author,
                        'slug': slug,
                        'cover_image': cover_image,
                        'description': '',
                        'status': status,
                        'total_chapters': total_chapters,
                        'rating': 0.0,
                        'views': 0,
                        'created_at': datetime.now(timezone.utc),
                        'updated_at': datetime.now(timezone.utc)
                    }
                    novel_data_list.append(novel_data)
                    logging.debug(f"Added novel: {title}")
                except Exception as e:
                    logging.error(f"Error processing novel item on {list_url}: {e}")
                    continue

            logging.debug(f"Total novels extracted from {list_url}: {len(novel_data_list)}")
            return novel_data_list if novel_data_list else None

        except Exception as e:
            logging.error(f"Failed to process page {list_url}: {e}")
            return None

    async def get_novel_details(self, session, slug):
        """Extract detailed information from a novel's detail page."""
        detail_url = f"{self.base_url}/{slug}"
        try:
            html = await self.fetch_page(session, detail_url)
            soup = BeautifulSoup(html, 'html.parser')

            # Extract author
            author = 'Unknown'
            author_tag = soup.find('p', string=re.compile(r'Tác giả:'))
            if author_tag:
                strong_tag = author_tag.find('strong')
                if strong_tag and strong_tag.next_sibling:
                    author = strong_tag.next_sibling.strip()

            # Extract genres (categories)
            genres_container = soup.find('div', class_='flex flex-wrap items-baseline gap-1')
            genres = []
            if genres_container:
                genre_links = genres_container.find_all('a', class_='hover:underline')
                genres = [link.get_text(strip=True).rstrip(',') for link in genre_links]

            # Extract status
            status_tag = soup.find('p', string=re.compile(r'Trạng thái:'))
            status_text = status_tag.find('span').get_text(strip=True) if status_tag else 'updating'
            status = 'completed' if status_text.lower() == 'full' else 'updating'

            # Extract rating
            rating_tag = soup.find('span', class_='text-gray-600 ml-2 text-xs')
            rating = 0.0
            if rating_tag:
                rating_text = rating_tag.get_text(strip=True)
                match = re.search(r'Đánh giá: (\d+\.?\d*)/10', rating_text)
                if match:
                    rating = float(match.group(1))

            # Extract description
            description_container = soup.find('div', class_='mt-4 text-gray-700 space-y-3 text-sm')
            description = ''
            if description_container:
                paragraphs = description_container.find_all('p')
                description = ' '.join(p.get_text(strip=True) for p in paragraphs if p.get_text(strip=True))

            # Extract cover image
            cover_img = soup.find('img', class_='object-cover')
            cover_image = cover_img['src'] if cover_img else 'https://cdn.apptruyen.lol/images/public/default-image.jpg'

            novel_details = {
                'slug': slug,
                'author': author,
                'genres': genres,
                'status': status,
                'rating': rating,
                'description': description,
                'cover_image': cover_image,
                'updated_at': datetime.now(timezone.utc)
            }
            logging.debug(f"Extracted details for novel: {slug}")
            return novel_details

        except Exception as e:
            logging.error(f"Failed to process detail page {detail_url}: {e}")
            return None

    def save_to_postgresql(self, novel_info_list):
        """Save a batch of novels to PostgreSQL database."""
        if not novel_info_list:
            logging.warning("No novels to save")
            return 0

        conn = None
        cursor = None
        try:
            conn = self.db_pool.getconn()
            cursor = conn.cursor()

            insert_query = """
                INSERT INTO novels (
                    id, author, cover_image, created_at, description, rating, slug, status,
                    title, title_nomalized, total_chapters, updated_at, views
                ) VALUES %s
                ON CONFLICT (slug) DO NOTHING
                RETURNING id
            """
            values = [(
                novel['id'],
                novel['author'],
                novel['cover_image'],
                novel['created_at'],
                novel['description'],
                novel['rating'],
                novel['slug'],
                novel['status'],
                novel['title'],
                novel['title_nomalized'],
                novel['total_chapters'],
                novel['updated_at'],
                novel['views']
            ) for novel in novel_info_list]

            from psycopg2.extras import execute_values
            execute_values(cursor, insert_query, values)
            novel_ids = [row[0] for row in cursor.fetchall()]
            conn.commit()

            logging.info(f"Saved {len(novel_ids)} novels to database")
            for novel, novel_id in zip(novel_info_list, novel_ids):
                logging.debug(f"Saved novel: {novel['title']} with ID: {novel_id}")
            logging.warning(f"Skipped {len(novel_info_list) - len(novel_ids)} novels (possible duplicates)")
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

    def update_novel_details(self, novel_details_list, novel_ids):
        """Update a batch of novels with detailed information and manage genres."""
        if not novel_details_list:
            logging.warning("No novel details to update")
            return 0

        conn = None
        cursor = None
        try:
            conn = self.db_pool.getconn()
            cursor = conn.cursor()

            # Update novels
            update_query = """
                UPDATE novels
                SET author = %s,
                    cover_image = %s,
                    description = %s,
                    rating = %s,
                    status = %s,
                    updated_at = %s
                WHERE slug = %s
                RETURNING id
            """
            novel_values = [(
                novel['author'],
                novel['cover_image'],
                novel['description'],
                float(novel['rating']),
                novel['status'],
                novel['updated_at'],
                novel['slug']
            ) for novel in novel_details_list]

            # Execute update query
            updated_novel_ids = []
            for values in novel_values:
                cursor.execute(update_query, values)
                updated_novel_id = cursor.fetchone()
                if updated_novel_id:
                    updated_novel_ids.append(updated_novel_id[0])

            # Manage genres
            for novel, novel_id in zip(novel_details_list, novel_ids):
                if not novel['genres']:
                    continue

                for genre_name in novel['genres']:
                    # Insert or get genre
                    genre_query = """
                        INSERT INTO genres (id, name, description)
                        VALUES (%s, %s, %s)
                        ON CONFLICT (name) DO NOTHING
                        RETURNING id
                    """
                    genre_id = str(uuid.uuid4())
                    cursor.execute(genre_query, (
                        genre_id,
                        genre_name,
                        "Novel genre description"
                    ))
                    result = cursor.fetchone()
                    if result:
                        genre_id = result[0]
                    else:
                        # Genre already exists, fetch its ID
                        cursor.execute("SELECT id FROM genres WHERE name = %s", (genre_name,))
                        genre_id = cursor.fetchone()[0]

                    # Link novel and genre
                    novel_genre_query = """
                        INSERT INTO novel_genres (novel_id, genre_id)
                        VALUES (%s, %s)
                        ON CONFLICT DO NOTHING
                    """
                    cursor.execute(novel_genre_query, (
                        novel_id,
                        genre_id
                    ))

            conn.commit()
            logging.info(f"Updated {len(updated_novel_ids)} novels with details")
            return len(updated_novel_ids)

        except psycopg2.OperationalError as e:
            logging.error(f"Database connection failed: {e}")
            return 0
        except psycopg2.IntegrityError as e:
            logging.error(f"Database integrity error: {e}")
            if conn:
                conn.rollback()
            return 0
        except Exception as e:
            logging.error(f"Failed to update novels: {e}")
            if conn:
                conn.rollback()
            return 0
        finally:
            if cursor:
                cursor.close()
            if conn:
                self.db_pool.putconn(conn)

    def load_checkpoint(self, checkpoint_file):
        """Load the last processed item from checkpoint file."""
        if os.path.exists(checkpoint_file):
            with open(checkpoint_file, 'r') as f:
                try:
                    return f.read().strip()
                except ValueError:
                    logging.warning(f"Invalid checkpoint file {checkpoint_file}, starting from beginning")
        return None

    def save_checkpoint(self, checkpoint_file, item):
        """Save the current item to checkpoint file."""
        with open(checkpoint_file, 'w') as f:
            f.write(str(item))
        logging.debug(f"Saved checkpoint to {checkpoint_file}: {item}")

    async def crawl_novels(self, start_page=1, max_pages=10):
        """Crawl novel lists and details."""
        total_novels_saved = 0
        total_details_updated = 0
        start_time = time.time()

        # Load checkpoint for list pages
        last_page = self.load_checkpoint(self.checkpoint_list_file)
        start_page = int(last_page) + 1 if last_page and last_page.isdigit() else start_page

        async with aiohttp.ClientSession() as session:
            # Step 1: Crawl novel lists
            for page in range(start_page, max_pages + 1):
                list_url = f"{self.base_url}/danh-sach/truyen-hot?page={page}"
                logging.info(f"Processing page {page}/{max_pages}")

                novel_info_list = await self.get_novel_info(session, list_url)
                if not novel_info_list:
                    logging.warning(f"No novels found on page {page}, stopping")
                    break

                # Save novels and get their IDs
                novel_ids = self.save_to_postgresql(novel_info_list)
                total_novels_saved += len(novel_ids)

                # Step 2: Crawl details for this batch
                novel_details_list = []
                for i in range(0, len(novel_info_list), self.max_concurrency):
                    batch = novel_info_list[i:i + self.max_concurrency]
                    batch_slugs = [novel['slug'] for novel in batch]
                    tasks = [self.get_novel_details(session, slug) for slug in batch_slugs]
                    results = await asyncio.gather(*tasks, return_exceptions=True)
                    novel_details_list.extend([result for result in results if result is not None])

                # Update details and genres
                if novel_details_list:
                    updated_count = self.update_novel_details(novel_details_list, novel_ids[:len(novel_details_list)])
                    total_details_updated += updated_count

                self.save_checkpoint(self.checkpoint_list_file, page)
                await asyncio.sleep(self.rate_limit_delay)

        elapsed_time = time.time() - start_time
        logging.info(f"Crawling completed: {total_novels_saved} novels saved, {total_details_updated} details updated in {elapsed_time:.2f} seconds")
        return total_novels_saved, total_details_updated

def main():
    # Database configuration
    db_config = {
        'dbname': os.getenv('PG_DBNAME'),
        'user': os.getenv('PG_USER'),
        'password': os.getenv('PG_PASSWORD'),
        'host': os.getenv('PG_HOST'),
        'port': os.getenv('PG_PORT')
    }

    # Initialize crawler
    base_url = "https://www.truyenfull.co"
    crawler = NovelCrawler(base_url, db_config)

    # Run crawler
    total_novels, total_details = asyncio.run(crawler.crawl_novels(max_pages=3))
    print(f"Total novels saved: {total_novels}")
    print(f"Total novel details updated: {total_details}")

if __name__ == "__main__":
    main()