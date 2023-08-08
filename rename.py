import os
import json
import sys

def rename_mode1():
    path = "./data/input/"
    file_list = os.listdir(path)
    id_to_name_path = "./data/id_to_name.json"
    name_to_id_path = "./data/name_to_id.json"
    id_to_name = {}
    name_to_id = {}
    file_id = 1

    for file in file_list:
        if file.endswith(".java"):
            file_name = file[0: len(file) - 5]
            os.system("mv " + path + file + " " + path + "{0}.java".format(file_id))
            id_to_name[file_id] = file_name
            name_to_id[file_name] = file_id
            file_id += 1

    with open(id_to_name_path, "w") as f:
        json.dump(id_to_name, f)

    with open(name_to_id_path, "w") as f:
        json.dump(name_to_id, f)


def rename_mode2(src_dir, dst_dir):
    path = "./data/tmp_data/"
    src_path = path + src_dir + "/"
    dst_path = path + dst_dir + "/"

    file_list_src = os.listdir(src_path)
    file_list_dst = os.listdir(dst_path)

    id_to_name_path = "./data/id_to_name.json"
    name_to_id_path = "./data/name_to_id.json"
    id_to_name = {}
    name_to_id = {}
    file_id = 1

    for file in file_list_src:
        if file.endswith(".java"):
            file_name = file[0: len(file) - 5]
            os.system("mv " + src_path + file + " " + src_path + "{0}.java".format(file_id ))
            id_to_name[file_id] = file_name
            name_to_id[file_name] = file_id
            file_id += 1

    file_id = 1
    
    for file in file_list_dst:
        if file.endswith(".java"):
            file_name = file[0: len(file) - 5]
            os.system("mv " + dst_path + file + " " + dst_path + "{0}.java".format(file_id + 500000))
            id_to_name[file_id + 500000] = file_name
            name_to_id[file_name] = file_id + 500000
            file_id += 1

    with open(id_to_name_path, "w") as f:
        json.dump(id_to_name, f)

    with open(name_to_id_path, "w") as f:
        json.dump(name_to_id, f)
