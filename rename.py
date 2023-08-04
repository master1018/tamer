import os

path = "./data/tmp_data/"
file_list = os.listdir(path)

for file in file_list:
    if file.endswith(".java"):
        file_id = int(file[0: len(file) - 5])
        os.system("mv " + path + file + " " + path + "{0}.java".format(file_id + 500000))
