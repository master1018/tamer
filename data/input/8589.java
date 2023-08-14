public abstract class JavadocTester {
    protected static final String FS = System.getProperty("file.separator");
    protected static final String PS = System.getProperty("path.separator");
    protected static final String NL = System.getProperty("line.separator");
    protected static final String SRC_DIR = System.getProperty("test.src", ".");
    protected static final String JAVA_VERSION = System.getProperty("java.version");
    protected static final String[][] NO_TEST = new String[][] {};
    public static final String ERROR_OUTPUT = "ERROR_OUTPUT";
    public static final String NOTICE_OUTPUT = "NOTICE_OUTPUT";
    public static final String WARNING_OUTPUT = "WARNING_OUTPUT";
    public static final String STANDARD_OUTPUT = "STANDARD_OUTPUT";
    public static final String DEFAULT_DOCLET_CLASS = "com.sun.tools.doclets.formats.html.HtmlDoclet";
    public static final String DEFAULT_DOCLET_CLASS_OLD = "com.sun.tools.doclets.standard.Standard";
    public StringWriter errors;
    public StringWriter notices;
    public StringWriter warnings;
    public StringBuffer standardOut;
    private static int numTestsRun = 0;
    private static int numTestsPassed = 0;
    private static int javadocRunNum = 0;
    protected boolean exactNewlineMatch = true;
    public JavadocTester() {
    }
    public abstract String getBugId();
    public abstract String getBugName();
    public static int run(JavadocTester tester, String[] args,
            String[][] testArray, String[][] negatedTestArray) {
        int returnCode = tester.runJavadoc(args);
        tester.runTestsOnHTML(testArray, negatedTestArray);
        return returnCode;
    }
    public int runJavadoc(String[] args) {
        float javaVersion = Float.parseFloat(JAVA_VERSION.substring(0,3));
        String docletClass = javaVersion < 1.5 ?
            DEFAULT_DOCLET_CLASS_OLD : DEFAULT_DOCLET_CLASS;
        return runJavadoc(docletClass, args);
    }
    public int runJavadoc(String docletClass, String[] args) {
        javadocRunNum++;
        if (javadocRunNum == 1) {
            System.out.println("\n" + "Running javadoc...");
        } else {
            System.out.println("\n" + "Running javadoc (run "
                                    + javadocRunNum + ")...");
        }
        initOutputBuffers();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PrintStream prev = System.out;
        System.setOut(new PrintStream(stdout));
        int returnCode = com.sun.tools.javadoc.Main.execute(
                getBugName(),
                new PrintWriter(errors, true),
                new PrintWriter(warnings, true),
                new PrintWriter(notices, true),
                docletClass,
                getClass().getClassLoader(),
                args);
        System.setOut(prev);
        standardOut = new StringBuffer(stdout.toString());
        printJavadocOutput();
        return returnCode;
    }
    private void initOutputBuffers() {
        errors   = new StringWriter();
        notices  = new StringWriter();
        warnings = new StringWriter();
    }
    public void runTestsOnHTML(String[][] testArray, String[][] negatedTestArray) {
        runTestsOnHTML(testArray, false);
        runTestsOnHTML(negatedTestArray, true);
    }
    private void runTestsOnHTML(String[][] testArray , boolean isNegated) {
        for (int i = 0; i < testArray.length; i++) {
            numTestsRun++;
            System.out.print("Running subtest #" + numTestsRun + "... ");
            String stringToFind = testArray[i][1];
            String fileString;
            try {
                fileString = readFileToString(testArray[i][0]);
            } catch (Error e) {
                if (isNegated) {
                  numTestsPassed += 1;
                  System.out.println("Passed\n not found:\n"
                    + stringToFind + " in non-existent " + testArray[i][0] + "\n");
                  continue;
                }
                throw e;
            }
            boolean isFound = findString(fileString, stringToFind);
            if ((isNegated && !isFound) || (!isNegated && isFound) ) {
                numTestsPassed += 1;
                System.out.println( "Passed" + "\n"
                                    + (isNegated ? "not found:" : "found:") + "\n"
                                    + stringToFind + " in " + testArray[i][0] + "\n");
            } else {
                System.out.println( "FAILED" + "\n"
                                    + "for bug " + getBugId()
                                    + " (" + getBugName() + ")" + "\n"
                                    + "when searching for:" + "\n"
                                    + stringToFind
                                    + " in " + testArray[i][0] + "\n");
            }
        }
    }
    public void runDiffs(String[][] filePairs) throws Error {
        runDiffs(filePairs, true);
    }
    public void runDiffs(String[][] filePairs, boolean throwErrorIfNoMatch) throws Error {
        for (int i = 0; i < filePairs.length; i++) {
            diff(filePairs[i][0], filePairs[i][1], throwErrorIfNoMatch);
        }
    }
    public void checkExitCode(int expectedExitCode, int actualExitCode) {
        numTestsRun++;
        if (expectedExitCode == actualExitCode) {
            System.out.println( "Passed" + "\n" + " got return code " +
                actualExitCode);
            numTestsPassed++;
        } else {
            System.out.println( "FAILED" + "\n" + "for bug " + getBugId()
                + " (" + getBugName() + ")" + "\n" + "Expected return code " +
                expectedExitCode + " but got " + actualExitCode);
        }
    }
    protected void printSummary() {
        if ( numTestsRun != 0 && numTestsPassed == numTestsRun ) {
            System.out.println("\n" + "All " + numTestsPassed
                                             + " subtests passed");
        } else {
            throw new Error("\n" + (numTestsRun - numTestsPassed)
                                    + " of " + (numTestsRun)
                                    + " subtests failed for bug " + getBugId()
                                    + " (" + getBugName() + ")" + "\n");
        }
    }
    protected void printJavadocOutput() {
        System.out.println(STANDARD_OUTPUT + " : \n" + getStandardOutput());
        System.err.println(ERROR_OUTPUT + " : \n" + getErrorOutput());
        System.err.println(WARNING_OUTPUT + " : \n" + getWarningOutput());
        System.out.println(NOTICE_OUTPUT + " : \n" + getNoticeOutput());
    }
    public String readFileToString(String fileName) throws Error {
        if (fileName.equals(ERROR_OUTPUT)) {
            return getErrorOutput();
        } else if (fileName.equals(NOTICE_OUTPUT)) {
            return getNoticeOutput();
        } else if (fileName.equals(WARNING_OUTPUT)) {
            return getWarningOutput();
        } else if (fileName.equals(STANDARD_OUTPUT)) {
            return getStandardOutput();
        }
        try {
            File file = new File(fileName);
            if ( !file.exists() ) {
                System.out.println("\n" + "FILE DOES NOT EXIST: " + fileName);
            }
            BufferedReader in = new BufferedReader(new FileReader(file));
            char[] allChars = new char[(int)file.length()];
            in.read(allChars, 0, (int)file.length());
            in.close();
            String allCharsString = new String(allChars);
            return allCharsString;
        } catch (FileNotFoundException e) {
            System.err.println(e);
            throw new Error("File not found: " + fileName);
        } catch (IOException e) {
            System.err.println(e);
            throw new Error("Error reading file: " + fileName);
        }
    }
    public boolean diff(String file1, String file2, boolean throwErrorIFNoMatch) throws Error {
        String file1Contents = readFileToString(file1);
        String file2Contents = readFileToString(file2);
        numTestsRun++;
        if (file1Contents.trim().compareTo(file2Contents.trim()) == 0) {
            System.out.println("Diff successful: " + file1 + ", " + file2);
            numTestsPassed++;
            return true;
        } else if (throwErrorIFNoMatch) {
            throw new Error("Diff failed: " + file1 + ", " + file2);
        } else {
            return false;
        }
    }
    private boolean findString(String fileString, String stringToFind) {
        if (exactNewlineMatch) {
            return fileString.indexOf(stringToFind) >= 0;
        } else {
            return fileString.replace(NL, "\n").indexOf(stringToFind.replace(NL, "\n")) >= 0;
        }
    }
    public String getStandardOutput() {
        return standardOut.toString();
    }
    public String getErrorOutput() {
        return errors.getBuffer().toString();
    }
    public String getNoticeOutput() {
        return notices.getBuffer().toString();
    }
    public String getWarningOutput() {
        return warnings.getBuffer().toString();
    }
    public static void copyDir(String targetDir, String destDir) {
        if (targetDir.endsWith("SCCS")) {
            return;
        }
        try {
            File targetDirObj = new File(targetDir);
            File destDirParentObj = new File(destDir);
            File destDirObj = new File(destDirParentObj, targetDirObj.getName());
            if (! destDirParentObj.exists()) {
                destDirParentObj.mkdir();
            }
            if (! destDirObj.exists()) {
                destDirObj.mkdir();
            }
            String[] files = targetDirObj.list();
            for (int i = 0; i < files.length; i++) {
                File srcFile = new File(targetDirObj, files[i]);
                File destFile = new File(destDirObj, files[i]);
                if (srcFile.isFile()) {
                    System.out.println("Copying " + srcFile + " to " + destFile);
                        copyFile(destFile, srcFile);
                } else if(srcFile.isDirectory()) {
                    copyDir(srcFile.getAbsolutePath(), destDirObj.getAbsolutePath());
                }
            }
        } catch (IOException exc) {
            throw new Error("Could not copy " + targetDir + " to " + destDir);
        }
    }
    public static void copyFile(File destfile, File srcfile)
        throws IOException {
        byte[] bytearr = new byte[512];
        int len = 0;
        FileInputStream input = new FileInputStream(srcfile);
        File destDir = destfile.getParentFile();
        destDir.mkdirs();
        FileOutputStream output = new FileOutputStream(destfile);
        try {
            while ((len = input.read(bytearr)) != -1) {
                output.write(bytearr, 0, len);
            }
        } catch (FileNotFoundException exc) {
        } catch (SecurityException exc) {
        } finally {
            input.close();
            output.close();
        }
    }
}
