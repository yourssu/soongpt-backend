#!/usr/bin/env python3
"""
Truncate tables and re-insert all data.
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
    ("26-1-course-field-inserts.sql", "course_field"),
    ("26-1-course-time-inserts.sql", "course_time"),
    ("26-1-target-inserts.sql", "target"),
    ("26-1-course-secondary-major-inserts.sql", "course_secondary_major_classification"),
]


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
                current_statement.append(char) # Append both
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

def execute_sql_file(cursor, file_path, table_name):
    """Execute SQL file and return number of rows inserted."""
    print(f"\n{'='*80}")
    print(f"Executing: {os.path.basename(file_path)}")
    print(f"Table: {table_name}")
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


def truncate_tables(cursor, conn):
    """Truncate all tables in reverse order."""
    print("\n" + "="*80)
    print("Truncating tables...")
    print("="*80)

    # Disable foreign key checks temporarily
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
    conn.commit()

    # Truncate in reverse order (to handle foreign keys)
    for _, table_name in reversed(SQL_FILES):
        try:
            cursor.execute(f"TRUNCATE TABLE {table_name};")
            conn.commit()  # Commit immediately after each truncate
            print(f"  ✓ Truncated {table_name}")
        except mysql.connector.Error as e:
            print(f"  ✗ Failed to truncate {table_name}: {e}")
            conn.rollback()

    # Re-enable foreign key checks
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    conn.commit()
    print("\n✓ All tables truncated")


def verify_data(cursor, table_name):
    """Verify data was inserted."""
    cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
    count = cursor.fetchone()[0]
    print(f"  ✓ {table_name} table: {count:,} rows")
    return count


def main():
    print("="*80)
    print("SQL TRUNCATE & INSERT Executor")
    print("="*80)
    print(f"\nDatabase: {db_name}")
    print(f"Host: {db_host}:{db_port}")
    print(f"User: {db_user}")

    # Connect to database
    print("\nConnecting to database...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG, autocommit=False)
        cursor = conn.cursor()
        print("✓ Connected successfully")
    except mysql.connector.Error as e:
        print(f"✗ Connection failed: {e}")
        sys.exit(1)

    try:
        # Step 1: Truncate all tables
        truncate_tables(cursor, conn)

        # Step 2: Execute each SQL file
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
