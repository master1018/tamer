public class GetLongAction implements java.security.PrivilegedAction<Long> {
    private String theProp;
    private long defaultVal;
    private boolean defaultSet = false;
    public GetLongAction(String theProp) {
        this.theProp = theProp;
    }
    public GetLongAction(String theProp, long defaultVal) {
        this.theProp = theProp;
        this.defaultVal = defaultVal;
        this.defaultSet = true;
    }
    public Long run() {
        Long value = Long.getLong(theProp);
        if ((value == null) && defaultSet)
            return new Long(defaultVal);
        return value;
    }
}
