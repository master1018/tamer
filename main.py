import base64
import time
from graphviz import Graph
from pathlib import Path
import os
import streamlit as st
from matplotlib import pyplot as plt
import numpy as np
import pandas as pd
from st_aggrid import AgGrid, DataReturnMode, GridUpdateMode, GridOptionsBuilder
from streamlit_echarts import st_echarts

choice_code     =   ["Java","C", "C++",  "Python"]
file_type       =   [".java",".c", ".cpp",  ".py"]

color_arr       =   ["blue", "green", "black", "red", "yellow"]

labels1         =   ["no clone", "low clone", "medium clone ", "high clone"]
labels2         =   ["low", "medium", "high"]

exec_jar_pos        = "./sourcecode/out/artifacts/finals_jar/finals.jar"

option_pie = {
        "legend": {},
        "tooltip": {
            "trigger": 'axis',
            "showContent": "false"
        },
        "dataset": {
            "source": [
                ['yhr', '2023'],
                ['无克隆', 1],
                ['轻度克隆', 2],
                ['中度克隆', 3],
                ['高度克隆', 4]
            ]
        },
        "series": [
            {
                "type": 'pie',
                "id": 'pie',
                "radius": ['40%', '75%'],
                #"center": ['50%', '30%'],
                "emphasis": {"focus": 'data',
                            "fontSize": '20',
                            "fontWeight": 'bold'},
                "label": {
                    "formatter": '{b}: {@2023} ({d}%)'
                },
            }
        ],
            "tooltip": {
                    "show": "true",
                },
            "label": {
                "show":"true"
    },
    }

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
    command = "java -jar " + exec_jar_pos
    os.system(command)
    res = Result()
    res.get_result_msg("./result/output")
    res.parse_result_msg()
    res.save_result()

def callback1() -> None:
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

    st.session_state.src_file = st.sidebar.file_uploader("上传源文件", accept_multiple_files=False, type=st.session_state.file_type[1:])
    st.session_state.dst_file = st.sidebar.file_uploader("上传待检测文件", accept_multiple_files=False, type=st.session_state.file_type[1:])
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
