from streamlit_elements import elements, mui, html
with elements("nested_children"):
    import streamlit as st
    # You can nest children using multiple 'with' statements.
    #
    # <Paper>
    #   <Typography>
    #     <p>Hello world</p>
    #     <p>Goodbye world</p>
    #   </Typography>
    # </Paper>

    with mui.Paper(elevation=5):
        with mui.Typography:
            html.p("Hello world")
            html.p("Goodbye world")