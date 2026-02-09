#!/usr/bin/env python3
"""Check for table locks in the database."""

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

def main():
    print("="*80)
    print("Checking Database Locks")
    print("="*80)

    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("✓ Connected successfully\n")

        # Check running processes
        print("Running Processes:")
        print("-"*80)
        cursor.execute("""
            SELECT
                ID, USER, HOST, DB, COMMAND, TIME, STATE, INFO
            FROM information_schema.PROCESSLIST
            WHERE DB = %s AND COMMAND != 'Sleep'
            ORDER BY TIME DESC
        """, (db_name,))

        processes = cursor.fetchall()
        if processes:
            for p in processes:
                print(f"ID: {p[0]}, User: {p[1]}, Time: {p[5]}s")
                print(f"  State: {p[6]}")
                print(f"  Query: {p[7][:100] if p[7] else 'N/A'}")
                print()
        else:
            print("No active processes found.\n")

        # Check metadata locks
        print("\nMetadata Locks:")
        print("-"*80)
        cursor.execute("""
            SELECT
                OBJECT_SCHEMA, OBJECT_NAME, LOCK_TYPE, LOCK_STATUS, OWNER_THREAD_ID
            FROM performance_schema.metadata_locks
            WHERE OBJECT_SCHEMA = %s
        """, (db_name,))

        locks = cursor.fetchall()
        if locks:
            for lock in locks:
                print(f"Table: {lock[0]}.{lock[1]}")
                print(f"  Lock Type: {lock[2]}, Status: {lock[3]}, Thread: {lock[4]}")
        else:
            print("No metadata locks found.\n")

        # Check table locks
        print("\nTable Locks:")
        print("-"*80)
        cursor.execute("SHOW OPEN TABLES WHERE In_use > 0")

        table_locks = cursor.fetchall()
        if table_locks:
            for tl in table_locks:
                print(f"Database: {tl[0]}, Table: {tl[1]}, In_use: {tl[2]}")
        else:
            print("No table locks found.\n")

        cursor.close()
        conn.close()

    except mysql.connector.Error as e:
        print(f"✗ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()