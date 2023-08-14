public class WErrorLast extends JavacTestingAbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            messager.printMessage(Diagnostic.Kind.WARNING, "last round");
        }
        return true;
    }
}
