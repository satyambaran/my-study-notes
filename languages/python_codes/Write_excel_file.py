import pandas as pd

# Create some sample data
data = {
    "Product": ["Laptop", "Tablet", "Smartphone"],
    "Price": [80000, 20000, 30000],
    "Quantity": [5, 10, 7]
}

# Convert the data into a DataFrame
df = pd.DataFrame(data)

# Write the DataFrame to an Excel file
# index=False prevents writing row numbers
df.to_excel("output.xlsx", index=False)
print("Data written to 'output.xlsx'.")
# df.to_excel("/Users/satyambaran/Documents/study/languages/python_codes/output.xlsx",
#             sheet_name="Sales Data", index=True)
with pd.ExcelWriter("/Users/satyambaran/Documents/study/languages/python_codes/output.xlsx", engine="openpyxl") as writer:
    # Write the first DataFrame
    df.to_excel(writer, sheet_name="Sales Data", index=False)

    # Create another DataFrame
    summary = {
        "Region": ["North", "South", "West"],
        "Total Sales": [50000, 70000, 30000]
    }
    summary_df = pd.DataFrame(summary)

    # Write the second DataFrame to another sheet
    summary_df.to_excel(writer, sheet_name="Summary", index=False)

print("Data written to multiple sheets in 'output.xlsx'.")
