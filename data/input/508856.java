final class DexEncodedValueImpl implements DexEncodedValue {
    private final DexBuffer buffer;
    private byte typeAndValueArg;
    private DexEncodedValueType type;
    private String[] stringPool;
    private Object value;
    private int[] typeIds;
    private final FieldIdItem[] fieldIdItems;
    private final DexAnnotation annotation;
    public DexEncodedValueImpl(DexBuffer buffer, DexAnnotation annotation,
            int[] typeIds, String[] stringPool, FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.annotation = annotation;
        this.typeIds = typeIds;
        this.stringPool = stringPool;
        this.fieldIdItems = fieldIdItems;
        parseValue();
    }
    private void parseValue() {
        typeAndValueArg = buffer.readUByte();
        type = DexEncodedValueType.get(typeAndValueArg);
        int valueArg = DexEncodedValueType.valueArg(typeAndValueArg);
        switch (type) {
        case VALUE_BYTE:
            value = getByteValue(valueArg);
            break;
        case VALUE_SHORT:
            value = getShortValue(valueArg);
            break;
        case VALUE_CHAR:
            value = getCharValue(valueArg);
            break;
        case VALUE_INT:
            value = getIntValue(valueArg);
            break;
        case VALUE_LONG:
            value = getLongValue(valueArg);
            break;
        case VALUE_FLOAT:
            value = getFloatValue(valueArg);
            break;
        case VALUE_DOUBLE:
            value = getDoubleValue(valueArg);
            break;
        case VALUE_STRING:
            value = getStringValue(valueArg);
            break;
        case VALUE_TYPE:
            value = getTypeValue(valueArg);
            break;
        case VALUE_FIELD:
            value = getFieldValue(valueArg);
            break;
        case VALUE_METHOD:
            value = getMethodValue(valueArg);
            break;
        case VALUE_ENUM:
            value = getEnumValue(valueArg);
            break;
        case VALUE_ARRAY:
            value = getArrayValue(valueArg);
            break;
        case VALUE_ANNOTATION:
            value = getAnnotationValue(valueArg);
            break;
        case VALUE_NULL:
            value = getNullValue(valueArg);
            break;
        case VALUE_BOOLEAN:
            value = getBooleanValue(valueArg);
            break;
        default:
            throw new IllegalArgumentException("DexEncodedValueType " + type
                    + " not recognized");
        }
    }
    private Boolean getBooleanValue(int valueArg) {
        return valueArg == 1;
    }
    private Object getNullValue(int valueArg) {
        return null; 
    }
    private Object getAnnotationValue(int valueArg) {
        return new DexEncodedAnnotationImpl(buffer, annotation, typeIds,
                stringPool, fieldIdItems);
    }
    private List<DexEncodedValue> getArrayValue(int valueArg) {
        int size = buffer.readUleb128();
        List<DexEncodedValue> values = new ArrayList<DexEncodedValue>(size);
        for (int i = 0; i < size; i++) {
            values.add(new DexEncodedValueImpl(buffer, annotation, typeIds,
                    stringPool, fieldIdItems));
        }
        return values;
    }
    private Object getEnumValue(int valueArg) {
        int fieldOffset = buffer.readInt(valueArg + 1);
        FieldIdItem fieldIdItem = fieldIdItems[fieldOffset];
        String constantName = stringPool[fieldIdItem.name_idx];
        String typeName = stringPool[typeIds[fieldIdItem.type_idx]];
        return typeName + "!" + constantName;
    }
    private Object getMethodValue(int valueArg) {
        buffer.skip(valueArg + 1);
        return null;
    }
    private Object getFieldValue(int valueArg) {
        int fieldOffset = buffer.readInt(valueArg + 1);
        FieldIdItem fieldIdItem = fieldIdItems[fieldOffset];
        String fieldName = stringPool[fieldIdItem.name_idx];
        String typeName = stringPool[typeIds[fieldIdItem.type_idx]];
        return typeName + "!" + fieldName;
    }
    private Object getTypeValue(int valueArg) {
        valueArg++; 
        return stringPool[typeIds[buffer.readInt(valueArg)]];
    }
    private Object getStringValue(int valueArg) {
        valueArg++;
        return stringPool[buffer.readInt(valueArg)];
    }
    private Object getDoubleValue(int valueArg) {
        return buffer.readDouble(valueArg + 1);
    }
    private Float getFloatValue(int valueArg) {
        return buffer.readFloat(valueArg + 1);
    }
    private Long getLongValue(int valueArg) {
        return buffer.readLong(valueArg + 1);
    }
    private Integer getIntValue(int valueArg) {
        return buffer.readInt(valueArg + 1);
    }
    private Character getCharValue(int valueArg) {
        return buffer.readChar(valueArg + 1);
    }
    private Short getShortValue(int valueArg) {
        return buffer.readShort(valueArg + 1);
    }
    private Byte getByteValue(int valueArg) {
        assert valueArg == 0 : "Illegal valueArg for VALUE_BYTE: " + valueArg;
        return null;
    }
    public DexEncodedValueType getType() {
        return type;
    }
    public Object getValue() {
        return value;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("=");
        if (type == VALUE_ARRAY) {
            if (getValue() instanceof List<?>) {
                List<?> values = (List<?>) getValue();
                for (Object object : values) {
                    DexEncodedValue val = (DexEncodedValue) object;
                    builder.append(val.getValue());
                }
            }
        } else {
            builder.append(getValue());
        }
        return builder.toString();
    }
}
