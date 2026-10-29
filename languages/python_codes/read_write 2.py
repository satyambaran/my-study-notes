# Read data from an Excel file
data = pd.read_excel("input.xlsx")

# Perform some analysis (e.g., calculate total sales)
data["Total Sales"] = data["Price"] * data["Quantity"]

# Write the analyzed data to a new Excel file
data.to_excel("analyzed_data.xlsx", sheet_name="Analysis", index=False)
print("Analysis saved to 'analyzed_data.xlsx'.")
