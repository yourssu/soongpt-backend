#!/usr/bin/env python3
"""
Execute SQL INSERT files in the correct order.
"""

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

# SQL files to execute (in order)
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
SQL_FILES = [
    ("26-1-course-inserts.sql", "course"),
    ("26-1-course-time-inserts.sql", "course_time"),
    ("26-1-target-inserts.sql", "target"),
]


def execute_sql_file(cursor, file_path, table_name):
    """Execute SQL file and return number of rows inserted."""
    print(f"\n{'='*80}")
    print(f"Executing: {os.path.basename(file_path)}")
    print(f"Table: {table_name}")
    print(f"{'='*80}")

    with open(file_path, 'r', encoding='utf-8') as f:
        sql_content = f.read()

    # Split by semicolon and filter out comments
    statements = [s.strip() for s in sql_content.split(';') if s.strip() and not s.strip().startswith('--')]

    total = len(statements)
    print(f"Total statements: {total}")

    success_count = 0
    error_count = 0

    for i, statement in enumerate(statements, 1):
        if i % 500 == 0:
            print(f"  Progress: {i}/{total} ({i*100//total}%)")

        try:
            cursor.execute(statement + ';')
            success_count += 1
        except mysql.connector.Error as e:
            error_count += 1
            if error_count <= 5:  # Show first 5 errors
                print(f"  Error at statement {i}: {e}")
                if error_count == 5:
                    print(f"  ... (suppressing further errors)")

    print(f"\nResults:")
    print(f"  Success: {success_count}")
    print(f"  Errors: {error_count}")

    return success_count, error_count


def verify_data(cursor, table_name):
    """Verify data was inserted."""
    cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
    count = cursor.fetchone()[0]
    print(f"  ✓ {table_name} table: {count:,} rows")
    return count


def main():
    print("="*80)
    print("SQL INSERT Executor")
    print("="*80)
    print(f"\nDatabase: {db_name}")
    print(f"Host: {db_host}:{db_port}")
    print(f"User: {db_user}")

    # Connect to database
    print("\nConnecting to database...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("✓ Connected successfully")
    except mysql.connector.Error as e:
        print(f"✗ Connection failed: {e}")
        sys.exit(1)

    try:
        # Execute each SQL file
        total_success = 0
        total_errors = 0

        for sql_file, table_name in SQL_FILES:
            file_path = os.path.join(OUTPUT_DIR, sql_file)

            if not os.path.exists(file_path):
                print(f"\n✗ File not found: {sql_file}")
                continue

            success, errors = execute_sql_file(cursor, file_path, table_name)
            total_success += success
            total_errors += errors

            # Commit after each file
            conn.commit()
            print(f"✓ Changes committed")

        # Verify all data
        print("\n" + "="*80)
        print("Data Verification")
        print("="*80)

        for _, table_name in SQL_FILES:
            verify_data(cursor, table_name)

        # Summary
        print("\n" + "="*80)
        print("Summary")
        print("="*80)
        print(f"Total statements executed: {total_success + total_errors}")
        print(f"  ✓ Success: {total_success}")
        print(f"  ✗ Errors: {total_errors}")

        if total_errors == 0:
            print("\n🎉 All SQL files executed successfully!")
        else:
            print(f"\n⚠️  Completed with {total_errors} errors")

    except Exception as e:
        print(f"\n✗ Error: {e}")
        conn.rollback()
        sys.exit(1)

    finally:
        cursor.close()
        conn.close()
        print("\n✓ Database connection closed")


if __name__ == "__main__":
    main()
