public final class FieldAnnotationStruct
        implements ToHuman, Comparable<FieldAnnotationStruct> {
    private final CstFieldRef field;
    private AnnotationSetItem annotations;
    public FieldAnnotationStruct(CstFieldRef field,
            AnnotationSetItem annotations) {
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        if (annotations == null) {
            throw new NullPointerException("annotations == null");
        }
        this.field = field;
        this.annotations = annotations;
    }
    public int hashCode() {
        return field.hashCode();
    }
    public boolean equals(Object other) {
        if (! (other instanceof FieldAnnotationStruct)) {
            return false;
        }
        return field.equals(((FieldAnnotationStruct) other).field);
    }
    public int compareTo(FieldAnnotationStruct other) {
        return field.compareTo(other.field);
    }
    public void addContents(DexFile file) {
        FieldIdsSection fieldIds = file.getFieldIds();
        MixedItemSection wordData = file.getWordData();
        fieldIds.intern(field);
        annotations = wordData.intern(annotations);
    }
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int fieldIdx = file.getFieldIds().indexOf(field);
        int annotationsOff = annotations.getAbsoluteOffset();
        if (out.annotates()) {
            out.annotate(0, "    " + field.toHuman());
            out.annotate(4, "      field_idx:       " + Hex.u4(fieldIdx));
            out.annotate(4, "      annotations_off: " +
                    Hex.u4(annotationsOff));
        }
        out.writeInt(fieldIdx);
        out.writeInt(annotationsOff);
    }
    public String toHuman() {
        return field.toHuman() + ": " + annotations;
    }
    public CstFieldRef getField() {
        return field;
    }
    public Annotations getAnnotations() {
        return annotations.getAnnotations();
    }
}
