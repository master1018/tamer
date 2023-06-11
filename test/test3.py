import os, sys

# 打开文件
path = "/Users/haoranyan/git_rep/tamer/result/data"
dirs = os.listdir(path)

# 输出所有文件和文件夹
for file in dirs:
   print(file)