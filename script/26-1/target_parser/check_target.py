
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

def check_target_logic():
    print("Checking target table logic...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Check validation rule: scope_type = 'DEPARTMENT' => college_name IS NULL
        query = """
        SELECT count(*) 
        FROM target 
        WHERE scope_type = 'DEPARTMENT' 
          AND college_name IS NOT NULL
        """
        cursor.execute(query)
        violation_count = cursor.fetchone()[0]
        
        print(f"Violations (DEPARTMENT but college_name not NULL): {violation_count}")

        if violation_count > 0:
            print("\nSample violations:")
            cursor.execute("""
            SELECT id, scope_type, college_name, department_name, major_name 
            FROM target 
            WHERE scope_type = 'DEPARTMENT' 
              AND college_name IS NOT NULL 
            LIMIT 5
            """)
            for row in cursor.fetchall():
                print(row)
        else:
            print("✓ Logic verified: All DEPARTMENT targets have NULL college_name")

    except mysql.connector.Error as e:
        print(f"✗ Error: {e}")
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    check_target_logic()
