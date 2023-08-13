public class CK_SSL3_KEY_MAT_PARAMS{
    public long ulMacSizeInBits;
    public long ulKeySizeInBits;
    public long ulIVSizeInBits;
    public boolean bIsExport;
    public CK_SSL3_RANDOM_DATA RandomInfo;
    public CK_SSL3_KEY_MAT_OUT pReturnedKeyMaterial;
    public CK_SSL3_KEY_MAT_PARAMS(int macSize, int keySize, int ivSize, boolean export, CK_SSL3_RANDOM_DATA random) {
        ulMacSizeInBits = macSize;
        ulKeySizeInBits = keySize;
        ulIVSizeInBits = ivSize;
        bIsExport = export;
        RandomInfo = random;
        pReturnedKeyMaterial = new CK_SSL3_KEY_MAT_OUT();
        if (ivSize != 0) {
            int n = ivSize >> 3;
            pReturnedKeyMaterial.pIVClient = new byte[n];
            pReturnedKeyMaterial.pIVServer = new byte[n];
        }
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Constants.INDENT);
        buffer.append("ulMacSizeInBits: ");
        buffer.append(ulMacSizeInBits);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulKeySizeInBits: ");
        buffer.append(ulKeySizeInBits);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulIVSizeInBits: ");
        buffer.append(ulIVSizeInBits);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("bIsExport: ");
        buffer.append(bIsExport);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("RandomInfo: ");
        buffer.append(RandomInfo);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pReturnedKeyMaterial: ");
        buffer.append(pReturnedKeyMaterial);
        return buffer.toString();
    }
}
