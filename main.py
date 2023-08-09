import base64
import time
import sys
from graphviz import Graph
from pathlib import Path
import os
sys.path.insert(1, "C:/users/86176/appdata/roaming/python/python310/site-packages")
import streamlit as st
from matplotlib import pyplot as plt
import numpy as np
import pandas as pd
from st_aggrid import AgGrid, DataReturnMode, GridUpdateMode, GridOptionsBuilder
from streamlit_echarts import st_echarts
from echarts_option import option_pie, option_gauge, option_bar, option_pie2
from cwe_db import *
from streamlit_option_menu import option_menu
from streamlit_ace import st_ace
from PIL import Image
from tmp_data import clone_pairs
from streamlit_elements import elements, mui, html
from streamlit_elements import dashboard
import random
import json
from tamer_tool import *
from rename import *

with open("./data/name_to_id.json", "r") as f:
    name_to_id = json.load(f)
with open("./data/id_to_name.json", "r") as f:
    id_to_name = json.load(f)

choice_code     =   ["Java","C", "C++",  "Python"]
file_type       =   [".java",".c", ".cpp",  ".py"]

color_arr       =   ["blue", "green", "black", "red", "yellow"]

labels1         =   ["no clone", "low clone", "medium clone ", "high clone"]
labels2         =   ["low", "medium", "high"]

exec_jar_pos        = "./sourcecode/out/artifacts/finals_jar/finals.jar"
#  1000279=[1000463], 1000533=[1000559, 1000970, 1000499, 1000179]
# 1000077=[1000140], 1000837=[1000431, 1000814, 1000285, 1000043, 2000166, 1000096], 2000272=[2000223], 

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
        self.tmp_data = []
        fp = open(fileName, "r")
        while True:
            line = fp.readline()
            if not line:
                break
            else:
                self.tmp_data.append(line[0: len(line) - 1])
        fp.close()

    def parse_result_msg(self):
        self.line_msg = []
        self.similar_arr = []
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
        st.session_state.res_list = []
        for i in range(0, len(self.line_msg)):
            st.session_state.res_list.append([i + 1, "({0}, {1})".format(self.line_msg[i][0], self.line_msg[i][1]), "({0}, {1})".format(self.line_msg[i][2], self.line_msg[i][3]), "{0}%".format(self.similar_arr[i]), ""])

    # 重复打开关闭文件效率会很差，目前时间影响不大，考虑后期进行优化
    def save_result(self, filename="./result/res"):
        fp = open(filename, "w")
        for i in range(0, len(self.similar_arr)):
            fp.write(str(self.similar_arr[i]) + "%\n")
            fp.write("src\n")
            # cmp file 1 的内容
            begin = self.line_msg[i][0]
            end = self.line_msg[i][1]
            count = 0
            fp1 = open(self.cmp_file1, "r")
            dele_space = 0
            while True:
                line = fp1.readline()
                if not line:
                    break
                count += 1
                if count == begin:
                    for j in range(0, len(line)):
                        if line[j] != " ":
                            break
                        else:
                            dele_space += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line[dele_space:len(line)])
            fp.write("dst\n")
            fp1.close()
            # cmp file 2 的内容
            begin = self.line_msg[i][2]
            end = self.line_msg[i][3]
            count = 0
            fp2 = open(self.cmp_file2, "r")
            dele_space = 0
            while True:
                line = fp2.readline()
                if not line:
                    break
                count += 1
                if count == begin:
                    for j in range(0, len(line)):
                        if line[j] != " ":
                            break
                        else:
                            dele_space += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line[dele_space:len(line)])
            fp2.close()
        fp.close()

    def read_data_set(self):
        fp = open("./result/output2", "r")
        tmp_str = fp.read()
        tmp_list = tmp_str.split(" ")
        tmp_list.pop(len(tmp_list) - 1)
        self.similar_arr = list(map(int, tmp_list))
        
        stat = [0, 0, 0, 0, 0]

        for i in range(0, len(self.similar_arr)):
            if (self.similar_arr[i] >= 1000):
                self.similar_arr[i] = 1000

            if self.similar_arr[i] >= 20:
                stat[0] += 1

            if self.similar_arr[i] <= 400 and self.similar_arr[i] > 200:
                stat[1] += 1
            elif self.similar_arr[i] > 400 and self.similar_arr[i] <= 700:
                stat[2] += 1
            elif self.similar_arr[i] > 700:
                stat[3] += 1
            else:
                stat[4] += 1
            self.similar_arr[i] = self.similar_arr[i] / 1000
        
        option_gauge["series"][0]["data"][0]["value"] = int((stat[0] / len(self.similar_arr)) * 1000) / 10

        option_pie2["dataset"]["source"][1][1] = stat[1]
        option_pie2["dataset"]["source"][2][1] = stat[2]
        option_pie2["dataset"]["source"][3][1] = stat[3]

        option_bar["series"][0]["data"][0]["value"] = stat[3]
        option_bar["series"][0]["data"][1]["value"] = stat[2]
        option_bar["series"][0]["data"][2]["value"] = stat[1]

        st.session_state.options.append(option_gauge)
        st.session_state.options.append(option_pie2)
        st.session_state.options.append(option_bar)
        
        return self.similar_arr

