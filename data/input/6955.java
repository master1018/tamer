public class CK_AES_CTR_PARAMS {
    private final long ulCounterBits;
    private final byte cb[];
    public CK_AES_CTR_PARAMS(byte[] cb) {
        ulCounterBits = 128;
        this.cb = cb.clone();
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("ulCounterBits: ");
        buffer.append(ulCounterBits);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("cb: ");
        buffer.append(Functions.toHexString(cb));
        return buffer.toString();
    }
}
