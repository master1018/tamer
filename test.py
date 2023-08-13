class Result:
    cmp_file1 = ""
    cmp_file2 = ""
    line_msg = []
    similar_arr = []
    tmp_data = []
    def __init__(self) -> None:
        pass

    def parse_optional_java_class(self, optional_str):
        ret1 = ""
        ret2 = ""
        idx = 0
        print(optional_str)
        while optional_str[idx] != "e":
            idx += 1
        idx += 1
        while optional_str[idx] != ",":
            ret1 += optional_str[idx]
            idx += 1
        while optional_str[idx] != "e":
            idx += 1
        idx += 1
        while optional_str[idx] != ",":
            ret2 += optional_str[idx]
            idx += 1
        print("Yes")
        return [int(ret1), int(ret2)]

    
    def get_result_msg(self, fileName):
        self.tmp_data = []
        fp = open(fileName, "r")
        while True:
            line = fp.readline()
            if not line:
                break
            else:
                self.tmp_data.append(line[0: len(line) - 1])
        fp.close()

    def parse_result_msg(self):
        self.line_msg = []
        self.similar_arr = []
        self.cmp_file1 = self.tmp_data[0]
        self.cmp_file2 = self.tmp_data[1]
        idx = 2
        while idx < len(self.tmp_data):
            if (self.tmp_data[idx] == "begin"):
                idx += 1
                while self.tmp_data[idx] != "end":
                    # 存储相似度信息
                    if self.tmp_data[idx].isdigit():
                        self.similar_arr.append(int(self.tmp_data[idx]))
                    else:
                        # 存储行数位置信息
                        ret1 = self.parse_optional_java_class(self.tmp_data[idx])
                        idx += 1
                        ret2 = self.parse_optional_java_class(self.tmp_data[idx])
                        self.line_msg.append([ret1[0], ret1[1], ret2[0], ret2[1]])
                    idx += 1
            idx += 1
        # 去重
        new_list1 = []
        new_index = []
        for i in range(0, len(self.line_msg)):
            if (self.line_msg[i] not in new_list1):
                new_list1.append(self.line_msg[i])
                new_index.append(i)
        self.line_msg = new_list1
        new_list2 = []
        for i in range(0, len(new_index)):
            new_list2.append(self.similar_arr[new_index[i]])
        self.similar_arr = new_list2
        assert(len(self.similar_arr) == len(self.line_msg))
        st.session_state.res_list = []
        for i in range(0, len(self.line_msg)):
            st.session_state.res_list.append([i + 1, "({0}, {1})".format(self.line_msg[i][0], self.line_msg[i][1]), "({0}, {1})".format(self.line_msg[i][2], self.line_msg[i][3]), "{0}%".format(self.similar_arr[i]), ""])

    # 重复打开关闭文件效率会很差，目前时间影响不大，考虑后期进行优化
    def save_result(self, filename="./result/res"):
        fp = open(filename, "w")
        for i in range(0, len(self.similar_arr)):
            fp.write(str(self.similar_arr[i]) + "%\n")
            fp.write("src\n")
            # cmp file 1 的内容
            begin = self.line_msg[i][0]
            end = self.line_msg[i][1]
            count = 0
            fp1 = open(self.cmp_file1, "r")
            dele_space = 0
            while True:
                line = fp1.readline()
                if not line:
                    break
                count += 1
                if count == begin:
                    for j in range(0, len(line)):
                        if line[j] != " ":
                            break
                        else:
                            dele_space += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line[dele_space:len(line)])
            fp.write("dst\n")
            fp1.close()
            # cmp file 2 的内容
            begin = self.line_msg[i][2]
            end = self.line_msg[i][3]
            count = 0
            fp2 = open(self.cmp_file2, "r")
            dele_space = 0
            while True:
                line = fp2.readline()
                if not line:
                    break
                count += 1
                if count == begin:
                    for j in range(0, len(line)):
                        if line[j] != " ":
                            break
                        else:
                            dele_space += 1
                if count >= begin and count <= end:
                    fp.write(str(count) + ". " + line[dele_space:len(line)])
            fp2.close()
        fp.close()

    def read_data_set(self):
        fp = open("./result/output2", "r")
        tmp_str = fp.read()
        tmp_list = tmp_str.split(" ")
        tmp_list.pop(len(tmp_list) - 1)
        self.similar_arr = list(map(int, tmp_list))
        
        stat = [0, 0, 0, 0, 0]

        for i in range(0, len(self.similar_arr)):
            if (self.similar_arr[i] >= 1000):
                self.similar_arr[i] = 1000

            if self.similar_arr[i] >= 500:
                stat[0] += 1

            if self.similar_arr[i] <= 700 and self.similar_arr[i] > 500:
                stat[1] += 1
            elif self.similar_arr[i] > 700 and self.similar_arr[i] <= 900:
                stat[2] += 1
            elif self.similar_arr[i] > 900:
                stat[3] += 1
            else:
                stat[4] += 1
            self.similar_arr[i] = self.similar_arr[i] / 1000
        
        return self.similar_arr
    
res = Result()
res.get_result_msg("./result/output")
res.parse_result_msg()
res.save_result()