public class TestReturnCode extends JavacTestingAbstractProcessor {
    private boolean errorOnFirst;
    private boolean errorOnLast;
    private boolean exceptionOnFirst;
    private boolean exceptionOnLast;
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            System.out.format("Variables: %b\t%b\t%b\t%b%n",
                              errorOnFirst,
                              errorOnLast,
                              exceptionOnFirst,
                              exceptionOnLast);
            if (errorOnFirst)
                messager.printMessage(ERROR, "Error on first round.");
            if (exceptionOnFirst)
                throw new RuntimeException("Exception on first round.");
        } else {
            if (errorOnLast)
                messager.printMessage(ERROR, "Error on last round.");
            if (exceptionOnLast)
                throw new RuntimeException("Exception on last round.");
        }
        return true;
    }
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Set<String> keySet = processingEnv.getOptions().keySet();
        errorOnFirst      = keySet.contains("ErrorOnFirst");
        errorOnLast     = keySet.contains("ErrorOnLast");
        exceptionOnFirst  = keySet.contains("ExceptionOnFirst");
        exceptionOnLast = keySet.contains("ExceptionOnLast");
    }
}
