public class CK_ECDH2_DERIVE_PARAMS {
    public long kdf;
    public byte[] pSharedData;
    public byte[] pPublicData;
    public long ulPrivateDataLen;
    public long hPrivateData;
    public byte[] pPublicData2;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("kdf: 0x");
        buffer.append(Functions.toFullHexString(kdf));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSharedDataLen: ");
        buffer.append(pSharedData.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSharedData: ");
        buffer.append(Functions.toHexString(pSharedData));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPublicDataLen: ");
        buffer.append(pPublicData.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPublicData: ");
        buffer.append(Functions.toHexString(pPublicData));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulPrivateDataLen: ");
        buffer.append(ulPrivateDataLen);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hPrivateData: ");
        buffer.append(hPrivateData);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPublicDataLen2: ");
        buffer.append(pPublicData2.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPublicData2: ");
        buffer.append(Functions.toHexString(pPublicData2));
        return buffer.toString();
    }
}
