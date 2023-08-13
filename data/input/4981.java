public class GetTypeElemBadArg extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> tes,
                           RoundEnvironment round) {
        if (round.processingOver()) return true;
        TypeElement te = elements.getTypeElement("Superless");
        tellAbout(te);
        te = elements.getTypeElement("Bo.o.o.gus");
        if (te != null) {
            tellAbout(te);
            throw new AssertionError();
        }
        return true;
    }
    private static void tellAbout(TypeElement t) {
        System.out.println(t);
        System.out.println(t.getClass());
        System.out.println(t.getKind());
        System.out.println();
    }
}
