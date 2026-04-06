from openpyxl import load_workbook

# Load the existing workbook
file_path = "output.xlsx"
book = load_workbook(file_path)

# Load the active sheet or a specific sheet
sheet = book.active  # Or book['SheetName']

# Append a row
sheet.append(["New Product", 15000, 3])

# Save the workbook
book.save(file_path)
print("Data appended to 'output.xlsx'.")
