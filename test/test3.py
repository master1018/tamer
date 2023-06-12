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
                        html_ele.h1("CWE-78")
                        html_ele.p("CWE-78是一种常见的安全漏洞，也称为OS命令注入或命令注入。\n应用程序在构造操作系统命令时，使用了来自外部可控的输入数据，但没有正确地对输入数据进行过滤或验证。这使得攻击者能够通过在输入数据中插入恶意命令或特殊字符，将意外的命令注入到操作系统中。")
        with mui.Paper(elevation=6):
                with mui.Typography:
                        html_ele.h1("CWE-78")
                        html_ele.p("CWE-78是一种常见的安全漏洞，也称为OS命令注入或命令注入。\n应用程序在构造操作系统命令时，使用了来自外部可控的输入数据，但没有正确地对输入数据进行过滤或验证。这使得攻击者能够通过在输入数据中插入恶意命令或特殊字符，将意外的命令注入到操作系统中。")
