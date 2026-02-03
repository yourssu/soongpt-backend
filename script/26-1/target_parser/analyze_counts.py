import pandas as pd
import os

# File paths
input_file = "ssu26-1.csv"

# Current directory
current_dir = os.path.dirname(os.path.abspath(__file__))
input_path = os.path.join(current_dir, input_file)

def analyze_csv():
    if not os.path.exists(input_path):
        print(f"Error: Input file '{input_file}' not found in {current_dir}")
        return

    try:
        # Read the CSV file
        df = pd.read_csv(input_path)
        
        total_records = len(df)
        unique_ids = df['과목번호'].nunique()
        unique_names = df['과목명'].nunique()
        
        print(f"Analysis of '{input_file}':")
        print(f"Total Class Records (Rows): {total_records}")
        print(f"Unique Course Numbers (Subjects): {unique_ids}")
        print(f"Unique Course Names: {unique_names}")
        
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    analyze_csv()
