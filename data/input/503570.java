public class Support_BitSet {
    private long[] bits;
    private static final int ELM_SIZE = 64; 
    public Support_BitSet() {
        this(64);
    }
    public Support_BitSet(int nbits) {
        if (nbits >= 0) {
            bits = new long[(nbits / ELM_SIZE) + (nbits % ELM_SIZE > 0 ? 1 : 0)];
        } else {
            throw new NegativeArraySizeException();
        }
    }
    public void clear(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                bits[pos / ELM_SIZE] &= ~(1L << (pos % ELM_SIZE));
            } else {
                growBits(pos); 
            }
        } else {
            throw new IndexOutOfBoundsException("Negative index specified");
        }
    }
    public boolean get(int pos) {
        if (pos >= 0) {
            if (pos < bits.length * ELM_SIZE) {
                return (bits[pos / ELM_SIZE] & (1L << (pos % ELM_SIZE))) != 0;
            }
            return false;
        }
        throw new IndexOutOfBoundsException("Negative index specified");
    }
    private void growBits(int pos) {
        pos++; 
        long[] tempBits = new long[(pos / ELM_SIZE)
                + (pos % ELM_SIZE > 0 ? 1 : 0)];
        System.arraycopy(bits, 0, tempBits, 0, bits.length);
        bits = tempBits;
    }
    public void set(int pos) {
        if (pos >= 0) {
            if (pos >= bits.length * ELM_SIZE) {
                growBits(pos);
            }
            bits[pos / ELM_SIZE] |= 1L << (pos % ELM_SIZE);
        } else {
            throw new IndexOutOfBoundsException("Negative index specified");
        }
    }
    public int size() {
        return bits.length * ELM_SIZE;
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(bits.length / 2);
        int bitCount = 0;
        sb.append('{');
        boolean comma = false;
        for (long element : bits) {
            if (element == 0) {
                bitCount += ELM_SIZE;
                continue;
            }
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((element & (1L << j)) != 0)) {
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
    public int length() {
        int idx = bits.length - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        if (idx == -1) {
            return 0;
        }
        int i = ELM_SIZE - 1;
        long val = bits[idx];
        while ((val & (1L << i)) == 0 && i > 0) {
            i--;
        }
        return idx * ELM_SIZE + i + 1;
    }
}
