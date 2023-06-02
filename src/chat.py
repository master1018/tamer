from pathlib import Path
from random import randrange

import streamlit as st
from src.styles.menu_styles import FOOTER_STYLES, HEADER_STYLES
from src.utils.conversation import get_user_input, show_chat_buttons, show_conversation
from src.utils.footer import show_donates, show_info
from src.utils.helpers import get_files_in_dir, get_random_img
from src.utils.lang import en, ru
from streamlit_option_menu import option_menu

# --- PATH SETTINGS ---
current_dir: Path = Path(__file__).parent if "__file__" in locals() else Path.cwd()
css_file: Path = current_dir / "src/styles/.css"
assets_dir: Path = current_dir / "assets"
icons_dir: Path = assets_dir / "icons"
img_dir: Path = assets_dir / "img"
tg_svg: Path = icons_dir / "tg.svg"

# --- GENERAL SETTINGS ---
PAGE_TITLE: str = "Code Clone detection"
PAGE_ICON: str = "🤖"
LANG_EN: str = "En"
LANG_RU: str = "Ru"

# 显示上传数据的方式
AI_MODEL_OPTIONS: list[str] = [
    "简易模式",
    "详细模式",
]

st.set_page_config(page_title=PAGE_TITLE, page_icon=PAGE_ICON)

# --- LOAD CSS ---
with open(css_file) as f:
    st.markdown(f"<style>{f.read()}</style>", unsafe_allow_html=True)

selected_lang = option_menu(
    menu_title=None,
    options=[LANG_EN, LANG_RU, ],
    icons=["globe2", "translate"],
    menu_icon="cast",
    default_index=0,
    orientation="horizontal",
    styles=HEADER_STYLES
)

# Storing The Context
if "locale" not in st.session_state:
    st.session_state.locale = en
if "generated" not in st.session_state:
    st.session_state.generated = []
if "past" not in st.session_state:
    st.session_state.past = []
if "messages" not in st.session_state:
    st.session_state.messages = []
if "user_text" not in st.session_state:
    st.session_state.user_text = [None, None]
if "input_kind" not in st.session_state:
    st.session_state.input_kind = st.session_state.locale.input_kind_1
if "seed" not in st.session_state:
    st.session_state.seed = randrange(10**3)  # noqa: S311
if "costs" not in st.session_state:
    st.session_state.costs = []
if "total_tokens" not in st.session_state:
    st.session_state.total_tokens = []
if "code_type" not in st.session_state:
    st.session_state.code_type = ""


def main() -> None:
    c1, c2 = st.columns(2)
    with c1, c2:
        # 显示结果展示的方式，简单还是复杂点
        c1.selectbox(label=st.session_state.locale.select_placeholder1, key="model", options=AI_MODEL_OPTIONS)
        # 数据如何上传，本地上传，还是远程下载过来（测试中）？
        st.session_state.input_kind = c2.radio(
            label=st.session_state.locale.input_kind,
            options=(st.session_state.locale.input_kind_1, st.session_state.locale.input_kind_2),
            horizontal=True
        )

        # 可供选择的语言
        st.session_state.code_type = c1.selectbox(label=st.session_state.locale.select_placeholder2, key="role",
                             options=st.session_state.locale.ai_role_options)
        if st.session_state.code_type == "C":
            st.session_state.code_type = ".c"
        elif st.session_state.code_type == "C++":
            st.session_state.code_type = ".cpp"
        elif st.session_state.code_type == "Java":
            st.session_state.code_type = ".java"
        else:
            st.session_state.code_type = ".py"
    #if st.session_state.user_text:
     #   show_conversation()
     #   st.session_state.user_text = ""
    get_user_input()
    show_chat_buttons()


def run_agi():
    # set language
    match selected_lang:
        case "En":
            st.session_state.locale = en
        case "Ru":
            st.session_state.locale = en
        case _:
            st.session_state.locale = en
    st.markdown(f"<h1 style='text-align: center;'>{st.session_state.locale.title}</h1>", unsafe_allow_html=True)
    selected_footer = option_menu(
        menu_title=None,
        options=[
            st.session_state.locale.footer_option1,
            st.session_state.locale.footer_option0,
            st.session_state.locale.footer_option2,
        ],
        icons=["info-circle", "chat-square-text", "piggy-bank"],  # https://icons.getbootstrap.com/
        menu_icon="cast",
        default_index=0,
        orientation="horizontal",
        styles=FOOTER_STYLES
    )
    # show different footer
    match selected_footer:
        case st.session_state.locale.footer_option0:
            main()
        case st.session_state.locale.footer_option1:
            st.image(f"{img_dir}/{get_random_img(get_files_in_dir(img_dir))}")
            show_info(tg_svg)
        case st.session_state.locale.footer_option2:
            show_donates()
        case _:
            show_info(tg_svg)


if __name__ == "__main__":
    run_agi()
