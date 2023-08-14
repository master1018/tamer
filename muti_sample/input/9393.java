public class PhantomUpdate extends AbstractProcessor {
    boolean firstRound = true;
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (firstRound) {
            verifyOptions();
            printGoodbyeWorld();
            firstRound = false;
        }
        return true; 
    }
    private void verifyOptions() {
        Map<String, String> actualOptions = processingEnv.getOptions();
        Map<String, String> expectedOptions = new LinkedHashMap<String, String>();
        expectedOptions.put("foo", null);
        expectedOptions.put("bar", "baz");
        if (!actualOptions.equals(expectedOptions) ) {
            System.err.println("Expected options " + expectedOptions +
                               "\n but got " + actualOptions);
            throw new RuntimeException("Options mismatch");
        }
    }
    private void printGoodbyeWorld() {
        try {
            PrintWriter pw = new PrintWriter(processingEnv.getFiler().createSourceFile("GoodbyeWorld").openWriter());
            pw.println("public class GoodbyeWorld {");
            pw.println("  
            pw.println("  public static void main(String argv[]) {");
            pw.println("    System.out.println(\"Goodbye World\");");
            pw.println("  }");
            pw.println("}");
            pw.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
