from pycparser import parse_file, c_generator
from pycparser.plyparser import ParseError
from pycparser import c_parser, c_generator
from pycparser.c_ast import * # ast节点类（所有节点类型都继承该文件中的Node类）
import os

def getAttribute(attr):
    print(attr)
    return ''

def translate_to_code(ast_node):
    generator = c_generator.CGenerator()
    print(generator.visit(ast_node))

def getAst(node):
    nodeName = node.__class__.__name__
    nodeChildren = node.children()
    nodeAttributes = node.attr_names
    print("=============")
    translate_to_code(node)
    nodeAttr = [nodeName]
    for _, n in nodeChildren:
        nodeAttr.extend(getAst(n))

    for attr in nodeAttributes:
        attribute = getattr(node, attr) # 先获取属性
        nodeAttr.extend(getAttribute(attribute)) # 自定义当前节点属性str样式

    return nodeAttr

filePath = './test.c'

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
    print(getAst(ast))
except ParseError as e:
    print('代码有错：'+str(e))
except Exception as r:
    print('错误：'+str(r))
