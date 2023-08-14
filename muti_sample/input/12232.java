public class GetIntegerAction
        implements java.security.PrivilegedAction<Integer> {
    private String theProp;
    private int defaultVal;
    private boolean defaultSet = false;
    public GetIntegerAction(String theProp) {
        this.theProp = theProp;
    }
    public GetIntegerAction(String theProp, int defaultVal) {
        this.theProp = theProp;
        this.defaultVal = defaultVal;
        this.defaultSet = true;
    }
    public Integer run() {
        Integer value = Integer.getInteger(theProp);
        if ((value == null) && defaultSet)
            return new Integer(defaultVal);
        return value;
    }
}
