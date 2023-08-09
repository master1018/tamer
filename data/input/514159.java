public class BitSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 7997698588986878753L;
    private static final int OFFSET = 6;
    private static final int ELM_SIZE = 1 << OFFSET;
    private static final int RIGHT_BITS = ELM_SIZE - 1;
    private static final long[] TWO_N_ARRAY = new long[] { 0x1L, 0x2L, 0x4L,
            0x8L, 0x10L, 0x20L, 0x40L, 0x80L, 0x100L, 0x200L, 0x400L, 0x800L,
            0x1000L, 0x2000L, 0x4000L, 0x8000L, 0x10000L, 0x20000L, 0x40000L,
            0x80000L, 0x100000L, 0x200000L, 0x400000L, 0x800000L, 0x1000000L,
            0x2000000L, 0x4000000L, 0x8000000L, 0x10000000L, 0x20000000L,
            0x40000000L, 0x80000000L, 0x100000000L, 0x200000000L, 0x400000000L,
            0x800000000L, 0x1000000000L, 0x2000000000L, 0x4000000000L,
            0x8000000000L, 0x10000000000L, 0x20000000000L, 0x40000000000L,
            0x80000000000L, 0x100000000000L, 0x200000000000L, 0x400000000000L,
            0x800000000000L, 0x1000000000000L, 0x2000000000000L,
            0x4000000000000L, 0x8000000000000L, 0x10000000000000L,
            0x20000000000000L, 0x40000000000000L, 0x80000000000000L,
            0x100000000000000L, 0x200000000000000L, 0x400000000000000L,
            0x800000000000000L, 0x1000000000000000L, 0x2000000000000000L,
            0x4000000000000000L, 0x8000000000000000L };
    private long[] bits;
    private transient boolean needClear;
    private transient int actualArrayLength;
    private transient boolean isLengthActual;
    public BitSet() {
        bits = new long[1];
        actualArrayLength = 0;
        isLengthActual = true;
    }
    public BitSet(int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException();
        }
        bits = new long[(nbits >> OFFSET) + ((nbits & RIGHT_BITS) > 0 ? 1 : 0)];
        actualArrayLength = 0;
        isLengthActual = true;
    }
    private BitSet(long[] bits, boolean needClear, int actualArrayLength,
            boolean isLengthActual) {
        this.bits = bits;
        this.needClear = needClear;
        this.actualArrayLength = actualArrayLength;
        this.isLengthActual = isLengthActual;
    }
    @Override
    public Object clone() {
        try {
            BitSet clone = (BitSet) super.clone();
            clone.bits = bits.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BitSet) {
            long[] bsBits = ((BitSet) obj).bits;
            int length1 = this.actualArrayLength, length2 = ((BitSet) obj).actualArrayLength;
            if (this.isLengthActual && ((BitSet) obj).isLengthActual
                    && length1 != length2) {
                return false;
            }
            if (length1 <= length2) {
                for (int i = 0; i < length1; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length1; i < length2; i++) {
                    if (bsBits[i] != 0) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < length2; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length2; i < length1; i++) {
                    if (bits[i] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    private final void growLength(int len) {
        long[] tempBits = new long[Math.max(len, bits.length * 2)];
        System.arraycopy(bits, 0, tempBits, 0, this.actualArrayLength);
        bits = tempBits;
    }
    @Override
    public int hashCode() {
        long x = 1234;
        for (int i = 0, length = actualArrayLength; i < length; i++) {
            x ^= bits[i] * (i + 1);
        }
        return (int) ((x >> 32) ^ x);
    }
    public boolean get(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        int arrayPos = pos >> OFFSET;
        if (arrayPos < actualArrayLength) {
            return (bits[arrayPos] & TWO_N_ARRAY[pos & RIGHT_BITS]) != 0;
        }
        return false;
    }
    public BitSet get(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        int last = actualArrayLength << OFFSET;
        if (pos1 >= last || pos1 == pos2) {
            return new BitSet(0);
        }
        if (pos2 > last) {
            pos2 = last;
        }
        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));
        if (idx1 == idx2) {
            long result = (bits[idx1] & (factor1 & factor2)) >>> (pos1 % ELM_SIZE);
            if (result == 0) {
                return new BitSet(0);
            }
            return new BitSet(new long[] { result }, needClear, 1, true);
        }
        long[] newbits = new long[idx2 - idx1 + 1];
        newbits[0] = bits[idx1] & factor1;
        newbits[newbits.length - 1] = bits[idx2] & factor2;
        for (int i = 1; i < idx2 - idx1; i++) {
            newbits[i] = bits[idx1 + i];
        }
        int numBitsToShift = pos1 & RIGHT_BITS;
        int actualLen = newbits.length;
        if (numBitsToShift != 0) {
            for (int i = 0; i < newbits.length; i++) {
                newbits[i] = newbits[i] >>> (numBitsToShift);
                if (i != newbits.length - 1) {
                    newbits[i] |= newbits[i + 1] << (ELM_SIZE - (numBitsToShift));
                }
                if (newbits[i] != 0) {
                    actualLen = i + 1;
                }
            }
        }
        return new BitSet(newbits, needClear, actualLen,
                newbits[actualLen - 1] != 0);
    }
    public void set(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        int len = (pos >> OFFSET) + 1;
        if (len > bits.length) {
            growLength(len);
        }
        bits[len - 1] |= TWO_N_ARRAY[pos & RIGHT_BITS];
        if (len > actualArrayLength) {
            actualArrayLength = len;
            isLengthActual = true;
        }
        needClear();
    }
    public void set(int pos, boolean val) {
        if (val) {
            set(pos);
        } else {
            clear(pos);
        }
    }
    public void set(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        if (pos1 == pos2) {
            return;
        }
        int len2 = ((pos2 - 1) >> OFFSET) + 1;
        if (len2 > bits.length) {
            growLength(len2);
        }
        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));
        if (idx1 == idx2) {
            bits[idx1] |= (factor1 & factor2);
        } else {
            bits[idx1] |= factor1;
            bits[idx2] |= factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] |= (~0L);
            }
        }
        if (idx2 + 1 > actualArrayLength) {
            actualArrayLength = idx2 + 1;
            isLengthActual = true;
        }
        needClear();
    }
    private void needClear() {
        this.needClear = true;
    }
    public void set(int pos1, int pos2, boolean val) {
        if (val) {
            set(pos1, pos2);
        } else {
            clear(pos1, pos2);
        }
    }
    public void clear() {
        if (needClear) {
            for (int i = 0; i < bits.length; i++) {
                bits[i] = 0L;
            }
            actualArrayLength = 0;
            isLengthActual = true;
            needClear = false;
        }
    }
    public void clear(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        if (!needClear) {
            return;
        }
        int arrayPos = pos >> OFFSET;
        if (arrayPos < actualArrayLength) {
            bits[arrayPos] &= ~(TWO_N_ARRAY[pos & RIGHT_BITS]);
            if (bits[actualArrayLength - 1] == 0) {
                isLengthActual = false;
            }
        }
    }
    public void clear(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        if (!needClear) {
            return;
        }
        int last = (actualArrayLength << OFFSET);
        if (pos1 >= last || pos1 == pos2) {
            return;
        }
        if (pos2 > last) {
            pos2 = last;
        }
        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));
        if (idx1 == idx2) {
            bits[idx1] &= ~(factor1 & factor2);
        } else {
            bits[idx1] &= ~factor1;
            bits[idx2] &= ~factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] = 0L;
            }
        }
        if ((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0)) {
            isLengthActual = false;
        }
    }
    public void flip(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        int len = (pos >> OFFSET) + 1;
        if (len > bits.length) {
            growLength(len);
        }
        bits[len - 1] ^= TWO_N_ARRAY[pos & RIGHT_BITS];
        if (len > actualArrayLength) {
            actualArrayLength = len;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        needClear();
    }
    public void flip(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        if (pos1 == pos2) {
            return;
        }
        int len2 = ((pos2 - 1) >> OFFSET) + 1;
        if (len2 > bits.length) {
            growLength(len2);
        }
        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));
        if (idx1 == idx2) {
            bits[idx1] ^= (factor1 & factor2);
        } else {
            bits[idx1] ^= factor1;
            bits[idx2] ^= factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] ^= (~0L);
            }
        }
        if (len2 > actualArrayLength) {
            actualArrayLength = len2;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        needClear();
    }
    public boolean intersects(BitSet bs) {
        long[] bsBits = bs.bits;
        int length1 = actualArrayLength, length2 = bs.actualArrayLength;
        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < length2; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        }
        return false;
    }
    public void and(BitSet bs) {
        long[] bsBits = bs.bits;
        if (!needClear) {
            return;
        }
        int length1 = actualArrayLength, length2 = bs.actualArrayLength;
        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                bits[i] &= bsBits[i];
            }
        } else {
            for (int i = 0; i < length2; i++) {
                bits[i] &= bsBits[i];
            }
            for (int i = length2; i < length1; i++) {
                bits[i] = 0;
            }
            actualArrayLength = length2;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
    }
    public void andNot(BitSet bs) {
        long[] bsBits = bs.bits;
        if (!needClear) {
            return;
        }
        int range = actualArrayLength < bs.actualArrayLength ? actualArrayLength
                : bs.actualArrayLength;
        for (int i = 0; i < range; i++) {
            bits[i] &= ~bsBits[i];
        }
        if (actualArrayLength < range) {
            actualArrayLength = range;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
    }
    public void or(BitSet bs) {
        int bsActualLen = bs.getActualArrayLength();
        if (bsActualLen > bits.length) {
            long[] tempBits = new long[bsActualLen];
            System.arraycopy(bs.bits, 0, tempBits, 0, bs.actualArrayLength);
            for (int i = 0; i < actualArrayLength; i++) {
                tempBits[i] |= bits[i];
            }
            bits = tempBits;
            actualArrayLength = bsActualLen;
            isLengthActual = true;
        } else {
            long[] bsBits = bs.bits;
            for (int i = 0; i < bsActualLen; i++) {
                bits[i] |= bsBits[i];
            }
            if (bsActualLen > actualArrayLength) {
                actualArrayLength = bsActualLen;
                isLengthActual = true;
            }
        }
        needClear();
    }
    public void xor(BitSet bs) {
        int bsActualLen = bs.getActualArrayLength();
        if (bsActualLen > bits.length) {
            long[] tempBits = new long[bsActualLen];
            System.arraycopy(bs.bits, 0, tempBits, 0, bs.actualArrayLength);
            for (int i = 0; i < actualArrayLength; i++) {
                tempBits[i] ^= bits[i];
            }
            bits = tempBits;
            actualArrayLength = bsActualLen;
            isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        } else {
            long[] bsBits = bs.bits;
            for (int i = 0; i < bsActualLen; i++) {
                bits[i] ^= bsBits[i];
            }
            if (bsActualLen > actualArrayLength) {
                actualArrayLength = bsActualLen;
                isLengthActual = true;
            }
        }
        needClear();
    }
    public int size() {
        return bits.length << OFFSET;
    }
    public int length() {
        int idx = actualArrayLength - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        actualArrayLength = idx + 1;
        if (idx == -1) {
            return 0;
        }
        int i = ELM_SIZE - 1;
        long val = bits[idx];
        while ((val & (TWO_N_ARRAY[i])) == 0 && i > 0) {
            i--;
        }
        return (idx << OFFSET) + i + 1;
    }
    private final int getActualArrayLength() {
        if (isLengthActual) {
            return actualArrayLength;
        }
        int idx = actualArrayLength - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        actualArrayLength = idx + 1;
        isLengthActual = true;
        return actualArrayLength;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(bits.length / 2);
        int bitCount = 0;
        sb.append('{');
        boolean comma = false;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 0) {
                bitCount += ELM_SIZE;
                continue;
            }
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((bits[i] & (TWO_N_ARRAY[j])) != 0)) {
                    if (comma) {
                        sb.append(", "); 
                    }
                    sb.append(bitCount);
                    comma = true;
                }
                bitCount++;
            }
        }
        sb.append('}');
        return sb.toString();
    }
    public int nextSetBit(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        if (pos >= actualArrayLength << OFFSET) {
            return -1;
        }
        int idx = pos >> OFFSET;
        if (bits[idx] != 0L) {
            for (int j = pos & RIGHT_BITS; j < ELM_SIZE; j++) {
                if (((bits[idx] & (TWO_N_ARRAY[j])) != 0)) {
                    return (idx << OFFSET) + j;
                }
            }
        }
        idx++;
        while (idx < actualArrayLength && bits[idx] == 0L) {
            idx++;
        }
        if (idx == actualArrayLength) {
            return -1;
        }
        for (int j = 0; j < ELM_SIZE; j++) {
            if (((bits[idx] & (TWO_N_ARRAY[j])) != 0)) {
                return (idx << OFFSET) + j;
            }
        }
        return -1;
    }
    public int nextClearBit(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException(Msg.getString("K0006")); 
        }
        int length = actualArrayLength;
        int bssize = length << OFFSET;
        if (pos >= bssize) {
            return pos;
        }
        int idx = pos >> OFFSET;
        if (bits[idx] != (~0L)) {
            for (int j = pos % ELM_SIZE; j < ELM_SIZE; j++) {
                if (((bits[idx] & (TWO_N_ARRAY[j])) == 0)) {
                    return idx * ELM_SIZE + j;
                }
            }
        }
        idx++;
        while (idx < length && bits[idx] == (~0L)) {
            idx++;
        }
        if (idx == length) {
            return bssize;
        }
        for (int j = 0; j < ELM_SIZE; j++) {
            if (((bits[idx] & (TWO_N_ARRAY[j])) == 0)) {
                return (idx << OFFSET) + j;
            }
        }
        return bssize;
    }
    public boolean isEmpty() {
        if (!needClear) {
            return true;
        }
        int length = bits.length;
        for (int idx = 0; idx < length; idx++) {
            if (bits[idx] != 0L) {
                return false;
            }
        }
        return true;
    }
    public int cardinality() {
        if (!needClear) {
            return 0;
        }
        int count = 0;
        int length = bits.length;
        for (int idx = 0; idx < length; idx++) {
            count += pop(bits[idx] & 0xffffffffL);
            count += pop(bits[idx] >>> 32);
        }
        return count;
    }
    private final int pop(long x) {
        x = x - ((x >>> 1) & 0x55555555);
        x = (x & 0x33333333) + ((x >>> 2) & 0x33333333);
        x = (x + (x >>> 4)) & 0x0f0f0f0f;
        x = x + (x >>> 8);
        x = x + (x >>> 16);
        return (int) x & 0x0000003f;
    }
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        this.isLengthActual = false;
        this.actualArrayLength = bits.length;
        this.needClear = this.getActualArrayLength() != 0;
    }
}
