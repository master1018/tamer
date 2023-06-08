import base64
import time
from graphviz import Graph
from pathlib import Path
import os
import streamlit as st
import pandas as pd

choice_code     =   ["C", "C++", "Java", "Python"]
file_type       =   [".c", ".cpp", ".java", ".py"]

color_arr       =   ["blue", "green", "black", "red", "yellow"]

exec_jar_pos        = "./sourcecode/out/artifacts/finals_jar/finals.jar"

class Result:
    cmp_file1 = ""
    cmp_file2 = ""
    line_msg = []
    similar_arr = []
    tmp_data = []
    def __init__(self) -> None:
        pass

    def parse_optional_java_class(self, optional_str):
        ret1 = ""
        ret2 = ""
        idx = 0
        while optional_str[idx] != "e":
            idx += 1
        idx += 1
        while optional_str[idx] != ",":
            ret1 += optional_str[idx]
            idx += 1
        while optional_str[idx] != "e":
            idx += 1
        idx += 1
        while optional_str[idx] != ",":
            ret2 += optional_str[idx]
            idx += 1
        return [int(ret1), int(ret2)]

    
    def get_result_msg(self, fileName):
        fp = open(fileName, "r")
        while True:
            line = fp.readline()
            if not line:
                break
            else:
                self.tmp_data.append(line[0: len(line) - 1])
        fp.close()

    def parse_result_msg(self):
        self.cmp_file1 = self.tmp_data[0]
        self.cmp_file2 = self.tmp_data[1]
        idx = 2
        while idx < len(self.tmp_data):
            if (self.tmp_data[idx] == "begin"):
                idx += 1
                while self.tmp_data[idx] != "end":
                    # 存储相似度信息
                    if self.tmp_data[idx].isdigit():
                        self.similar_arr.append(int(self.tmp_data[idx]))
                    else:
                        # 存储行数位置信息
                        ret1 = self.parse_optional_java_class(self.tmp_data[idx])
                        idx += 1
                        ret2 = self.parse_optional_java_class(self.tmp_data[idx])
                        self.line_msg.append([ret1[0], ret1[1], ret2[0], ret2[1]])
                    idx += 1
            idx += 1
        # 去重
        new_list1 = []
        new_index = []
        for i in range(0, len(self.line_msg)):
            if (self.line_msg[i] not in new_list1):
                new_list1.append(self.line_msg[i])
                new_index.append(i)
        self.line_msg = new_list1
        new_list2 = []
        for i in range(0, len(new_index)):
            new_list2.append(self.similar_arr[new_index[i]])
        self.similar_arr = new_list2
        assert(len(self.similar_arr) == len(self.line_msg))

    # 重复打开关闭文件效率会很差，目前时间影响不大，考虑后期进行优化
    def save_result(self):
        fp = open("./result/res", "w")
        for i in range(0, len(self.similar_arr)):
            fp.write(str(self.similar_arr[i]) + "%\n")
            fp.write("src\n")
            # cmp file 1 的内容
            begin = self.line_msg[i][0]
            end = self.line_msg[i][1]
            count = 0
            fp1 = open(self.cmp_file1, "r")
            while True:
                line = fp1.readline()
                if not line:
                    break
                count += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line)
            fp.write("dst\n")
            fp1.close()
            # cmp file 2 的内容
            begin = self.line_msg[i][2]
            end = self.line_msg[i][3]
            count = 0
            fp2 = open(self.cmp_file2, "r")
            while True:
                line = fp2.readline()
                if not line:
                    break
                count += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line)
            fp2.close()
        fp.close()
    
#
#    为了便于前后端数据的交互,设置res文件的格式如下:
    
#    *****文件开始,不包含这句*****
#    98%
#    src
#    1. if (a == 1 && b == 2)
#    2.      return true;
#    dst
#    4. if (a[1] == 1 && a[2] == 2)
#    5.      return 1;
#    77%
#    src
#    7. int tmp = sort(a, a + 1);
#    8. return binary_search(a, a + 1, tmp)
#    dst
#    7. int tmp = qsort(b, b + 1);
#    8. return erfenchazhao(b, tmp); 
#    *****文件结束*****
def build_graph(a, b, c) -> None:
    dot = Graph()
    dot.attr('node', shape='box')
    dot.attr(rankdir='LR')
    dot.node("S", "代码段克隆情况")
    for i in range(0, len(a)):
        a[i] = a[i].replace("\n", "\\l")
        b[i] = b[i].replace("\n", "\\l")
        dot.node('A' + str(i), a[i], color=color_arr[i % 5])
        dot.node('B' + str(i), b[i], color=color_arr[i % 5])
        dot.edge("S", 'A' + str(i))
        dot.edge('A' + str(i), "B" + str(i), c[i], color=color_arr[i % 5], style="dashed")

    dot.render("./result/res_graph", format="png", view=False)


