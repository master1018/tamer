public class Counter implements AnnotationProcessorFactory {
    static class CounterProc implements AnnotationProcessor {
        static class CountingVisitor extends SimpleDeclarationVisitor {
            int count;
            int count() {
                return count;
            }
            CountingVisitor() {
                count = 0;
            }
            public void visitDeclaration(Declaration d) {
                count++;
                System.out.println(d.getSimpleName());
            }
        }
        AnnotationProcessorEnvironment env;
        CounterProc(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            for(TypeDeclaration td: env.getSpecifiedTypeDeclarations() ) {
                CountingVisitor sourceOrder = new CountingVisitor();
                CountingVisitor someOrder = new CountingVisitor();
                System.out.println("Source Order Scanner");
                td.accept(getSourceOrderDeclarationScanner(sourceOrder,
                                                           NO_OP));
                System.out.println("\nSome Order Scanner");
                td.accept(getDeclarationScanner(someOrder,
                                                NO_OP));
                if (sourceOrder.count() != someOrder.count() )
                    throw new RuntimeException("Counts from different scanners don't agree");
            }
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
        return new CounterProc(env);
    }
}
