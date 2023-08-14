public class IvParameterSpec implements AlgorithmParameterSpec {
    private final byte[] iv;
    public IvParameterSpec(byte[] iv) {
        if (iv == null) {
            throw new NullPointerException(Messages.getString("crypto.38")); 
        }
        this.iv = new byte[iv.length];
        System.arraycopy(iv, 0, this.iv, 0, iv.length);
    }
    public IvParameterSpec(byte[] iv, int offset, int len) {
        if ((iv == null) || (iv.length - offset < len)) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.39")); 
        }
        if (offset < 0 || len < 0) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("crypto.3A")); 
        }
        this.iv = new byte[len];
        System.arraycopy(iv, offset, this.iv, 0, len);
    }
    public byte[] getIV() {
        byte[] res = new byte[iv.length];
        System.arraycopy(iv, 0, res, 0, iv.length);
        return res;
    }
}
