final class DexMethodImpl implements DexMethod {
    private DexBuffer buffer;
    private MethodsIdItem methodsIdItem;
    private String[] stringPool;
    private int[] typeIds;
    private ProtIdItem protoIdItem;
    private List<DexParameter> parameters;
    private final int accessFlags;
    private final MethodAnnotation methodAnnotation;
    private Set<DexAnnotation> annotations;
    private final TypeFormatter formatter = new TypeFormatter();
    private final DexClass declaringClass;
    private final ParameterAnnotation parameterAnnotation;
    private Map<Integer, Integer> parameterIdToIndex;
    private final FieldIdItem[] fieldIdItems;
    public DexMethodImpl(DexBuffer buffer, DexClass declaringClass,
            MethodsIdItem methodsIdItem, ProtIdItem protoIdItem,
            int accessFlags, MethodAnnotation methodAnnotation,
            ParameterAnnotation parameterAnnotation, String[] stringPool,
            int[] typeIds, FieldIdItem[] fieldIdItems) {
        this.buffer = buffer;
        this.declaringClass = declaringClass;
        this.methodsIdItem = methodsIdItem;
        this.protoIdItem = protoIdItem;
        this.accessFlags = accessFlags;
        this.methodAnnotation = methodAnnotation;
        this.parameterAnnotation = parameterAnnotation;
        this.stringPool = stringPool;
        this.typeIds = typeIds;
        this.fieldIdItems = fieldIdItems;
        parseAnnotations();
        parseParameterAnnotations();
    }
    private void parseParameterAnnotations() {
        parameterIdToIndex = new HashMap<Integer, Integer>();
        if (parameterAnnotation != null) {
            buffer.setPosition(parameterAnnotation.annotationsOff);
            int numberOfParameters = buffer.readUInt();
            for (int i = 0; i < numberOfParameters; i++) {
                parameterIdToIndex.put(i, buffer.readUInt());
            }
        }
    }
    private void parseAnnotations() {
        annotations = new HashSet<DexAnnotation>();
        if (methodAnnotation != null) {
            buffer.setPosition(methodAnnotation.annotationsOff);
            final int size = buffer.readUInt();
            for (int i = 0; i < size; i++) {
                annotations.add(new DexAnnotationImpl(buffer.createCopy(),
                        buffer.readUInt(), typeIds, stringPool, fieldIdItems));
            }
        }
    }
    public String getName() {
        return stringPool[methodsIdItem.name_idx];
    }
    public String getReturnType() {
        return stringPool[typeIds[protoIdItem.return_type_idx]];
    }
    public synchronized List<DexParameter> getParameters() {
        if (parameters == null) {
            parameters = new LinkedList<DexParameter>();
            if (protoIdItem.parameter_off != 0) {
                buffer.setPosition(protoIdItem.parameter_off);
                int size = buffer.readUInt();
                int[] paramTypeIdx = new int[size];
                for (int i = 0; i < size; i++) {
                    paramTypeIdx[i] = buffer.readUShort();
                }
                for (int i = 0; i < paramTypeIdx.length; i++) {
                    parameters.add(new DexParameterImpl(buffer.createCopy(),
                            stringPool[typeIds[paramTypeIdx[i]]],
                            parameterIdToIndex.get(i), typeIds, stringPool,
                            fieldIdItems));
                }
            }
        }
        return parameters;
    }
    public int getModifiers() {
        return accessFlags;
    }
    public Set<DexAnnotation> getAnnotations() {
        return annotations;
    }
    public DexClass getDeclaringClass() {
        return declaringClass;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(formatter.formatAnnotations(getAnnotations()));
        builder.append(Modifier.toString(getModifiers()));
        builder.append(" ");
        builder.append(formatter.format(getReturnType()));
        builder.append(" ");
        builder.append(getName());
        builder.append("(");
        List<DexParameter> parameters = getParameters();
        for (DexParameter dexParameter : parameters) {
            builder.append(formatter.formatAnnotations(dexParameter
                    .getAnnotations()));
            builder.append(formatter.format(dexParameter.getTypeName()));
        }
        builder.append(")");
        return builder.toString();
    }
}
