public class WindowTitles
{
    private static final String BUGID = "4530730";
    private static final String BUGNAME = "WindowTitles";
    private static final String FS = System.getProperty("file.separator");
    private static final String PS = System.getProperty("path.separator");
    private static final String LS = System.getProperty("line.separator");
    private static final String TMPDIR_STRING1 = "." + FS + "docs1" + FS;
    private static final String TMPDIR_STRING2 = "." + FS + "docs2" + FS;
    public static int subtestNum = 0;
    public static int numSubtestsPassed = 0;
    public static void main(String[] args) {
        String srcdir = System.getProperty("test.src", ".");
        runJavadoc(new String[] {"-d", TMPDIR_STRING1,
                                 "-use",
                                 "-sourcepath", srcdir,
                                 "p1", "p2"});
        runTestsOnHTML(testArray);
        System.out.println("");  
        runJavadoc(new String[] {"-d", TMPDIR_STRING2,
                                 "-splitindex",
                                 "-sourcepath", System.getProperty("test.src", "."),
                                 "p1"});
        runTestsOnHTML(testSplitIndexArray);
        printSummary();
    }
    public static void runJavadoc(String[] javadocArgs) {
        if (com.sun.tools.javadoc.Main.execute(javadocArgs) != 0) {
            throw new Error("Javadoc failed to execute");
        }
    }
    private static final String[][] testArray = {
            { "<title>Overview</title>",
                    TMPDIR_STRING1 + "overview-summary.html"                  },
            { "<title>Class Hierarchy</title>",
                    TMPDIR_STRING1 + "overview-tree.html"                     },
            { "<title>Overview List</title>",
                    TMPDIR_STRING1 + "overview-frame.html"                    },
            { "<title>p1</title>",
                    TMPDIR_STRING1 + "p1" + FS + "package-summary.html"       },
            { "<title>p1</title>",
                    TMPDIR_STRING1 + "p1" + FS + "package-frame.html"         },
            { "<title>p1 Class Hierarchy</title>",
                    TMPDIR_STRING1 + "p1" + FS + "package-tree.html"          },
            { "<title>Uses of Package p1</title>",
                    TMPDIR_STRING1 + "p1" + FS + "package-use.html"           },
            { "<title>C1</title>",
                    TMPDIR_STRING1 + "p1" + FS + "C1.html"                    },
            { "<title>All Classes</title>",
                    TMPDIR_STRING1 + "allclasses-frame.html"                  },
            { "<title>All Classes</title>",
                    TMPDIR_STRING1 + "allclasses-noframe.html"                },
            { "<title>Constant Field Values</title>",
                    TMPDIR_STRING1 + "constant-values.html"                   },
            { "<title>Deprecated List</title>",
                    TMPDIR_STRING1 + "deprecated-list.html"                   },
            { "<title>Serialized Form</title>",
                    TMPDIR_STRING1 + "serialized-form.html"                   },
            { "<title>API Help</title>",
                    TMPDIR_STRING1 + "help-doc.html"                          },
            { "<title>Index</title>",
                    TMPDIR_STRING1 + "index-all.html"                         },
            { "<title>Uses of Class p1.C1</title>",
                    TMPDIR_STRING1 + "p1" + FS + "class-use" + FS + "C1.html" },
        };
    private static final String[][] testSplitIndexArray = {
            { "<title>C-Index</title>",
                    TMPDIR_STRING2 + "index-files" + FS + "index-1.html"       },
        };
    public static void runTestsOnHTML(String[][] testArray) {
        for (int i = 0; i < testArray.length; i++) {
            subtestNum += 1;
            String fileString = readFileToString(testArray[i][1]);
            String stringToFind = testArray[i][0];
            if (findString(fileString, stringToFind) == -1) {
                System.out.println("\nSub-test " + (subtestNum)
                    + " for bug " + BUGID + " (" + BUGNAME + ") FAILED\n"
                    + "when searching for:\n"
                    + stringToFind);
            } else {
                numSubtestsPassed += 1;
                System.out.println("\nSub-test " + (subtestNum) + " passed:\n" + stringToFind);
            }
        }
    }
    public static void printSummary() {
        if ( numSubtestsPassed == subtestNum ) {
            System.out.println("\nAll " + numSubtestsPassed + " subtests passed");
        } else {
            throw new Error("\n" + (subtestNum - numSubtestsPassed) + " of " + (subtestNum)
                             + " subtests failed for bug " + BUGID + " (" + BUGNAME + ")\n");
        }
    }
    public static String readFileToString(String filename) {
        try {
            File file = new File(filename);
            if ( !file.exists() ) {
                System.out.println("\nFILE DOES NOT EXIST: " + filename);
            }
            BufferedReader in = new BufferedReader(new FileReader(file));
            char[] allChars = new char[(int)file.length()];
            in.read(allChars, 0, (int)file.length());
            in.close();
            String allCharsString = new String(allChars);
            return allCharsString;
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return "";
        } catch (IOException e) {
            System.err.println(e);
            return "";
        }
    }
    public static int findString(String fileString, String stringToFind) {
        return fileString.indexOf(stringToFind);
    }
}
