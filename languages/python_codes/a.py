# import os
# from dotenv import load_dotenv
import numpy as np
import pandas as pd
dict={
    's_1':[100, 90, np.nan, 95],
    's_2':[30, np.nan, 45,56],
}
df=pd.DataFrame(dict)
print(df.dropna())
# # actual_name = "/Users/satyambaran/Documents/study/python_codes/tempCodeRunnerFile.py"
# # file_path = '/Users/satyambaran/Documents/study/python_codes/tempCoderUNNErFile.py'
# # print(file_path)

# # cwd = os.path.dirname(os.path.abspath(__file__))
# # file_list = os.listdir(cwd)
# # for file in file_list:
# #     if (file_path.lower() == (os.path.join(cwd, file)).lower()):
# #         file_path = os.path.join(cwd, file)
# # print(file_path)

# # -- coding: utf-8 --
# txt='ssd gor too'
# print(txt.replace('too','two'))
# import sys
# import xlrd
# import xlwt
# import re
# import os
# # workbook_name = input('SmartTRXRemote_eim_soc_config.xlsx')
# sheet_name = "STRX_EIM_TARGET_MACRO_CONFIGS"  # Name of the Sheet inside xls file
# macro_column_name = "NAME"
# value_column_name = "Value"
# # C:\STRx_db\rfp-fval\validation\eim\SoC_Parameters
# cwd = os.path.dirname(os.path.abspath(__file__))
# file_name = input("Please enter you file name:")
# file_path = os.path.join(cwd, "SmartTRX"+file_name+'_eim_soc_config.xlsx')
# print(file_path)

# file_list = os.listdir(cwd)
# for file in file_list:
#     if (file_path.lower() == (os.path.join(cwd, file)).lower()):
#         file_path = os.path.join(cwd, file)
# print(file_path)


# workbook = xlrd.open_workbook(file_path)
# sheet = workbook.sheet_by_name(sheet_name)
# # extract the column numbers of required data from IO Signal Table sheet
# for header_columns in enumerate(sheet.row_values(0)):
#     if (header_columns[1] == macro_column_name):
#         macro_column_num = header_columns[0]
#     elif (header_columns[1] == value_column_name):
#         # Check to see all the above variables are correctly passed
#         value_column_num = header_columns[0]
# try:
#     macro_column_num and value_column_num
# except:
#     sys.exit("\nERROR: One of the column was Not Found")
# output_header = open(os.path.join(
#     cwd, "SmartTRX"+file_name+"_eim_target_macro.h"), "w+")
# # file_path = #Process data to be Written in the header file for (row_num,column_value) in enumerate(sheet.col_values(macro_column_num,1),1): if re.match(column_value,"^\s*$"): continue else: output_header.write("#define " + sheet.cell_value(row_num,macro_column_num) + "\t\t\t\t" + sheet.cell_value(row_num,macro_column_num+12)) output_header.write("\n") output_header.close()
