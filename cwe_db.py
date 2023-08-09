from st_aggrid import AgGrid, DataReturnMode, GridUpdateMode, GridOptionsBuilder
import pandas as pd
import streamlit as st
import numpy as np

cwe_list = [
    {"name": "cwe-259",
     "level": "low",
     "description": "Use of Hard-coded Password",
     "solution": "yes",
    },

    {"name": "cwe-78",
     "level": "low",
     "description": "Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')",
     "solution": "为了防止CWE-78漏洞，应用程序应该对来自外部的输入数据进行严格的验证和过滤。最佳实践包括使用白名单验证输入数据，对特殊字符进行转义或删除，以及实施安全的输入验证和输出编码措施，以防止恶意输入的注入和执行。",
     "decript-cn": "CWE-78是一种常见的安全漏洞，也称为OS命令注入或命令注入。\n应用程序在构造操作系统命令时，使用了来自外部可控的输入数据，但没有正确地对输入数据进行过滤或验证。这使得攻击者能够通过在输入数据中插入恶意命令或特殊字符，将意外的命令注入到操作系统中。",
     "attacker": "攻击者利用CWE-78漏洞可以执行未经授权的操作系统命令，例如执行系统命令、读取敏感文件、修改数据或执行其他恶意活动。这种漏洞通常发生在网络应用程序中，特别是在与用户输入交互的功能中，如表单提交、搜索功能或命令行接口。"
    },

    {"name": "cwe-191",
     "level": "low",
     "description": "Integer Underflow (Wrap or Wraparound)",
     "solution": "yes",
    },

    {"name": "cwe-41",
     "level": "low",
     "description": "Improper Resolution of Path Equivalence",
     "solution": "yes",
    },

    {"name": "cwe-253",
     "level": "low",
     "description": "Incorrect Check of Function Return Value",
     "solution": "yes",
    },

    {"name": "cwe-460",
     "level": "low",
     "description": "Improper Cleanup on Thrown Exception",
     "solution": "yes",
    },

    {"name": "cwe-117",
     "level": "low",
     "description": "Improper Output Neutralization for Logs",
     "solution": "yes",
    },
    {"name": "cwe-259",
     "level": "low",
     "description": "Use of Hard-coded Password",
     "solution": "yes",
    },
    {"name": "cwe-259",
     "level": "low",
     "description": "Use of Hard-coded Password",
     "solution": "yes",
    },
    {"name": "cwe-259",
     "level": "low",
     "description": "Use of Hard-coded Password",
     "solution": "yes",
    }
]

def get_df(cwe_list):
    cwe_tmp= []
    for i in range(0, len(cwe_list)):
        cwe_tmp.append([cwe_list[i]['name'], cwe_list[i]['description']])
    df = pd.DataFrame(np.array(cwe_tmp))
    df.columns = ['name', 'description']
    return df

def show_cwe_list(cwe_list):
    df = get_df(cwe_list)
    gb = GridOptionsBuilder.from_dataframe(df)
    enable_enterprise_modules = True 
    #gb.configure_default_column(editable=True) #定义允许编辑
    #return_mode_value = DataReturnMode.FILTERED  #__members__[return_mode]
    #gb.configure_selection(use_checkbox=True) # 定义use_checkbox
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
                        update_mode=update_mode_value,
                        enable_enterprise_modules=enable_enterprise_modules,
                        theme='balham'
                        )  
    

def aggrid_cwe(df):
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
        return selected[0]['文件名']  
    