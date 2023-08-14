public class ErrorAPF implements AnnotationProcessorFactory {
    static class ErrorAP implements AnnotationProcessor {
        AnnotationProcessorEnvironment env;
        ErrorAP(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            Messager messager = env.getMessager();
            messager.printError("It's a mad, mad, mad, mad world");
            messager.printError("Something wicked this way comes");
            for(TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
                messager.printError(typeDecl.getPosition(), "Boring class name");
        }
    }
    static Collection<String> supportedTypes;
    static {
        String types[] = {"*"};
        supportedTypes = unmodifiableCollection(Arrays.asList(types));
    }
    static Collection<String> supportedOptions;
    static {
        String options[] = {""};
        supportedOptions = unmodifiableCollection(Arrays.asList(options));
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedTypes;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                               AnnotationProcessorEnvironment env) {
        return new ErrorAP(env);
    }
}
