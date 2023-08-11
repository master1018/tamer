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


cwe_list_new = [
    [
        "CWE-481",
        "Assigning instead of Comparing",
        "Java",
        "https://cwe.mitre.org/data/definitions/481.html"
    ],
    [
       "CWE-595",
       "Comparison of Object References Instead of Object Contents" ,
       "Java",
       "https://cwe.mitre.org/data/definitions/595.html"
    ],
    [
        "CWE-396",
        "Declaration of Catch for Generic Exception",
        "Java",
        "https://cwe.mitre.org/data/definitions/396.html"
    ],
    [
        "CWE-609",
        "Double-Checked Locking",
        "Java",
        "https://cwe.mitre.org/data/definitions/609.html"
    ],
    [
        "CWE-478",
        "Missing Default Case in Multiple Condition Expression",
        "Java",
        "https://cwe.mitre.org/data/definitions/478.html"
    ],
    [
        "CWE-484",
        "Omitted Break Statement in Switch**",
        "Java",
        "https://cwe.mitre.org/data/definitions/484.html"
    ],
    [
        "CWE-395",
        "Use of NullPointerException Catch to Detect NULL Pointer Dereference",
        "Java",
        "https://cwe.mitre.org/data/definitions/395.html"
    ],
    [
        "cwe-798",
        "Use of Hard-coded Credentials",
        "Java",
        "https://cwe.mitre.org/data/definitions/798.html"
    ],
    [
        "cwe-78",
        "Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')",
        "Java",
        "https://cwe.mitre.org/data/definitions/78.html"
    ]
]

