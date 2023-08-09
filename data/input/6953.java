public class AuthorDD
{
    private static final String BUGID = "4651598";
    private static final String BUGNAME = "AuthorDD";
    private static final String FS = System.getProperty("file.separator");
    private static final String PS = System.getProperty("path.separator");
    private static final String NL = System.getProperty("line.separator");
    public static int subtestNum = 0;
    public static int numSubtestsPassed = 0;
    public static void main(String[] args) {
        String srcdir = System.getProperty("test.src", ".");
        runJavadoc(new String[] {"-d", BUGID,
                                 "-author",
                                 "-version",
                                 "-sourcepath", srcdir,
                                 "p1"});
        runTestsOnHTML(testArray);
        printSummary();
    }
    public static void runJavadoc(String[] javadocArgs) {
        if (com.sun.tools.javadoc.Main.execute(AuthorDD.class.getClassLoader(),
                                               javadocArgs) != 0) {
            throw new Error("Javadoc failed to execute");
        }
    }
    private static final String[][] testArray = {
            { "<dt><span class=\"strong\">Since:</span></dt>"+NL+"  <dd>JDK 1.0</dd>",
                                  BUGID + FS + "p1" + FS + "C1.html" },
            { "<dt><span class=\"strong\">Author:</span></dt>"+NL+"  <dd>Doug Kramer, Jamie, Neal</dd>",
                                  BUGID + FS + "p1" + FS + "C1.html" },
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
