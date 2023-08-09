public class CK_INFO {
    public CK_VERSION cryptokiVersion;
    public char[] manufacturerID;
    public long flags;
    public char[] libraryDescription;
    public CK_VERSION libraryVersion;
    public CK_INFO(CK_VERSION cryptoVer, char[] vendor, long flags,
                   char[] libDesc, CK_VERSION libVer) {
        this.cryptokiVersion = cryptoVer;
        this.manufacturerID = vendor;
        this.flags = flags;
        this.libraryDescription = libDesc;
        this.libraryVersion = libVer;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("cryptokiVersion: ");
        buffer.append(cryptokiVersion.toString());
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("manufacturerID: ");
        buffer.append(new String(manufacturerID));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("flags: ");
        buffer.append(Functions.toBinaryString(flags));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("libraryDescription: ");
        buffer.append(new String(libraryDescription));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("libraryVersion: ");
        buffer.append(libraryVersion.toString());
        return buffer.toString() ;
    }
}
