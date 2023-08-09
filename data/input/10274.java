public class TestGetTypeDeclarationApf implements AnnotationProcessorFactory {
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
        return new TestGetTypeDeclarationAp(env);
    }
    private static class TestGetTypeDeclarationAp implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        TestGetTypeDeclarationAp(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            String classNames[] = {
                "java.lang.String",             
                "java.lang.Thread.State",       
                "java.util.Collection",
                "java.util.Map.Entry",
                "foo.bar.Baz.Xxyzzy.Wombat",
                "foo.bar.Baz.Xxyzzy",
                "foo.bar.Baz",
                "foo.bar.Quux",
                "foo.bar.Quux.Xxyzzy",
                "foo.bar.Quux.Xxyzzy.Wombat",
                "NestedClassAnnotations",
                "NestedClassAnnotations.NestedClass",
            };
            for(String className: classNames) {
                TypeDeclaration t = env.getTypeDeclaration(className);
                if (t == null)
                    throw new RuntimeException("No declaration found for " + className);
                if (! t.getQualifiedName().equals(className))
                    throw new RuntimeException("Class with wrong name found for " + className);
            }
            String nonuniqueCanonicalNames[] = {
                "p1.p2.C1",
            };
            for(String className: nonuniqueCanonicalNames) {
                ClassDeclaration c1 = (ClassDeclaration) env.getTypeDeclaration(className);
                ClassDeclaration c2 = (ClassDeclaration) c1.getDeclaringType();
                PackageDeclaration p     = env.getPackage("p1");
                if (!p.equals(c1.getPackage())  ||
                    c2 == null ||
                    !"C1".equals(c1.getSimpleName())) {
                    throw new RuntimeException("Bad class declaration");
                }
            }
            String notClassNames[] = {
                "",
                "XXYZZY",
                "java",
                "java.lang",
                "java.lang.Bogogogous",
                "1",
                "1.2",
                "3.14159",
                "To be or not to be is a tautology",
                "1+2=3",
                "foo+.x",
                "foo+x",
                "+",
                "?",
                "***",
                "java.*",
            };
            for(String notClassName: notClassNames) {
                Declaration t = env.getTypeDeclaration(notClassName);
                if (t != null) {
                    System.err.println("Unexpected declaration:" + t);
                    throw new RuntimeException("Declaration found for ``" + notClassName + "''.");
                }
            }
        }
    }
}
