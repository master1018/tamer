public class CK_PBE_PARAMS {
    public char[] pInitVector;
    public char[] pPassword;
    public char[] pSalt;
    public long ulIteration;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("pInitVector: ");
        buffer.append(pInitVector);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulPasswordLen: ");
        buffer.append(pPassword.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pPassword: ");
        buffer.append(pPassword);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulSaltLen: ");
        buffer.append(pSalt.length);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("pSalt: ");
        buffer.append(pSalt);
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulIteration: ");
        buffer.append(ulIteration);
        return buffer.toString();
    }
}
