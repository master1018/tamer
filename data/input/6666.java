public class Dee implements AnnotationProcessorFactory {
    static class DeeProc implements AnnotationProcessor {
        DeeProc(AnnotationProcessorEnvironment ape) {}
        public void process() {
            return;
        }
    }
    static Collection<String> supportedTypes;
    static {
        String types[] = {"dum"};
        supportedTypes = Collections.unmodifiableCollection(Arrays.asList(types));
    }
    static Collection<String> supportedOptions;
    static {
        String options[] = {""};
        supportedOptions = Collections.unmodifiableCollection(Arrays.asList(options));
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedTypes;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                        AnnotationProcessorEnvironment env) {
        return new DeeProc(env);
    }
}
