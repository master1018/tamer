public class Round2Apf implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("Round2"));
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
        return new Round2Ap(env, atds.size() == 0);
    }
    private static class Round2Ap implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        private final boolean empty;
        Round2Ap(AnnotationProcessorEnvironment env, boolean empty) {
            this.env = env;
            this.empty = empty;
        }
        public void process() {
            Round2Apf.round++;
            Filer f = env.getFiler();
            try {
                f.createSourceFile("Dummy2").println("@Round2 class Dummy2{}");
                throw new RuntimeException("Duplicate file creation allowed");
            } catch (IOException io) {}
            try {
                f.createTextFile(Filer.Location.SOURCE_TREE,
                                 "",
                                 new File("foo.txt"),
                                 null).println("xxyzzy");
                throw new RuntimeException("Duplicate file creation allowed");
            } catch (IOException io) {}
            try {
                f.createClassFile("Vacant");
                throw new RuntimeException("Duplicate file creation allowed");
            } catch (IOException io) {}
            try {
                f.createBinaryFile(Filer.Location.CLASS_TREE,
                                   "",
                                   new File("onezero"));
                throw new RuntimeException("Duplicate file creation allowed");
            } catch (IOException io) {}
            try {
                if (!empty) {
                    f.createClassFile("Dummy2");
                    f.createSourceFile("Vacant").println("class Vacant{}");
                    f.createSourceFile("Dummy3").println("@Round3 class Dummy3{}");
                    f.createClassFile("Dummy3");
                }
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
            System.out.println("Round2Apf: " + round);
        }
    }
}