def aggrid(df):
    gb = GridOptionsBuilder.from_dataframe(df)
    selection_mode = 'single' # 定义单选模式，多选为'multiple'
    enable_enterprise_modules = True # 设置企业化模型，可以筛选等
    #gb.configure_default_column(editable=True) #定义允许编辑
    
    return_mode_value = DataReturnMode.FILTERED  #__members__[return_mode]
    gb.configure_selection(selection_mode, use_checkbox=True) # 定义use_checkbox
    
    gb.configure_side_bar()
    gb.configure_grid_options(domLayout='normal')
    gb.configure_pagination(paginationAutoPageSize=False, paginationPageSize=5)
    #gb.configure_default_column(editable=True, groupable=True)
    gridOptions = gb.build()
    
    update_mode_value = GridUpdateMode.MODEL_CHANGED
    
    grid_response = AgGrid(
                        df, 
                        gridOptions=gridOptions,
                        fit_columns_on_grid_load = True,
                        data_return_mode=return_mode_value,
                        update_mode=update_mode_value,
                        enable_enterprise_modules=enable_enterprise_modules,
                        theme='streamlit'
                        )  
    #df = grid_response['data']
    selected = grid_response['selected_rows']
    if len(selected) == 0:
        return -1
    else:
        return selected[0]['克隆对索引']  


def list_to_df(src_list):
   # print(src_list)
    for i in range(0, len(src_list)):
        tmp = src_list[i][3][0: len(src_list[i][3]) - 1]
        tmp = int(tmp)
        if tmp <= 40:
            src_list[i][4] = "low"
        elif tmp > 40 and tmp <= 70:
            src_list[i][4] = "medium" 
        else:
            src_list[i][4] = "high"

  # print(src_list)
    src_array = np.array(src_list)
    df = pd.DataFrame(src_array)
   # print(df)
    df.columns = ['克隆对索引', '源代码段', '目标代码段', '相似度', '克隆程度']
    return df

def show_gif():
    fp = open("./tmp/sem", "w")
    fp.write("3")
    fp.close()
    if (st.session_state.tmp != None):
        st.session_state.tmp.empty()
    st.session_state.tmp = st.empty()
    file_ = open("/Users/haoranyan/git_rep/tamer/image/xinyemiao.gif", "rb")
    contents = file_.read()
    data_url = base64.b64encode(contents).decode("utf-8")
    file_.close()
    with st.session_state.tmp.container():
        st.markdown(
            f'<img src="data:image/gif;base64,{data_url}" alt="cat gif">',
        unsafe_allow_html=True,
        )

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
    dot.node("S", "Clone Pairs")
    for i in range(0, len(a)):
        a[i] = a[i].replace("\n", "\\l")
        b[i] = b[i].replace("\n", "\\l")
        dot.node('A' + str(i), a[i], color=color_arr[i % 5])
        dot.node('B' + str(i), b[i], color=color_arr[i % 5])
        dot.edge("S", 'A' + str(i))
        dot.edge('A' + str(i), "B" + str(i), c[i], color=color_arr[i % 5], style="dashed")

    dot.render("./result/res_graph", format="png", view=False)

def readline_count(file_name):
    return len(open(file_name,encoding="utf-8").readlines())

def res_visual(file="./result/res") -> None:
    fp = open(file, "r")
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
    ret1 = para1.copy()
    ret2 = para2.copy()
    ret3 = para3.copy()
    build_graph(para1, para2, para3)
    return ret1, ret2, ret3


def init() -> None:
    if "file_type" not in st.session_state:
        st.session_state.file_type = ""
    if "show_res" not in st.session_state:
        st.session_state.show_res = 0
    if "src_file" not in st.session_state:
        st.session_state.src_file   =   None
    if "dst_file" not in st.session_state:
        st.session_state.dst_file   =   None
    st.session_state.res_file   =   None
    st.session_state.tmp        =   None
    if 'res_list' not in st.session_state:
        st.session_state.res_list = []
    if 'show_index' not in st.session_state:
        st.session_state.show_index = -1
    if 'mode' not in st.session_state:
        st.session_state.mode = 1
    if 'options' not in st.session_state:
        st.session_state.options = []
    if 'clone_pairs' not in st.session_state:
        st.session_state.clone_pairs = []
    if "cwe_index" not in st.session_state:
        st.session_state.cwe_index = -1
    if "muti_mode" not in st.session_state:
        st.session_state.muti_mode = 0
    if "src_url" not in st.session_state:
        st.session_state.src_url = ""
    if "dst_url" not in st.session_state:
        st.session_state.dst_url = ""
    if "show_single_res" not in st.session_state:
        st.session_state.show_single_res = 0
    if "parse_result_list" not in st.session_state:
        st.session_state.parse_result_list = []

