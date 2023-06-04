from graphviz import Graph

def build_graph(a, b, c) -> None:
    dot = Graph()
    dot.attr('node', shape='box')
    dot.attr(rankdir='LR')
    for i in range(len(a) - 1, -1, -1):
        a[i] = a[i].replace("\n", "\\l")
        b[i] = b[i].replace("\n", "\\l")

        dot.node('A' + str(i), a[i])
        dot.node('B' + str(i), b[i])
        dot.edge('A' + str(i), "B" + str(i), c[i])

    dot.render("./result/res_graph", format="png", view=False)


def res_visual() -> None:
    fp = open("./result/res", "r")
    res_str_arr = []
    while True:
        line = fp.readline()
        if not line:
            break
        else:
            res_str_arr.append(line)
    fp.close()

    count = 0
    para1 = []
    para2 = []
    para3 = []
    idx = 0

    while count < len(res_str_arr):
        tmp_str = res_str_arr[count]
        if (len(tmp_str) <= 5 and tmp_str[len(tmp_str) - 2] == "%"):
            para3.append(tmp_str[0:len(tmp_str) - 1])
            count += 1
        elif (tmp_str == "src\n"):
            count += 1
            para1.append("")
            while True:
                src_str = res_str_arr[count]
                if (src_str == "dst\n"):
                    break
                else:
                    para1[idx] += src_str
                count += 1
        elif (tmp_str == "dst\n"):
            count += 1
            para2.append("")
            while True:
                if (count == len(res_str_arr)):
                    break
                dst_str = res_str_arr[count]
                if (len(dst_str) <= 5 and dst_str[len(dst_str) - 2] == "%"):
                    break
                else:
                    para2[idx] += dst_str
                count += 1
            idx += 1
        
    build_graph(para1, para2, para3)

res_visual()