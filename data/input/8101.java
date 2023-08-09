public class ProcBar extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver())
            messager.printMessage(NOTE, "Hello from ProcBar");
        return false;
    }
}
