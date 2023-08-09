public class RootObj extends Instance {
    RootType mType = RootType.UNKNOWN;
    int mIndex;
    int mThread;
    long mParent;
    String mComment;
    public RootObj(RootType type) {
        this(type, 0, 0, null);
    }
    public RootObj(RootType type, long id) {
        this(type, id, 0, null);
    }
    public RootObj(RootType type, long id, int thread, StackTrace stack) {
        mType = type;
        mId = id;
        mThread = thread;
        mStack = stack;
    }
    public final String getClassName(State state) {
        ClassObj theClass;
        if (mType == RootType.SYSTEM_CLASS) {
            theClass = state.findClass(mId);
        } else {
            Instance instance = state.findReference(mId);
            theClass = state.findClass(instance.mClassId);
        }
        if (theClass == null) {
            return "no class defined!!";
        }
        return theClass.mClassName;
    }
    @Override
    public final int getSize() {
        Instance instance = null;
        if (mType == RootType.SYSTEM_CLASS) {
            instance = mHeap.mState.findClass(mId);
        } else {
            instance = mHeap.mState.findReference(mId);
        }
        if (instance == null) {
            return 0;
        }
        return instance.getSize();
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
    }
    @Override
    public final void resolveReferences(State state) {
    }
    @Override
    public final String getTypeName() {
        return "root " + mType.getName();
    }
    public final String toString() {
        return String.format("%s@0x08x", mType.getName(), mId);
    }
}
