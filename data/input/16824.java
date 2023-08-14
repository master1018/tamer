abstract public class TypeComponentImpl extends MirrorImpl
    implements TypeComponent {
    protected final ReferenceTypeImpl declaringType;
    protected String signature;
    TypeComponentImpl(VirtualMachine vm, ReferenceTypeImpl declaringType) {
        super(vm);
        this.declaringType = declaringType;
    }
    public ReferenceType declaringType() {
        return declaringType;
    }
    public String signature() {
        return signature;
    }
    abstract public String name();
    abstract public int modifiers();
    abstract public boolean isPackagePrivate();
    abstract public boolean isPrivate();
    abstract public boolean isProtected();
    abstract public boolean isPublic();
    abstract public boolean isStatic();
    abstract public boolean isFinal();
    abstract public int hashCode();
}
