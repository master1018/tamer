public abstract class CstBaseMethodRef
        extends CstMemberRef {
    private final Prototype prototype;
    private Prototype instancePrototype;
     CstBaseMethodRef(CstType definingClass, CstNat nat) {
        super(definingClass, nat);
        String descriptor = getNat().getDescriptor().getString();
        this.prototype = Prototype.intern(descriptor);
        this.instancePrototype = null;
    }
    public final Prototype getPrototype() {
        return prototype;
    }
    public final Prototype getPrototype(boolean isStatic) {
        if (isStatic) {
            return prototype;
        } else {
            if (instancePrototype == null) {
                Type thisType = getDefiningClass().getClassType();
                instancePrototype = prototype.withFirstParameter(thisType);
            }
            return instancePrototype;
        }
    }
    @Override
    protected final int compareTo0(Constant other) {
        int cmp = super.compareTo0(other);
        if (cmp != 0) {
            return cmp;
        }
        CstBaseMethodRef otherMethod = (CstBaseMethodRef) other;
        return prototype.compareTo(otherMethod.prototype);
    }
    public final Type getType() {
        return prototype.getReturnType();
    }
    public final int getParameterWordCount(boolean isStatic) {
        return getPrototype(isStatic).getParameterTypes().getWordCount();
    }
    public final boolean isInstanceInit() {
        return getNat().isInstanceInit();
    }
    public final boolean isClassInit() {
        return getNat().isClassInit();
    }
}
