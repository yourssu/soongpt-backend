#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Complete Course Processing Pipeline
This script runs the entire course processing pipeline:
1. Parse course data (course_parser.py)
2. Generate custom target mappings (generate_custom_target_map.py)
3. Generate course SQL inserts (generate_course_sql.py)
4. Generate target SQL inserts (generate_target_sql_from_map.py)
"""

import os
import sys
import subprocess
import json
from typing import List, Dict

def run_script(script_name: str, description: str) -> bool:
    """
    Run a Python script and return success status.
    
    Args:
        script_name: Name of the script to run
        description: Description of what the script does
        
    Returns:
        True if script ran successfully, False otherwise
    """
    print(f"\n{'='*60}")
    print(f"Running: {description}")
    print(f"Script: {script_name}")
    print(f"{'='*60}")
    
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        script_path = os.path.join(script_dir, script_name)
        
        if not os.path.exists(script_path):
            print(f"ERROR: Script {script_path} not found!")
            return False
        
        # Run the script
        result = subprocess.run([sys.executable, script_path], 
                              capture_output=True, text=True, cwd=script_dir)
        
        if result.returncode == 0:
            print("✓ SUCCESS")
            print("Output:")
            print(result.stdout)
        else:
            print("✗ FAILED")
            print("Error output:")
            print(result.stderr)
            return False
            
    except Exception as e:
        print(f"✗ FAILED: {e}")
        return False
    
    return True

def check_prerequisites() -> bool:
    """Check if all required files and directories exist."""
    print("Checking prerequisites...")
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Check for required source files
    required_files = [
        "course_parser.py",
        "generate_course_sql.py",
        "generate_target_sql_from_map.py"
    ]
    
    for file_name in required_files:
        file_path = os.path.join(script_dir, file_name)
        if not os.path.exists(file_path):
            print(f"ERROR: Required file {file_name} not found!")
            return False
    
    # Check for course data files
    course_data_pattern = os.path.join(script_dir, "2025_2학기_*.json")
    if not any(os.path.exists(f) for f in [course_data_pattern]):
        print("WARNING: Course data files (2025_2학기_*.json) not found in script directory")
        print("Make sure to place course data files in the script directory")
    
    print("✓ Prerequisites check passed")
    return True

def create_output_directories():
    """Create necessary output directories."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    output_dirs = [
        "result",
        "result/course_parser",
        "result/custom_target", 
        "result/generate_sql_insert"
    ]
    
    for dir_name in output_dirs:
        dir_path = os.path.join(script_dir, dir_name)
        os.makedirs(dir_path, exist_ok=True)
        
    print("✓ Output directories created")

def print_summary():
    """Print summary of generated files."""
    print(f"\n{'='*60}")
    print("PIPELINE COMPLETED SUCCESSFULLY!")
    print(f"{'='*60}")
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # List generated files
    generated_files = [
        "result/course_parser/2025_2학기_parsed.json",
        "result/custom_target/custom_target_map.json",
        "result/custom_target/courses_with_custom_targets.json",
        "result/generate_sql_insert/course_insert.sql",
        "result/generate_sql_insert/target_insert_from_map.sql"
    ]
    
    print("\nGenerated files:")
    for file_path in generated_files:
        full_path = os.path.join(script_dir, file_path)
        if os.path.exists(full_path):
            file_size = os.path.getsize(full_path)
            print(f"  ✓ {file_path} ({file_size:,} bytes)")
        else:
            print(f"  ✗ {file_path} (not found)")
    
    print(f"\nNext steps:")
    print("1. Review the generated SQL files before running them")
    print("2. Execute course_insert.sql first to create course records")
    print("3. Execute target_insert_from_map.sql to create target records")
    print("4. Verify data integrity after running the SQL scripts")

def main():
    """Main pipeline execution function."""
    print("Course Processing Pipeline")
    print("=" * 60)
    
    # Check prerequisites
    if not check_prerequisites():
        print("Prerequisites check failed. Exiting.")
        sys.exit(1)
    
    # Create output directories
    create_output_directories()
    
    # Pipeline steps
    pipeline_steps = [
        ("course_parser.py", "Parse raw course data and classify targets"),
        ("generate_course_sql.py", "Generate SQL INSERT statements for CourseEntity"),
        ("generate_target_sql_from_map.py", "Generate SQL INSERT statements for TargetEntity with custom mappings")
    ]
    
    # Execute pipeline steps
    success_count = 0
    for script_name, description in pipeline_steps:
        if run_script(script_name, description):
            success_count += 1
        else:
            print(f"\nPipeline failed at step: {script_name}")
            print("Please fix the error and run the pipeline again.")
            sys.exit(1)
    
    # Print summary
    if success_count == len(pipeline_steps):
        print_summary()
    else:
        print(f"\nPipeline partially completed: {success_count}/{len(pipeline_steps)} steps successful")

if __name__ == "__main__":
    main()