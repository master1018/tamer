import os
import json

path = "./data/tmp_data/"
file_list = os.listdir(path)
id_to_name_path = "./data/id_to_name.json"
name_to_id_path = "./data/name_to_id.json"
id_to_name = {}
name_to_id = {}
file_id = 1

for file in file_list:
    if file.endswith(".java"):
        file_name = file[0: len(file) - 5]
        os.system("mv " + path + file + " " + path + "{0}.java".format(file_id + 500000))
        id_to_name[file_id + 500000] = file_name
        name_to_id[file_name] = file_id + 500000
        file_id += 1

with open(id_to_name_path, "w") as f:
    json.dump(id_to_name, f)

with open(name_to_id_path, "w") as f:
    json.dump(name_to_id, f)
