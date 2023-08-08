import os
from rename import *

def parse_for_git_repo_name(repo_url):
    cur = len(repo_url) - 1
    while repo_url[cur] != "/":
        cur -= 1
    return repo_url[cur + 1: len(repo_url) - 4]

def dfs_dir_for_file(dir, file_type, file_list):
    new_dir = dir

    if ".DS_Store" in dir:
        return
    
    if os.path.isfile(dir):
        if dir.endswith(file_type):
            file_list.append(dir)
    elif os.path.isdir(dir):
        for child_dir in os.listdir(dir):
            new_dir = os.path.join(dir, child_dir)
            dfs_dir_for_file(new_dir, file_type, file_list)
    return 


def parse_code_from_repo_double(git_repo_src, git_repo_dst, file_type):
    os.system("rm -rf ./data/remote_data/*")
    os.system("rm -rf ./data/tmp_data/*")
    os.system("mkdir ./data/remote_data/")
    os.system("mkdir ./data/tmp_data/")
    os.system("git clone " + git_repo_src)
    os.system("git clone " + git_repo_dst)
    
    repo_src_name = parse_for_git_repo_name(git_repo_src)
    repo_dst_name = parse_for_git_repo_name(git_repo_dst)

    os.system("mv -f " + "./" + repo_src_name + " ./data/remote_data/")
    os.system("mv -f " + "./" + repo_dst_name + " ./data/remote_data/")

    file_list_src = []
    file_list_dst = []

    dfs_dir_for_file("./data/remote_data/" + repo_src_name, file_type, file_list_src)
    dfs_dir_for_file("./data/remote_data/" + repo_dst_name, file_type, file_list_dst)

    os.system("rm -rf ./data/tmp_data/*")
    os.system("mkdir ./data/tmp_data/" + repo_src_name)
    os.system("mkdir ./data/tmp_data/" + repo_dst_name)

    for file in file_list_src:
        os.system("mv " + file + " ./data/tmp_data/" + repo_src_name + "/")

    for file in file_list_dst:
        os.system("mv " + file + " ./data/tmp_data/" + repo_dst_name + "/")

    rename_mode2(repo_src_name, repo_dst_name)

    os.system("rm -rf ./data/input/*")

    file_list = []
    dfs_dir_for_file("./data/tmp_data/", file_type, file_list)

    for file in file_list:
        os.system("mv " + file + " ./data/input/")

    os.system("python3 rm_comment.py ./data/input/")

    
def parse_code_from_repo_single(git_repo, file_type):
    os.system("mkdir ./data/remote_data/")
    os.system("git clone " + git_repo)
    
    repo_src_name = parse_for_git_repo_name(git_repo)

    os.system("rm -rf ./data/remote_data/*")
    os.system("mv  " + "./" + repo_src_name + " ./data/remote_data/")

    file_list_src = []

    dfs_dir_for_file("./data/remote_data/" + repo_src_name, file_type, file_list_src)

    os.system("rm -rf ./data/input/*")

    for file in file_list_src:
        os.system("mv " + file + " ./data/input/")
    
    os.system("python3 rm_comment.py ./data/input/")
parse_code_from_repo_double("https://github.com/master1018/tamer_dataset.git", "https://github.com/master1018/tamer_dataset2.git", "java")