public class OutputRedirect {
    public static void main(String[] args) {
        PrintStream originalOutput = System.out;
        try {
            doTest();
        } finally {
            System.setOut(originalOutput);
        }
    }
    static void doTest() {
        ByteArrayOutputStream redirectedOutput = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        System.setOut(new PrintStream(redirectedOutput));
        PrintWriter sink = new PrintWriter(new ByteArrayOutputStream());
        int result = Main.execute("javadoc", sink, sink, sink,
                                  "com.sun.tools.doclets.standard.Standard",
                                  new String[] {"p"}
                                  );
        if (redirectedOutput.toByteArray().length > 0) {
            originalOutput.println("Test failed; here's what javadoc wrote on its standard output:");
            originalOutput.println(redirectedOutput.toString());
            throw new Error("javadoc output wasn\'t properly redirected");
        } else if (result != 0) {
            throw new Error("javadoc run failed");
        } else {
            originalOutput.println("OK, good");
        }
    }
}