def show_result(filename) -> None:
    ret1, ret2, ret3 = res_visual()

    static_res = [0, 0, 0]
    for i in range(0, len(ret3)):
        cmp = int(ret3[i][0:len(ret3[i]) - 1])
        if (cmp <= 40):
            static_res[0] += list(ret2[i]).count("\n")
        elif cmp > 40 and cmp < 70:
            static_res[1] += list(ret2[i]).count("\n")
        else:
            static_res[2] += list(ret2[i]).count("\n")

    st.header("检测结果")
    with st.expander("克隆对总览"):
        with st.empty():
            st.image("./result/res_graph.png")
    st.write("克隆对")
        
    df = list_to_df(st.session_state.res_list)
    select_row = aggrid(df)
    c1, c2= st.columns(2)
        
    st.session_state.show_index = int(select_row)
    chose_index = st.session_state.show_index
    # 行对齐
    if (chose_index > 0):
        a = list(ret1[chose_index - 1]).count("\n")
        b = list(ret2[chose_index - 1]).count("\n")
        if (a > b):
            ret2[chose_index - 1] += ".\n" * (a - b - 1) + "."
        elif (a < b):
            ret1[chose_index - 1] += ".\n" * (b - a - 1) + "."
            
        c1.code(ret1[chose_index - 1], "java")
        c2.code(ret2[chose_index - 1], "java")
        #c3.write("相似度为: " + str(ret3[chose_index - 1]))

        # 绘制饼状图
    fig = plt.figure()
    dst_lines = readline_count("./data/input/" + filename + st.session_state.file_type)
    pie_sizes = [dst_lines - sum(static_res), static_res[0], static_res[1], static_res[2]]
    #pie_colors = ['yellowgreen', 'gold', 'lightskyblue', 'lightcoral']
    #pie_explode = (0, 0, 0, 0.1)
    option_pie["dataset"]["source"][1][1] = pie_sizes[0]
    option_pie["dataset"]["source"][2][1] = pie_sizes[1]
    option_pie["dataset"]["source"][3][1] = pie_sizes[2]
    option_pie["dataset"]["source"][4][1] = pie_sizes[3]
    c4, c5 = st.columns(2)
   # with c4:
      #  st_echarts(options=option)
    st_echarts(options=option_pie)

def exec_jar() -> None:
    # generate output
    os.system("python3 rm_comment.py ./data/input/")
    os.system("rm -f ./tmp/type")
    mode_write = str(st.session_state.mode)
    os.system("echo " + mode_write + " > ./tmp/type")
    command = "java -jar " + exec_jar_pos
    os.system(command)
    
    # parse for output
    res = Result()
    res.get_result_msg("./result/output")
    res.parse_result_msg()
    res.save_result()

def callback1() -> None:
    # 单件检测
    if st.session_state.mode == 1:
        if st.session_state.src_file != None:
            fp = open("./data/input/2" + st.session_state.file_type, "w")
            fp.write(st.session_state.src_file)
            fp.close()
            st.session_state.src_file = None

        if st.session_state.dst_file != None:
            fp = open("./data/input/1" + st.session_state.file_type, "w")
            fp.write(st.session_state.dst_file)
            fp.close()
            st.session_state.dst_file = None

        # 执行java脚本
        exec_jar()
        # 调用检测代码检测出结果，结果以文件方式保存，再重新读入
        # 下面用来测试，假设结果文件为result.c
        if st.session_state.tmp != None:
            st.session_state.tmp.empty()
    elif st.session_state.mode == 2:
        if st.session_state.muti_mode == 1:
            # generate output
            if st.session_state.src_url != "":
                parse_code_from_repo_single(st.session_state.src_url, st.session_state.file_type)
                rename_mode1()
            os.system("python3 rm_comment.py ./data/input/")
            os.system("rm -f ./tmp/type")
            mode_write = str(st.session_state.mode)
            os.system("echo " + mode_write + " > ./tmp/type")
            command = "java -jar " + exec_jar_pos
            os.system(command)
     
            st.session_state.clone_pairs = []
            st.session_state.options = []

        else:
            if st.session_state.src_url != "" and st.session_state.dst_url != "":
                parse_code_from_repo_double(st.session_state.src_url, st.session_state.dst_url, st.session_state.file_type)
            
            os.system("rm -f ./tmp/type")
            # use work_type 3 of tamer
            os.system("python3 rm_comment.py ./data/input/")
            mode_write = str(3)
            os.system("echo " + mode_write + " > ./tmp/type")
            command = "java -jar " + exec_jar_pos
            os.system(command)
            st.session_state.options = []
           

    elif st.session_state.mode == 3:
        # TODO add result auto parse
        # path = "./result/exp_data"
        #dir = os.listdir(path)
        #for file in dir:
        #    if file[0: len(file) - 1] == "output":
        #        res = Result()
        #        res.get_result_msg(path + "/" + file)
        #        res.parse_result_msg()
        #        save_path = path + "/res" + file[len(file) - 1] 
        #        res.save_result(save_path)
        
        a = 1
    st.session_state.show_res = 1

def set_bg_hack_url():
    '''
    A function to unpack an image from url and set as bg.
    Returns
    -------
    The background.
    '''
        
    st.markdown(
         f"""
         <style>
         .stApp {{
             background: url("https://img2.wallspic.com/crops/1/9/5/3/1/113591/113591-guan_bi_le-fen_hong_se-bai_se-2560x1440.jpg");
             background-size: cover
         }}
         </style>
         """,
         unsafe_allow_html=True
     )

def callback2() -> None:
    if st.session_state.muti_mode == 0:
        st.session_state.muti_mode = 1
    else:
        st.session_state.muti_mode = 0

def get_base64(bin_file):
    with open(bin_file, 'rb') as f:
        data = f.read()
    return base64.b64encode(data).decode()

def show_single_result(name1, name2) -> None:
    
    # parse for output
    res = Result()
    res.get_result_msg("./result/exp_data/output" + str(name_to_id[name1[:-5]]) + "_" + str(name_to_id[name2[:-5]]))
    res.parse_result_msg()
    res.save_result()

    if st.session_state.tmp != None:
        st.session_state.tmp.empty()
    show_result(str(name_to_id[name1[:-5]]))

