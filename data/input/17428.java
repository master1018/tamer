public class ConstantPoolPatch {
    final ConstantPoolParser outer;
    final Object[] patchArray;
    ConstantPoolPatch(ConstantPoolParser outer) {
        this.outer      = outer;
        this.patchArray = new Object[outer.getLength()];
    }
    public ConstantPoolPatch(byte[] classFile) throws InvalidConstantPoolFormatException {
        this(new ConstantPoolParser(classFile));
    }
    public ConstantPoolPatch(Class<?> templateClass) throws IOException, InvalidConstantPoolFormatException {
        this(new ConstantPoolParser(templateClass));
    }
    public ConstantPoolPatch(ConstantPoolPatch patch) {
        outer      = patch.outer;
        patchArray = patch.patchArray.clone();
    }
    public ConstantPoolParser getParser() {
        return outer;
    }
    public byte getTag(int index) {
        return outer.getTag(index);
    }
    public Object getPatch(int index) {
        Object value = patchArray[index];
        if (value == null)  return null;
        switch (getTag(index)) {
        case CONSTANT_Fieldref:
        case CONSTANT_Methodref:
        case CONSTANT_InterfaceMethodref:
            if (value instanceof String)
                value = stripSemis(2, (String) value);
            break;
        case CONSTANT_NameAndType:
            if (value instanceof String)
                value = stripSemis(1, (String) value);
            break;
        }
        return value;
    }
    public void clear() {
        Arrays.fill(patchArray, null);
    }
    public void clear(int index) {
        patchArray[index] = null;
    }
    public Object[] getPatches() {
        return patchArray.clone();
    }
    public Object[] getOriginalCP() throws InvalidConstantPoolFormatException {
        return getOriginalCP(0, patchArray.length, -1);
    }
    public void putPatches(final Map<String,String> utf8Map,
                           final Map<String,Object> classMap,
                           final Map<Object,Object> valueMap,
                           boolean deleteUsedEntries) throws InvalidConstantPoolFormatException {
        final HashSet<String> usedUtf8Keys;
        final HashSet<String> usedClassKeys;
        final HashSet<Object> usedValueKeys;
        if (deleteUsedEntries) {
            usedUtf8Keys  = (utf8Map  == null) ? null : new HashSet<String>();
            usedClassKeys = (classMap == null) ? null : new HashSet<String>();
            usedValueKeys = (valueMap == null) ? null : new HashSet<Object>();
        } else {
            usedUtf8Keys = null;
            usedClassKeys = null;
            usedValueKeys = null;
        }
        outer.parse(new ConstantPoolVisitor() {
            @Override
            public void visitUTF8(int index, byte tag, String utf8) {
                putUTF8(index, utf8Map.get(utf8));
                if (usedUtf8Keys != null)  usedUtf8Keys.add(utf8);
            }
            @Override
            public void visitConstantValue(int index, byte tag, Object value) {
                putConstantValue(index, tag, valueMap.get(value));
                if (usedValueKeys != null)  usedValueKeys.add(value);
            }
            @Override
            public void visitConstantString(int index, byte tag, String name, int nameIndex) {
                if (tag == CONSTANT_Class) {
                    putConstantValue(index, tag, classMap.get(name));
                    if (usedClassKeys != null)  usedClassKeys.add(name);
                } else {
                    assert(tag == CONSTANT_String);
                    visitConstantValue(index, tag, name);
                }
            }
        });
        if (usedUtf8Keys != null)   utf8Map.keySet().removeAll(usedUtf8Keys);
        if (usedClassKeys != null)  classMap.keySet().removeAll(usedClassKeys);
        if (usedValueKeys != null)  valueMap.keySet().removeAll(usedValueKeys);
    }
    Object[] getOriginalCP(final int startIndex,
                           final int endIndex,
                           final int tagMask) throws InvalidConstantPoolFormatException {
        final Object[] cpArray = new Object[endIndex - startIndex];
        outer.parse(new ConstantPoolVisitor() {
            void show(int index, byte tag, Object value) {
                if (index < startIndex || index >= endIndex)  return;
                if (((1 << tag) & tagMask) == 0)  return;
                cpArray[index - startIndex] = value;
            }
            @Override
            public void visitUTF8(int index, byte tag, String utf8) {
                show(index, tag, utf8);
            }
            @Override
            public void visitConstantValue(int index, byte tag, Object value) {
                assert(tag != CONSTANT_String);
                show(index, tag, value);
            }
            @Override
            public void visitConstantString(int index, byte tag,
                                            String value, int j) {
                show(index, tag, value);
            }
            @Override
            public void visitMemberRef(int index, byte tag,
                    String className, String memberName,
                    String signature,
                    int j, int k) {
                show(index, tag, new String[]{ className, memberName, signature });
            }
            @Override
            public void visitDescriptor(int index, byte tag,
                    String memberName, String signature,
                    int j, int k) {
                show(index, tag, new String[]{ memberName, signature });
            }
        });
        return cpArray;
    }
    void writeHead(OutputStream out) throws IOException {
        outer.writePatchedHead(out, patchArray);
    }
    void writeTail(OutputStream out) throws IOException {
        outer.writeTail(out);
    }
    private void checkConstantTag(byte tag, Object value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "invalid null constant value");
        if (classForTag(tag) != value.getClass())
            throw new IllegalArgumentException(
                    "invalid constant value"
                    + (tag == CONSTANT_None ? ""
                        : " for tag "+tagName(tag))
                    + " of class "+value.getClass());
    }
    private void checkTag(int index, byte putTag) {
        byte tag = outer.tags[index];
        if (tag != putTag)
            throw new IllegalArgumentException(
                "invalid put operation"
                + " for " + tagName(putTag)
                + " at index " + index + " found " + tagName(tag));
    }
    private void checkTagMask(int index, int tagBitMask) {
        byte tag = outer.tags[index];
        int tagBit = ((tag & 0x1F) == tag) ? (1 << tag) : 0;
        if ((tagBit & tagBitMask) == 0)
            throw new IllegalArgumentException(
                "invalid put operation"
                + " at index " + index + " found " + tagName(tag));
    }
    private static void checkMemberName(String memberName) {
        if (memberName.indexOf(';') >= 0)
            throw new IllegalArgumentException("memberName " + memberName + " contains a ';'");
    }
    public void putUTF8(int index, String utf8) {
        if (utf8 == null) { clear(index); return; }
        checkTag(index, CONSTANT_Utf8);
        patchArray[index] = utf8;
    }
    public void putConstantValue(int index, Object value) {
        if (value == null) { clear(index); return; }
        byte tag = tagForConstant(value.getClass());
        checkConstantTag(tag, value);
        checkTag(index, tag);
        patchArray[index] = value;
    }
    public void putConstantValue(int index, byte tag, Object value) {
        if (value == null) { clear(index); return; }
        checkTag(index, tag);
        if (tag == CONSTANT_Class && value instanceof String) {
            checkClassName((String) value);
        } else if (tag == CONSTANT_String) {
        } else {
            checkConstantTag(tag, value);
        }
        checkTag(index, tag);
        patchArray[index] = value;
    }
    public void putDescriptor(int index, String memberName, String signature) {
        checkTag(index, CONSTANT_NameAndType);
        checkMemberName(memberName);
        patchArray[index] = addSemis(memberName, signature);
    }
    public void putMemberRef(int index, byte tag,
                    String className, String memberName, String signature) {
        checkTagMask(tag, CONSTANT_MemberRef_MASK);
        checkTag(index, tag);
        checkClassName(className);
        checkMemberName(memberName);
        if (signature.startsWith("(") == (tag == CONSTANT_Fieldref))
            throw new IllegalArgumentException("bad signature: "+signature);
        patchArray[index] = addSemis(className, memberName, signature);
    }
    static private final int CONSTANT_MemberRef_MASK =
              CONSTANT_Fieldref
            | CONSTANT_Methodref
            | CONSTANT_InterfaceMethodref;
    private static final Map<Class<?>, Byte> CONSTANT_VALUE_CLASS_TAG
        = new IdentityHashMap<Class<?>, Byte>();
    private static final Class[] CONSTANT_VALUE_CLASS = new Class[16];
    static {
        Object[][] values = {
            {Integer.class, CONSTANT_Integer},
            {Long.class, CONSTANT_Long},
            {Float.class, CONSTANT_Float},
            {Double.class, CONSTANT_Double},
            {String.class, CONSTANT_String},
            {Class.class, CONSTANT_Class}
        };
        for (Object[] value : values) {
            Class<?> cls = (Class<?>)value[0];
            Byte     tag = (Byte) value[1];
            CONSTANT_VALUE_CLASS_TAG.put(cls, tag);
            CONSTANT_VALUE_CLASS[(byte)tag] = cls;
        }
    }
    static Class<?> classForTag(byte tag) {
        if ((tag & 0xFF) >= CONSTANT_VALUE_CLASS.length)
            return null;
        return CONSTANT_VALUE_CLASS[tag];
    }
    static byte tagForConstant(Class<?> cls) {
        Byte tag = CONSTANT_VALUE_CLASS_TAG.get(cls);
        return (tag == null) ? CONSTANT_None : (byte)tag;
    }
    private static void checkClassName(String className) {
        if (className.indexOf('/') >= 0 || className.indexOf(';') >= 0)
            throw new IllegalArgumentException("invalid class name " + className);
    }
    static String addSemis(String name, String... names) {
        StringBuilder buf = new StringBuilder(name.length() * 5);
        buf.append(name);
        for (String name2 : names) {
            buf.append(';').append(name2);
        }
        String res = buf.toString();
        assert(stripSemis(names.length, res)[0].equals(name));
        assert(stripSemis(names.length, res)[1].equals(names[0]));
        assert(names.length == 1 ||
               stripSemis(names.length, res)[2].equals(names[1]));
        return res;
    }
    static String[] stripSemis(int count, String string) {
        String[] res = new String[count+1];
        int pos = 0;
        for (int i = 0; i < count; i++) {
            int pos2 = string.indexOf(';', pos);
            if (pos2 < 0)  pos2 = string.length();  
            res[i] = string.substring(pos, pos2);
            pos = pos2;
        }
        res[count] = string.substring(pos);
        return res;
    }
    public String toString() {
        StringBuilder buf = new StringBuilder(this.getClass().getName());
        buf.append("{");
        Object[] origCP = null;
        for (int i = 0; i < patchArray.length; i++) {
            if (patchArray[i] == null)  continue;
            if (origCP != null) {
                buf.append(", ");
            } else {
                try {
                    origCP = getOriginalCP();
                } catch (InvalidConstantPoolFormatException ee) {
                    origCP = new Object[0];
                }
            }
            Object orig = (i < origCP.length) ? origCP[i] : "?";
            buf.append(orig).append("=").append(patchArray[i]);
        }
        buf.append("}");
        return buf.toString();
    }
}
