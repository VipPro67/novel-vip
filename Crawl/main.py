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
import cloudinary.api
import cloudinary.uploader
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type, before_sleep_log
from dotenv import load_dotenv
from tqdm import tqdm
import concurrent.futures
from io import BytesIO
import cloudinary.exceptions
# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(levelname)s - %(message)s')


class NovelChapterCrawler:
    def __init__(self, base_url, db_config):
        self.base_url = base_url
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5'
        }
        self.rate_limit_delay = 0.25  # Delay between requests (seconds)
        self.max_concurrency = 5  # Maximum concurrent HTTP requests
        self.db_pool = SimpleConnectionPool(1, 10, **db_config)
        self.output_dir = 'output'
        os.makedirs(self.output_dir, exist_ok=True)
        self.checkpoint_file = 'checkpoint_chapters.txt'
        self.default_max_chapters = 50  # Fallback if total_chapters is 0

    @retry(
        stop=stop_after_attempt(8),
        wait=wait_exponential(multiplier=1, min=1, max=100),
        retry=retry_if_exception_type(
            (aiohttp.ClientError, aiohttp.http_exceptions.HttpProcessingError)),
        before_sleep=before_sleep_log(logging, logging.WARNING)
    )
    async def fetch_page(self, session, url):
        timeout = aiohttp.ClientTimeout(total=15)
        async with session.get(url, headers=self.headers, timeout=timeout) as response:
            response.raise_for_status()
            return await response.text()

    async def get_chapter_content(self, session, slug, chapter_num):
        """Fetch content for a specific chapter."""
        url = f"{self.base_url}/{slug}/chuong-{chapter_num}"
        try:
            html = await self.fetch_page(session, url)
            soup = BeautifulSoup(html, 'html.parser')

            # Get title
            title_tag = soup.find('a', class_='chapter-title')
            if not title_tag:
                logging.info("Chapter title not found")
                return None
            title = title_tag.text.strip()

            # Get content (HTML preserved)
            content_div = soup.find('div', class_='chapter-c')
            if not content_div:
                logging.info("Chapter content not found")
                return None

            # Convert BeautifulSoup Tag to string first
            content_html = content_div.decode_contents()

            # Now clean the string HTML
            cleaned_html = bleach.clean(content_html, tags=[
                                        'div', 'p', 'span', 'img'], attributes={'img': ['src']}, strip=True)

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
            logging.error(
                f"Failed to fetch chapter {chapter_num} for slug {slug}: {str(e)}"
                f"Failed to fetch from url: {url}")
            return None
        
    def upload_json_content_to_cloudinary(self, data, slug, chapter_num):
        cloudinary.config(
            cloud_name='drpudphzv',
            api_key='942584967114298',
            api_secret='54KWkzHDwJ2dUsscUvzatdT61gY'
        )

        filename = f'chap-{chapter_num}.json'
        public_id = f"novels/{slug}/chapters/{filename}"

        # Check if file already exists
        try:
            cloudinary.api.resource(public_id, resource_type="raw")
            logging.info(f"File already exists on Cloudinary: {public_id}")
            return {
                'filename': filename,
                'json_url': f"https://res.cloudinary.com/drpudphzv/raw/upload/{public_id}",
                'chapter_num': chapter_num
            }
        except cloudinary.exceptions.NotFound:
            pass  # Continue to upload

        # Upload new file
        buffer = BytesIO()
        json_str = json.dumps(data, ensure_ascii=False, indent=2)
        buffer.write(json_str.encode('utf-8'))
        buffer.seek(0)
        try:
            response = cloudinary.uploader.upload(
                buffer,
                resource_type="raw",
                public_id=public_id
            )
            return {
                'filename': filename,
                'json_url': response['secure_url'],
                'chapter_num': chapter_num
            }
        except Exception as e:
            logging.error(f"Cloudinary upload failed for chap {chapter_num}: {e}")
            return None
        


    def save_to_postgresql(self, chapters, uploaded_files, novel_id):
        if not chapters:
            logging.warning("No chapters to save")
            return 0

        conn = None
        cursor = None
        try:
            conn = self.db_pool.getconn()
            cursor = conn.cursor()

            insert_query = """
            INSERT INTO chapters (
                id, audio_file_id, json_file_id, novel_id,
                chapter_number, title, views, created_at, updated_at
            )
            VALUES %s
            ON CONFLICT (novel_id, chapter_number) DO NOTHING
            RETURNING id
            """

            values = []
            for chapter in chapters:
                chapter_num = chapter['chapter_num']
                uploaded_file = next(
                    (f for f in uploaded_files if f['chapter_num'] == chapter_num), None)
                if not uploaded_file:
                    logging.warning(
                        f"No Cloudinary URL for chapter {chapter_num}")
                    continue

                chapter_id = str(uuid4())
                json_file_id = str(uuid4())
                now = datetime.now(timezone.utc)

                # Insert file metadata first
                cursor.execute("""
                    INSERT INTO file_metadata (
                        id, file_name, file_url, content_type, type,
                        public_id, uploaded_at, last_modified_at, size
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (
                    json_file_id,
                    uploaded_file['filename'],
                    uploaded_file['json_url'],
                    'application/json',
                    'json',
                    f"novels/{novel_id}/chapters/{uploaded_file['filename']}",
                    now, now, 0
                ))

                values.append((
                    chapter_id,
                    None,              # audio_file_id
                    json_file_id,      # json_file_id
                    novel_id,
                    chapter_num,
                    chapter['title'],
                    0,                 # views
                    now,
                    now
                ))

            from psycopg2.extras import execute_values
            execute_values(cursor, insert_query, values)
            chapter_ids = [row[0] for row in cursor.fetchall()]
            conn.commit()

            logging.info(f"Saved {len(chapter_ids)} chapters to database")
            return len(chapter_ids)

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
                    logging.warning(
                        f"Invalid checkpoint file {self.checkpoint_file}, starting from beginning")
        return None, 0

    def save_checkpoint(self, slug, chapter_num):
        """Save the current novel slug and chapter to checkpoint file."""
        with open(self.checkpoint_file, 'w') as f:
            f.write(f"{slug}:{chapter_num}")
        logging.debug(f"Saved checkpoint: {slug}:{chapter_num}")

    async def crawl_chapters(self,numtry):
        """Crawl chapters for all novels in the database concurrently."""
        total_chapters_saved = 0
        start_time = time.time()

        # Get all novels from the database
        conn = self.db_pool.getconn()
        cursor = conn.cursor()
        cursor.execute(
            """SELECT n.id, n.slug, n.title, n.total_chapters
            FROM novels n
            LEFT JOIN chapters c ON n.id = c.novel_id
            WHERE n.status != 'crawled' AND n.total_chapters > 0 AND n.status != 'ec ec'
            GROUP BY n.id, n.slug, n.title, n.total_chapters
            HAVING COUNT(c.id) < n.total_chapters
            ORDER BY n.total_chapters ASC
            LIMIT 1;
            """)
        novels = cursor.fetchall()
        cursor.close()
        self.db_pool.putconn(conn)
        if not novels:
            logging.info(
                "All novels crawled")
            return 0

        async def process_novel(novel):
            novel_id, slug, title, total_chapters = novel
            
            logging.info(f"Processing novel: {title} (slug: {slug})")

            # Get all existing chapter numbers for the novel
            conn = self.db_pool.getconn()
            cursor = conn.cursor()
            cursor.execute(
                "SELECT chapter_number FROM chapters WHERE novel_id = %s", (novel_id,))
            existing = {row[0] for row in cursor.fetchall()}
            cursor.close()
            self.db_pool.putconn(conn)

            # Calculate missing chapters
            all_chapters = set(range(1, total_chapters + 1))
            missing_chapters = sorted(all_chapters - existing)
            if not missing_chapters:
                logging.info(f"No missing chapters for novel {title}")
                return 0,novel_id

            chapters = []
            async with aiohttp.ClientSession() as session:
                for i in range(0, len(missing_chapters), self.max_concurrency):
                    batch = missing_chapters[i:i + self.max_concurrency]
                    tasks = [self.get_chapter_content(
                        session, slug, chapter_num) for chapter_num in batch]
                    results = await asyncio.gather(*tasks, return_exceptions=True)
                    chapters.extend(
                        [result for result in results if result is not None])

                    # Save checkpoint for the last chapter in the batch
                    if chapters:
                        self.save_checkpoint(slug, chapters[-1]['chapter_num'])

            # Save chapters as JSON files
            uploaded_files = [
                self.upload_json_content_to_cloudinary({
                    'slug': slug,
                    'title': title,
                    'chapterNumber': chapter['chapter_num'],
                    'chapterTitle': chapter['title'],
                    'content': chapter['content'],
                    'createdAt': datetime.now(timezone.utc).isoformat() + 'Z'
                }, slug, chapter['chapter_num']) for chapter in chapters
            ]
            # Filter out failed uploads
            uploaded_files = [file for file in uploaded_files if file]

            # Save to PostgreSQL
            conn = self.db_pool.getconn()
            saved_count = self.save_to_postgresql(
                chapters, uploaded_files, novel_id)

            # Update novel status
            cursor = conn.cursor()
            cursor.execute(
                "UPDATE novels SET status = 'updating' WHERE id = %s", (novel_id,))
            conn.commit()
            cursor.close()
            self.db_pool.putconn(conn)
            return saved_count, novel_id

        # Process novels concurrently
        tasks = [process_novel(novel) for novel in novels]
        results = await asyncio.gather(*tasks, return_exceptions=True)

        # Count total chapters saved
        for result in results:
            if isinstance(result, int):
                total_chapters_saved += result
            else:
                logging.error(f"Error processing novel: {result}")

        elapsed_time = time.time() - start_time
        logging.info(
            f"Chapter crawling completed: {total_chapters_saved} chapters saved in {elapsed_time:.2f} seconds")
        return total_chapters_saved, novels[0][0]  # pass the current novel ID so we can track retries

    def mark_as_skipped(self,novel_id):
        conn = self.db_pool.getconn()
        cursor = conn.cursor()
        cursor.execute(
            "UPDATE novels SET status = 'ec ec' WHERE id = %s", (novel_id,))
        conn.commit()
        cursor.close()
        self.db_pool.putconn(conn)

def main():
    base_url = "https://truyenfull.vision"
    db_config = {
        'dbname': 'novel_db',
        'user': 'novel_user',
        'password': 'novel_password',
        'host': 'localhost',
        'port': 5432
    }
    crawler = NovelChapterCrawler(base_url, db_config)
    retry_map = {}  # Track retry count per novel_id

    try:
        while True:
            total_chapters, novel_id = asyncio.run(crawler.crawl_chapters(retry_map))
            
            if total_chapters == 0:
                if novel_id:
                    retry_map[novel_id] = retry_map.get(novel_id, 0) + 1
                    if retry_map[novel_id] >= 5:
                        crawler.mark_as_skipped(novel_id)
                        logging.info(f"Novel {novel_id} marked as 'ec ec' after 5 retries.")
                        del retry_map[novel_id]
                else:
                    logging.info("All novels crawled or no eligible novel found.")
                    break
            else:
                logging.info("Sleeping before next novel...")
                time.sleep(1)
    except KeyboardInterrupt:
        logging.info("Stopped by user.")


if __name__ == "__main__":
    main()