#!/usr/bin/env python3
"""
Generate SQL INSERT statements for course_time table from CSV data.

Usage:
    python generate_course_time_inserts.py

Output:
    output/26-1-course-time-inserts.sql
"""

import csv
import re
import os

# Configuration
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CSV_PATH = os.path.abspath(os.path.join(BASE_DIR, "../course/ssu26-1.csv"))
OUTPUT_DIR = os.path.join(BASE_DIR, "output", "sql")
OUTPUT_SQL_PATH = os.path.join(OUTPUT_DIR, "26-1-course-time-inserts.sql")

# Day of week mapping (based on Week.kt)
DAY_OF_WEEK_MAP = {
    "월": "MONDAY",
    "화": "TUESDAY",
    "수": "WEDNESDAY",
    "목": "THURSDAY",
    "금": "FRIDAY",
    "토": "SATURDAY",
    "일": "SUNDAY",
}


def parse_time_to_minutes(time_str: str) -> int:
    """
    Convert time string to minutes.

    Examples:
        "14:00" -> 14*60 + 0 = 840
        "16:50" -> 16*60 + 50 = 1010
    """
    match = re.match(r'(\d+):(\d+)', time_str)
    if match:
        hour = int(match.group(1))
        minute = int(match.group(2))
        return hour * 60 + minute
    return 0


def parse_schedule_line(line: str):
    """
    Parse a single schedule line (may contain multiple days).

    Examples:
        "월 14:00-14:50 (조만식기념관 12214-이순녀)"
        -> [{'day': '월', 'start_time': '14:00', 'end_time': '14:50', 'room': '조만식기념관 12214'}]

        "월 목 13:30-14:45 (미래관 20108-최동일)"
        -> [{'day': '월', ...}, {'day': '목', ...}]
    """
    line = line.strip()
    if not line:
        return []

    # Pattern: (요일들) 시작시간-종료시간 (강의실-교수명)
    # Match one or more days followed by time range
    pattern = r'^([월화수목금토일\s]+)\s+(\d+:\d+)-(\d+:\d+)\s*(?:\(([^)]*)\))?'
    match = re.match(pattern, line)

    if not match:
        return []

    days_str = match.group(1).strip()
    start_time = match.group(2)
    end_time = match.group(3)
    room_info = match.group(4) if match.group(4) else None

    # Parse room (remove professor name after -)
    # - 강의실이 없는 케이스: "(-교수)" -> None(NULL)로 저장
    room = None
    if room_info:
        # Remove professor name (after last -)
        if '-' in room_info:
            room = room_info.rsplit('-', 1)[0].strip()
        else:
            room = room_info.strip()

        # If room is empty or just "-", set to None
        if not room or room == '-':
            room = None

    # Extract all days (월, 화, 수, etc.)
    days = re.findall(r'[월화수목금토일]', days_str)

    # Create result for each day
    results = []
    for day in days:
        results.append({
            'day': day,
            'start_time': start_time,
            'end_time': end_time,
            'room': room
        })

    return results


def escape_sql_string(s: str) -> str:
    """Escape single quotes in SQL strings."""
    if s is None:
        return "NULL"
    return "'" + s.replace("'", "''").replace("\\", "\\\\") + "'"


class CourseTimeInsertGenerator:
    """Generates SQL INSERT statements for course_time table."""

    def __init__(self):
        self.inserts = []
        self.skipped = []
        self.parse_errors = []

    def generate_inserts_for_course(self, course_code: int, schedule_room: str):
        """Generate INSERT statements for all time slots of a course."""
        if not schedule_room or not schedule_room.strip():
            return []

        inserts = []
        lines = schedule_room.split('\n')

        for line in lines:
            line = line.strip()
            if not line:
                continue

            parsed_list = parse_schedule_line(line)
            if not parsed_list:
                self.parse_errors.append(f"Failed to parse: {line} (course: {course_code})")
                continue

            # Process each parsed time slot (may be multiple for multi-day schedules)
            for parsed in parsed_list:
                # Convert day to Week enum
                day_of_week = DAY_OF_WEEK_MAP.get(parsed['day'])
                if not day_of_week:
                    self.parse_errors.append(f"Unknown day: {parsed['day']} (course: {course_code})")
                    continue

                # Convert times to minutes
                start_minute = parse_time_to_minutes(parsed['start_time'])
                end_minute = parse_time_to_minutes(parsed['end_time'])

                if start_minute >= end_minute:
                    self.parse_errors.append(f"Invalid time range: {parsed['start_time']}-{parsed['end_time']} (course: {course_code})")
                    continue

                # Build SQL
                sql = "INSERT INTO course_time (course_code, day_of_week, start_minute, end_minute, room) VALUES ("
                sql += f"{course_code}, "
                sql += f"'{day_of_week}', "
                sql += f"{start_minute}, "
                sql += f"{end_minute}, "
                sql += f"{escape_sql_string(parsed['room'])}"
                sql += ");"

                inserts.append(sql)

        return inserts

    def process_csv(self, csv_path: str):
        """Process CSV file and generate all INSERT statements."""
        with open(csv_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)

            for row in reader:
                code_str = row.get('과목번호', '').strip()
                schedule_room = row.get('강의시간(강의실)', '')

                # Skip invalid course codes
                if not code_str or not code_str.isdigit():
                    continue

                course_code = int(code_str)

                # Generate inserts for this course
                course_inserts = self.generate_inserts_for_course(course_code, schedule_room)
                self.inserts.extend(course_inserts)

        print(f"\n=== CSV Processing Summary ===")
        print(f"Total course_time inserts: {len(self.inserts)}")
        print(f"Parse errors: {len(self.parse_errors)}")

        if self.parse_errors and len(self.parse_errors) <= 20:
            print(f"\nParse errors:")
            for error in self.parse_errors[:20]:
                print(f"  - {error}")


def main():
    print("=== CourseTime SQL Generator ===\n")

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Initialize generator
    generator = CourseTimeInsertGenerator()

    # Process CSV
    print(f"Processing CSV from {CSV_PATH}...")
    generator.process_csv(CSV_PATH)

    # Write SQL file
    print(f"\nWriting SQL to {OUTPUT_SQL_PATH}...")
    with open(OUTPUT_SQL_PATH, 'w', encoding='utf-8') as f:
        f.write("-- Generated SQL INSERT statements for course_time table\n")
        f.write("-- Generated from: ssu26-1.csv\n")
        f.write(f"-- Total inserts: {len(generator.inserts)}\n\n")

        for insert in generator.inserts:
            f.write(insert + "\n")

    print(f"\n✅ Successfully generated {len(generator.inserts)} INSERT statements")
    print(f"   Output: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()