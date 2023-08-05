import os

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


def parse_code_from_repo(git_repo_src, git_repo_dst):
    os.mkdir("./data/remote_data/")
    os.system("git clone " + git_repo_src)
    os.system("git clone " + git_repo_dst)
    
    repo_src_name = parse_for_git_repo_name(git_repo_src)
    repo_dst_name = parse_for_git_repo_name(git_repo_dst)

    os.system("mv -rf " + "./" + repo_src_name + " ./data/remote_data/")
    os.system("mv -rf " + "./" + repo_dst_name + " ./data/remote_data/")



# test
file_list = []
dfs_dir_for_file("/Users/haoranyan/git_rep/Android-2.2.3", ".java", file_list)
print(file_list)