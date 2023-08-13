public class TestGetPackageApf implements AnnotationProcessorFactory {
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
        return new TestGetPackageAp(env);
    }
    private static class TestGetPackageAp implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        TestGetPackageAp(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            boolean failed = false;
            String packageNames[] = {
                "", 
                "java.lang.annotation",
                "java.lang",
                "java.util",
                "java.awt.image.renderable",
                "foo.bar",
                "foo",
                "p1",
            };
            for(String packageName: packageNames) {
                PackageDeclaration p = env.getPackage(packageName);
                if (p == null) {
                    failed = true;
                    System.err.println("ERROR: No declaration found for ``" + packageName + "''.");
                }
                else if (!packageName.equals(p.getQualifiedName())) {
                    failed = true;
                    System.err.println("ERROR: Unexpected package name; expected " + packageName +
                                       "got " + p.getQualifiedName());
                }
            }
            String notPackageNames[] = {
                "XXYZZY",
                "java.lang.String",
                "1",
                "1.2",
                "3.14159",
                "To be or not to be is a tautology",
                "1+2=3",
            };
            for(String notPackageName: notPackageNames) {
                PackageDeclaration p = env.getPackage(notPackageName);
                if (p != null) {
                    failed = true;
                    System.err.println("ERROR: Unexpected declaration: ``" + p + "''.");
                }
            }
            if (failed)
                throw new RuntimeException("Errors found testing getPackage.");
        }
    }
}
