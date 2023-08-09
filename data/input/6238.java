public class NullAPF implements AnnotationProcessorFactory {
    static class NullAP implements AnnotationProcessor {
        NullAP(AnnotationProcessorEnvironment ape) {
        }
        public void process() {
            return;
        }
    }
    static Collection<String> supportedTypes;
    static {
        String types[] = {"*"};
        supportedTypes = java.util.Arrays.asList(types);
    }
    public Collection<String> supportedOptions() {
        return java.util.Collections.emptySet();
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedTypes;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                        AnnotationProcessorEnvironment env) {
        return new NullAP(env);
    }
}