def callback3() -> None:
    print("st.session_state.show_single_res:" + str(st.session_state.show_single_res))
    if st.session_state.show_single_res == 0:
        st.session_state.show_single_res = 1
    else:
        st.session_state.show_single_res = 0
    print("st.session_state.show_single_res:" + str(st.session_state.show_single_res))

def show_result2() -> None:
    # print(id_to_name)
    st.write(" ")
    st.write(" ")
    st.write(" ")
    c1, c2, c3 = st.columns(3)
    with c1:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true" align="center">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2017/10/artifactory-feature-4-1.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">已检测有效代码</h2>
            <h3 align="center">    100982行</h3>""", unsafe_allow_html=True)
   # c1, c2 = st.columns(2)
    with c2:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2020/02/efficient.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">检测耗时</h2>
            <h3 align="center">    3.12s</h3>""", unsafe_allow_html=True)
    with c3:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2020/02/delivering-trust.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">发现克隆代码</h2>
            <h3 align="center">    756对</h3>""", unsafe_allow_html=True)

    c2, c3 = st.columns(2)
    selected = []
    res = Result()
    similar_arr = res.read_data_set()
    with c2:
        for i in range(0, 13):
            st.write(" ")
        st_echarts(st.session_state.options[1])
    with c3:
        for i in range(0, 10):
            st.write(" ")
        idx = -1
        if st.session_state.clone_pairs == []:
            tmp = 0
            for i in range(0, len(clone_pairs)):
                if type(clone_pairs[i]) == int:
                    tmp = clone_pairs[i]
                    idx += 1
                else:
                    for j in range(0, len(clone_pairs[i])):
                        # TODO: map对应的文件名
                        st.session_state.clone_pairs.append([id_to_name[str(tmp)] + ".java", id_to_name[str(clone_pairs[i][j])] + ".java", str(similar_arr[idx]) + "%"])
        df = pd.DataFrame(np.array(st.session_state.clone_pairs))
        df.columns = ["克隆代码1", "克隆代码2", "相似度"]
        gb = GridOptionsBuilder.from_dataframe(df)
        selection_mode = 'single' # 定义单选模式，多选为'multiple'
        enable_enterprise_modules = True # 设置企业化模型，可以筛选等
        #gb.configure_default_column(editable=True) #定义允许编辑
        
        return_mode_value = DataReturnMode.FILTERED  #__members__[return_mode]
        gb.configure_selection(selection_mode, use_checkbox=True) # 定义use_checkbox
        
        gb.configure_side_bar()
        gb.configure_grid_options(domLayout='normal')
        gb.configure_pagination(paginationAutoPageSize=False, paginationPageSize=5)
        #gb.configure_default_column(editable=True, groupable=True)
        gridOptions = gb.build()
        
        update_mode_value = GridUpdateMode.MODEL_CHANGED

        grid_response = AgGrid(
                        df, 
                        gridOptions=gridOptions,
                        fit_columns_on_grid_load = True,
                        data_return_mode=return_mode_value,
                        update_mode=update_mode_value,
                        enable_enterprise_modules=enable_enterprise_modules,
                        theme='streamlit'
                            )  
        selected = grid_response['selected_rows']
        # print(selected)

    if len(selected) > 0:
        path = "./data/input/"
            #st.session_state.src_file = path + selected['克隆代码1']
            #st.session_state.dst_file = path + selected['克隆代码2']
        c1, c2 = st.columns(2)

        tmp1 = ""
        tmp2 = ""
        fp = open(path + str(name_to_id[selected[0]['克隆代码1'][:-5]]) + ".java", "r")
        tmp1 = fp.read()
        fp.close()
        fp = open(path + str(name_to_id[selected[0]['克隆代码2'][:-5]]) + ".java", "r")
        tmp2 = fp.read()
        fp.close()

        a = list(tmp1).count("\n")
        b = list(tmp2).count("\n")
        if (a > b):
            tmp2 += ".\n" * (a - b - 1) + "."
        elif (a < b):
            tmp1 += ".\n" * (b - a - 1) + "."
        
        with c1.expander(selected[0]['克隆代码1']):
            st.code(tmp1, "java", line_numbers=True)
        with c2.expander(selected[0]['克隆代码2']):
            st.code(tmp2, "java", line_numbers=True)
        #c3.write("相似度为: " + str(ret3[chose_index - 1]))
        st.button("单件检测结果", on_click=callback3)  
        print("st.session_state.show_single_res: ", str(st.session_state.show_single_res))
        if(st.session_state.show_single_res):
            print("show single result")
            show_single_result(selected[0]['克隆代码1'], selected[0]['克隆代码2'])

    for i in range(0, 13):
        st.write(" ")
    #with c5:
    #    st_echarts(st.session_state.options[2])

def show_result2_2() -> None:
    # print(id_to_name)
    st.write(" ")
    st.write(" ")
    st.write(" ")
    c1, c2, c3 = st.columns(3)
    with c1:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true" align="center">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2017/10/artifactory-feature-4-1.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">已检测有效代码</h2>
            <h3 align="center">    100982行</h3>""", unsafe_allow_html=True)
   # c1, c2 = st.columns(2)
    with c2:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2020/02/efficient.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">检测耗时</h2>
            <h3 align="center">    3.12s</h3>""", unsafe_allow_html=True)
    with c3:
        st.markdown("""
            <video width="250" autoplay="true" muted="true" loop="true">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2020/02/delivering-trust.mp4" 
                    type="video/mp4" />
            </video>
            <h2 align="center">发现克隆代码</h2>
            <h3 align="center">    756对</h3>""", unsafe_allow_html=True)

    c2, c3 = st.columns(2)
    selected = []
    res = Result()
    result_dir = "./result/exp_data/"
    output_file = os.listdir(result_dir)
    if st.session_state.parse_result_list == []:
        write_str = ""
    # static the sum result 
        for output in output_file:
            if "output" in output and output[0] != '.':
                res.get_result_msg(result_dir + output)
                res.parse_result_msg()
                for i in range(0, len(res.line_msg)):
                    file_id1 = 0
                    file_id2 = 0

                    cur = len(res.cmp_file1) - 1
                    while res.cmp_file1[cur] != "/":
                        cur -= 1
                    file_id1 = int(res.cmp_file1[cur + 1: len(res.cmp_file1) - 5])

                    cur = len(res.cmp_file2) - 1
                    while res.cmp_file2[cur] != "/":
                        cur -= 1
                    file_id2 = int(res.cmp_file2[cur + 1: len(res.cmp_file2) - 5])

                    write_str += str(res.similar_arr[i] * 10)
                    write_str += " "

                    st.session_state.parse_result_list.append([id_to_name[str(file_id1)] + ".java", "({0}, {1})".format(res.line_msg[i][0], res.line_msg[i][1]), \
                                            id_to_name[str(file_id2)] + ".java", "({0}, {1})".format(res.line_msg[i][2], res.line_msg[i][3]), \
                                                "{0}%".format(res.similar_arr[i]) ])
        fp = open("./result/output2", "w")
        fp.write(write_str)
        fp.close()

    with c2:
        for i in range(0, 13):
            st.write(" ")
        res = Result()
        res.read_data_set()
        st_echarts(st.session_state.options[1])
    for i in range(0, 10):
            st.write(" ")
        #if st.session_state.clone_pairs == []:
        #    tmp = 0
        #    for i in range(0, len(clone_pairs)):
        #        if type(clone_pairs[i]) == int:
        #            tmp = clone_pairs[i]
        #        else:
        #            for j in range(0, len(clone_pairs[i])):
        #                # TODO: map对应的文件名
        #                st.session_state.clone_pairs.append([id_to_name[str(tmp)] + ".java", id_to_name[str(clone_pairs[i][j])] + ".java", str(similar_arr[i]) + "%"])
    df = pd.DataFrame(np.array(st.session_state.parse_result_list))
    df.columns = ["源仓库文件", "克隆代码行索引1",  "目标仓库文件", "克隆代码行索引2","相似度"]
    gb = GridOptionsBuilder.from_dataframe(df)
    selection_mode = 'single' # 定义单选模式，多选为'multiple'
    enable_enterprise_modules = True # 设置企业化模型，可以筛选等
        #gb.configure_default_column(editable=True) #定义允许编辑
        
    return_mode_value = DataReturnMode.FILTERED  #__members__[return_mode]
    gb.configure_selection(selection_mode, use_checkbox=True) # 定义use_checkbox
        
    gb.configure_side_bar()
    gb.configure_grid_options(domLayout='normal')
    gb.configure_pagination(paginationAutoPageSize=False, paginationPageSize=5)
        #gb.configure_default_column(editable=True, groupable=True)
    gridOptions = gb.build()
        
    update_mode_value = GridUpdateMode.MODEL_CHANGED

    grid_response = AgGrid(
                        df, 
                        gridOptions=gridOptions,
                        fit_columns_on_grid_load = True,
                        data_return_mode=return_mode_value,
                        update_mode=update_mode_value,
                        enable_enterprise_modules=enable_enterprise_modules,
                        theme='streamlit'
                            )  
    selected = grid_response['selected_rows']

    if len(selected) > 0:
        name1 = selected[0]['源仓库文件']
        name2 = selected[0]['目标仓库文件']
        res = Result()
        res.get_result_msg("./result/exp_data/output" + str(name_to_id[name1[:-5]]) + "_" + str(name_to_id[name2[:-5]]))
        res.parse_result_msg()
        res.save_result()
        
        path = "./data/input/"
        code1 = ""
        code2 = ""

        begin = 0
        end = 0
        cur = 0

        while selected[0]['克隆代码行索引1'][cur] != ',':
            cur += 1
        begin = int(selected[0]['克隆代码行索引1'][1: cur])
        end = int(selected[0]['克隆代码行索引1'][cur + 1: len(selected[0]['克隆代码行索引1']) - 1])

        fp = open(path + str(name_to_id[name1[:-5]]) + ".java")
        count = 0
        while True:
            count += 1
            line = fp.readline()
            if not line:
                break
            if count >=  begin and count <= end:
                code1 += line
        
        begin = 0
        end = 0
        cur = 0

        while selected[0]['克隆代码行索引2'][cur] != ',':
            cur += 1
        begin = int(selected[0]['克隆代码行索引2'][1: cur])
        end = int(selected[0]['克隆代码行索引2'][cur + 1: len(selected[0]['克隆代码行索引2'] ) - 1])

        fp = open(path + str(name_to_id[name2[:-5]]) + ".java")
        count = 0
        while True:
            count += 1
            line = fp.readline()
            if not line:
                break
            if count >=  begin and count <= end:
                code2 += line

        a = list(code1).count("\n")
        b = list(code2).count("\n")
        if (a > b):
            code2 += ".\n" * (a - b - 1) + "."
        elif (a < b):
            code1 += ".\n" * (b - a - 1) + "."

        s1, s2 = st.columns(2)
        s1.code(code1, "java", line_numbers=True)
        s2.code(code2, "java", line_numbers=True)

    #for i in range(0, 13):
    #    st.write(" ")
    #with c5:
    #    st_echarts(st.session_state.options[2])

def show_info() -> None:
    #set_bg_hack_url()
    
    #st.image("./image/title1.png", use_column_width=True)
    #st.image("./image/blank.png", width=600)
    st.image("./image/bg_white.gif", use_column_width=True)
    with elements("dashboard"):

    # You can create a draggable and resizable dashboard using
    # any element available in Streamlit Elements.

        # First, build a default layout for every element you want to include in your dashboard

        layout = [
            # Parameters: element_identifier, x_pos, y_pos, width, height, [item properties...]
            dashboard.Item("first_item", 0, 0, 3.5, 2.5,isResizable=False, isDraggable=False, moved=False),
            dashboard.Item("second_item", 4, 0, 3.5, 2.5,isResizable=False, isDraggable=False, moved=False),
            dashboard.Item("third_item", 8, 0, 3.5, 2.5, isResizable=False, isDraggable=False, moved=False),
        ]

        # Next, create a dashboard layout using the 'with' syntax. It takes the layout
        # as first parameter, plus additional properties you can find in the GitHub links below.

        with dashboard.Grid(layout):
            with mui.Paper( key = "first_item",elevation=6):
                with mui.Typography(padding=3):
                    # 添加空行
                    
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/8slsfiw.png"), html.font(" 代码克隆类型", color= "purple"), align="center", color='warning')
                    html.div("主要有四种类型的代码克隆，分别为文本克隆、词法克隆、语法克隆以及语义克隆。",css={"text-indent":"2em"})
                    for i in range(7):
                        html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")
            with mui.Paper(key = "second_item",elevation=6):
                with mui.Typography(padding=3):
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/vnseiw.png"),html.font(" 代码克隆的表现形式", color= "purple"),align="center")
                    html.div("由于代码克隆的定义是面向程序代码片段，即一段连续的代码。所以按照不同的代码粒度，代码克隆的表现形式呈现差异化，一般可以分为以下四类：",css={"text-indent":"2em"})
                    html.div("1.文件克隆：一对相似的程序代码文件。",css={"text-indent":"2em"})
                    html.div("2.类克隆：在面向对象的代码中，一对相似的类定义代码。",css={"text-indent":"2em"})
                    html.div("3.函数克隆：一对相似的函数代码。",css={"text-indent":"2em"})
                    html.div("4.块克隆：一对相似的代码块。",css={"text-indent":"2em"})
                    #html.p1("克隆代码可以各种形式存在，主要有文件克隆、类克隆、函数克隆以及代码块克隆。")
                    html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")
            with mui.Paper(key = "third_item",elevation=6):
                with mui.Typography(padding=3):
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/vnsm8v.png"),html.font(" 抽象语法树", color= "purple"),align="center")
                    html.div("抽象语法树可以看作是源代码的一种抽象表示，它去除了源代码中的细节和冗余信息，只保留了语法结构的关键部分。每个节点代表源代码中的一个语法结构元素，如表达式、变量、函数等，而节点之间的关系则表示了它们的层次和相互关联。",css={"text-indent":"2em"})
                    #html.p1("源代码语法结构的一种抽象表示，树上的每个结点代表源代码中的一个token特征。")
                    for i in range(4):
                        html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")
        with dashboard.Grid(layout):
            with mui.Paper(key="first_item", elevation=6):
                with mui.Typography(padding=3):
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/vnsx1u.png"),html.font(" N-grams特征提取", color = "purple"),align="center")
                    html.div("通过该方法可以获取源代码相邻token的信息，便于查找具有相似特征的代码文件。",css={"text-indent":"2em"})
                    #html.p1("通过该方法可以获取源代码相邻token的信息，便于查找具有相似特征的代码文件。")
                    for i in range(7):
                        html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")
            with mui.Paper(key="second_item", elevation=6):
                with mui.Typography(padding=3):
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/vnt58o.png"),html.font(" 代码相似度计算", color = "purple"),align="center")
                    html.div("可以计算两个代码文件之间的相似程度。",css={"text-indent":"2em"})
                    #html.p1("可以计算两个代码文件之间的相似程度。",size='large')
                    for i in range(8):
                        html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")
            with mui.Paper(key="third_item", elevation=6):
                with mui.Typography(padding=3):
                    html.br()
                    html.h1(html.img(src="https://great.wzznft.com/i/2023/06/12/vntfx9.png"),html.font(" CWE漏洞模板库", color = "purple"),align="center")
                    html.div("搜集了常见CWE漏洞模板代码，构建起了CWE漏洞模板库，可以用于漏洞检测。",css={"text-indent":"2em"})
                    #html.p1("搜集了常见CWE漏洞模板代码，构建起了CWE漏洞模板库，可以用于漏洞检测。")
                    for i in range(7):
                        html.br()
                    with mui.Button(align="bottom",color="inherit", size="small",variant="string"):
                        mui.icon.DoubleArrow()
                        mui.Typography("Read More")


def show_intro() -> None:
    st.image("./image/advantages.gif", use_column_width=True)

    
def show_single() -> None:
    #set_bg_hack_url()
    #m = st.markdown("""
    #<style>
    #div.stButton > button:first-child {
    #    background-color: #e0e0ef;color:black;font-size:20px;height:2em;width:5em;border-radius:10px 10px 10px 10px;
    #}
    #</style>""", unsafe_allow_html=True)
    st.image("./image/title_light.png", use_column_width=True)
    st.markdown(
    """
    <style>
    [data-baseweb="select"] {
        margin-top: -35px;
    }
    </style>
    """,
    unsafe_allow_html=True,
    )

    c1, c2, c3 = st.columns([0.45, 0.45, 0.1])
    # Spawn a new Ace editor
    with c1:
        st.session_state.src_file = st_ace(language="java", theme="github", height=400, keybinding="vscode", key=1, font_size=10)
    with c2:
        st.session_state.dst_file = st_ace(language="java", theme="github", height=400, keybinding="vscode", key=2, font_size=10)
    with c3:
        for i in range(0, 10):
            st.write(" ")
        select_code = st.selectbox("",options=choice_code)
        st.session_state.file_type = file_type[choice_code.index(select_code)]
        st.button("检测", on_click=callback1)


