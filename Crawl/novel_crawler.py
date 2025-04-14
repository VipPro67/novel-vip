import requests
from bs4 import BeautifulSoup
import os
import time
import json
from datetime import datetime
import cloudinary
import cloudinary.uploader
import psycopg2
import psycopg2.extras
from uuid import uuid4
import logging
from dotenv import load_dotenv
import re
from urllib.parse import urljoin

load_dotenv()

# Set up logging
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

class NovelInfoCrawler:
    def __init__(self, base_url):
        self.base_url = base_url
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        self.rate_limit_delay = 0.5  # Delay between requests (in seconds)

    def download_image(self, image_url):
        try:
            response = requests.get(image_url, headers=self.headers)
            if response.status_code == 200:
                # Create a temporary file for the image
                temp_path = f"temp_cover_{uuid4()}.jpg"
                with open(temp_path, 'wb') as f:
                    f.write(response.content)
                return temp_path
            return None
        except Exception as e:
            logging.error(f"Error downloading image: {e}")
            return None

    def upload_to_cloudinary(self, image_path, novel_slug):
        cloudinary.config(
            cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME'),
            api_key=os.getenv('CLOUDINARY_API_KEY'),
            api_secret=os.getenv('CLOUDINARY_API_SECRET')
        )
        
        try:
            public_id = f"novel/covers/{novel_slug}"
            response = cloudinary.uploader.upload(
                image_path,
                public_id=public_id,
                overwrite=True
            )
            os.remove(image_path)  # Clean up temporary file
            return response['secure_url']
        except Exception as e:
            logging.error(f"Cloudinary upload error: {e}")
            if os.path.exists(image_path):
                os.remove(image_path)
            return None

    def clean_slug(self, text):
        # Remove special characters and convert spaces to hyphens
        text = re.sub(r'[^\w\s-]', '', text.lower())
        return re.sub(r'[-\s]+', '-', text).strip('-')

    def get_novel_info(self, novel_url):
        try:
            response = requests.get(novel_url, headers=self.headers, timeout=10)
            response.raise_for_status()
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # Extract novel information
            title = soup.find('h1', class_='title').text.strip()
            author = soup.find('div', class_='author').text.strip()
            description = soup.find('div', class_='desc').text.strip()
            cover_img = soup.find('div', class_='book').find('img')['src']
            categories = [cat.text.strip() for cat in soup.find_all('a', class_='category')]
            
            # Generate a unique slug
            base_slug = self.clean_slug(title)
            slug = f"{base_slug}-{str(uuid4())[:8]}"
            
            # Download and upload cover image
            temp_image_path = self.download_image(urljoin(self.base_url, cover_img))
            cover_url = None
            if temp_image_path:
                cover_url = self.upload_to_cloudinary(temp_image_path, slug)
            
            novel_data = {
                'id': str(uuid4()),
                'title': title,
                'author': author,
                'description': description,
                'cover_url': cover_url,
                'categories': categories,
                'slug': slug,
                'status': 'ongoing',
                'created_at': datetime.utcnow().isoformat(),
                'updated_at': datetime.utcnow().isoformat()
            }
            
            time.sleep(self.rate_limit_delay)
            return novel_data
            
        except Exception as e:
            logging.error(f"Failed to fetch novel info: {str(e)}")
            return None

    def save_to_postgresql(self, novel_data):
        try:
            conn = psycopg2.connect(
                dbname=os.getenv('PG_DBNAME'),
                user=os.getenv('PG_USER'),
                password=os.getenv('PG_PASSWORD'),
                host=os.getenv('PG_HOST'),
                port=os.getenv('PG_PORT')
            )
            cursor = conn.cursor()

            # Insert novel
            novel_insert_query = """
            INSERT INTO novels (
                id, title, author, description, cover_url, slug, 
                status, created_at, updated_at, views
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            RETURNING id
            """
            
            cursor.execute(novel_insert_query, (
                novel_data['id'],
                novel_data['title'],
                novel_data['author'],
                novel_data['description'],
                novel_data['cover_url'],
                novel_data['slug'],
                novel_data['status'],
                novel_data['created_at'],
                novel_data['updated_at'],
                0  # Initial views
            ))
            
            novel_id = cursor.fetchone()[0]

            # Insert categories
            for category in novel_data['categories']:
                # First, ensure category exists
                cursor.execute(
                    "INSERT INTO categories (name, slug) VALUES (%s, %s) ON CONFLICT (name) DO NOTHING",
                    (category, self.clean_slug(category))
                )
                
                # Then create novel-category relationship
                cursor.execute("""
                    INSERT INTO novel_categories (novel_id, category_id)
                    SELECT %s, id FROM categories WHERE name = %s
                """, (novel_id, category))

            conn.commit()
            logging.info(f"Successfully saved novel {novel_data['title']} to database")
            return novel_id
            
        except Exception as e:
            logging.error(f"PostgreSQL error: {e}")
            if conn:
                conn.rollback()
            return None
        finally:
            if cursor:
                cursor.close()
            if conn:
                conn.close()

def main():
    # Example usage
    base_url = "https://truyenyy.app"  # Replace with your target website
    novel_url = "https://truyenyy.app/truyen/co-chan-nhan"  # Replace with target novel URL
    
    crawler = NovelInfoCrawler(base_url)
    novel_info = crawler.get_novel_info(novel_url)
    
    if novel_info:
        novel_id = crawler.save_to_postgresql(novel_info)
        if novel_id:
            logging.info(f"Novel saved successfully with ID: {novel_id}")
            # You can now use this novel_id with the chapter crawler
            print(f"Novel ID: {novel_id}")
            print(f"Novel slug: {novel_info['slug']}")
    else:
        logging.error("Failed to crawl novel information")

if __name__ == "__main__":
    main()