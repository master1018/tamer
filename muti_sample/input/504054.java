public class SigClassReference implements IClassReference, Serializable {
    private final IClassDefinition definition;
    public SigClassReference(IClassDefinition definition) {
        this.definition = definition;
    }
    public IClassDefinition getClassDefinition() {
        return definition;
    }
    @Override
    public boolean equals(Object obj) {
        return SigClassReference.equals(this, obj);
    }
    public static boolean equals(IClassReference thiz, Object that) {
        if (that instanceof IClassReference) {
            return thiz.getClassDefinition().equals(
                    ((IClassReference) that).getClassDefinition());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return SigClassReference.hashCode(this);
    }
    public static int hashCode(IClassReference thiz) {
        return thiz.getClassDefinition().hashCode();
    }
    @Override
    public String toString() {
        return SigClassReference.toString(this);
    }
    public static String toString(IClassReference thiz) {
        return "-> " + thiz.getClassDefinition().getQualifiedName();
    }
}
