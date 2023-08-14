public class GetBooleanAction
        implements java.security.PrivilegedAction<Boolean> {
    private String theProp;
    public GetBooleanAction(String theProp) {
        this.theProp = theProp;
    }
    public Boolean run() {
        return Boolean.getBoolean(theProp);
    }
}
