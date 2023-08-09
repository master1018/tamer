public class TestContext extends JavacTestingAbstractProcessor {
    Trees treeUtils;
    int round = 0;
    @Override
    public void init(ProcessingEnvironment pEnv) {
        super.init(pEnv);
        treeUtils = Trees.instance(processingEnv);
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        round++;
        JavacProcessingEnvironment jpe = (JavacProcessingEnvironment) processingEnv;
        Context c = jpe.getContext();
        check(c.get(JavacElements.class), eltUtils);
        check(c.get(JavacTypes.class), typeUtils);
        check(c.get(JavacTrees.class), treeUtils);
        final int MAXROUNDS = 3;
        if (round < MAXROUNDS)
            generateSource("Gen" + round);
        return true;
    }
    <T> void check(T actual, T expected) {
        if (actual != expected) {
            messager.printMessage(ERROR,
                "round " + round + " unexpected value for " + expected.getClass().getName() + ": " + actual);
        }
    }
    void generateSource(String name) {
        String text = "class " + name + " { }\n";
        try (Writer out = filer.createSourceFile(name).openWriter()) {
                out.write(text);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
