import pandas as pd
from openpyxl import load_workbook

# --- Write: Create Excel with multiple sheets using pandas ---
def write_excel(file_path="output.xlsx"):
    data = {
        "Product": ["Laptop", "Tablet", "Smartphone"],
        "Price": [80000, 20000, 30000],
        "Quantity": [5, 10, 7]
    }
    df = pd.DataFrame(data)

    summary = {
        "Region": ["North", "South", "West"],
        "Total Sales": [50000, 70000, 30000]
    }
    summary_df = pd.DataFrame(summary)

    with pd.ExcelWriter(file_path, engine="openpyxl") as writer:
        df.to_excel(writer, sheet_name="Sales Data", index=False)
        summary_df.to_excel(writer, sheet_name="Summary", index=False)
    print(f"Data written to '{file_path}'.")


# --- Update: Append a row to an existing Excel file using openpyxl ---
def append_row(file_path="output.xlsx", row=None):
    if row is None:
        row = ["New Product", 15000, 3]
    book = load_workbook(file_path)
    sheet = book.active
    sheet.append(row)
    book.save(file_path)
    print(f"Row appended to '{file_path}'.")


# --- Read/Clean: Load Excel and drop rows with missing values using pandas ---
def clean_dataframe():
    data = {
        's_1': [100, 90, None, 95],
        's_2': [30, None, 45, 56],
    }
    df = pd.DataFrame(data)
    print(df.dropna())


if __name__ == "__main__":
    write_excel()
    append_row()
    clean_dataframe()
