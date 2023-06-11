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
from cwe_db import cwe_list
from streamlit_option_menu import option_menu
from streamlit_ace import st_ace

choice_code     =   ["Java","C", "C++",  "Python"]
file_type       =   [".java",".c", ".cpp",  ".py"]

color_arr       =   ["blue", "green", "black", "red", "yellow"]

labels1         =   ["no clone", "low clone", "medium clone ", "high clone"]
labels2         =   ["low", "medium", "high"]

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

        st.session_state.res_list = []
        for i in range(0, len(self.line_msg)):
            st.session_state.res_list.append([i + 1, "({0}, {1})".format(self.line_msg[i][0], self.line_msg[i][1]), "({0}, {1})".format(self.line_msg[i][2], self.line_msg[i][3]), "{0}%".format(self.similar_arr[i]), ""])

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
        
        print(stat[0])
        print(len(self.similar_arr))
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
    ret1 = para1.copy()
    ret2 = para2.copy()
    ret3 = para3.copy()
    build_graph(para1, para2, para3)
    return ret1, ret2, ret3


def init() -> None:
    st.session_state.file_type  =   ""
    st.session_state.src_file   =   None
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


def show_result() -> None:
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
    dst_lines = readline_count("./data/input/1.java" + st.session_state.file_type)
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
    os.system("rm -f ./tmp/type")
    mode_write = str(st.session_state.mode)
    os.system("echo " + mode_write + " > ./tmp/type")
    command = "java -jar " + exec_jar_pos
    os.system(command)
    res = Result()
    res.get_result_msg("./result/output")
    res.parse_result_msg()
    res.save_result()

def callback1() -> None:
    # 单件检测
    if st.session_state.mode == 1:
        if st.session_state.src_file != None:
            fp = open("./data/input/2" + st.session_state.file_type, "w")
            fp.write(st.session_state.src_file.getvalue().decode("utf-8"))
            fp.close()
            st.session_state.src_file = None

        if st.session_state.dst_file != None:
            fp = open("./data/input/1" + st.session_state.file_type, "w")
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
        if st.session_state.tmp != None:
            st.session_state.tmp.empty()
    elif st.session_state.mode == 2:
        st.session_state.options = []
        res = Result()
        res.read_data_set()
        fp = open("./tmp/sem", "w")
        fp.write("3")
        fp.close()

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
             background: url("https://great.wzznft.com/i/2023/06/11/r8q7tq.gif");
             background-size: cover
         }}
         </style>
         """,
         unsafe_allow_html=True
     )

def callback2() -> None:
    fp = open("./tmp/sem", "w")
    fp.write("1")
    fp.close()

def get_base64(bin_file):
    with open(bin_file, 'rb') as f:
        data = f.read()
    return base64.b64encode(data).decode()

def show_result2() -> None:
    c1, c2 = st.columns(2)
    with c1:
        st_echarts(st.session_state.options[0])
    with c2:
        st_echarts(st.session_state.options[1])
    c3, c4 = st.columns(2)
    with c3:
        st_echarts(st.session_state.options[2])

def show_info() -> None:
    #set_bg_hack_url()
    
    #st.image("./image/title1.png", use_column_width=True)
    #st.image("./image/blank.png", width=600)
    st.image("./image/bg_white.gif", use_column_width=True)


def show_intro() -> None:
    c1, c2, c3, c4= st.columns([0.2, 0.35, 0.35, 0.1])
    c2.image("./image/advantages1.png")
    #c4.image("./image/blank.png", width = 50)
    c3.image("./image/test_cut.gif")
    #st.image("./image/blank.png", width=150)
    c1, c2, c3, c4, c5= st.columns([0.15, 0.35, 0.05, 0.35, 0.1])
    #c2.image("./image/blank.png", width = 50)
    c2.image("./image/search11.gif")
    c4.image("./image/blank.png", width=20)
    c4.image("./image/app.png")
    
def show_single() -> None:
    #m = st.markdown("""
    #<style>
    #div.stButton > button:first-child {
    #    background-color: #e0e0ef;color:black;font-size:20px;height:2em;width:5em;border-radius:10px 10px 10px 10px;
    #}
    #</style>""", unsafe_allow_html=True)
    st.image("./image/title1.png", use_column_width=True)
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
    c1, c2, c3 = st.columns([0.5, 0.1, 0.4])
    c1.selectbox("",options=choice_code)
    c2.button("检测")
    c3.checkbox("我已阅读并同意用户使用条款和隐私政策")
    #st.set_page_config(layout="wide")
    c1, c2 = st.columns(2)
    # Spawn a new Ace editor
    with c1:
        st_ace(language="java", theme="github", height=500, keybinding="vscode", key=1, font_size=10)
    with c2:
        st_ace(language="java", theme="github", height=500, keybinding="vscode", key=2, font_size=10)

def show_multi() -> None:
    m = st.markdown("""
    <style>
    div.stButton > button:first-child {
        background-color: #e0e0ef;color:black;font-size:20px;height:4em;width:4em;border-radius:100px 100px 100px 100px;
    }
    </style>""", unsafe_allow_html=True)
    st.image("./image/title1.png", use_column_width=True)
    st.image("./image/blank.png", width=100)
    c1, c2, c3 = st.columns([0.47, 0.06, 0.47])
    c1.text_input("请输入源文件路径")
    c3.text_input("请输入待检测文件路径")
    c2.image("./image/blank.png", width=1)
    c2.button("检测")
    st.image("./image/blank.png", width=100)
    #c1, c2, c3, c4 = st.columns([0.1, 0.4, 0.1, 0.4])
    #c1.image("./image/blank.png", width=20)
    #c1.image("./image/ic1.png", use_column_width=True);
    #c2.header("代码克隆检测")
    #c2.subheader("Tamer使用基于AST分解的克隆检测方案，检测准确率高，速度快")
    

    #c1.selectbox("选择语言",options=choice_code)
    #c2.image("./image/blank.png", width=20)
    #c2.button("检测")


def main() -> None:
    init()
    st.sidebar.image("./image/logo.png")
    with st.sidebar:
        selected = option_menu("菜单", ["主页", '产品介绍', '单件检测', '批量检测', '漏洞检测'],
                            icons=['house', 'bar-chart', 'file-earmark-check', 'file-earmark-code', 'exclamation-circle'], menu_icon="cast", default_index=0)
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
    fp.close()
    fp = open("./tmp/sem", "r")
    sem_show = int(fp.read())
    fp.close()
    if sem_show == 1:
        show_info()
    elif sem_show == 2:
        show_intro()
    elif sem_show == 3:
        st.session_state.mode = 1
        show_single()
    elif sem_show == 4:
        st.session_state.mode = 2
        st.session_state.src_file = st.sidebar.text_input("源代码地址")
        st.session_state.dst_file = st.sidebar.text_input("待测代码地址")
        show_multi()
    
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
