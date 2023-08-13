public class DexParameterImpl implements DexParameter {
    private final String typeName;
    private final Integer annotationOffset;
    private Set<DexAnnotation> annotations;
    private final DexBuffer buffer;
    private final int[] typeIds;
    private final String[] stringPool;
    private final FieldIdItem[] fieldIdItems;
    public DexParameterImpl(DexBuffer buffer, String typeName,
            Integer annotationOffset, int[] typeIds, String[] stringPool,
            FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.typeName = typeName;
        this.annotationOffset = annotationOffset;
        this.typeIds = typeIds;
        this.stringPool = stringPool;
        this.fieldIdItems = fieldIdItems;
        parseAnnotations();
    }
    private void parseAnnotations() {
        annotations = new HashSet<DexAnnotation>();
        if (annotationOffset != null) {
            buffer.setPosition(annotationOffset);
            final int size = buffer.readUInt();
            for (int i = 0; i < size; i++) {
                annotations.add(new DexAnnotationImpl(buffer.createCopy(),
                        buffer.readUInt(), typeIds, stringPool, fieldIdItems));
            }
        }
    }
    public String getTypeName() {
        return typeName;
    }
    public Set<DexAnnotation> getAnnotations() {
        return annotations;
    }
    @Override
    public String toString() {
        return getTypeName();
    }
}
