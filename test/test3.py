import streamlit as st

from streamlit_ace import st_ace
st.set_page_config(layout="wide")
c1, c2 = st.columns(2)
# Spawn a new Ace editor
with c1:
   st_ace(language="java", theme="github", height=500, keybinding="vscode", key=1, font_size=10)
with c2:
   st_ace(language="java", theme="github", height=500, keybinding="vscode", key=2, font_size=10)