def show_multi() -> None:
   # set_bg_hack_url()
    m = st.markdown("""
    <style>
    div.stButton > button:first-child {
        background-color: #f44336; /* Green */
        border: none;
        color: white;
        padding: 20px 40px;
        text-align: center;
        text-decoration: none;
        display: inline-block;
        font-size: 16px;
    }
    </style>""", unsafe_allow_html=True)
    st.image("./image/title_light.png", use_column_width=True)
   # c1, c2, c3, c4, c5 = st.columns([0.37,0.03, 0.1,0.4,0.1])
   # c1.text_input("请输入源文件路径")
   # c4.text_input("请输入待检测文件路径")
   # c3.button("检测", on_click=callback1)
   # c5.selectbox("请选择代码语言",options=choice_code)
    c1, c2 = st.columns(2)

    with c1:
        st.markdown("""
            <video width="400" autoplay="true" muted="true" loop="true" align="center">
            <source 
                    src="https://www.jfrogchina.com/wp-content/uploads/2020/02/Realtime-Vul-R1-Animation-400X400.mp4" 
                    type="video/mp4" />
            </video>
            """, unsafe_allow_html=True)
    c2.text("")
    c2.text("")
    c2.text("")
    c2.text("")
    if st.session_state.muti_mode == 1:
        st.session_state.src_url = c2.text_input("请输入文件路径")
    if st.session_state.muti_mode == 0:
        st.session_state.src_url = c2.text_input("请输入源文件路径")
    c3, c5, c4 = c2.columns(3)
    c3.button("检测", on_click=callback1)
    c5.button("切换", on_click=callback2)
    select_code = c4.selectbox("请选择代码语言",options=choice_code)
    st.session_state.file_type = file_type[choice_code.index(select_code)]
    if st.session_state.muti_mode == 0:
        st.session_state.dst_url = c2.text_input("请输入待检测文件路径")

