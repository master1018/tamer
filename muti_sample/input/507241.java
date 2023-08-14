public final class CstString
        extends TypedConstant {
    private final CstUtf8 string;
    public CstString(CstUtf8 string) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        this.string = string;
    }
    public CstString(String string) {
        this(new CstUtf8(string));
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CstString)) {
            return false;
        }
        return string.equals(((CstString) other).string);
    }
    @Override
    public int hashCode() {
        return string.hashCode();
    }
    @Override
    protected int compareTo0(Constant other) {
        return string.compareTo(((CstString) other).string);
    }
    @Override
    public String toString() {
        return "string{" + toHuman() + '}';
    }
    public Type getType() {
        return Type.STRING;
    }
    @Override
    public String typeName() {
        return "string";
    }
    @Override
    public boolean isCategory2() {
        return false;
    }
    public String toHuman() {
        return string.toQuoted();
    }
    public CstUtf8 getString() {
        return string;
    }
}
