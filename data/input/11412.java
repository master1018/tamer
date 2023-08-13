public class CK_SSL3_RANDOM_DATA {
    public byte[] pClientRandom;
    public byte[] pServerRandom;
    public CK_SSL3_RANDOM_DATA(byte[] clientRandom, byte[] serverRandom) {
        pClientRandom = clientRandom;
        pServerRandom = serverRandom;
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Constants.INDENT);
        buffer.append("pClientRandom: ");
        buffer.append(Functions.toHexString(pClientRandom));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulClientRandomLen: ");
        buffer.append(pClientRandom.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pServerRandom: ");
        buffer.append(Functions.toHexString(pServerRandom));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulServerRandomLen: ");
        buffer.append(pServerRandom.length);
        return buffer.toString();
    }
}
