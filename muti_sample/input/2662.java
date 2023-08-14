public class MessagerBasics extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (processingEnv.getOptions().containsKey("finalError"))
                messager.printMessage(ERROR,   "Does not compute");
            else {
                messager.printMessage(NOTE,    "Post no bills");
                messager.printMessage(WARNING, "Beware the ides of March!");
            }
        }
        return true;
    }
}
