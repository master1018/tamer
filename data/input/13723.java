public class CK_SSL3_KEY_MAT_OUT{
    public long hClientMacSecret;
    public long hServerMacSecret;
    public long hClientKey;
    public long hServerKey;
    public byte[] pIVClient;
    public byte[] pIVServer;
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Constants.INDENT);
        buffer.append("hClientMacSecret: ");
        buffer.append(hClientMacSecret);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hServerMacSecret: ");
        buffer.append(hServerMacSecret);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hClientKey: ");
        buffer.append(hClientKey);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hServerKey: ");
        buffer.append(hServerKey);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pIVClient: ");
        buffer.append(Functions.toHexString(pIVClient));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pIVServer: ");
        buffer.append(Functions.toHexString(pIVServer));
        return buffer.toString();
    }
}
