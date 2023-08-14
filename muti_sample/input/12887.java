public class T6424358 extends AbstractProcessor {
    @TestMe enum Test { FOO; }
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment) {
        final Messager log = processingEnv.getMessager();
        final Elements elements = processingEnv.getElementUtils();
        final TypeElement testMe = elements.getTypeElement("TestMe");
        class Scan extends ElementScanner7<Void,Void> {
            @Override
            public Void visitExecutable(ExecutableElement e, Void p) {
                System.err.println("Looking at " + e);
                if ("values".contentEquals(e.getSimpleName()) &&
                    e.getModifiers().contains(Modifier.FINAL)) {
                    log.printMessage(ERROR, "final modifier on values()", e);
                    throw new AssertionError("final modifier on values()"); 
                }
                return null;
            }
        }
        Scan scan = new Scan();
        for (Element e : roundEnvironment.getElementsAnnotatedWith(testMe))
            scan.scan(e);
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
