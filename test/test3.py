import numpy as np
import pandas as pd
from st_aggrid import AgGrid, DataReturnMode, GridUpdateMode, GridOptionsBuilder
a = [[1, 2], [3, 4]]


df = pd.DataFrame(np.array(a))
print(df)
gb = GridOptionsBuilder.from_dataframe(df)
enable_enterprise_modules = True # 筛选
gb.configure_side_bar()
gb.configure_grid_options(domLayout='normal')
gb.configure_pagination(paginationAutoPageSize=False, paginationPageSize=10)
        #gb.configure_default_column(editable=True, groupable=True)
gridOptions = gb.build()
update_mode_value = GridUpdateMode.MODEL_CHANGED
        
AgGrid(
                            df, 
                            gridOptions=gridOptions,
                            fit_columns_on_grid_load = True,
                            update_mode=update_mode_value,
                            enable_enterprise_modules=enable_enterprise_modules,
                            theme='streamlit'
                            )  