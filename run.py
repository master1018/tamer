import functools
from pathlib import Path

import streamlit as st
import pandas as pd

choice_code     =   ["C", "C++", "Java", "Python"]
file_type       =   [".c", ".cpp", ".java", ".py"]

def init() -> None:
    st.session_state.file_type  =   ""
    st.session_state.src_file   =   None
    st.session_state.dst_file   =   None
    st.session_state.res_file   =   None


def show_result() -> None:
    fp = open("./tmp/res.c", "r")
    st.session_state.res_file = fp.read()
    with st.expander("检测结果"):
        c1, c2, c3= st.columns(3)
        c1.code(st.session_state.res_file, language="c")
        c2.text("test")
        c3.code(st.session_state.res_file, language="c")

def callback1() -> None:
    if st.session_state.src_file != None:
        fp = open("src" + st.session_state.file_type, "w")
        fp.write(st.session_state.src_file.getvalue().decode("utf-8"))
        fp.close()
        st.session_state.src_file = None

    if st.session_state.dst_file != None:
        fp = open("dst" + st.session_state.file_type, "w")
        fp.write(st.session_state.dst_file.getvalue().decode("utf-8"))
        fp.close()
        st.session_state.dst_file = None

    # 调用检测代码检测出结果，结果以文件方式保存，再重新读入
    # 下面用来测试，假设结果文件为result.c
    fp = open("./tmp/sem", "w")
    fp.write("2")
    fp.close()

def callback2() -> None:
    fp = open("./tmp/sem", "w")
    fp.write("1")
    fp.close()
    
def show_info() -> None:
    st.header("Tamer：代码克隆检测")

    with st.expander("关于我们"):
        st.write(Path("README.md").read_text())

    st.write(Path("README.md").read_text())


def main() -> None:

    fp = open("./tmp/sem", "r")
    sem_show = int(fp.read())
    fp.close()

    init()

    if sem_show == 1:
        show_info()
    elif sem_show == 2:
        show_result()

    st.sidebar.subheader("Tamer：代码克隆检测")

    code_selection = st.sidebar.selectbox(
        "选择代码语言", options=choice_code
    )
    
    st.session_state.file_type = file_type[choice_code.index(code_selection)]

    st.session_state.src_file = st.sidebar.file_uploader("上传源文件", accept_multiple_files=False)
    st.session_state.dst_file = st.sidebar.file_uploader("上传待检测文件", accept_multiple_files=False)
    c1, c2 = st.sidebar.columns(2)
    c1.button("检测", on_click=callback1)
    c2.button("返回", on_click=callback2)

 


if __name__ == "__main__":
    st.set_page_config(
        "Fidelity Account View by Gerard Bentley",
        "📊",
        initial_sidebar_state="expanded",
        layout="wide",
    )
    main()
