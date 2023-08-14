public class TestProcessorFactory implements AnnotationProcessorFactory {
    public Collection<String> supportedOptions() {
        return new ArrayList<String>();
    }
    public Collection<String> supportedAnnotationTypes() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("Test");
        res.add("Ignore");
        return res;
    }
    public AnnotationProcessor getProcessorFor(
                                        Set<AnnotationTypeDeclaration> as,
                                        AnnotationProcessorEnvironment env) {
        Tester tester = Tester.activeTester;
        try {
            ClassDeclaration testerDecl = null;
            for (TypeDeclaration decl : env.getSpecifiedTypeDeclarations()) {
                if (decl.getQualifiedName().equals(
                                               tester.getClass().getName())) {
                    testerDecl = (ClassDeclaration) decl;
                    break;
                }
            }
            tester.thisClassDecl = testerDecl;
            tester.env = env;
            tester.init();
            return new TestProcessor(env, tester);
        } catch (Exception e) {
            throw new Error("Couldn't create test annotation processor", e);
        }
    }
}
