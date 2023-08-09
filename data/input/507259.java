public class ByteLookupTable extends LookupTable {
    private byte data[][];
    public ByteLookupTable(int offset, byte[] data) {
        super(offset, 1);
        if (data.length < 1)
            throw new IllegalArgumentException("Length of data should not be less then one");
        this.data = new byte[1][data.length];
        this.data[0] = data;
    }
    public ByteLookupTable(int offset, byte[][] data) {
        super(offset, data.length);
        this.data = new byte[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }
    public final byte[][] getTable() {
        return data;
    }
    @Override
    public int[] lookupPixel(int[] src, int[] dst) {
        if (dst == null) {
            dst = new int[src.length];
        }
        int offset = getOffset();
        if (getNumComponents() == 1) {
            for (int i = 0; i < src.length; i++) {
                dst[i] = data[0][src[i] - offset];
            }
        } else {
            for (int i = 0; i < getNumComponents(); i++) {
                dst[i] = data[i][src[i] - offset];
            }
        }
        return dst;
    }
    public byte[] lookupPixel(byte[] src, byte[] dst) {
        if (dst == null) {
            dst = new byte[src.length];
        }
        int offset = getOffset();
        if (getNumComponents() == 1) {
            for (int i = 0; i < src.length; i++) {
                dst[i] = data[0][src[i] - offset];
            }
        } else {
            for (int i = 0; i < getNumComponents(); i++) {
                dst[i] = data[i][src[i] - offset];
            }
        }
        return dst;
    }
}
