final class DexAnnotationImpl implements DexAnnotation {
    private int offset;
    private DexBuffer buffer;
    private int[] typeIds;
    private String[] stringPool;
    private Visibility visibility;
    private DexEncodedAnnotationImpl encodedAnnotation;
    private TypeFormatter formatter = new TypeFormatter();
    private final FieldIdItem[] fieldIdItems;
    public DexAnnotationImpl(DexBuffer buffer, int offset, int[] typeIds,
            String[] stringPool, FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.offset = offset;
        this.typeIds = typeIds;
        this.stringPool = stringPool;
        this.fieldIdItems = fieldIdItems;
        parseAnnotations();
    }
    private void parseAnnotations() {
        buffer.setPosition(offset);
        visibility = Visibility.get(buffer.readUByte());
        encodedAnnotation = new DexEncodedAnnotationImpl(buffer, this, typeIds,
                stringPool, fieldIdItems);
    }
    public List<DexAnnotationAttribute> getAttributes() {
        return encodedAnnotation.getValue();
    }
    public String getTypeName() {
        return encodedAnnotation.getTypeName();
    }
    public Visibility getVisibility() {
        return visibility;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("@");
        builder.append(formatter.format(encodedAnnotation.getTypeName()));
        if (!getAttributes().isEmpty()) {
            builder.append(" (");
            for (DexAnnotationAttribute value : getAttributes()) {
                builder.append(value.toString());
                builder.append(" ");
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
