public class CallForwardInfo {
    public int             status;      
    public int             reason;      
    public int             serviceClass; 
    public int             toa;         
    public String          number;      
    public int             timeSeconds; 
    public String toString() {
        return super.toString() + (status == 0 ? " not active " : " active ")
            + " reason: " + reason
            + " serviceClass: " + serviceClass
            + " \"" + PhoneNumberUtils.stringFromStringAndTOA(number, toa) + "\" "
            + timeSeconds + " seconds";
    }
}
