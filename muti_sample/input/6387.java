public class CK_SLOT_INFO {
    public char[] slotDescription;
    public char[] manufacturerID;
    public long flags;
    public CK_VERSION hardwareVersion;
    public CK_VERSION firmwareVersion;
    public CK_SLOT_INFO(char[] slotDesc, char[] vendor,
                        long flags, CK_VERSION hwVer, CK_VERSION fwVer) {
        this.slotDescription = slotDesc;
        this.manufacturerID = vendor;
        this.flags = flags;
        this.hardwareVersion = hwVer;
        this.firmwareVersion = fwVer;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("slotDescription: ");
        buffer.append(new String(slotDescription));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("manufacturerID: ");
        buffer.append(new String(manufacturerID));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("flags: ");
        buffer.append(Functions.slotInfoFlagsToString(flags));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hardwareVersion: ");
        buffer.append(hardwareVersion.toString());
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("firmwareVersion: ");
        buffer.append(firmwareVersion.toString());
        return buffer.toString() ;
    }
}
