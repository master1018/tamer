public class JavascriptWinTitle {
    private static final String BUGID = "4645058";
    private static final String BUGNAME = "JavascriptWinTitle";
    private static final String FS = System.getProperty("file.separator");
    private static final String PS = System.getProperty("path.separator");
    private static final String LS = System.getProperty("line.separator");
    private static final String TMPDEST_DIR1 = "." + FS + "docs1" + FS;
    private static final String TMPDEST_DIR2 = "." + FS + "docs2" + FS;
    public static int subtestNum = 0;
    public static int numSubtestsPassed = 0;
    public static void main(String[] args) {
        String srcdir = System.getProperty("test.src", ".");
        runJavadoc(new String[] {"-d", TMPDEST_DIR1,
                                 "-doctitle", "Document Title",
                                 "-windowtitle", "Window Title",
                                 "-overview", (srcdir + FS + "overview.html"),
                                 "-linkoffline",
                                    "http:
                                 "-sourcepath", srcdir,
                                 "p1", "p2"});
        runTestsOnHTML(testArray);
        printSummary();
    }
    public static void runJavadoc(String[] javadocArgs) {
        if (com.sun.tools.javadoc.Main.execute(javadocArgs) != 0) {
            throw new Error("Javadoc failed to execute");
        }
    }
    private static final String[][] testArray = {
            {  "<script type=\"text/javascript\">",
                     TMPDEST_DIR1 + "overview-summary.html"  },
            {  "<body>",
                     TMPDEST_DIR1 + "overview-summary.html"  },
            {  "<body>",
                     TMPDEST_DIR1 + FS + "p1" + FS + "package-summary.html"  },
            {   "<body>",
                     TMPDEST_DIR1 + "overview-frame.html"  },
            {   "<body>",
                     TMPDEST_DIR1 + "allclasses-frame.html"  },
            {   "<body>",
                     TMPDEST_DIR1 + FS + "p1" + FS + "package-frame.html"  },
            {"<script type=\"text/javascript\"><!--" + LS +
                     "    if (location.href.indexOf('is-external=true') == -1) {" + LS +
                     "        parent.document.title=\"C (Window Title)\";" + LS +
                     "    }" + LS + "
             TMPDEST_DIR1 + FS + "p1" + FS + "C.html"
            }
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
