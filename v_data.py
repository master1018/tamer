from random import choice

v_cwe_list = [[11, '11.java', '(8, 15)', 83], [12, '12.java', '(25, 31)', 91], [13, '13.java', '(86, 93)', 72], [14, '14.java', '(37, 48)', 73], [15, '15.java', '(42, 43)', 83], [16, '16.java', '(39, 46)', 72], [17, '17.java', '(45, 48)', 81], [18, '18.java', '(6, 18)', 52], [19, '19.java', '(24, 32)', 53], [20, '20.java', '(3, 18)', 85], [21, '21.java', '(49, 54)', 88], [22, '22.java', '(58, 61)', 81], [23, '23.java', '(32, 40)', 57], [24, '24.java', '(85, 92)', 88], [25, '25.java', '(60, 68)', 95], [26, '26.java', '(71, 85)', 93], [27, '27.java', '(25, 27)', 85], [28, '28.java', '(15, 29)', 91], [29, '29.java', '(84, 97)', 66], [30, '30.java', '(46, 59)', 69], [31, '31.java', '(28, 40)', 65], [32, '32.java', '(95, 104)', 88], [33, '33.java', '(30, 40)', 79], [34, '34.java', '(73, 79)', 69], [35, '35.java', '(76, 81)', 78], [36, '36.java', '(82, 85)', 88], [37, '37.java', '(88, 90)', 77], [38, '38.java', '(13, 21)', 85], [39, '39.java', '(92, 97)', 79], [40, '40.java', '(33, 34)', 95], [41, '41.java', '(23, 25)', 64], [42, '42.java', '(24, 26)', 69], [43, '43.java', '(97, 101)', 64], [44, '44.java', '(60, 66)', 65], [45, '45.java', '(60, 64)', 52], [46, '46.java', '(57, 58)', 93], [47, '47.java', '(60, 65)', 65], [48, '48.java', '(43, 58)', 99], [49, '49.java', '(12, 18)', 83], [50, '50.java', '(80, 92)', 65], [51, '51.java', '(40, 48)', 85], [52, '52.java', '(87, 97)', 86], [53, '53.java', '(24, 29)', 74], [54, '54.java', '(18, 20)', 72], [55, '55.java', '(46, 52)', 73], [56, '56.java', '(43, 49)', 64], [57, '57.java', '(24, 31)', 64], [58, '58.java', '(93, 108)', 77], [59, '59.java', '(45, 55)', 64], [60, '60.java', '(27, 31)', 91], [61, '61.java', '(57, 68)', 71], [62, '62.java', '(54, 57)', 92], [63, '63.java', '(43, 51)', 85], [64, '64.java', '(13, 26)', 85], [65, '65.java', '(50, 63)', 91], [66, '66.java', '(5, 9)', 100], [67, '67.java', '(74, 81)', 64], [68, '68.java', '(2, 5)', 100], [69, '69.java', '(89, 94)', 89], [70, '70.java', '(87, 96)', 88], [71, '71.java', '(99, 104)', 85], [72, '72.java', '(75, 83)', 95], [73, '73.java', '(35, 46)', 85], [74, '74.java', '(95, 109)', 93], [75, '75.java', '(70, 85)', 99], [76, '76.java', '(87, 88)', 73], [77, '77.java', '(87, 92)', 81], [78, '78.java', '(89, 92)', 79], [79, '79.java', '(96, 97)', 66], [80, '80.java', '(61, 71)', 91], [81, '81.java', '(50, 55)', 85], [82, '82.java', '(3, 11)', 79], [83, '83.java', '(62, 67)', 91], [84, '84.java', '(28, 34)', 61], [85, '85.java', '(69, 80)', 88], [86, '86.java', '(40, 52)', 85], [87, '87.java', '(35, 46)', 92], [88, '88.java', '(32, 36)', 54], [89, '89.java', '(91, 97)', 79], [90, '90.java', '(55, 66)', 85], [91, '91.java', '(58, 60)', 79], [92, '92.java', '(55, 58)', 54], [93, '93.java', '(14, 15)', 72]]



# cwe-481
v_code1 = '''
if (isValid = true) {
            System.out.println("Performing processing");
            doSomethingImportant();
        } else {
            System.out.println("Not Valid, do not perform processing");
            return;
        }
            '''
# cwe-481
v_code2 = '''
            if (firstRun = true )
            {
                activityIntent.putExtra(EXTRA_FIRST_RUN, true);
                startActivity(activityIntent);
            }
            '''
# cwe-78
v_code5 = '''
                 if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
        {
            osCommand = "c:\\WINDOWS\\SYSTEM32\\cmd.exe /c dir ";
        }
        else
        {
            osCommand = "/bin/ls ";
        }
            '''
# cwe-484
v_code7 = '''
switch (intRandom)
            {
            case 1:
                stringValue = "one";
            case 2:
                stringValue = "two"; 
            default:
                stringValue = "Default";
            }
        '''
# cwe-798
v_code9 = '''
if (!password.equals("Mew!")) {
return(0)
}
            '''
# cwe-798
v_code10 = '''
if (password.equals("68af404b513073584c4b6f22b6c63e6b")) {
System.out.println("Entering Diagnostic Mode...");
return true;
}
            '''

v_code_list = [v_code1, v_code2, "hi", "hi",v_code5, "hi",v_code7, "hi", v_code9, v_code10]
v_cwe_map = [0, 0, -1, -1, 8, -1, 5, -1, 7, 7]
v_cwe = []
v_cwe.append([1, '1.java', "(3, 9)", 71]) # code1
v_cwe.append([2, '2.java', "(83, 84)", 67]) # code2
v_cwe.append([3, "3.java", "(19, 26)", 74])  # code3
v_cwe.append([4, '4.java', "(33, 38)", 64])
v_cwe.append([5, "5.java", "(69, 76)", 91])
v_cwe.append([6, "6.java", "(21, 29)", 89]) # code4
v_cwe.append([7, "7.java", "(76, 78)", 92]) # code5
v_cwe.append([8, '8.java', "(53, 58)", 77])
v_cwe.append([9, "9.java", "(49, 56)", 69])
v_cwe.append([10, "10.java", "(43, 46)", 85]) # code6

v_cwe.extend(v_cwe_list)

a_graph_src = []
a_graph_dst = []
a_graph_edge = []
cwe_rand = ['CWE-103', 'CWE-104', 'CWE-110', 'CWE-197', 'CWE-78', 'CWE-209', 'CWE-246', 'CWE-787', 'CWE-366', 'CWE-470', 'CWE-476', 'CWE-484', 'CWE-491', 'CWE-537', 'CWE-567', 'CWE-568', 'CWE-577', 'CWE-580', 'CWE-582', 'CWE-586', 'CWE-767']
a_graph_src.extend(['CWE-481', 'CWE-481', 'CWE-78', "CWE-395", "CWE-607", "CWE-484", "CWE-798", "CWE-537", "CWE-491", "CWE-798"])

for i in range(0, 10):
    a_graph_dst.append(v_cwe[i][1])
    a_graph_edge.append(str(v_cwe[i][3]) + "%")

for i in range(10, len(v_cwe)):
    a_graph_src.append(choice(cwe_rand))
    a_graph_dst.append(v_cwe[i][1])
    a_graph_edge.append(str(v_cwe[i][3]) + "%")
