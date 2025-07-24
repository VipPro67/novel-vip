import asyncio
import aiohttp
import logging
import os
import json
import time
import bleach
from datetime import datetime, timezone
from bs4 import BeautifulSoup
from uuid import uuid4
import psycopg2
from psycopg2.pool import SimpleConnectionPool
import cloudinary
import cloudinary.uploader
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type
from dotenv import load_dotenv
from tqdm import tqdm
import concurrent.futures

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Verify environment variables
required_vars = [
    'CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET',
    'PG_DBNAME', 'PG_USER', 'PG_PASSWORD', 'PG_HOST', 'PG_PORT'
]
for var in required_vars:
    if not os.getenv(var):
        logging.error(f"Missing environment variable: {var}")
        raise ValueError(f"Environment variable {var} is not set")

class NovelChapterCrawler:
    def __init__(self, base_url, db_config):
        self.base_url = base_url
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5'
        }
        self.rate_limit_delay = 0.1  # Delay between requests (seconds)
        self.max_concurrency = 5  # Maximum concurrent HTTP requests
        self.db_pool = SimpleConnectionPool(1, 10, **db_config)
        self.output_dir = 'output'
        os.makedirs(self.output_dir, exist_ok=True)
        self.checkpoint_file = 'checkpoint_chapters.txt'
        self.default_max_chapters = 50  # Fallback if total_chapters is 0

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

    async def get_chapter_content(self, session, slug, chapter_num):
        """Fetch content for a specific chapter."""
        url = f"{self.base_url}/{slug}/chap-{chapter_num}"
        try:
            html = await self.fetch_page(session, url)
            soup = BeautifulSoup(html, 'html.parser')

            # Get title
            title_tag = soup.find('h1', class_='text-lg text-center')
            if not title_tag:
                raise ValueError("Chapter title not found")
            title = title_tag.text.strip()

            # Get content (HTML preserved)
            content_div = soup.find('div', class_='flex flex-col gap-6')
            if not content_div:
                raise ValueError("Chapter content not found")

            # Convert BeautifulSoup Tag to string first// argument can not of 'Tag' type, must be 'str'
            content_html = content_div.decode_contents()

            # Now clean the string HTML
            cleaned_html = bleach.clean(content_html, tags=['div', 'p', 'span', 'img'], attributes={'img': ['src']}, strip=True)

            # Optional: assign cleaned_html to content if you need
            content = cleaned_html
            await asyncio.sleep(self.rate_limit_delay)
            logging.info(f"Fetched chapter {chapter_num} for slug {slug}")
            return {
                'title': title,
                'content': content,
                'chapter_num': chapter_num
            }
        except Exception as e:
            logging.error(f"Failed to fetch chapter {chapter_num} for slug {slug}: {str(e)}")
            return None

    def save_chapter_as_json(self, chapter, slug, title):
        """Save chapter data as JSON file."""
        data = {
            'slug': slug,
            'title': title,
            'chapterNumber': chapter['chapter_num'],
            'chapterTitle': chapter['title'],
            'content': chapter['content'],
            'createdAt': datetime.now(timezone.utc).isoformat() + 'Z'
        }
        # Ensure the output directory exists
        os.makedirs(os.path.join(self.output_dir, slug), exist_ok=True)
        # Save to JSON file
        file_path = os.path.join(self.output_dir, f'{slug}/chap-{chapter["chapter_num"]}.json')
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        return file_path

    def upload_file_to_cloudinary(self, file_path):
        """Upload a file to Cloudinary."""
        cloudinary.config(
            cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME'),
            api_key=os.getenv('CLOUDINARY_API_KEY'),
            api_secret=os.getenv('CLOUDINARY_API_SECRET')
        )
        filename = os.path.basename(file_path)
        slug = os.path.dirname(file_path).split('/')[-1]
        public_id = "novels/" + slug+"/chapters/" + filename
        try:
            response = cloudinary.uploader.upload(
                file_path,
                resource_type="raw",
                public_id=public_id
            )
            logging.info(f"Uploaded {filename}: {response['secure_url']}")
            return {
                'filename': filename,
                'json_url': response['secure_url'],
                'chapter_num': int(filename.split('chap-')[-1].replace('.json', ''))
            }
        except Exception as e:
            logging.error(f"Failed to upload {filename}: {e}")
            return None

    async def upload_files_to_cloudinary(self, file_paths):
        """Upload multiple files to Cloudinary concurrently."""
        uploaded_files = []
        with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
            futures = {executor.submit(self.upload_file_to_cloudinary, file_path): file_path for file_path in file_paths}
            for future in tqdm(concurrent.futures.as_completed(futures), total=len(futures), desc="Uploading to Cloudinary"):
                result = future.result()
                if result:
                    uploaded_files.append(result)
        return uploaded_files

    def save_to_postgresql(self, chapters, uploaded_files, novel_id):
        """Save chapters to PostgreSQL database."""
        if not chapters:
            logging.warning("No chapters to save")
            return 0

        conn = None
        cursor = None
        try:
            conn = self.db_pool.getconn()
            cursor = conn.cursor()

            insert_query = """
            INSERT INTO chapters (id, audio_url, chapter_number, created_at, json_url, title, updated_at, views, novel_id)
            VALUES %s
            ON CONFLICT (novel_id, chapter_number) DO NOTHING
            RETURNING id
            """
            values = []
            for chapter in chapters:
                chapter_num = chapter['chapter_num']
                uploaded_file = next((f for f in uploaded_files if f['chapter_num'] == chapter_num), None)
                if not uploaded_file:
                    logging.warning(f"No Cloudinary URL for chapter {chapter_num}")
                    continue

                values.append((
                    str(uuid4()),
                    None,  # audio_url
                    chapter_num,
                    datetime.now(timezone.utc).isoformat(),
                    uploaded_file['json_url'],
                    chapter['title'],
                    datetime.now(timezone.utc).isoformat(),
                    0,  # views
                    novel_id
                ))

            from psycopg2.extras import execute_values
            execute_values(cursor, insert_query, values)
            chapter_ids = [row[0] for row in cursor.fetchall()]
            conn.commit()

            logging.info(f"Saved {len(chapter_ids)} chapters to database")
            return len(chapter_ids)

        except psycopg2.OperationalError as e:
            logging.error(f"Database connection failed: {e}")
            return 0
        except psycopg2.IntegrityError as e:
            logging.error(f"Database integrity error: {e}")
            if conn:
                conn.rollback()
            return 0
        except Exception as e:
            logging.error(f"Failed to save chapters to database: {e}")
            if conn:
                conn.rollback()
            return 0
        finally:
            if cursor:
                cursor.close()
            if conn:
                self.db_pool.putconn(conn)

    def load_checkpoint(self):
        """Load the last processed novel slug and chapter from checkpoint file."""
        if os.path.exists(self.checkpoint_file):
            with open(self.checkpoint_file, 'r') as f:
                try:
                    data = f.read().strip()
                    if data:
                        slug, chapter_num = data.split(':')
                        return slug, int(chapter_num)
                except ValueError:
                    logging.warning(f"Invalid checkpoint file {self.checkpoint_file}, starting from beginning")
        return None, 0

    def save_checkpoint(self, slug, chapter_num):
        """Save the current novel slug and chapter to checkpoint file."""
        with open(self.checkpoint_file, 'w') as f:
            f.write(f"{slug}:{chapter_num}")
        logging.debug(f"Saved checkpoint: {slug}:{chapter_num}")

    async def crawl_chapters(self):
        """Crawl chapters for all novels in the database."""
        total_chapters_saved = 0
        start_time = time.time()

        # Get all novels from the database
        conn = self.db_pool.getconn()
        cursor = conn.cursor()
        cursor.execute("SELECT id, slug, title, total_chapters FROM novels WHERE status = 'crawling'  ORDER BY total_chapters ASC LIMIT 1")
        novels = cursor.fetchall()
        
        # Get last chapter in database
        last_chapter = 10
        if novels:
            #cursor.execute("SELECT MAX(chapter_number) FROM chapters WHERE novel_id = %s", (novels[0][0],))
            #last_chapter = cursor.fetchone()[0] or 0
            logging.info(f"Last chapter in database: {last_chapter}")
        # Load checkpoint
        cursor.close()
        self.db_pool.putconn(conn)
        last_slug, last_chapter = self.load_checkpoint()
        last_chapter = 10
        start_index = 0
        if last_slug and any(n[1] == last_slug for n in novels):
            start_index = next(i for i, n in enumerate(novels) if n[1] == last_slug)

        async with aiohttp.ClientSession() as session:
            for i in range(start_index, len(novels)):
                novel_id, slug, title, total_chapters = novels[i]
                logging.info(f"Processing novel: {title} (slug: {slug})")

                # Determine chapter range
                start_chapter = last_chapter + 1 if slug == last_slug else 1
                end_chapter = total_chapters if total_chapters > 0 else self.default_max_chapters
                logging.info(f"Crawling chapters {start_chapter} to {end_chapter} for {title}")

                chapters = []
                for j in range(start_chapter, end_chapter + 1, self.max_concurrency):
                    batch = range(j, min(j + self.max_concurrency, end_chapter + 1))
                    tasks = [self.get_chapter_content(session, slug, chapter_num) for chapter_num in batch]
                    results = await asyncio.gather(*tasks, return_exceptions=True)
                    chapters.extend([result for result in results if result is not None])

                    # Save checkpoint for the last chapter in the batch
                    if chapters:
                        self.save_checkpoint(slug, chapters[-1]['chapter_num'])

                # Save chapters as JSON files
                file_paths = [self.save_chapter_as_json(chapter, slug, title) for chapter in chapters]

                # Upload to Cloudinary
                uploaded_files = await self.upload_files_to_cloudinary(file_paths)

                # Save to PostgreSQL
                saved_count = self.save_to_postgresql(chapters, uploaded_files, novel_id)
                total_chapters_saved += saved_count

        elapsed_time = time.time() - start_time
        logging.info(f"Chapter crawling completed: {total_chapters_saved} chapters saved in {elapsed_time:.2f} seconds")
        return total_chapters_saved

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
    base_url = "https://www.truyenfull.vision"
    crawler = NovelChapterCrawler(base_url, db_config)

    # Crawl chapters
    total_chapters = asyncio.run(crawler.crawl_chapters())
    print(f"Total chapters saved: {total_chapters}")

if __name__ == "__main__":
    main()