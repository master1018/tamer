public class StaticApf implements AnnotationProcessorFactory {
    static int round = -1;
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("*"));
    private static final Collection<String> supportedOptions = emptySet();
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                               AnnotationProcessorEnvironment env) {
        return new StaticAp(env);
    }
    private static class StaticAp implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        StaticAp(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            int size = env.getSpecifiedTypeDeclarations().size();
            try {
                round++;
                switch (size) {
                case 0:
                    if (round == 0) {
                        env.getFiler().createSourceFile("Round1").print("class Round1 {}");
                    } else
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    break;
                case 1:
                    if (round == 1) {
                        env.getFiler().createSourceFile("AhOne").print("class AhOne {}");
                        env.getFiler().createSourceFile("AndAhTwo").print("class AndAhTwo {}");
                        env.getFiler().createClassFile("Foo");
                    } else
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    break;
                case 2:
                    if (round != 2) {
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    }
                    break;
                }
            } catch (java.io.IOException ioe) {
                    throw new RuntimeException();
                }
            }
    }
}
