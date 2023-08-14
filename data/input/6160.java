public class Round4Apf implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("Round4"));
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
        return new Round4Ap(env, atds.size() == 0);
    }
    private static class Round4Ap implements AnnotationProcessor, RoundCompleteListener {
        private final AnnotationProcessorEnvironment env;
        private final boolean empty;
        Round4Ap(AnnotationProcessorEnvironment env, boolean empty) {
            this.env = env;
            this.empty = empty;
        }
        public void process() {
            Round4Apf.round++;
            try {
                if (!empty)
                    env.getFiler().createSourceFile("Dummy5").println("@Round5 class Dummy5{}");
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
            System.out.println("Round4Apf: " + round);
            env.addListener(this);
        }
        public void roundComplete(RoundCompleteEvent event) {
            RoundState rs = event.getRoundState();
            System.out.println("\t" + rs.toString());
            System.out.println("Round4Apf: " + round + " complete");
            if (rs.finalRound()) {
                System.out.println("Valediction");
            }
        }
    }
}
