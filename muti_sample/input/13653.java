public class Round3Apf implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("Round3"));
    private static final Collection<String> supportedOptions = emptySet();
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    private static int round = 0;
    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env) {
        return new Round3Ap(env, atds.size() == 0);
    }
    private static class Round3Ap implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        private final boolean empty;
        Round3Ap(AnnotationProcessorEnvironment env, boolean empty) {
            this.env = env;
            this.empty = empty;
        }
        public void process() {
            Round3Apf.round++;
            try {
                if (!empty)
                    env.getFiler().createSourceFile("Dummy4").println("@Round4 class Dummy4{}");
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
            System.out.println("Round3Apf: " + round);
        }
    }
}
