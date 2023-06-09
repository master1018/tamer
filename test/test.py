import numpy as np
import pandas as pd
from st_aggrid import AgGrid, DataReturnMode, GridUpdateMode, GridOptionsBuilder

def highlight_df(val):
    color = ""
    if val == "low":
        color += 'green'
    elif val == "medium":
        color += "yellow"
    else:
        color += "red"
    return 'color: %s' % color 

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
    
    return selected  


test =  [
            ["1", "(12, 13)", "(11, 2)", "low"],
            ["2", "(12, 13)", "(11, 2)", "low"],
             ["1", "(12, 13)", "(11, 2)", "high"],
            ["3", "(12, 13)", "(11, 2)", "medium"],
             ["4", "(12, 13)", "(11, 2)", "50%"],
            ["5", "(12, 13)", "(11, 2)", "50%"],
             ["6", "(12, 13)", "(11, 2)", "50%"],
            ["7", "(12, 13)", "(11, 2)", "50%"],
            ["1", "(12, 13)", "(11, 2)", "50%"],
            ["2", "(12, 13)", "(11, 2)", "50%"],
             ["1", "(12, 13)", "(11, 2)", "50%"],
            ["3", "(12, 13)", "(11, 2)", "50%"],
             ["4", "(12, 13)", "(11, 2)", "50%"],
            ["5", "(12, 13)", "(11, 2)", "50%"],
             ["6", "(12, 13)", "(11, 2)", "50%"],
            ["7", "(12, 13)", "(11, 2)", "50%"]
        ]

a = np.array(test)
print(a)

df = pd.DataFrame(a)
df.columns = ["index", "src", "dst", "similar"]
df.style.applymap(highlight_df)
#print(df)
a = aggrid(df)

