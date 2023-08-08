import ast
import astunparse

def get_subtree_code(node):
    return astunparse.unparse(node)

class CodeVisitor(ast.NodeVisitor):
    depth = 0
    fp1 = None
    fp2 = None

    def get_code(self, node):
        try:
            print("[START]", file=self.fp2)
            print(get_subtree_code(node), file=self.fp2)
            print("[END]", file=self.fp2)
        except Exception:
            print("")

    def generic_visit(self, node):
        print(" " * self.depth * 2 + type(node).__name__, file=self.fp1)
        self.get_code(node)
        self.depth += 1
        ast.NodeVisitor.generic_visit(self, node)
        self.depth -= 1
 
    def visit_FunctionDef(self, node):
        print(type(node).__name__)
        ast.NodeVisitor.generic_visit(self, node)
 
    def visit_Assign(self, node):
        print(type(node).__name__)
        ast.NodeVisitor.generic_visit(self, node)

# Example Python code
def python_parser_t(file_path):
    fp = open(file_path, "r")
    code = fp.read()
    fp.close()
    # Parse the code into AST
    root = ast.parse(code)

    # Get a specific subtree (e.g., the function definition) and print its code
    #function_node = root.body[0]

    code_visit = CodeVisitor()
    code_visit.fp1 = open("./ast_tree_python", "w")
    code_visit.fp2 = open("./ast_to_code_python", "w")
    code_visit.generic_visit(root)
    code_visit.fp1.close()
    code_visit.fp2.close()
