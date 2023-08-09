public class TestProcUseImplicitWarning extends JavacTestingAbstractProcessor {
    int round = 0;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        round++;
        if (round == 1) {
            boolean error = options.containsKey("error");
            if (error)
                messager.printMessage(ERROR, "error generated per option");
        }
        return false;
    }
}
