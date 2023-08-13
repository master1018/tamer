public class CK_SESSION_INFO {
    public long slotID;
    public long state;
    public long flags;          
    public long ulDeviceError;  
    public CK_SESSION_INFO(long slotID, long state,
                           long flags, long ulDeviceError) {
        this.slotID = slotID;
        this.state = state;
        this.flags = flags;
        this.ulDeviceError = ulDeviceError;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("slotID: ");
        buffer.append(String.valueOf(slotID));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("state: ");
        buffer.append(Functions.sessionStateToString(state));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("flags: ");
        buffer.append(Functions.sessionInfoFlagsToString(flags));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulDeviceError: ");
        buffer.append(Functions.toHexString(ulDeviceError));
        return buffer.toString() ;
    }
}