def res_visual() -> None:
    fp = open("./result/res", "r")
    res_str_arr = []
    while True:
        line = fp.readline()
        if not line:
            break
        else:
            res_str_arr.append(line)
    fp.close()

    count = 0
    para1 = []
    para2 = []
    para3 = []
    idx = 0

    while count < len(res_str_arr):
        tmp_str = res_str_arr[count]
        if (len(tmp_str) <= 5 and tmp_str[len(tmp_str) - 2] == "%"):
            para3.append(tmp_str[0:len(tmp_str) - 1])
            count += 1
        elif (tmp_str == "src\n"):
            count += 1
            para1.append("")
            while True:
                src_str = res_str_arr[count]
                if (src_str == "dst\n"):
                    break
                else:
                    para1[idx] += src_str
                count += 1
        elif (tmp_str == "dst\n"):
            count += 1
            para2.append("")
            while True:
                if (count == len(res_str_arr)):
                    break
                dst_str = res_str_arr[count]
                if (len(dst_str) <= 5 and dst_str[len(dst_str) - 2] == "%"):
                    break
                else:
                    para2[idx] += dst_str
                count += 1
            idx += 1
        
    build_graph(para1, para2, para3)


def init() -> None:
    st.session_state.file_type  =   ""
    st.session_state.src_file   =   None
    st.session_state.dst_file   =   None
    st.session_state.res_file   =   None


def show_result() -> None:
    res_visual()
    st.header("检测结果")
    with st.empty():
        st.image("./result/res_graph.png")
    

def exec_jar() -> None:
    command = "java -jar " + exec_jar_pos
    os.system(command)
    res = Result()
    res.get_result_msg("./result/output")
    res.parse_result_msg()
    res.save_result()

def callback1() -> None:
    if st.session_state.src_file != None:
        fp = open("./data/input/1" + st.session_state.file_type, "w")
        fp.write(st.session_state.src_file.getvalue().decode("utf-8"))
        fp.close()
        st.session_state.src_file = None

    if st.session_state.dst_file != None:
        fp = open("./data/input/2" + st.session_state.file_type, "w")
        fp.write(st.session_state.dst_file.getvalue().decode("utf-8"))
        fp.close()
        st.session_state.dst_file = None

    # 执行java脚本
    exec_jar()

    # 调用检测代码检测出结果，结果以文件方式保存，再重新读入
    # 下面用来测试，假设结果文件为result.c
    fp = open("./tmp/sem", "w")
    fp.write("2")
    fp.close()

def callback2() -> None:
    fp = open("./tmp/sem", "w")
    fp.write("1")
    fp.close()

def get_base64(bin_file):
    with open(bin_file, 'rb') as f:
        data = f.read()
    return base64.b64encode(data).decode()

def show_info() -> None:
    c1, c2= st.columns([0.8, 0.2])
    c1.header("❄️ Tamer 代码克隆检测 ❄️")
    c2.image("./image/1.png")
    #with st.expander("关于我们"):
     #   st.write(Path("README.md").read_text())
    st.text("这一部分可以用typora写一些关于我们产品的介绍，使用说明等")
    st.text("更多了解")
    chose = st.selectbox(label="test", options=["关于我们", "Tamer的优点", "Tamer的应用场景"])
    if chose ==  "关于我们":
        st.write(Path("README.md").read_text())
    elif chose == "Tamer的优点":
        st.write("hh")
    else:
        st.write("emmm")

def main() -> None:
    fp = open("./tmp/sem", "r")
    sem_show = int(fp.read())
    fp.close()

    init()

    if sem_show == 1:
        show_info()
    elif sem_show == 2:
        show_result()

    #st.sidebar.subheader("Tamer：代码克隆检测")
    st.sidebar.image("./image/logo.png")
    code_selection = st.sidebar.selectbox(
        "选择代码语言", options=choice_code
    )
    
    st.session_state.file_type = file_type[choice_code.index(code_selection)]

    st.session_state.src_file = st.sidebar.file_uploader("上传源文件", accept_multiple_files=False)
    st.session_state.dst_file = st.sidebar.file_uploader("上传待检测文件", accept_multiple_files=False)
    c1, c2 = st.sidebar.columns(2)
    c1.button("检测", on_click=callback1)
    c2.button("首页", on_click=callback2)

 


if __name__ == "__main__":
    st.set_page_config(
        "Tamer：代码克隆检测系统",
        "📊",
        initial_sidebar_state="expanded",
        layout="wide",
    )
    main()
