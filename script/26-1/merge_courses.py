
import os
import pandas as pd
import glob

def merge_course_files():
    # Define the directory containing the Excel files
    data_dir = os.path.join(os.path.dirname(__file__), 'field_parser')
    
    # Pattern to match all Excel files
    file_pattern = os.path.join(data_dir, '*.xlsx')
    files = glob.glob(file_pattern)
    files = [f for f in files if not os.path.basename(f).startswith('~$')] # Ignore temp files
    
    if not files:
        print(f"No Excel files found in {data_dir}")
        return

    print(f"Found {len(files)} files to process.")

    all_data = []

    for file_path in files:
        try:
            print(f"Processing {os.path.basename(file_path)}...")
            # Read the Excel file
            df = pd.read_excel(file_path)
            
            # Check if required columns exist
            required_columns = ['과목번호', '교과영역', '과목명']
            if not all(col in df.columns for col in required_columns):
                print(f"Skipping {os.path.basename(file_path)}: Missing required columns. Found: {list(df.columns)}")
                continue
                
            # Extract specific columns
            subset = df[required_columns]
            all_data.append(subset)
            
        except Exception as e:
            print(f"Error processing {os.path.basename(file_path)}: {e}")

    if all_data:
        merged_df = pd.concat(all_data, ignore_index=True)
        
        # Remove duplicates based on '과목번호'
        initial_count = len(merged_df)
        merged_df = merged_df.drop_duplicates(subset=['과목번호'])
        final_count = len(merged_df)
        print(f"Removed {initial_count - final_count} duplicates.")
        
        # Output as CSV in the same directory as input files
        output_file = os.path.join(data_dir, 'merged_courses.csv')
        merged_df.to_csv(output_file, index=False, encoding='utf-8-sig')
        print(f"Successfully merged {len(merged_df)} rows into {output_file}")
    else:
        print("No data extracted.")

if __name__ == "__main__":
    merge_course_files()
