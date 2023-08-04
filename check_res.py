import os
import json

def check_cwe_gpt_dataset():
    count = 0

    file_list = os.listdir("./result/exp_data")

    fp = open("/Users/haoranyan/git_rep/cwe_java_dataset/gpt_dataset/index.json", "r")
    index = json.loads(fp.read())
    fp.close()

    for file in file_list:
        if "output" not in file:
            continue
        if "swp" in file:
            continue
        split_pos = 0
        while file[split_pos] != "_":
            split_pos += 1
        try:
            id1 = int(file[6: split_pos])
            id2 = int(file[split_pos + 1: len(file)]) - 500000
        except Exception:
            print(file)
        for i in range(0, len(index)):
            left = index[i]['start']
            right = index[i]['end']
            if (id1 >= left and id1 <= right) and(id2 >= left and id2 <= right):
                count += 1
    
    print(count)
    print(len(file_list))



check_cwe_gpt_dataset()