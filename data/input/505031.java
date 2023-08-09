public class DirectClassFile implements ClassFile {
    private static final int CLASS_FILE_MAGIC = 0xcafebabe;
    private static final int CLASS_FILE_MIN_MAJOR_VERSION = 45;
    private static final int CLASS_FILE_MAX_MAJOR_VERSION = 50;
    private static final int CLASS_FILE_MAX_MINOR_VERSION = 0;
    private final String filePath;
    private final ByteArray bytes;
    private final boolean strictParse;
    private StdConstantPool pool;
    private int accessFlags;
    private CstType thisClass;
    private CstType superClass;
    private TypeList interfaces;
    private FieldList fields;
    private MethodList methods;
    private StdAttributeList attributes;
    private AttributeFactory attributeFactory;
    private ParseObserver observer;
    public static String stringOrNone(Object obj) {
        if (obj == null) {
            return "(none)";
        }
        return obj.toString();
    }
    public DirectClassFile(ByteArray bytes, String filePath,
                           boolean strictParse) {
        if (bytes == null) {
            throw new NullPointerException("bytes == null");
        }
        if (filePath == null) {
            throw new NullPointerException("filePath == null");
        }
        this.filePath = filePath;
        this.bytes = bytes;
        this.strictParse = strictParse;
        this.accessFlags = -1;
    }
    public DirectClassFile(byte[] bytes, String filePath,
                           boolean strictParse) {
        this(new ByteArray(bytes), filePath, strictParse);
    }
    public void setObserver(ParseObserver observer) {
        this.observer = observer;
    }
    public void setAttributeFactory(AttributeFactory attributeFactory) {
        if (attributeFactory == null) {
            throw new NullPointerException("attributeFactory == null");
        }
        this.attributeFactory = attributeFactory;
    }
    public ByteArray getBytes() {
        return bytes;
    }
    public int getMagic() {
        parseToInterfacesIfNecessary();
        return getMagic0();
    }
    public int getMinorVersion() {
        parseToInterfacesIfNecessary();
        return getMinorVersion0();
    }
    public int getMajorVersion() {
        parseToInterfacesIfNecessary();
        return getMajorVersion0();
    }
    public int getAccessFlags() {
        parseToInterfacesIfNecessary();
        return accessFlags;
    }
    public CstType getThisClass() {
        parseToInterfacesIfNecessary();
        return thisClass;
    }
    public CstType getSuperclass() {
        parseToInterfacesIfNecessary();
        return superClass;
    }
    public ConstantPool getConstantPool() {
        parseToInterfacesIfNecessary();
        return pool;
    }
    public TypeList getInterfaces() {
        parseToInterfacesIfNecessary();
        return interfaces;
    }
    public FieldList getFields() {
        parseToEndIfNecessary();
        return fields;
    }
    public MethodList getMethods() {
        parseToEndIfNecessary();
        return methods;
    }
    public AttributeList getAttributes() {
        parseToEndIfNecessary();
        return attributes;
    }
    public CstUtf8 getSourceFile() {
        AttributeList attribs = getAttributes();
        Attribute attSf = attribs.findFirst(AttSourceFile.ATTRIBUTE_NAME);
        if (attSf instanceof AttSourceFile) {
            return ((AttSourceFile) attSf).getSourceFile();
        }
        return null;
    }
    public TypeList makeTypeList(int offset, int size) {
        if (size == 0) {
            return StdTypeList.EMPTY;
        }
        if (pool == null) {
            throw new IllegalStateException("pool not yet initialized");
        }
        return new DcfTypeList(bytes, offset, size, pool, observer);
    }
    public int getMagic0() {
        return bytes.getInt(0);
    }
    public int getMinorVersion0() {
        return bytes.getUnsignedShort(4);
    }
    public int getMajorVersion0() {
        return bytes.getUnsignedShort(6);
    }
    private void parseToInterfacesIfNecessary() {
        if (accessFlags == -1) {
            parse();
        }
    }
    private void parseToEndIfNecessary() {
        if (attributes == null) {
            parse();
        }
    }
    private void parse() {
        try {
            parse0();
        } catch (ParseException ex) {
            ex.addContext("...while parsing " + filePath);
            throw ex;
        } catch (RuntimeException ex) {
            ParseException pe = new ParseException(ex);
            pe.addContext("...while parsing " + filePath);
            throw pe;
        }
    }
    private boolean isGoodVersion(int magic, int minorVersion,
            int majorVersion) {
        if (magic == CLASS_FILE_MAGIC && minorVersion >= 0) {
            if (majorVersion == CLASS_FILE_MAX_MAJOR_VERSION) {
                if (minorVersion <= CLASS_FILE_MAX_MINOR_VERSION) {
                    return true;
                }
            } else if (majorVersion < CLASS_FILE_MAX_MAJOR_VERSION &&
                       majorVersion >= CLASS_FILE_MIN_MAJOR_VERSION) {
                return true;
            }
        }
        return false;
    }
    private void parse0() {
        if (bytes.size() < 10) {
            throw new ParseException("severely truncated class file");
        }
        if (observer != null) {
            observer.parsed(bytes, 0, 0, "begin classfile");
            observer.parsed(bytes, 0, 4, "magic: " + Hex.u4(getMagic0()));
            observer.parsed(bytes, 4, 2,
                            "minor_version: " + Hex.u2(getMinorVersion0()));
            observer.parsed(bytes, 6, 2,
                            "major_version: " + Hex.u2(getMajorVersion0()));
        }
        if (strictParse) {
            if (!isGoodVersion(getMagic0(), getMinorVersion0(),
                               getMajorVersion0())) {
                throw new ParseException("bad class file magic (" +
                                         Hex.u4(getMagic0()) +
                                         ") or version (" +
                                         Hex.u2(getMajorVersion0()) + "." +
                                         Hex.u2(getMinorVersion0()) + ")");
            }
        }
        ConstantPoolParser cpParser = new ConstantPoolParser(bytes);
        cpParser.setObserver(observer);
        pool = cpParser.getPool();
        pool.setImmutable();
        int at = cpParser.getEndOffset();
        int accessFlags = bytes.getUnsignedShort(at); 
        int cpi = bytes.getUnsignedShort(at + 2); 
        thisClass = (CstType) pool.get(cpi);
        cpi = bytes.getUnsignedShort(at + 4); 
        superClass = (CstType) pool.get0Ok(cpi);
        int count = bytes.getUnsignedShort(at + 6); 
        if (observer != null) {
            observer.parsed(bytes, at, 2,
                            "access_flags: " + 
                            AccessFlags.classString(accessFlags));
            observer.parsed(bytes, at + 2, 2, "this_class: " + thisClass);
            observer.parsed(bytes, at + 4, 2, "super_class: " +
                            stringOrNone(superClass));
            observer.parsed(bytes, at + 6, 2,
                            "interfaces_count: " + Hex.u2(count));
            if (count != 0) {
                observer.parsed(bytes, at + 8, 0, "interfaces:");
            }
        }
        at += 8;
        interfaces = makeTypeList(at, count);
        at += count * 2;
        if (strictParse) {
            String thisClassName = thisClass.getClassType().getClassName();
            if (!(filePath.endsWith(".class") &&
                  filePath.startsWith(thisClassName) &&
                  (filePath.length() == (thisClassName.length() + 6)))) {
                throw new ParseException("class name (" + thisClassName +
                                         ") does not match path (" +
                                         filePath + ")");
            }
        }
        this.accessFlags = accessFlags;
        FieldListParser flParser =
            new FieldListParser(this, thisClass, at, attributeFactory);
        flParser.setObserver(observer);
        fields = flParser.getList();
        at = flParser.getEndOffset();
        MethodListParser mlParser =
            new MethodListParser(this, thisClass, at, attributeFactory);
        mlParser.setObserver(observer);
        methods = mlParser.getList();
        at = mlParser.getEndOffset();
        AttributeListParser alParser =
            new AttributeListParser(this, AttributeFactory.CTX_CLASS, at,
                                    attributeFactory);
        alParser.setObserver(observer);
        attributes = alParser.getList();
        attributes.setImmutable();
        at = alParser.getEndOffset();
        if (at != bytes.size()) {
            throw new ParseException("extra bytes at end of class file, " +
                                     "at offset " + Hex.u4(at));
        }
        if (observer != null) {
            observer.parsed(bytes, at, 0, "end classfile");
        }
    }
    private static class DcfTypeList implements TypeList {
        private final ByteArray bytes;
        private final int size;
        private final StdConstantPool pool;
        public DcfTypeList(ByteArray bytes, int offset, int size,
                StdConstantPool pool, ParseObserver observer) {
            if (size < 0) {
                throw new IllegalArgumentException("size < 0");
            }
            bytes = bytes.slice(offset, offset + size * 2);
            this.bytes = bytes;
            this.size = size;
            this.pool = pool;
            for (int i = 0; i < size; i++) {
                offset = i * 2;
                int idx = bytes.getUnsignedShort(offset);
                CstType type;
                try {
                    type = (CstType) pool.get(idx);
                } catch (ClassCastException ex) {
                    throw new RuntimeException("bogus class cpi", ex);
                }
                if (observer != null) {
                    observer.parsed(bytes, offset, 2, "  " + type);
                }
            }
        }
        public boolean isMutable() {
            return false;
        }
        public int size() {
            return size;
        }
        public int getWordCount() {
            return size;
        }
        public Type getType(int n) {
            int idx = bytes.getUnsignedShort(n * 2);
            return ((CstType) pool.get(idx)).getClassType();
        }
        public TypeList withAddedType(Type type) {
            throw new UnsupportedOperationException("unsupported");
        }        
    }
}
