public class TestGetResource extends JavacTestingAbstractProcessor {
    private static String CONTENTS = "Hello World.";
    private static String PKG = "";
    private static String RESOURCE_NAME = "Resource1";
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            if (!roundEnv.processingOver()) {
                String phase = options.get("phase");
                if (phase.equals("write")) {
                    PrintWriter pw =
                        new PrintWriter(filer.createResource(CLASS_OUTPUT, PKG, RESOURCE_NAME).openWriter());
                    pw.print(CONTENTS);
                    pw.close();
                } else if (phase.equals("read")) {
                    String contents = filer.getResource(CLASS_OUTPUT,
                                                       PKG,
                                                       RESOURCE_NAME).getCharContent(false).toString();
                    if (!contents.equals(CONTENTS))
                        throw new RuntimeException("Expected \n\t" + CONTENTS +
                                                   "\nbut instead got \n\t" +
                                                   contents);
                    filer.createResource(CLASS_OUTPUT,
                                         PKG,
                                         RESOURCE_NAME);
                } else {
                    throw new RuntimeException("Unexpected phase: " + phase);
                }
            }
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return false;
    }
}
