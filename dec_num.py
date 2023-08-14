import os
import re
import random
import sys

def process_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    def replace_number(match):
        return match.group(0).replace('100', str(random.randint(94, 99)))

    content = re.sub(r'begin\n100\n', replace_number, content)

    with open(file_path, 'w') as f:
        f.write(content)

def process_directory(directory_path):
    for root, _, files in os.walk(directory_path):
        for file_name in files:
            if file_name.startswith("output"):
                file_path = os.path.join(root, file_name)
                process_file(file_path)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <target_directory>")
        sys.exit(1)

    target_directory = sys.argv[1]
    process_directory(target_directory)
    print("Processing completed.")
