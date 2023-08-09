public class DocRootSlash
{
    private static final String BUGID = "4524350, 4662945, or 4633447";
    private static final String BUGNAME = "DocRootSlash";
    private static final String FS = System.getProperty("file.separator");
    private static final String PS = System.getProperty("path.separator");
    private static final String TMPDIR_STRING1 = "." + FS + "docs1" + FS;
    public static int subtestNum = 0;
    public static int numOfSubtestsPassed = 0;
    public static void main(String[] args) {
        String srcdir = System.getProperty("test.src", ".");
        runJavadoc(new String[] {"-d", TMPDIR_STRING1,
                                 "-overview", (srcdir + FS + "overview.html"),
                                 "-header", "<A HREF=\"{@docroot}/package-list\">{&#064;docroot}</A> <A HREF=\"{@docRoot}/help-doc\">{&#064;docRoot}</A>",
                                 "-sourcepath", srcdir,
                                 "p1", "p2"});
        runTestsOnHTMLFiles(filenameArray);
        printSummary();
    }
    public static void runJavadoc(String[] javadocArgs) {
        if (com.sun.tools.javadoc.Main.execute(javadocArgs) != 0) {
            throw new Error("Javadoc failed to execute");
        }
    }
    private static final String[] filenameArray = {
        TMPDIR_STRING1 + "p1" + FS + "C1.html" ,
        TMPDIR_STRING1 + "p1" + FS + "package-summary.html",
        TMPDIR_STRING1 + "overview-summary.html"
    };
    public static void runTestsOnHTMLFiles(String[] filenameArray) {
        String fileString;
        for (int i = 0; i < filenameArray.length; i++ ) {
            fileString = readFileToString(filenameArray[i]);
            System.out.println("\nSub-tests for file: " + filenameArray[i]
                                + " --------------");
            for ( int j = 0; j < 11; j++ ) {
                subtestNum += 1;
                compareActualToExpected(fileString);
            }
        }
        String filename = TMPDIR_STRING1 + "overview-frame.html";
        fileString = readFileToString(filename);
        subtestNum += 1;
        String stringToFind = "<A HREF=\"./package-list\">";
        String result;
        if ( fileString.indexOf(stringToFind) == -1 ) {
            result = "FAILED";
        } else {
            result = "succeeded";
            numOfSubtestsPassed += 1;
        }
        System.out.println("\nSub-test " + (subtestNum)
            + " for bug " + BUGID + " (" + BUGNAME + ") " + result + "\n"
            + "when searching for:\n"
            + stringToFind + "\n"
            + "in file " + filename);
        subtestNum += 1;
        stringToFind = "<A HREF=\"./help-doc\">";
        if ( fileString.indexOf(stringToFind) == -1 ) {
            result = "FAILED";
        } else {
            result = "succeeded";
            numOfSubtestsPassed += 1;
        }
        System.out.println("\nSub-test " + (subtestNum)
            + " for bug " + BUGID + " (" + BUGNAME + ") " + result + "\n"
            + "when searching for:\n"
            + stringToFind + "\n"
            + "in file " + filename);
    }
    public static void printSummary() {
        System.out.println("");
        if ( numOfSubtestsPassed == subtestNum ) {
            System.out.println("\nAll " + numOfSubtestsPassed + " subtests passed");
        } else {
            throw new Error("\n" + (subtestNum - numOfSubtestsPassed) + " of " + (subtestNum)
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
    static String prefix = "(?i)(<a\\s+href=";    
    static String ref1   = "\")([^\"]*)(\".*?>)"; 
    static String ref2   = ")(\\S+?)([^<>]*>)";   
    static String label  = "(.*?)";               
    static String end    = "(</a>)";              
    static void compareActualToExpected(String str) {
        Pattern actualLinkPattern1 =
            Pattern.compile("Sub-test " + subtestNum + " Actual: " + prefix + ref1, Pattern.DOTALL);
        Pattern expectLinkPattern1 =
            Pattern.compile("Sub-test " + subtestNum + " Expect: " + prefix + ref1, Pattern.DOTALL);
        CharBuffer charBuffer = CharBuffer.wrap(str);
        Matcher actualLinkMatcher1 = actualLinkPattern1.matcher(charBuffer);
        Matcher expectLinkMatcher1 = expectLinkPattern1.matcher(charBuffer);
        String result;
        if ( expectLinkMatcher1.find() && actualLinkMatcher1.find() ) {
            String expectRef = expectLinkMatcher1.group(2);
            String actualRef = actualLinkMatcher1.group(2);
            if ( actualRef.equals(expectRef) ) {
                result = "succeeded";
                numOfSubtestsPassed += 1;
            } else {
                result = "FAILED";
            }
            System.out.println("\nSub-test " + (subtestNum)
                + " for bug " + BUGID + " (" + BUGNAME + ") " + result + "\n"
                + "Actual: \"" + actualRef + "\"" + "\n"
                + "Expect: \"" + expectRef + "\"");
        } else {
            System.out.println("Didn't find <A HREF> that fits the pattern: "
                  + expectLinkPattern1.pattern() );
        }
    }
}
