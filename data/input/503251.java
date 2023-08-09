public abstract class StdMember implements Member {
    private final CstType definingClass;
    private final int accessFlags;
    private final CstNat nat;
    private final AttributeList attributes;
    public StdMember(CstType definingClass, int accessFlags, CstNat nat,
                     AttributeList attributes) {
        if (definingClass == null) {
            throw new NullPointerException("definingClass == null");
        }
        if (nat == null) {
            throw new NullPointerException("nat == null");
        }
        if (attributes == null) {
            throw new NullPointerException("attributes == null");
        }
        this.definingClass = definingClass;
        this.accessFlags = accessFlags;
        this.nat = nat;
        this.attributes = attributes;
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(getClass().getName());
        sb.append('{');
        sb.append(nat.toHuman());
        sb.append('}');
        return sb.toString();
    }
    public final CstType getDefiningClass() {
        return definingClass;
    }
    public final int getAccessFlags() {
        return accessFlags;
    }
    public final CstNat getNat() {
        return nat;
    }
    public final CstUtf8 getName() {
        return nat.getName();
    }
    public final CstUtf8 getDescriptor() {
        return nat.getDescriptor();
    }
    public final AttributeList getAttributes() {
        return attributes;
    }
}
