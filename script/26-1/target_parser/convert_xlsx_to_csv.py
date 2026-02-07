import pandas as pd
import os

# File paths
input_file = "../course/export20260206144825.xlsx"
output_file = "../course/ssu26-1.csv"

# Current directory
current_dir = os.path.dirname(os.path.abspath(__file__))
input_path = os.path.join(current_dir, input_file)
output_path = os.path.join(current_dir, output_file)

def convert_xlsx_to_csv():
    if not os.path.exists(input_path):
        print(f"Error: Input file '{input_file}' not found in {current_dir}")
        return

    print(f"Reading '{input_file}'...")
    try:
        # Read the Excel file
        df = pd.read_excel(input_path)
        
        # Save as CSV with utf-8-sig encoding for Korean compatibility
        print(f"Converting to '{output_file}' with utf-8-sig encoding...")
        df.to_csv(output_path, index=False, encoding='utf-8-sig')
        
        print("Conversion complete!")
        print(f"Saved to: {output_path}")
        
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    convert_xlsx_to_csv()
