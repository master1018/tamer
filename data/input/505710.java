public final class IntArrayWrapper {
    private int[] mData;
    public IntArrayWrapper(int[] data) {
        mData = data;
    }
    public void set(int[] data) {
        mData = data;
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode(mData);
    }
    @Override
    public boolean equals(Object obj) {
        if (getClass().equals(obj.getClass())) {
            return Arrays.equals(mData, ((IntArrayWrapper)obj).mData);
        }
        return super.equals(obj);
    }
}
