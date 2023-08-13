public class BootstrapAPF implements AnnotationProcessorFactory {
    static final Collection<String> supportedOptions =
        Collections.unmodifiableSet(new HashSet<String>());
    static Collection<String> supportedAnnotations;
    static {
        Collection<String> c = new HashSet<String>();
        c.add("*");
        supportedAnnotations = Collections.unmodifiableCollection(c);
    }
    public BootstrapAPF(){}
    public Collection<String> supportedOptions() {
        return BootstrapAPF.supportedOptions;
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env) {
        return new PrintAP(env);
    }
}
