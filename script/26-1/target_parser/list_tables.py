
import mysql.connector
import os
import sys
from dotenv import load_dotenv
import re

# Load environment
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
ENV_PATH = os.path.join(PROJECT_ROOT, ".env")

if os.path.exists(ENV_PATH):
    load_dotenv(ENV_PATH)

# Parse DB_URL
db_url = os.getenv('DB_URL', '')
if db_url:
    match = re.match(r'jdbc:mysql://([^:]+):(\d+)/(.+)', db_url)
    if match:
        db_host = match.group(1)
        db_port = int(match.group(2))
        db_name = match.group(3)
    else:
        print(f"Error: Invalid DB_URL format")
        sys.exit(1)
else:
    print("Error: DB_URL not found in .env")
    sys.exit(1)

db_user = os.getenv('DB_USERNAME', 'yourssu')
db_password = os.getenv('DB_PASSWORD', '')

DB_CONFIG = {
    'host': db_host,
    'database': db_name,
    'user': db_user,
    'password': db_password,
    'port': db_port
}

def list_tables():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SHOW TABLES")
        tables = cursor.fetchall()
        print("Tables in database:")
        for table in tables:
            print(table[0])
        cursor.close()
        conn.close()
    except mysql.connector.Error as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    list_tables()
