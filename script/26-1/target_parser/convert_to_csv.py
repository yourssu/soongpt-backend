
import pandas as pd
import os
import sys

# Configuration
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# Output to ../course/ssu26-1.csv
COURSE_DIR = os.path.abspath(os.path.join(BASE_DIR, "../course"))
OUTPUT_CSV_NAME = "ssu26-1.csv"
OUTPUT_PATH = os.path.join(COURSE_DIR, OUTPUT_CSV_NAME)

def convert_xlsx_to_csv(input_xlsx_path):
    """
    Convert XLSX to CSV with specific formatting for ssu26-1.csv
    """
    if not os.path.exists(input_xlsx_path):
        print(f"Error: Input file not found at {input_xlsx_path}")
        return

    # Ensure output directory exists
    if not os.path.exists(COURSE_DIR):
        print(f"Creating directory: {COURSE_DIR}")
        os.makedirs(COURSE_DIR, exist_ok=True)

    print(f"Converting {input_xlsx_path} to {OUTPUT_PATH}...")

    try:
        # Read Excel file
        # dtype=str ensures all data is read as text, preserving formats like "01" or "4.0"
        df = pd.read_excel(input_xlsx_path, dtype=str)
        
        # Clean data: Replace NaN with empty string
        df = df.fillna('')
        
        # Strip whitespace from column names
        df.columns = [c.strip() for c in df.columns]

        # Write to CSV
        # index=False: Don't write row numbers
        # encoding='utf-8-sig': Excel-friendly UTF-8 (includes BOM)
        df.to_csv(OUTPUT_PATH, index=False, encoding='utf-8-sig')
        
        print(f"✅ Conversion successful!")
        print(f"   Output: {OUTPUT_PATH}")
        print(f"   Rows: {len(df)}")
        
    except Exception as e:
        print(f"❌ Error during conversion: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python convert_to_csv.py <path_to_xlsx_file>")
        print("Example: python convert_to_csv.py raw_data.xlsx")
        sys.exit(1)
        
    input_file = sys.argv[1]
    convert_xlsx_to_csv(input_file)
