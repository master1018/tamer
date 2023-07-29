from streamlit_elements import elements, mui
from streamlit_elements import html as html_ele
with elements("nested_children"):

    # You can nest children using multiple 'with' statements.
    #
    # <Paper>
    #   <Typography>
    #     <p>Hello world</p>
    #     <p>Goodbye world</p>
    #   </Typography>
    # </Paper>

        with mui.Paper(elevation=6):
                with mui.Typography:
                        html_ele.img(src="https://www.snort.org/assets/Snort_fulllogo.png")