cwe_des = [
        [
        "CWE-481是可变比较和赋值（Mutable Comparison and Assignment）的编号，是一种常见的编程错误或安全漏洞。它指的是在编程中混淆比较操作和赋值操作的情况，通常是由于使用单个等号（=）而不是双等号（==）来执行比较操作，从而导致错误的结果。",
        "使用恰当的比较操作符：确保在比较操作中使用正确的比较操作符，例如双等号（==）或三等号（===）来进行相等性比较。避免使用赋值操作符（=）进行比较。"
    ],
    [
       "CWE-595（比较对象引用而不是对象内容）是指在编程中，错误地比较了对象的引用（内存地址）而不是对象的内容。这种错误可能导致程序逻辑错误、数据一致性问题或安全漏洞。" ,
       "覆盖equals()方法（适用于面向对象语言）：对于面向对象的语言，可以在对象的类中覆盖equals()方法来实现对象内容的比较。在equals()方法中，比较对象的属性或值，并返回相应的比较结果。"
    ],
    [
        "CWE-396（Declaration of Catch for Generic Exception）是指在异常处理中捕获了通用异常（Generic Exception），而不是具体的异常类型。这种问题可能导致不充分的异常处理，使得代码难以调试、维护和理解，并可能隐藏真正的异常情况。",
        "1、捕获具体的异常类型：在异常处理代码中，应该捕获具体的异常类型，而不是通用异常。根据不同的异常情况，执行相应的处理逻辑，以确保针对不同的异常类型采取适当的处理措施。\n2、分析异常处理逻辑：仔细分析异常处理逻辑，并确保对所有可能的异常情况进行了正确的处理。根据异常类型的不同，选择适当的处理方式，如日志记录、回滚事务或通知用户等。\n3、异常处理的层次结构：在设计异常处理代码时，可以考虑使用异常处理的层次结构，以便捕获和处理不同的异常类型。这样可以提高代码的可读性和可维护性，并确保每个异常情况都得到适当的处理。"
    ],
    [
        "CWE-609（Double-Checked Locking）是指在多线程环境下使用了双重检查锁定机制，但没有正确处理同步和可见性问题，从而导致并发错误和线程安全问题。",
        "1、避免使用双重检查锁定：由于双重检查锁定机制复杂且容易出错，建议避免使用它，尤其是在没有足够了解多线程编程和底层平台行为的情况下。\n2、使用线程安全的初始化方法：使用线程安全的初始化方法，如使用静态初始化器、懒汉模式或使用并发容器来延迟初始化对象。这些方法能够避免双重检查锁定机制带来的并发问题。\n3、使用同步机制：如果必须使用双重检查锁定，确保在同步块中正确地处理同步和可见性问题。可以使用内置的锁（synchronized）或显式的锁（Lock）来保证线程安全性。"
    ],
    [
        "CWE-478（Missing Default Case in Multiple Condition Expression）是一个与程序开发中的开关语句（如switch语句）相关的常见软件安全弱点。它指的是在多条件表达式中缺少默认情况（即default或else语句），导致可能存在未处理的输入情况。",
        "在开关语句的最后添加一个default情况，用于处理未匹配到任何其他条件的情况。在该情况下，可以执行适当的错误处理、日志记录或其他必要的操作。"
    ],
    ["CWE-484：Omitted Break Statement in Switch是指在switch语句中，某个case分支内部没有包含break语句，导致在该分支执行完毕后，程序继续执行下一个case分支的代码，而不是跳出switch语句。","要解决CWE-484漏洞，需要在每个case分支内部添加break语句，以确保在执行完当前分支后跳出switch语句。"]
,
["CWE-395：Use of NullPointerException Catch to Detect NULL Pointer Dereference涉及到在捕获NullPointerException异常时，不应将其用作防止空指针解引用的编程检查的替代方法。这是因为通过捕获NullPointerException来解决空指针解引用问题通常并不是一个良好的做法。在某些情况下，程序员可能会捕获NullPointerException异常，但这不应作为防止空指针解引用的主要方法。常见的情况包括：程序包含了空指针解引用问题，但捕获异常似乎比修复潜在的问题更容易；程序故意抛出NullPointerException来表示错误条件；代码是测试工具的一部分，该测试工具向被测试类提供意外输入。在这三种情况中，只有最后一种情况是可以接受的，因为它涉及到测试工具的使用，用于模拟异常情况。","避免在代码中捕获NullPointerException异常作为防止空指针解引用的主要方法。相反，应该在代码中实施良好的编程实践来防止空指针解引用问题。以下是一些解决方案：\n程序化检查： 在执行可能导致空指针解引用的操作之前，始终进行程序化检查，确保相关引用不为null。可以使用条件语句（例如if语句）来检查引用是否为null，然后采取相应的操作。\n使用Optional类（如果适用）： 在一些编程语言中，如Java，可以使用Optional类来处理可能为空的引用，以避免空指针异常。\n空值安全编码： 使用现代编程语言和框架中提供的空值安全功能，例如Kotlin的安全调用运算符（?.）、C#的null条件运算符（?.）等。"]
,
["CWE-798：Use of Hard-coded Credentials是一种硬编码凭证的使用漏洞。这种漏洞发生在开发人员将凭证（如用户名、密码、密钥等）以明文形式硬编码到应用程序或系统代码中。攻击者可以通过分析应用程序的二进制文件、源代码或配置文件，轻松地获得这些凭证。一旦攻击者获得了这些凭证，他们可能会用于未经授权的访问、数据泄露、恶意操作等。","使用配置文件： 将敏感凭证存储在配置文件中，而不是直接存储在代码中。配置文件应该具有适当的访问权限，以防止未经授权的访问。\n使用加密： 如果必须在代码中存储凭证，至少应将其加密存储。使用加密算法将凭证保护起来，以降低攻击者获取明文凭证的难度。\n使用密钥管理工具： 考虑使用密钥管理工具来安全地存储和管理凭证。这些工具可以提供加密、访问控制和审计功能。\n动态生成凭证： 如果可能，考虑动态生成凭证，以便每次需要时都可以生成唯一的凭证。这可以减少恶意访问的风险。\n权限分离： 不同的凭证应该有不同的权限，确保每个凭证只能访问其需要的资源。\n定期更换凭证： 不定期地更换凭证，即使凭证泄露，攻击者也只能在有限的时间内访问资源。\n安全编码实践： 开发人员应遵循安全编码实践，避免在代码中硬编码凭证，以及避免将敏感信息直接暴露。\n代码审查： 定期进行代码审查，特别关注是否存在硬编码凭证的情况，以及是否有更安全的方法来管理凭证。"]
,
["CWE-78：Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')是一种操作系统命令注入漏洞。当应用程序在构建操作系统命令时未正确对用户提供的输入进行验证、过滤或转义时，攻击者可以通过在输入中插入特殊字符或命令来执行恶意操作。这可能会导致攻击者执行任意系统命令，危害系统的完整性和机密性。","输入验证与过滤：应用程序应该对所有从用户获取的输入进行验证，确保输入的数据符合预期的格式和范围。过滤不必要的特殊字符，并拒绝或纠正无效输入。\n参数化查询：对于需要构建操作系统命令的情况，应该使用参数化查询或预编译语句，而不是将用户输入直接插入命令中。这可以防止恶意输入被误解为命令。\n输入转义：在将用户输入插入命令之前，对输入数据进行适当的转义，以确保特殊字符不会被解释为命令的一部分。\n最小权限原则：应用程序应以最小的权限执行操作系统命令，以减少攻击者能够滥用的可能性。"]
]

def get_df(cwe_list):
    cwe_tmp= []
    for i in range(0, len(cwe_list)):
        cwe_tmp.append([cwe_list[i]['name'], cwe_list[i]['description']])
    df = pd.DataFrame(np.array(cwe_tmp))
    df.columns = ['name', 'description']
    return df

def show_cwe_list_md(cwe):
    markdown_table = '''
                        | {0[0]}  | {1[0]}   |
    | ---- | ---- |
    | {0[1]}   | {1[1]}  |
    | {0[2]}    | {1[2]}  |
    | {0[3]}   | {1[3]}  |
                    '''.format(["ID", "Description", "Language", "Url"], cwe)
    st.markdown(markdown_table)

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
        return selected[0]['序列']  
    