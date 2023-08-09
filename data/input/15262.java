public abstract class JavacTestingAbstractProcessor extends AbstractProcessor {
    private static final Set<String> allAnnotations;
    static {
        Set<String> tmp = new HashSet<>();
        tmp.add("*");
        allAnnotations = Collections.unmodifiableSet(tmp);
    }
    protected Elements eltUtils;
    protected Elements elements;
    protected Types    typeUtils;
    protected Types    types;
    protected Filer    filer;
    protected Messager messager;
    protected Map<String, String> options;
    protected JavacTestingAbstractProcessor() {
        super();
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        if (ssv != null)
            throw new IllegalStateException("SupportedSourceVersion annotation not supported here.");
        return SourceVersion.latest();
    }
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
        if (sat != null)
            return super.getSupportedAnnotationTypes();
        else
            return allAnnotations;
    }
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elements = eltUtils  = processingEnv.getElementUtils();
        types = typeUtils = processingEnv.getTypeUtils();
        filer     = processingEnv.getFiler();
        messager  = processingEnv.getMessager();
        options   = processingEnv.getOptions();
    }
}
