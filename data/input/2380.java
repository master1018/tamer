public class T6634138 extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            System.out.println("Writing out source files.");
            try {
                PrintWriter pw = new PrintWriter(filer.createSourceFile("foo.WrittenAfterProcessing").openWriter());
                try {
                     pw.println("package foo;");
                     pw.println("public class WrittenAfterProcessing {");
                     pw.println("  public WrittenAfterProcessing() {super();}");
                     pw.println("}");
                 } finally {
                     pw.close();
                 }
                pw = new PrintWriter(filer.createSourceFile("foo.package-info").openWriter());
                try {
                     pw.println("@Deprecated");
                     pw.println("package foo;");
                 } finally {
                     pw.close();
                 }
            } catch(IOException io) {
                throw new RuntimeException(io);
            }
        }
        return true;
    }
}
