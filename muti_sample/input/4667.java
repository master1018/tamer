public class CK_SSL3_MASTER_KEY_DERIVE_PARAMS {
    public CK_SSL3_RANDOM_DATA RandomInfo;
    public CK_VERSION pVersion;
    public CK_SSL3_MASTER_KEY_DERIVE_PARAMS(CK_SSL3_RANDOM_DATA random, CK_VERSION version) {
        RandomInfo = random;
        pVersion = version;
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Constants.INDENT);
        buffer.append("RandomInfo: ");
        buffer.append(RandomInfo);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pVersion: ");
        buffer.append(pVersion);
        return buffer.toString();
    }
}