def show_result3() -> None:
    # TODO 需要根据规则文件的行数来寻找具体的cwe为多少，然后从cwe_db内读取相应的参数
    c1, c2 = st.columns([0.5,0.5])
    with c1:
    #    st.image("./image/bug_small.gif")
        st.text("这里放饼状图！")
        
    with c2:
        st.text("这里放柱状图！")
    cwe_cal = [
        ['2.java', '(7, 10)', 'cwe-259', '64%', 'low'],
        ['3.java', '(12, 21)', 'cwe-78', '92%', 'high'],
        ['4.java', '(9, 14)', 'cwe-117', '86%', 'middle']
    ]
    df = pd.DataFrame(cwe_cal)
    df.columns = ['文件名', '漏洞位置', 'cwe类型', '可信度', '危险程度']
    select = -1
    #with c2:
    select = aggrid_cwe(df)
    if select != -1:
        select_file = select[0]
        ret1, ret2, ret3 = res_visual("./result/exp_data/res" + select_file)
        # TODO 如果一个代码中有多个漏洞，表格选择的返回值需要有两个，一个是文件的索引，一个是行数的索引
        # 行数的索引需要转换成idx值，可以做一个map表进行映射     
        #print(ret2)
        #c1, c2 = st.columns(2)
        cwe_index = -1
        if int(select_file) == 3:
            cwe_index = 1
        
        st.code(ret2[0], "java")
        if cwe_index != -1:
            with st.expander("CWE描述"):
                st.write(cwe_list[cwe_index]['decript-cn'])
            with st.expander("安全隐患"):
                st.write(cwe_list[cwe_index]['attacker'])
            with st.expander('解决方案'):
                st.write(cwe_list[cwe_index]['solution'])
            st.markdown(f"""
                - [更多关于CWE-78](https://cwe.mitre.org/data/definitions/78.html)
            """)

