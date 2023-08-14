public class ShortLookupTable extends LookupTable {
    private short data[][];
    public ShortLookupTable(int offset, short[] data) {
        super(offset, 1);
        this.data = new short[1][data.length];
        this.data[0] = data;
    }
    public ShortLookupTable(int offset, short[][] data) {
        super(offset, data.length);
        this.data = new short[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }
    public final short[][] getTable() {
        return data;
    }
    public short[] lookupPixel(short[] src, short[] dst) {
        if (dst == null) {
            dst = new short[src.length];
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
}
