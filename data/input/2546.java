public class CK_PKCS5_PBKD2_PARAMS {
    public long saltSource;
    public byte[] pSaltSourceData;
    public long iterations;
    public long prf;
    public byte[] pPrfData;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("saltSource: ");
        buffer.append(saltSource);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSaltSourceData: ");
        buffer.append(Functions.toHexString(pSaltSourceData));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulSaltSourceDataLen: ");
        buffer.append(pSaltSourceData.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("iterations: ");
        buffer.append(iterations);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("prf: ");
        buffer.append(prf);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPrfData: ");
        buffer.append(Functions.toHexString(pPrfData));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulPrfDataLen: ");
        buffer.append(pPrfData.length);
        return buffer.toString();
    }
}
