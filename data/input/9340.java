public class GetPropertyAction implements java.security.PrivilegedAction {
    private String theProp;
    private String defaultVal;
    public GetPropertyAction(String theProp) {
        this.theProp = theProp;
    }
    public GetPropertyAction(String theProp, String defaultVal) {
        this.theProp = theProp;
        this.defaultVal = defaultVal;
    }
    public Object run() {
        String value = System.getProperty(theProp);
        return (value == null) ? defaultVal : value;
    }
}
