#!/usr/bin/env python3
"""
Execute SQL INSERT file for course_field table.
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

# SQL file to execute
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
SQL_FILE = "26-1-course-field-inserts.sql"


def split_sql_statements(sql_content):
    """Split SQL content by semicolon, respecting quotes."""
    statements = []
    current_statement = []
    in_quote = False

    i = 0
    length = len(sql_content)

    while i < length:
        char = sql_content[i]

        if char == "'":
            # Check if it's an escaped quote (double single quote)
            if in_quote and i + 1 < length and sql_content[i+1] == "'":
                current_statement.append(char)
                current_statement.append(char)  # Append both
                i += 2
                continue

            # Toggle quote state
            in_quote = not in_quote
            current_statement.append(char)
            i += 1
            continue

        if char == ';' and not in_quote:
            statement = "".join(current_statement).strip()
            if statement:
                statements.append(statement)
            current_statement = []
            i += 1
        else:
            current_statement.append(char)
            i += 1

    if current_statement:
        statement = "".join(current_statement).strip()
        if statement:
            statements.append(statement)

    return statements


def execute_sql_file(cursor, file_path):
    """Execute SQL file and return number of rows inserted."""
    print(f"\n{'='*80}")
    print(f"Executing: {os.path.basename(file_path)}")
    print(f"Table: course_field")
    print(f"{'='*80}")

    with open(file_path, 'r', encoding='utf-8') as f:
        sql_content = f.read()

    # Remove specific comment lines first
    lines = sql_content.split('\n')
    filtered_lines = [line for line in lines if not line.strip().startswith('--')]
    sql_content_clean = '\n'.join(filtered_lines)

    statements = split_sql_statements(sql_content_clean)

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


def create_table_if_not_exists(cursor):
    """Create course_field table if it doesn't exist."""
    print("\n" + "="*80)
    print("Checking/Creating table...")
    print("="*80)

    create_sql = """
    CREATE TABLE IF NOT EXISTS course_field (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        course_code BIGINT NOT NULL UNIQUE,
        course_name VARCHAR(255) NOT NULL,
        field VARCHAR(255) NOT NULL,
        INDEX idx_course_field_code (course_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    """

    try:
        cursor.execute(create_sql)
        print(f"  ✓ Table course_field ready")
    except mysql.connector.Error as e:
        print(f"  ✗ Failed to create table: {e}")
        raise


def truncate_table(cursor):
    """Truncate course_field table."""
    print("\n" + "="*80)
    print("Truncating table...")
    print("="*80)

    # Disable foreign key checks temporarily
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")

    try:
        cursor.execute("TRUNCATE TABLE course_field;")
        print(f"  ✓ Truncated course_field")
    except mysql.connector.Error as e:
        print(f"  ✗ Failed to truncate course_field: {e}")

    # Re-enable foreign key checks
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    print("\n✓ Table truncated")


def verify_data(cursor):
    """Verify data was inserted."""
    cursor.execute("SELECT COUNT(*) FROM course_field")
    count = cursor.fetchone()[0]
    print(f"  ✓ course_field table: {count:,} rows")
    return count


def main():
    print("="*80)
    print("Course Field SQL INSERT Executor")
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
        # Step 0: Create table if not exists
        create_table_if_not_exists(cursor)
        conn.commit()

        # Step 1: Truncate table
        truncate_table(cursor)
        conn.commit()

        # Step 2: Execute SQL file
        file_path = os.path.join(OUTPUT_DIR, SQL_FILE)

        if not os.path.exists(file_path):
            print(f"\n✗ File not found: {SQL_FILE}")
            sys.exit(1)

        success, errors = execute_sql_file(cursor, file_path)

        # Commit changes
        conn.commit()
        print(f"✓ Changes committed")

        # Verify data
        print("\n" + "="*80)
        print("Data Verification")
        print("="*80)

        verify_data(cursor)

        # Summary
        print("\n" + "="*80)
        print("Summary")
        print("="*80)
        print(f"Total statements executed: {success + errors}")
        print(f"  ✓ Success: {success}")
        print(f"  ✗ Errors: {errors}")

        if errors == 0:
            print("\n🎉 SQL file executed successfully!")
        else:
            print(f"\n⚠️  Completed with {errors} errors")

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