def show_exp() -> None:
    #set_bg_hack_url()
    st.image("./image/title_light.png", use_column_width=True)
    #st.image("./image/exp_flow.png")
    st.text("")
    st.text("")
    c1, c2, c3 = st.columns([0.7,0.2,0.1])
    c1.text_input("请输入源文件路径")
    select_code = c2.selectbox("请选择代码语言",options=choice_code)
    st.session_state.file_type = file_type[choice_code.index(select_code)]
    c3.text("")
    c3.text("")
    c3.button("检测", on_click=callback1)
    st.text("")
    st.text("")
    c1, c2 = st.columns([0.5,0.5])
    with c1:
        st.image("./image/exp.gif")
    
    with c2:
        # st.subheader("请选择漏洞来源：")
        st.text("")
        st.selectbox("请选择漏洞来源：", options=["CWE","CNVD"])
        st.text("")
        show_cwe_list(cwe_list)

def show_config() -> None:
    c1, c2 = st.columns([0.5,0.5])
    with c1:
        config = [
        ["二元表达式(BinaryExpression)"],["一元表达式(UnaryExpression)"],["函数调用表达式(FunctionCall)"],["条件语句(IfStatement)"],
        ["循环语句(WhileStatement)"],["循环语句(ForStatement)"],["开关语句(SwitchStatement)"],
        ["赋值语句(AssignmentStatement)"],["变量声明语句(VariableDeclaration)"],["FunctionDeclaration(函数声明语句)"],
        ["类声明语句(ClassDeclaration)"],["跳出语句(BreakStatement)"],["继续语句(ContinueStatement)"],
        ["返回语句(ReturnStatement)"],["字面量(Literal)"],["标识符(Identifier)"]

    ]
        df = pd.DataFrame(config)
        df.columns = ['检测节点类型']
        gb = GridOptionsBuilder.from_dataframe(df)
        selection_mode = 'multiple'
        enable_enterprise_modules = True # 设置企业化模型，可以筛选等
        #gb.configure_default_column(editable=True) #定义允许编辑
        
        return_mode_value = DataReturnMode.FILTERED  #__members__[return_mode]
        gb.configure_selection(selection_mode, use_checkbox=True) # 定义use_checkbox
        
        gb.configure_side_bar()
        gb.configure_grid_options(domLayout='normal')
        gb.configure_pagination(paginationAutoPageSize=False, paginationPageSize=10)
        #gb.configure_default_column(editable=True, groupable=True)
        gridOptions = gb.build()
        
        update_mode_value = GridUpdateMode.MODEL_CHANGED
        
        grid_response = AgGrid(
                            df, 
                            gridOptions=gridOptions,
                            fit_columns_on_grid_load = True,
                            data_return_mode=return_mode_value,
                            update_mode=update_mode_value,
                            enable_enterprise_modules=enable_enterprise_modules,
                            theme='streamlit'
                            )
    with c2:
        st.selectbox("检测所用线程数：", options=["1","2","3","4","5","6","7","8(max)"])
        st.text("")
        st.selectbox("主题颜色：", options=["light","dark","blue","grey","custom"])
        st.text("")
        c3, c4 = st.columns(2)
        c3.selectbox("报告内容：", options=["单件检测","批量检测","漏洞检测"])
        c4.selectbox("报告格式：", options=["pdf","txt","jpg","png","markdown"])
        c3, c4 = st.columns([0.8,0.2])
        c4.button("克隆报告导出")


