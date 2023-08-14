public final class BitString {
    private static final byte[] SET_MASK = { (byte) 128, 64, 32, 16, 8, 4, 2, 1 };
    private static final byte[] RESET_MASK = { 0x7f, (byte) 0xbf, (byte) 0xdf,
            (byte) 0xef, (byte) 0xf7, (byte) 0xfb, (byte) 0xfd, (byte) 0xfe, };
    public final byte[] bytes;
    public final int unusedBits;
    public BitString(byte[] bytes, int unusedBits) {
        if (unusedBits < 0 || unusedBits > 7) {
            throw new IllegalArgumentException(
                    Messages.getString("security.13D")); 
        }
        if (bytes.length == 0 && unusedBits != 0) {
            throw new IllegalArgumentException(
                    Messages.getString("security.13E")); 
        }
        this.bytes = bytes;
        this.unusedBits = unusedBits;
    }
    public BitString(boolean[] values) {
        unusedBits = values.length % 8;
        int size = values.length / 8;
        if (unusedBits != 0) {
            size++;
        }
        bytes = new byte[size];
        for (int i = 0; i < values.length; i++) {
            setBit(i, values[i]);
        }
    }
    public boolean getBit(int bit) {
        int offset = bit % 8;
        int index = bit / 8;
        return (bytes[index] & SET_MASK[offset]) != 0;
    }
    public void setBit(int bit, boolean value) {
        int offset = bit % 8;
        int index = bit / 8;
        if (value) {
            bytes[index] |= SET_MASK[offset];
        } else {
            bytes[index] &= RESET_MASK[offset];
        }
    }
    public boolean[] toBooleanArray() {
        boolean[] result = new boolean[bytes.length * 8 - unusedBits];
        for (int i = 0; i < result.length; i++) {
            result[i] = getBit(i);
        }
        return result;
    }
}
