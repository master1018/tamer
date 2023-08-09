final class DexAnnotationAttributeImpl implements
        DexAnnotationAttribute {
    int nameIdx; 
    DexEncodedValue value;
    private String[] stringPool;
    private DexBuffer buffer;
    private final int[] typeIds;
    private final FieldIdItem[] fieldIdItems;
    private final DexAnnotation annotation;
    public DexAnnotationAttributeImpl(DexBuffer buffer,
            DexAnnotation annotation, int[] typeIds, String[] stringPool,
            FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.annotation = annotation;
        this.typeIds = typeIds;
        this.stringPool = stringPool;
        this.fieldIdItems = fieldIdItems;
        parseValue();
    }
    private void parseValue() {
        nameIdx = buffer.readUleb128();
        value = new DexEncodedValueImpl(buffer, annotation, typeIds,
                stringPool, fieldIdItems);
    }
    public String getName() {
        return stringPool[nameIdx];
    }
    public DexEncodedValue getEncodedValue() {
        return value;
    }
    @Override
    public String toString() {
        return getName() + " " + getEncodedValue();
    }
    public DexAnnotation getAnnotation() {
        return annotation;
    }
}
