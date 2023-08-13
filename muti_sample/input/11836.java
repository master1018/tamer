public class CK_RSA_PKCS_PSS_PARAMS {
    public long hashAlg;
    public long mgf;
    public long sLen;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("hashAlg: 0x");
        buffer.append(Functions.toFullHexString(hashAlg));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("mgf: 0x");
        buffer.append(Functions.toFullHexString(mgf));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("sLen: ");
        buffer.append(sLen);
        return buffer.toString();
    }
}
