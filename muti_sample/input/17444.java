public class FreshnessApf implements AnnotationProcessorFactory {
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("*"));
    private static final Collection<String> supportedOptions = emptySet();
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env) {
        return new FreshnessAp(env);
    }
    private static class FreshnessAp implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        FreshnessAp(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            System.out.println("Testing for freshness.");
            boolean empty = true;
            for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations()) {
                for (FieldDeclaration fieldDecl: typeDecl.getFields() ) {
                    empty = false;
                    System.out.println(typeDecl.getQualifiedName() +
                                       "." + fieldDecl.getSimpleName());
                    System.out.println(((DeclaredType) fieldDecl.getType()).getDeclaration().getAnnotationMirrors());
                    if (((DeclaredType) fieldDecl.getType()).getDeclaration().getAnnotationMirrors().size() == 0)
                        env.getMessager().printError("Expected an annotation.");
                }
            }
            if (empty)
                env.getMessager().printError("No fields encountered.");
        }
    }
}
