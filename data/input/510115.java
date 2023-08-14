public final class AnnotationParser {
    private final DirectClassFile cf;
    private final ConstantPool pool;
    private final ByteArray bytes;
    private final ParseObserver observer;
    private final ByteArray.MyDataInputStream input;
    private int parseCursor;
    public AnnotationParser(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (cf == null) {
            throw new NullPointerException("cf == null");
        }
        this.cf = cf;
        this.pool = cf.getConstantPool();
        this.observer = observer;
        this.bytes = cf.getBytes().slice(offset, offset + length);
        this.input = bytes.makeDataInputStream();
        this.parseCursor = 0;
    }
    public Constant parseValueAttribute() {
        Constant result;
        try {
            result = parseValue();
            if (input.available() != 0) {
                throw new ParseException("extra data in attribute");
            }
        } catch (IOException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
        return result;
    }
    public AnnotationsList parseParameterAttribute(
            AnnotationVisibility visibility) {
        AnnotationsList result;
        try {
            result = parseAnnotationsList(visibility);
            if (input.available() != 0) {
                throw new ParseException("extra data in attribute");
            }
        } catch (IOException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
        return result;
    }
    public Annotations parseAnnotationAttribute(
            AnnotationVisibility visibility) {
        Annotations result;
        try {
            result = parseAnnotations(visibility);
            if (input.available() != 0) {
                throw new ParseException("extra data in attribute");
            }
        } catch (IOException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
        return result;
    }
    private AnnotationsList parseAnnotationsList(
            AnnotationVisibility visibility) throws IOException {
        int count = input.readUnsignedByte();
        if (observer != null) {
            parsed(1, "num_parameters: " + Hex.u1(count));
        }
        AnnotationsList outerList = new AnnotationsList(count);
        for (int i = 0; i < count; i++) {
            if (observer != null) {
                parsed(0, "parameter_annotations[" + i + "]:");
                changeIndent(1);
            }
            Annotations annotations = parseAnnotations(visibility);
            outerList.set(i, annotations);
            if (observer != null) {
                observer.changeIndent(-1);
            }
        }
        outerList.setImmutable();
        return outerList;
    }
    private Annotations parseAnnotations(AnnotationVisibility visibility)
            throws IOException {
        int count = input.readUnsignedShort();
        if (observer != null) {
            parsed(2, "num_annotations: " + Hex.u2(count));
        }
        Annotations annotations = new Annotations();
        for (int i = 0; i < count; i++) {
            if (observer != null) {
                parsed(0, "annotations[" + i + "]:");
                changeIndent(1);
            }
            Annotation annotation = parseAnnotation(visibility);
            annotations.add(annotation);
            if (observer != null) {
                observer.changeIndent(-1);
            }
        }
        annotations.setImmutable();
        return annotations;
    }
    private Annotation parseAnnotation(AnnotationVisibility visibility)
            throws IOException {
        requireLength(4);
        int typeIndex = input.readUnsignedShort();
        int numElements = input.readUnsignedShort();
        CstUtf8 typeUtf8 = (CstUtf8) pool.get(typeIndex);
        CstType type = new CstType(Type.intern(typeUtf8.getString()));
        if (observer != null) {
            parsed(2, "type: " + type.toHuman());
            parsed(2, "num_elements: " + numElements);
        }
        Annotation annotation = new Annotation(type, visibility);
        for (int i = 0; i < numElements; i++) {
            if (observer != null) {
                parsed(0, "elements[" + i + "]:");
                changeIndent(1);
            }
            NameValuePair element = parseElement();
            annotation.add(element);
            if (observer != null) {
                changeIndent(-1);
            }
        }
        annotation.setImmutable();
        return annotation;
    }
    private NameValuePair parseElement() throws IOException {
        requireLength(5);
        int elementNameIndex = input.readUnsignedShort();
        CstUtf8 elementName = (CstUtf8) pool.get(elementNameIndex);
        if (observer != null) {
            parsed(2, "element_name: " + elementName.toHuman());
            parsed(0, "value: ");
            changeIndent(1);
        }
        Constant value = parseValue();
        if (observer != null) {
            changeIndent(-1);
        }
        return new NameValuePair(elementName, value);
    }
    private Constant parseValue() throws IOException {
        int tag = input.readUnsignedByte();
        if (observer != null) {
            CstUtf8 humanTag = new CstUtf8(Character.toString((char) tag));
            parsed(1, "tag: " + humanTag.toQuoted());
        }
        switch (tag) {
            case 'B': {
                CstInteger value = (CstInteger) parseConstant();
                return CstByte.make(value.getValue());
            }
            case 'C': {
                CstInteger value = (CstInteger) parseConstant();
                int intValue = value.getValue();
                return CstChar.make(value.getValue());
            }
            case 'D': {
                CstDouble value = (CstDouble) parseConstant();
                return value;
            }
            case 'F': {
                CstFloat value = (CstFloat) parseConstant();
                return value;
            }
            case 'I': {
                CstInteger value = (CstInteger) parseConstant();
                return value;
            }
            case 'J': {
                CstLong value = (CstLong) parseConstant();
                return value;
            }
            case 'S': {
                CstInteger value = (CstInteger) parseConstant();
                return CstShort.make(value.getValue());
            }
            case 'Z': {
                CstInteger value = (CstInteger) parseConstant();
                return CstBoolean.make(value.getValue());
            }
            case 'c': {
                int classInfoIndex = input.readUnsignedShort();
                CstUtf8 value = (CstUtf8) pool.get(classInfoIndex);
                Type type = Type.internReturnType(value.getString());
                if (observer != null) {
                    parsed(2, "class_info: " + type.toHuman());
                }
                return new CstType(type);
            }
            case 's': {
                CstString value = new CstString((CstUtf8) parseConstant());
                return value;
            }
            case 'e': {
                requireLength(4);
                int typeNameIndex = input.readUnsignedShort();
                int constNameIndex = input.readUnsignedShort();
                CstUtf8 typeName = (CstUtf8) pool.get(typeNameIndex);
                CstUtf8 constName = (CstUtf8) pool.get(constNameIndex);
                if (observer != null) {
                    parsed(2, "type_name: " + typeName.toHuman());
                    parsed(2, "const_name: " + constName.toHuman());
                }
                return new CstEnumRef(new CstNat(constName, typeName));
            }
            case '@': {
                Annotation annotation =
                    parseAnnotation(AnnotationVisibility.EMBEDDED);
                return new CstAnnotation(annotation);
            }
            case '[': {
                requireLength(2);
                int numValues = input.readUnsignedShort();
                CstArray.List list = new CstArray.List(numValues);
                if (observer != null) {
                    parsed(2, "num_values: " + numValues);
                    changeIndent(1);
                }
                for (int i = 0; i < numValues; i++) {
                    if (observer != null) {
                        changeIndent(-1);
                        parsed(0, "element_value[" + i + "]:");
                        changeIndent(1);
                    }
                    list.set(i, parseValue());
                }
                if (observer != null) {
                    changeIndent(-1);
                }
                list.setImmutable();
                return new CstArray(list);
            }
            default: {
                throw new ParseException("unknown annotation tag: " +
                        Hex.u1(tag));
            }
        }
    }
    private Constant parseConstant() throws IOException {
        int constValueIndex = input.readUnsignedShort();
        Constant value = (Constant) pool.get(constValueIndex);
        if (observer != null) {
            String human = (value instanceof CstUtf8) 
                ? ((CstUtf8) value).toQuoted() 
                : value.toHuman();
            parsed(2, "constant_value: " + human);
        }
        return value;
    }
    private void requireLength(int requiredLength) throws IOException {
        if (input.available() < requiredLength) {
            throw new ParseException("truncated annotation attribute");
        }
    }
    private void parsed(int length, String message) {
        observer.parsed(bytes, parseCursor, length, message);
        parseCursor += length;
    }
    private void changeIndent(int indent) {
        observer.changeIndent(indent);
    }
}
