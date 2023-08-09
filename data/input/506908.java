public abstract class LookupTable {
    private int offset;
    private int numComponents;
    protected LookupTable(int offset, int numComponents) {
        if (offset < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.232")); 
        }
        if (numComponents < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.233")); 
        }
        this.offset = offset;
        this.numComponents = numComponents;
    }
    public int getOffset() {
        return offset;
    }
    public int getNumComponents() {
        return numComponents;
    }
    public abstract int[] lookupPixel(int[] src, int[] dst);
}
