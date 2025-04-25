import requests
from bs4 import BeautifulSoup
import os
import time
import json
import concurrent.futures
from tqdm import tqdm
from datetime import datetime
import cloudinary
import cloudinary.uploader
import psycopg2
import psycopg2.extras
from uuid import uuid4
import logging
from dotenv import load_dotenv

load_dotenv()
# Set up logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
# Load environment variables
# Verify environment variables
required_vars = [
    'CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET',
    'PG_DBNAME', 'PG_USER', 'PG_PASSWORD', 'PG_HOST', 'PG_PORT'
]
for var in required_vars:
    if not os.getenv(var):
        logging.error(f"Missing environment variable: {var}")
        raise ValueError(f"Environment variable {var} is not set")
        
class NovelCrawler:
    def __init__(self, config):
        self.base_url = config['url']
        self.start_chapter = config['start_chapter']
        self.end_chapter = config['end_chapter']
        self.title = config['title']
        self.slug = config['slug']
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        self.output_dir = 'output'
        os.makedirs(self.output_dir, exist_ok=True)
        self.rate_limit_delay = 0.1  # Delay between requests (in seconds)

    def get_chapter_content(self, chapter_num):
        url = f"{self.base_url}/chuong-{chapter_num}.html"
        try:
            response = requests.get(url, headers=self.headers, timeout=10)
            response.raise_for_status()
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # Get title
            title_tag = soup.find('h2', class_='heading-font mt-2')
            if not title_tag:
                raise ValueError("Chapter title not found")
            title = f"{title_tag.text.strip()}"
            
            # Get content (HTML preserved)
            content_div = soup.find('div', id='inner_chap_content_1')
            if not content_div:
                raise ValueError("Chapter content not found")
            
            # Clean up new lines and replace <br> tags with newline characters for better readability
            content = str(content_div)  # Keep HTML formatting as it is

            time.sleep(self.rate_limit_delay)  # Rate limiting
            return {
                'title': title,
                'content': content,  # Keep HTML as raw string
                'chapter_num': chapter_num
            }

        except Exception as e:
            logging.error(f"Failed to fetch chapter {chapter_num}: {str(e)}")
        return None

    def save_chapter_as_json(self, chapter):
        data = {
            'slug': self.slug,
            'title': self.title,
            'chapterNumber': chapter['chapter_num'],
            'chapterTitle': chapter['title'],
            'content': chapter['content'],
            'createdAt': datetime.utcnow().isoformat() + 'Z'
        }
        file_path = os.path.join(self.output_dir, f'{self.slug}-chap-{chapter["chapter_num"]}.json')
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        return file_path

    def crawl_and_convert(self):
        chapters = []
        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            futures = {executor.submit(self.get_chapter_content, chapter_num): chapter_num for chapter_num in range(self.start_chapter, self.end_chapter + 1)}
            for future in tqdm(concurrent.futures.as_completed(futures), total=len(futures), desc="Crawling chapters"):
                chapter = future.result()
                if chapter:
                    file_path = self.save_chapter_as_json(chapter)
                    chapters.append({
                        'chapter_num': chapter['chapter_num'],
                        'title': chapter['title'],
                        'file_path': file_path
                    })
        return chapters

def upload_file_to_cloudinary(file_path):
    cloudinary.config(
        cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME'),
        api_key=os.getenv('CLOUDINARY_API_KEY'),
        api_secret=os.getenv('CLOUDINARY_API_SECRET')
    )
    filename = os.path.basename(file_path)
    public_id = "novel/chapters/" + os.path.splitext(filename)[0]
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
            'chapter_num': int(filename.split('-chap-')[-1].replace('.json', ''))
        }
    except Exception as e:
        logging.error(f"Failed to upload {filename}: {e}")
        return None

def upload_files_to_cloudinary(folder_path):
    uploaded_files = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        futures = {executor.submit(upload_file_to_cloudinary, os.path.join(folder_path, filename)): filename for filename in os.listdir(folder_path) if filename.endswith(".json")}
        for future in tqdm(concurrent.futures.as_completed(futures), total=len(futures), desc="Uploading to Cloudinary"):
            result = future.result()
            if result:
                uploaded_files.append(result)
    return uploaded_files

def save_to_postgresql(chapters, uploaded_files, novel_id):
    try:
        conn = psycopg2.connect(
            dbname=os.getenv('PG_DBNAME'),
            user=os.getenv('PG_USER'),
            password=os.getenv('PG_PASSWORD'),
            host=os.getenv('PG_HOST'),
            port=os.getenv('PG_PORT')
        )
        cursor = conn.cursor()

        insert_query = """
        INSERT INTO chapters (id, audio_url, chapter_number, created_at, json_url, title, updated_at, views, novel_id)
        VALUES %s
        ON CONFLICT (id) DO NOTHING
        """

        values = []
        for chapter in chapters:
            chapter_num = chapter['chapter_num']
            uploaded_file = next((f for f in uploaded_files if f['chapter_num'] == chapter_num), None)
            if not uploaded_file:
                logging.warning(f"No Cloudinary URL for chapter {chapter_num}")
                continue

            values.append((
                str(uuid4()),  # Generate unique ID
                None,  # audio_url
                chapter_num,
                datetime.utcnow().isoformat(),
                uploaded_file['json_url'],
                chapter['title'],
                datetime.utcnow().isoformat(),
                0,  # views
                novel_id
            ))

        psycopg2.extras.execute_values(cursor, insert_query, values)
        conn.commit()
        logging.info("Successfully saved chapters to PostgreSQL")
    except Exception as e:
        logging.error(f"PostgreSQL error: {e}")
    finally:
        cursor.close()
        conn.close()

def main():

    config = {
        'url': "https://truyenyy.co/truyen/co-chan-nhan", # Replace with source URL
        'start_chapter':1, # Replace with actual start chapter
        'end_chapter': 50, # Replace with actual end chapter
        'title': "Cổ Chân Nhân", # Replace with actual title
        'slug': "co-chan-nhan-44df36d0" # Replace with actual slug
    }
    novel_id = "44df36d0-baf2-42ab-92e8-fa5791fc1b9e"  # Replace with actual novel_id from novels table

    # Step 1: Crawl chapters
    crawler = NovelCrawler(config)
    chapters = crawler.crawl_and_convert()

    # Step 2: Upload to Cloudinary
    uploaded_files = upload_files_to_cloudinary(crawler.output_dir)

    # Step 3: Save to PostgreSQL
    save_to_postgresql(chapters, uploaded_files, novel_id)

if __name__ == "__main__":
    main()