public class GetBooleanSecurityPropertyAction
        implements java.security.PrivilegedAction<Boolean> {
    private String theProp;
    public GetBooleanSecurityPropertyAction(String theProp) {
        this.theProp = theProp;
    }
    public Boolean run() {
        boolean b = false;
        try {
            String value = Security.getProperty(theProp);
            b = (value != null) && value.equalsIgnoreCase("true");
        } catch (NullPointerException e) {}
        return b;
    }
}
