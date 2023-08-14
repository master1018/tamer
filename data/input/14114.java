public class NoSupers extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> tes,
                           RoundEnvironment round) {
        if (round.processingOver()) return true;
        PrimitiveType intType = types.getPrimitiveType(TypeKind.INT);
        if (! types.directSupertypes(intType).isEmpty())
            throw new AssertionError();
        return true;
    }
}
