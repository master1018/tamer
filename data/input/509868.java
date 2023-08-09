public class ClassInstance extends Instance {
    private byte[] mFieldValues;
    public ClassInstance(long id, StackTrace stack, long classId) {
        mId = id;
        mStack = stack;
        mClassId = classId;
    }
    public final void loadFieldData(DataInputStream in, int numBytes)
            throws IOException {
        mFieldValues = new byte[numBytes];
        in.readFully(mFieldValues);
    }
    @Override
    public void resolveReferences(State state) {
        ClassObj isa = mHeap.mState.findClass(mClassId);
        resolve(state, isa, isa.mStaticFieldTypes, isa.mStaticFieldValues);
        resolve(state, isa, isa.mFieldTypes, mFieldValues);
    }
    private void resolve(State state, ClassObj isa, int[] types, 
            byte[] values) {
        ByteArrayInputStream bais = new ByteArrayInputStream(values);
        DataInputStream dis = new DataInputStream(bais);
        final int N = types.length;
        try {
            for (int i = 0; i < N; i++) {
                int type = types[i];
                int size = Types.getTypeSize(type);
                    if (type == Types.OBJECT) {
                        long id;
                        if (size == 4) {
                            id = dis.readInt();
                        } else {
                            id = dis.readLong();
                        }
                        Instance instance = state.findReference(id);
                        if (instance != null) {
                            instance.addParent(this);
                        }
                    } else {
                        dis.skipBytes(size);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public final int getSize() {
        ClassObj isa = mHeap.mState.findClass(mClassId);
        return isa.getSize();
    }
    @Override
    public final void visit(Set<Instance> resultSet, Filter filter) {
        if (resultSet.contains(this)) {
            return;
        }
        if (filter != null) {
            if (filter.accept(this)) {
                resultSet.add(this);
            }
        } else {
            resultSet.add(this);
        }
        State state = mHeap.mState;
        ClassObj isa = state.findClass(mClassId);
        int[] types = isa.mFieldTypes;
        ByteArrayInputStream bais = new ByteArrayInputStream(mFieldValues);
        DataInputStream dis = new DataInputStream(bais);
        final int N = types.length;
        try {
            for (int i = 0; i < N; i++) {
                int type = types[i];
                int size = Types.getTypeSize(type);
                if (type == Types.OBJECT) {
                    long id;
                    if (size == 4) {
                        id = dis.readInt();
                    } else {
                        id = dis.readLong();
                    }
                    Instance instance = state.findReference(id);
                    if (instance != null) {
                        instance.visit(resultSet, filter);
                    }
                } else {
                    dis.skipBytes(size);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public final String getTypeName() {
        ClassObj theClass = mHeap.mState.findClass(mClassId);
        return theClass.mClassName;
    }
    public final String toString() {
        return String.format("%s@0x%08x", getTypeName(), mId);
    }
    @Override
    public String describeReferenceTo(long referent) {
        ClassObj isa = mHeap.mState.findClass(mClassId);
        int[] types = isa.mFieldTypes;
        String[] fieldNames = isa.mFieldNames;
        ByteArrayInputStream bais = new ByteArrayInputStream(mFieldValues);
        DataInputStream dis = new DataInputStream(bais);
        final int N = types.length;
        StringBuilder result = new StringBuilder("Referenced in field(s):");
        int numReferences = 0;
        try {
            for (int i = 0; i < N; i++) {
                int type = types[i];
                int size = Types.getTypeSize(type);
                if (type == Types.OBJECT) {
                    long id;
                    if (size == 4) {
                        id = dis.readInt();
                    } else {
                        id = dis.readLong();
                    }
                    if (id == referent) {
                        numReferences++;
                        result.append("\n    ");
                        result.append(fieldNames[i]);
                    }
                } else {
                    dis.skipBytes(size);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (numReferences == 0) {
            return super.describeReferenceTo(referent);
        }
        return result.toString();
    }
}