def main() -> None:
    init()
    st.sidebar.image("./image/logo.png")
    with st.sidebar:
        selected = option_menu("菜单", ["主页", '产品介绍', '单件检测', '批量检测', '漏洞检测', '系统全局配置'],
                            icons=['house', 'bar-chart', 'file-earmark-check', 'file-earmark-code', 'exclamation-circle', 'gear'], menu_icon="cast", default_index=0)
    fp = open("./tmp/sem", "w")
    if(selected == "主页"):
        fp.write("1")
    elif(selected == "产品介绍"):
        fp.write("2")
    elif(selected == "单件检测"):
        fp.write("3")
    elif(selected == "批量检测"):
        fp.write("4")
    elif(selected == "漏洞检测"):
        fp.write("5")
    elif(selected == "系统全局配置"):
        fp.write("6")
    fp.close()
    fp = open("./tmp/sem", "r")
    sem_show = int(fp.read())
    fp.close()
    if sem_show == 1:
        show_info()
    elif sem_show == 2:
        show_intro()
    elif sem_show == 3:
        if (st.session_state.show_res == 1):
            show_result("1")
        else:
            st.session_state.mode = 1
            show_single()
    elif sem_show == 4:
        st.session_state.show_res = 1
        st.session_state.muti_mode = 0
        if (st.session_state.show_res == 1 and st.session_state.muti_mode == 0):
            show_result2_2()

        elif (st.session_state.show_res == 1):
            show_result2()
        else:
            st.session_state.mode = 2
            show_multi()
    elif sem_show == 5:
        if (st.session_state.show_res == 1):
            show_result3()
        #show_cwe_list(cwe_list)
        else:
            st.session_state.mode = 3
           # st.button("检测", on_click=callback1)
            show_exp()
    elif sem_show == 6:
        show_config()
    
    #c1, c2 = st.sidebar.columns(2)
    #c1.button("检测", on_click=callback1)
    #c2.button("首页", on_click=callback2)
    
    #st.sidebar.subheader("Tamer：代码克隆检测")



if __name__ == "__main__":
    st.set_page_config(
        "Tamer：代码克隆检测系统",
        "📊",
        initial_sidebar_state="expanded",
        layout="wide",
    )
    main()
