public class DexData {
    private RandomAccessFile mDexFile;
    private HeaderItem mHeaderItem;
    private String[] mStrings;              
    private TypeIdItem[] mTypeIds;
    private ProtoIdItem[] mProtoIds;
    private FieldIdItem[] mFieldIds;
    private MethodIdItem[] mMethodIds;
    private ClassDefItem[] mClassDefs;
    private byte tmpBuf[] = new byte[4];
    private boolean isBigEndian = false;
    public DexData(RandomAccessFile raf) {
        mDexFile = raf;
    }
    public void load() throws IOException {
        parseHeaderItem();
        loadStrings();
        loadTypeIds();
        loadProtoIds();
        loadFieldIds();
        loadMethodIds();
        loadClassDefs();
        markInternalClasses();
    }
    void parseHeaderItem() throws IOException {
        mHeaderItem = new HeaderItem();
        seek(0);
        byte[] magic = new byte[8];
        readBytes(magic);
        if (!Arrays.equals(magic, HeaderItem.DEX_FILE_MAGIC)) {
            System.err.println("Magic number is wrong -- are you sure " +
                "this is a DEX file?");
            throw new DexDataException();
        }
        seek(8+4+20+4+4);
        mHeaderItem.endianTag = readInt();
        if (mHeaderItem.endianTag == HeaderItem.ENDIAN_CONSTANT) {
        } else if (mHeaderItem.endianTag == HeaderItem.REVERSE_ENDIAN_CONSTANT){
            isBigEndian = true;
        } else {
            System.err.println("Endian constant has unexpected value " +
                Integer.toHexString(mHeaderItem.endianTag));
            throw new DexDataException();
        }
        seek(8+4+20);  
        mHeaderItem.fileSize = readInt();
        mHeaderItem.headerSize = readInt();
         readInt();
         readInt();
         readInt();
         readInt();
        mHeaderItem.stringIdsSize = readInt();
        mHeaderItem.stringIdsOff = readInt();
        mHeaderItem.typeIdsSize = readInt();
        mHeaderItem.typeIdsOff = readInt();
        mHeaderItem.protoIdsSize = readInt();
        mHeaderItem.protoIdsOff = readInt();
        mHeaderItem.fieldIdsSize = readInt();
        mHeaderItem.fieldIdsOff = readInt();
        mHeaderItem.methodIdsSize = readInt();
        mHeaderItem.methodIdsOff = readInt();
        mHeaderItem.classDefsSize = readInt();
        mHeaderItem.classDefsOff = readInt();
         readInt();
         readInt();
    }
    void loadStrings() throws IOException {
        int count = mHeaderItem.stringIdsSize;
        int stringOffsets[] = new int[count];
        seek(mHeaderItem.stringIdsOff);
        for (int i = 0; i < count; i++) {
            stringOffsets[i] = readInt();
        }
        mStrings = new String[count];
        seek(stringOffsets[0]);
        for (int i = 0; i < count; i++) {
            seek(stringOffsets[i]);         
            mStrings[i] = readString();
        }
    }
    void loadTypeIds() throws IOException {
        int count = mHeaderItem.typeIdsSize;
        mTypeIds = new TypeIdItem[count];
        seek(mHeaderItem.typeIdsOff);
        for (int i = 0; i < count; i++) {
            mTypeIds[i] = new TypeIdItem();
            mTypeIds[i].descriptorIdx = readInt();
        }
    }
    void loadProtoIds() throws IOException {
        int count = mHeaderItem.protoIdsSize;
        mProtoIds = new ProtoIdItem[count];
        seek(mHeaderItem.protoIdsOff);
        for (int i = 0; i < count; i++) {
            mProtoIds[i] = new ProtoIdItem();
            mProtoIds[i].shortyIdx = readInt();
            mProtoIds[i].returnTypeIdx = readInt();
            mProtoIds[i].parametersOff = readInt();
        }
        for (int i = 0; i < count; i++) {
            ProtoIdItem protoId = mProtoIds[i];
            int offset = protoId.parametersOff;
            if (offset == 0) {
                protoId.types = new int[0];
                continue;
            } else {
                seek(offset);
                int size = readInt();       
                protoId.types = new int[size];
                for (int j = 0; j < size; j++) {
                    protoId.types[j] = readShort() & 0xffff;
                }
            }
        }
    }
    void loadFieldIds() throws IOException {
        int count = mHeaderItem.fieldIdsSize;
        mFieldIds = new FieldIdItem[count];
        seek(mHeaderItem.fieldIdsOff);
        for (int i = 0; i < count; i++) {
            mFieldIds[i] = new FieldIdItem();
            mFieldIds[i].classIdx = readShort() & 0xffff;
            mFieldIds[i].typeIdx = readShort() & 0xffff;
            mFieldIds[i].nameIdx = readInt();
        }
    }
    void loadMethodIds() throws IOException {
        int count = mHeaderItem.methodIdsSize;
        mMethodIds = new MethodIdItem[count];
        seek(mHeaderItem.methodIdsOff);
        for (int i = 0; i < count; i++) {
            mMethodIds[i] = new MethodIdItem();
            mMethodIds[i].classIdx = readShort() & 0xffff;
            mMethodIds[i].protoIdx = readShort() & 0xffff;
            mMethodIds[i].nameIdx = readInt();
        }
    }
    void loadClassDefs() throws IOException {
        int count = mHeaderItem.classDefsSize;
        mClassDefs = new ClassDefItem[count];
        seek(mHeaderItem.classDefsOff);
        for (int i = 0; i < count; i++) {
            mClassDefs[i] = new ClassDefItem();
            mClassDefs[i].classIdx = readInt();
             readInt();
             readInt();
             readInt();
             readInt();
             readInt();
             readInt();
             readInt();
        }
    }
    void markInternalClasses() {
        for (int i = mClassDefs.length -1; i >= 0; i--) {
            mTypeIds[mClassDefs[i].classIdx].internal = true;
        }
        for (int i = 0; i < mTypeIds.length; i++) {
            String className = mStrings[mTypeIds[i].descriptorIdx];
            if (className.length() == 1) {
                mTypeIds[i].internal = true;
            } else if (className.charAt(0) == '[') {
                mTypeIds[i].internal = true;
            }
        }
    }
    private String classNameFromTypeIndex(int idx) {
        return mStrings[mTypeIds[idx].descriptorIdx];
    }
    private String[] argArrayFromProtoIndex(int idx) {
        ProtoIdItem protoId = mProtoIds[idx];
        String[] result = new String[protoId.types.length];
        for (int i = 0; i < protoId.types.length; i++) {
            result[i] = mStrings[mTypeIds[protoId.types[i]].descriptorIdx];
        }
        return result;
    }
    private String returnTypeFromProtoIndex(int idx) {
        ProtoIdItem protoId = mProtoIds[idx];
        return mStrings[mTypeIds[protoId.returnTypeIdx].descriptorIdx];
    }
    public ClassRef[] getExternalReferences() {
        ClassRef[] sparseRefs = new ClassRef[mTypeIds.length];
        int count = 0;
        for (int i = 0; i < mTypeIds.length; i++) {
            if (!mTypeIds[i].internal) {
                sparseRefs[i] =
                    new ClassRef(mStrings[mTypeIds[i].descriptorIdx]);
                count++;
            }
        }
        addExternalFieldReferences(sparseRefs);
        addExternalMethodReferences(sparseRefs);
        ClassRef[] classRefs = new ClassRef[count];
        int idx = 0;
        for (int i = 0; i < mTypeIds.length; i++) {
            if (sparseRefs[i] != null)
                classRefs[idx++] = sparseRefs[i];
        }
        assert idx == count;
        return classRefs;
    }
    private void addExternalFieldReferences(ClassRef[] sparseRefs) {
        for (int i = 0; i < mFieldIds.length; i++) {
            if (!mTypeIds[mFieldIds[i].classIdx].internal) {
                FieldIdItem fieldId = mFieldIds[i];
                FieldRef newFieldRef = new FieldRef(
                        classNameFromTypeIndex(fieldId.classIdx),
                        classNameFromTypeIndex(fieldId.typeIdx),
                        mStrings[fieldId.nameIdx]);
                sparseRefs[mFieldIds[i].classIdx].addField(newFieldRef);
            }
        }
    }
    private void addExternalMethodReferences(ClassRef[] sparseRefs) {
        for (int i = 0; i < mMethodIds.length; i++) {
            if (!mTypeIds[mMethodIds[i].classIdx].internal) {
                MethodIdItem methodId = mMethodIds[i];
                MethodRef newMethodRef = new MethodRef(
                        classNameFromTypeIndex(methodId.classIdx),
                        argArrayFromProtoIndex(methodId.protoIdx),
                        returnTypeFromProtoIndex(methodId.protoIdx),
                        mStrings[methodId.nameIdx]);
                sparseRefs[mMethodIds[i].classIdx].addMethod(newMethodRef);
            }
        }
    }
    void seek(int position) throws IOException {
        mDexFile.seek(position);
    }
    void readBytes(byte[] buffer) throws IOException {
        mDexFile.readFully(buffer);
    }
    byte readByte() throws IOException {
        mDexFile.readFully(tmpBuf, 0, 1);
        return tmpBuf[0];
    }
    short readShort() throws IOException {
        mDexFile.readFully(tmpBuf, 0, 2);
        if (isBigEndian) {
            return (short) ((tmpBuf[1] & 0xff) | ((tmpBuf[0] & 0xff) << 8));
        } else {
            return (short) ((tmpBuf[0] & 0xff) | ((tmpBuf[1] & 0xff) << 8));
        }
    }
    int readInt() throws IOException {
        mDexFile.readFully(tmpBuf, 0, 4);
        if (isBigEndian) {
            return (tmpBuf[3] & 0xff) | ((tmpBuf[2] & 0xff) << 8) |
                   ((tmpBuf[1] & 0xff) << 16) | ((tmpBuf[0] & 0xff) << 24);
        } else {
            return (tmpBuf[0] & 0xff) | ((tmpBuf[1] & 0xff) << 8) |
                   ((tmpBuf[2] & 0xff) << 16) | ((tmpBuf[3] & 0xff) << 24);
        }
    }
    int readUnsignedLeb128() throws IOException {
        int result = 0;
        byte val;
        do {
            val = readByte();
            result = (result << 7) | (val & 0x7f);
        } while (val < 0);
        return result;
    }
    String readString() throws IOException {
        int utf16len = readUnsignedLeb128();
        byte inBuf[] = new byte[utf16len * 3];      
        int idx;
        for (idx = 0; idx < inBuf.length; idx++) {
            byte val = readByte();
            if (val == 0)
                break;
            inBuf[idx] = val;
        }
        return new String(inBuf, 0, idx, "UTF-8");
    }
    static class HeaderItem {
        public int fileSize;
        public int headerSize;
        public int endianTag;
        public int stringIdsSize, stringIdsOff;
        public int typeIdsSize, typeIdsOff;
        public int protoIdsSize, protoIdsOff;
        public int fieldIdsSize, fieldIdsOff;
        public int methodIdsSize, methodIdsOff;
        public int classDefsSize, classDefsOff;
        public static final byte[] DEX_FILE_MAGIC = {
            0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00 };
        public static final int ENDIAN_CONSTANT = 0x12345678;
        public static final int REVERSE_ENDIAN_CONSTANT = 0x78563412;
    }
    static class TypeIdItem {
        public int descriptorIdx;       
        public boolean internal;        
    }
    static class ProtoIdItem {
        public int shortyIdx;           
        public int returnTypeIdx;       
        public int parametersOff;       
        public int types[];             
    }
    static class FieldIdItem {
        public int classIdx;            
        public int typeIdx;             
        public int nameIdx;             
    }
    static class MethodIdItem {
        public int classIdx;            
        public int protoIdx;            
        public int nameIdx;             
    }
    static class ClassDefItem {
        public int classIdx;            
    }
}
