import streamlit as st
from streamlit_echarts import st_echarts
st.set_page_config(layout="wide")
option = {
        "legend": {},
        "tooltip": {
            "trigger": 'axis',
            "showContent": "false"
        },
        "dataset": {
            "source": [
                ['销量', '2015'],
                ['春天', 56.5],
                ['夏天', 51.1],
                ['秋天', 40.1],
                ['冬天', 25.2]
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
                    "formatter": '{b}: {@2015} ({d}%)'
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

st_echarts(options=option)
