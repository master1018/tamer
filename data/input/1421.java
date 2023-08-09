public class GCMParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv;
    private int tLen;
    public GCMParameterSpec(int tLen, byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("src array is null");
        }
        init(tLen, src, 0, src.length);
    }
    public GCMParameterSpec(int tLen, byte[] src, int offset, int len) {
        init(tLen, src, offset, len);
    }
    private void init(int tLen, byte[] src, int offset, int len) {
        if (tLen < 0) {
            throw new IllegalArgumentException(
                "Length argument is negative");
        }
        this.tLen = tLen;
        if ((src == null) ||(len < 0) || (offset < 0)
                || ((len + offset) > src.length)) {
            throw new IllegalArgumentException("Invalid buffer arguments");
        }
        iv = new byte[len];
        System.arraycopy(src, offset, iv, 0, len);
    }
    public int getTLen() {
        return tLen;
    }
    public byte[] getIV() {
        return iv.clone();
    }
}
