public final class UCompactIntArray implements Cloneable {
    public UCompactIntArray() {
        values = new int[16][];
        indices = new short[16][];
        blockTouched = new boolean[16][];
        planeTouched = new boolean[16];
    }
    public UCompactIntArray(int defaultValue) {
        this();
        this.defaultValue = defaultValue;
    }
    public int elementAt(int index) {
        int plane = (index & PLANEMASK) >> PLANESHIFT;
        if (!planeTouched[plane]) {
            return defaultValue;
        }
        index &= CODEPOINTMASK;
        return values[plane][(indices[plane][index >> BLOCKSHIFT] & 0xFFFF)
                       + (index & BLOCKMASK)];
    }
    public void setElementAt(int index, int value) {
        if (isCompact) {
            expand();
        }
        int plane = (index & PLANEMASK) >> PLANESHIFT;
        if (!planeTouched[plane]) {
            initPlane(plane);
        }
        index &= CODEPOINTMASK;
        values[plane][index] = value;
        blockTouched[plane][index >> BLOCKSHIFT] = true;
    }
    public void compact() {
        if (isCompact) {
            return;
        }
        for (int plane = 0; plane < PLANECOUNT; plane++) {
            if (!planeTouched[plane]) {
                continue;
            }
            int limitCompacted = 0;
            int iBlockStart = 0;
            short iUntouched = -1;
            for (int i = 0; i < indices[plane].length; ++i, iBlockStart += BLOCKCOUNT) {
                indices[plane][i] = -1;
                if (!blockTouched[plane][i] && iUntouched != -1) {
                    indices[plane][i] = iUntouched;
                } else {
                    int jBlockStart = limitCompacted * BLOCKCOUNT;
                    if (i > limitCompacted) {
                        System.arraycopy(values[plane], iBlockStart,
                                         values[plane], jBlockStart, BLOCKCOUNT);
                    }
                    if (!blockTouched[plane][i]) {
                        iUntouched = (short)jBlockStart;
                    }
                    indices[plane][i] = (short)jBlockStart;
                    limitCompacted++;
                }
            }
            int newSize = limitCompacted * BLOCKCOUNT;
            int[] result = new int[newSize];
            System.arraycopy(values[plane], 0, result, 0, newSize);
            values[plane] = result;
            blockTouched[plane] = null;
        }
        isCompact = true;
    }
    private void expand() {
        int i;
        if (isCompact) {
            int[]   tempArray;
            for (int plane = 0; plane < PLANECOUNT; plane++) {
                if (!planeTouched[plane]) {
                    continue;
                }
                blockTouched[plane] = new boolean[INDEXCOUNT];
                tempArray = new int[UNICODECOUNT];
                for (i = 0; i < UNICODECOUNT; ++i) {
                    tempArray[i] = values[plane][indices[plane][i >> BLOCKSHIFT]
                                                & 0xffff + (i & BLOCKMASK)];
                    blockTouched[plane][i >> BLOCKSHIFT] = true;
                }
                for (i = 0; i < INDEXCOUNT; ++i) {
                    indices[plane][i] = (short)(i<<BLOCKSHIFT);
                }
                values[plane] = tempArray;
            }
            isCompact = false;
        }
    }
    private void initPlane(int plane) {
        values[plane] = new int[UNICODECOUNT];
        indices[plane] = new short[INDEXCOUNT];
        blockTouched[plane] = new boolean[INDEXCOUNT];
        planeTouched[plane] = true;
        if (planeTouched[0] && plane != 0) {
            System.arraycopy(indices[0], 0, indices[plane], 0, INDEXCOUNT);
        } else {
            for (int i = 0; i < INDEXCOUNT; ++i) {
                indices[plane][i] = (short)(i<<BLOCKSHIFT);
            }
        }
        for (int i = 0; i < UNICODECOUNT; ++i) {
            values[plane][i] = defaultValue;
        }
    }
    public int getKSize() {
        int size = 0;
        for (int plane = 0; plane < PLANECOUNT; plane++) {
            if (planeTouched[plane]) {
                size += (values[plane].length * 4 + indices[plane].length * 2);
            }
        }
        return size / 1024;
    }
    private static final int PLANEMASK = 0x30000;
    private static final int PLANESHIFT = 16;
    private static final int PLANECOUNT = 0x10;
    private static final int CODEPOINTMASK  = 0xffff;
    private static final int UNICODECOUNT = 0x10000;
    private static final int BLOCKSHIFT = 7;
    private static final int BLOCKCOUNT = (1<<BLOCKSHIFT);
    private static final int INDEXSHIFT = (16-BLOCKSHIFT);
    private static final int INDEXCOUNT = (1<<INDEXSHIFT);
    private static final int BLOCKMASK = BLOCKCOUNT - 1;
    private int defaultValue;
    private int values[][];
    private short indices[][];
    private boolean isCompact;
    private boolean[][] blockTouched;
    private boolean[] planeTouched;
};
