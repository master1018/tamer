from pycparser import parse_file, c_generator
from pycparser.plyparser import ParseError
from pycparser import c_parser, c_generator
from pycparser.c_ast import * # ast节点类（所有节点类型都继承该文件中的Node类）
import os

def getAttribute(attr):
   #print(attr)
    return ''

def translate_to_code(ast_node, fp2):
    generator = c_generator.CGenerator()
    print("[START]", file=fp2)
    print(generator.visit(ast_node), file=fp2)
    print("[END]", file=fp2)

def getAst(depth, node, fp1, fp2):
    nodeName = node.__class__.__name__
    nodeChildren = node.children()
    nodeAttributes = node.attr_names
    #print("=============")
    translate_to_code(node, fp2)
    nodeAttr = [nodeName]
    have_child = 0
    print(" " * depth * 2 + nodeName, file=fp1)
    for _, n in nodeChildren:
        have_child = 1
        nodeAttr.extend(getAst(depth + 1, n, fp1, fp2))

    for attr in nodeAttributes:
        attribute = getattr(node, attr) # 先获取属性
        nodeAttr.extend(getAttribute(attribute)) # 自定义当前节点属性str样式

    return nodeAttr

def c_parser_t(filePath):
    fp1 = open("./ast_tree_c", "w")
    fp2 = open("./ast_to_code_c", "w")
    try:
        ast = None
        # ast = parse_file(filePath)
        with open(filePath) as f:
            txtList = f.readlines()
            txt = ''
            for each in txtList:
                if each.find('#include') != -1 :
                    continue
                elif each.find('//') != -1:
                    txt += each[:each.find('//')]
                else :
                    txt += each
                txt += '\n'
        ast = c_parser.CParser().parse(txt)
        getAst(0, ast, fp1, fp2)
    except ParseError as e:
        print('代码有错：'+str(e))
    except Exception as r:
        print('错误：'+str(r))
    fp1.close()
    fp2.close()

c_parser_t("./test.c")