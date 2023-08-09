public class ProcFoo extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver())
            messager.printMessage(NOTE, "Hello from ProcFoo");
        return false;
    }
}
