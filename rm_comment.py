#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import re

def remove_comments_and_empty_lines(file_path):
    try:
        with open(file_path, "r") as file:
            content = file.read()
    except Exception:
        print(file_path)
        return

    # Use regular expressions to remove single-line comments
    content = re.sub(r'//.*', '', content)

    # Use regular expressions to remove multi-line comments
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)

    # Use regular expressions to remove empty lines
    content = re.sub(r'^\s*$', '', content, flags=re.MULTILINE)

    # Remove consecutive empty lines
    content = re.sub(r'\n\s*\n', '\n', content)

    with open(file_path, "w") as file:
        file.write(content)

def process_directory(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                #print(f"处理文件: {file_path}")
                remove_comments_and_empty_lines(file_path)
                
def delete_code_before_class(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, "r") as f:
                        lines = f.readlines()
                except Exception:
                    print(file_path)
                    continue
                new_lines = []
                inside_class = False
                for line in lines:
                    if "class" in line and not inside_class:
                        inside_class = True
                    if inside_class:
                        new_lines.append(line)
                
                with open(file_path, "w") as f:
                    f.writelines(new_lines)

if __name__ == "__main__":
    import sys

    # 检查参数，确保提供了要处理的目录路径
    if len(sys.argv) != 2:
        print("用法: {} <目录路径>".format(sys.argv[0]))
        sys.exit(1)

    # 获取输入目录
    target_directory = sys.argv[1]

    # 检查目录是否存在
    if not os.path.isdir(target_directory):
        print("错误: 目录 '{}' 不存在。".format(target_directory))
        sys.exit(1)

    # 开始处理目录及其子目录下的Java文件
    process_directory(target_directory)
    delete_code_before_class(target_directory)

    print("处理完成！")
