public class T6597678 extends JavacTestingAbstractProcessor {
    public static void main(String... args) throws Exception {
        new T6597678().run();
    }
    void run() throws Exception {
        String myName = T6597678.class.getSimpleName();
        File testSrc = new File(System.getProperty("test.src"));
        File file = new File(testSrc, myName + ".java");
        compile(
            "-proc:only",
            "-processor", myName,
            file.getPath());
    }
    void compile(String... args) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javac.Main.compile(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            throw new Exception("compilation failed unexpectedly: rc=" + rc);
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        Locale locale = context.get(Locale.class);
        JavacMessages messages = context.get(JavacMessages.messagesKey);
        round++;
        if (round == 1) {
            initialLocale = locale;
            initialMessages = messages;
        } else {
            checkEqual("locale", locale, initialLocale);
            checkEqual("messages", messages, initialMessages);
        }
        return true;
    }
    <T> void checkEqual(String label, T actual, T expected) {
        if (actual != expected)
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Unexpected value for " + label
                    + "; expected: " + expected
                    + "; found: " + actual);
    }
    int round = 0;
    Locale initialLocale;
    JavacMessages initialMessages;
}
