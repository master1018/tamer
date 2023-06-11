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
     "solution": "yes",
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
    
show_cwe_list(cwe_list)