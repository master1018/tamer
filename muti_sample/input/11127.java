public class T6194785 extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment)
    {
        class Scan extends ElementScanner7<Void,Void> {
            @Override
            public Void visitExecutable(ExecutableElement e, Void ignored) {
                for (VariableElement p : e.getParameters())
                    if ("arg0".equals(p.getSimpleName().toString()))
                        throw new AssertionError(e);
                return null;
            }
        }
        Scan scan = new Scan();
        for (Element e : roundEnvironment.getRootElements()) {
            scan.scan(e);
        }
        return true;
    }
}
