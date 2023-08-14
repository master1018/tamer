public class CK_MECHANISM_INFO {
    public long ulMinKeySize;
    public long ulMaxKeySize;
    public long flags;
    public CK_MECHANISM_INFO(long minKeySize, long maxKeySize,
                             long flags) {
        this.ulMinKeySize = minKeySize;
        this.ulMaxKeySize = maxKeySize;
        this.flags = flags;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("ulMinKeySize: ");
        buffer.append(String.valueOf(ulMinKeySize));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulMaxKeySize: ");
        buffer.append(String.valueOf(ulMaxKeySize));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("flags: ");
        buffer.append(String.valueOf(flags));
        buffer.append(" = ");
        buffer.append(Functions.mechanismInfoFlagsToString(flags));
        return buffer.toString() ;
    }
}
