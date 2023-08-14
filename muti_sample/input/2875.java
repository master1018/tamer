public final class CompactByteArray implements Cloneable {
    public static  final int UNICODECOUNT =65536;
    public CompactByteArray(byte defaultValue)
    {
        int i;
        values = new byte[UNICODECOUNT];
        indices = new short[INDEXCOUNT];
        hashes = new int[INDEXCOUNT];
        for (i = 0; i < UNICODECOUNT; ++i) {
            values[i] = defaultValue;
        }
        for (i = 0; i < INDEXCOUNT; ++i) {
            indices[i] = (short)(i<<BLOCKSHIFT);
            hashes[i] = 0;
        }
        isCompact = false;
    }
     public CompactByteArray(short indexArray[],
                            byte newValues[])
    {
        int i;
        if (indexArray.length != INDEXCOUNT)
            throw new IllegalArgumentException("Index out of bounds!");
        for (i = 0; i < INDEXCOUNT; ++i) {
            short index = indexArray[i];
            if ((index < 0) || (index >= newValues.length+BLOCKCOUNT))
                throw new IllegalArgumentException("Index out of bounds!");
        }
        indices = indexArray;
        values = newValues;
        isCompact = true;
    }
    public byte elementAt(char index)
    {
        return (values[(indices[index >> BLOCKSHIFT] & 0xFFFF)
                       + (index & BLOCKMASK)]);
    }
    public void setElementAt(char index, byte value)
    {
        if (isCompact)
            expand();
        values[(int)index] = value;
        touchBlock(index >> BLOCKSHIFT, value);
    }
    public void setElementAt(char start, char end, byte value)
    {
        int i;
        if (isCompact) {
            expand();
        }
        for (i = start; i <= end; ++i) {
            values[i] = value;
            touchBlock(i >> BLOCKSHIFT, value);
        }
    }
    public void compact()
    {
        if (!isCompact) {
            int limitCompacted = 0;
            int iBlockStart = 0;
            short iUntouched = -1;
            for (int i = 0; i < indices.length; ++i, iBlockStart += BLOCKCOUNT) {
                indices[i] = -1;
                boolean touched = blockTouched(i);
                if (!touched && iUntouched != -1) {
                    indices[i] = iUntouched;
                } else {
                    int jBlockStart = 0;
                    int j = 0;
                    for (j = 0; j < limitCompacted;
                            ++j, jBlockStart += BLOCKCOUNT) {
                        if (hashes[i] == hashes[j] &&
                                arrayRegionMatches(values, iBlockStart,
                                values, jBlockStart, BLOCKCOUNT)) {
                            indices[i] = (short)jBlockStart;
                            break;
                        }
                    }
                    if (indices[i] == -1) {
                        System.arraycopy(values, iBlockStart,
                            values, jBlockStart, BLOCKCOUNT);
                        indices[i] = (short)jBlockStart;
                        hashes[j] = hashes[i];
                        ++limitCompacted;
                        if (!touched) {
                            iUntouched = (short)jBlockStart;
                        }
                    }
                }
            }
            int newSize = limitCompacted*BLOCKCOUNT;
            byte[] result = new byte[newSize];
            System.arraycopy(values, 0, result, 0, newSize);
            values = result;
            isCompact = true;
            hashes = null;
        }
    }
    final static boolean arrayRegionMatches(byte[] source, int sourceStart,
                                            byte[] target, int targetStart,
                                            int len)
    {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta])
            return false;
        }
        return true;
    }
    private final void touchBlock(int i, int value) {
        hashes[i] = (hashes[i] + (value<<1)) | 1;
    }
    private final boolean blockTouched(int i) {
        return hashes[i] != 0;
    }
    public short getIndexArray()[]
    {
        return indices;
    }
    public byte getStringArray()[]
    {
        return values;
    }
    public Object clone()
    {
        try {
            CompactByteArray other = (CompactByteArray) super.clone();
            other.values = (byte[])values.clone();
            other.indices = (short[])indices.clone();
            if (hashes != null) other.hashes = (int[])hashes.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)                      
            return true;
        if (getClass() != obj.getClass())         
            return false;
        CompactByteArray other = (CompactByteArray) obj;
        for (int i = 0; i < UNICODECOUNT; i++) {
            if (elementAt((char)i) != other.elementAt((char)i))
                return false;
        }
        return true; 
    }
    public int hashCode() {
        int result = 0;
        int increment = Math.min(3, values.length/16);
        for (int i = 0; i < values.length; i+= increment) {
            result = result * 37 + values[i];
        }
        return result;
    }
    private void expand()
    {
        int i;
        if (isCompact) {
            byte[]  tempArray;
            hashes = new int[INDEXCOUNT];
            tempArray = new byte[UNICODECOUNT];
            for (i = 0; i < UNICODECOUNT; ++i) {
                byte value = elementAt((char)i);
                tempArray[i] = value;
                touchBlock(i >> BLOCKSHIFT, value);
            }
            for (i = 0; i < INDEXCOUNT; ++i) {
                indices[i] = (short)(i<<BLOCKSHIFT);
            }
            values = null;
            values = tempArray;
            isCompact = false;
        }
    }
    private byte[] getArray()
    {
        return values;
    }
    private static  final int BLOCKSHIFT =7;
    private static  final int BLOCKCOUNT =(1<<BLOCKSHIFT);
    private static  final int INDEXSHIFT =(16-BLOCKSHIFT);
    private static  final int INDEXCOUNT =(1<<INDEXSHIFT);
    private static  final int BLOCKMASK = BLOCKCOUNT - 1;
    private byte[] values;  
    private short indices[];
    private boolean isCompact;
    private int[] hashes;
};
