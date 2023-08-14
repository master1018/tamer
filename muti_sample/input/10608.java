public class CK_RSA_PKCS_OAEP_PARAMS {
    public long hashAlg;
    public long mgf;
    public long source;
    public byte[] pSourceData;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("hashAlg: ");
        buffer.append(hashAlg);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("mgf: ");
        buffer.append(mgf);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("source: ");
        buffer.append(source);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSourceData: ");
        buffer.append(pSourceData.toString());
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSourceDataLen: ");
        buffer.append(Functions.toHexString(pSourceData));
        return buffer.toString() ;
    }
}
