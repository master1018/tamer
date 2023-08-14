final class DexEncodedAnnotationImpl implements
        DexEncodedAnnotation {
    private List<DexAnnotationAttribute> values;
    private final DexBuffer buffer;
    private final int[] typeIds;
    private final String[] stringPool;
    private int typeIdx;
    private final FieldIdItem[] fieldIdItems;
    private final DexAnnotation annotation;
    public DexEncodedAnnotationImpl(DexBuffer buffer, DexAnnotation annotation,
            int[] typeIds, String[] stringPool, FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.annotation = annotation;
        this.typeIds = typeIds;
        this.stringPool = stringPool;
        this.fieldIdItems = fieldIdItems;
        parseEncodedAnnotation();
    }
    private void parseEncodedAnnotation() {
        typeIdx = buffer.readUleb128();
        int size = buffer.readUleb128();
        values = new ArrayList<DexAnnotationAttribute>(size);
        for (int j = 0; j < size; j++) {
            values.add(new DexAnnotationAttributeImpl(buffer, annotation,
                    typeIds, stringPool, fieldIdItems));
        }
    }
    public DexEncodedValueType getType() {
        return DexEncodedValueType.VALUE_ANNOTATION;
    }
    public List<DexAnnotationAttribute> getValue() {
        return values;
    }
    public String getTypeName() {
        return stringPool[typeIds[typeIdx]];
    }
    @Override
    public String toString() {
        return getTypeName() + ":" + getValue();
    }
}
