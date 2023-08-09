public class MyProcessor extends AbstractProcessor {
    private Messager messager;
    public void init(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }
    public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
        if (!renv.processingOver()) {
            for(TypeElement e : ElementFilter.typesIn(renv.getRootElements())) {
                for (TypeParameterElement tp : e.getTypeParameters()) {
                    if (tp.getSimpleName().toString().length() > 1) {
                        messager.printMessage(WARNING,
                            "Type param names should be of length 1", tp);
                    }
                }
            }
        }
        return true;
    }
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
