public final class EncodedField extends EncodedMember
        implements Comparable<EncodedField> {
    private final CstFieldRef field;
    public EncodedField(CstFieldRef field, int accessFlags) {
        super(accessFlags);
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        this.field = field;
    }
    public int hashCode() {
        return field.hashCode();
    }
    public boolean equals(Object other) {
        if (! (other instanceof EncodedField)) {
            return false;
        }
        return compareTo((EncodedField) other) == 0;
    }
    public int compareTo(EncodedField other) {
        return field.compareTo(other.field);
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(getClass().getName());
        sb.append('{');
        sb.append(Hex.u2(getAccessFlags()));
        sb.append(' ');
        sb.append(field);
        sb.append('}');
        return sb.toString();
    }
    @Override
    public void addContents(DexFile file) {
        FieldIdsSection fieldIds = file.getFieldIds();
        fieldIds.intern(field);
    }
    @Override
    public CstUtf8 getName() {
        return field.getNat().getName();
    }
    public String toHuman() {
        return field.toHuman();
    }
    @Override
    public void debugPrint(PrintWriter out, boolean verbose) {
        out.println(toString());
    }
    public CstFieldRef getRef() {
        return field;
    }
    @Override
    public int encode(DexFile file, AnnotatedOutput out, 
            int lastIndex, int dumpSeq) {
        int fieldIdx = file.getFieldIds().indexOf(field);
        int diff = fieldIdx - lastIndex;
        int accessFlags = getAccessFlags();
        if (out.annotates()) {
            out.annotate(0, String.format("  [%x] %s", dumpSeq,
                            field.toHuman()));
            out.annotate(Leb128Utils.unsignedLeb128Size(diff),
                    "    field_idx:    " + Hex.u4(fieldIdx));
            out.annotate(Leb128Utils.unsignedLeb128Size(accessFlags),
                    "    access_flags: " +
                    AccessFlags.fieldString(accessFlags));
        }
        out.writeUnsignedLeb128(diff);
        out.writeUnsignedLeb128(accessFlags);
        return fieldIdx;
    }
}
