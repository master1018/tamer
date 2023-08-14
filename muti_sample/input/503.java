public class DBCSDecoderMapping {
    private short[] index1;
    private String[] index2;
    protected int start;
    protected int end;
    protected static final char REPLACE_CHAR='\uFFFD';
    public DBCSDecoderMapping(short[] index1, String[] index2,
                             int start, int end) {
        this.index1 = index1;
        this.index2 = index2;
        this.start = start;
        this.end = end;
    }
    protected char decodeSingle(int b) {
        if (b >= 0)
            return (char) b;
        return REPLACE_CHAR;
    }
    protected char decodeDouble(int byte1, int byte2) {
        if (((byte1 < 0) || (byte1 > index1.length))
            || ((byte2 < start) || (byte2 > end)))
            return REPLACE_CHAR;
        int n = (index1[byte1] & 0xf) * (end - start + 1) + (byte2 - start);
        return index2[index1[byte1] >> 4].charAt(n);
    }
}
