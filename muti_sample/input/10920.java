public class GSSException extends Exception {
    private static final long serialVersionUID = -2706218945227726672L;
    public static final int BAD_BINDINGS = 1; 
    public static final int BAD_MECH = 2;
    public static final int BAD_NAME = 3;
    public static final int BAD_NAMETYPE = 4;
    public static final int BAD_STATUS = 5;
    public static final int BAD_MIC = 6;
    public static final int CONTEXT_EXPIRED = 7;
    public static final int CREDENTIALS_EXPIRED  = 8;
    public static final int DEFECTIVE_CREDENTIAL = 9;
    public static final int DEFECTIVE_TOKEN = 10;
    public static final int FAILURE = 11;
    public static final int NO_CONTEXT = 12;
    public static final int NO_CRED = 13;
    public static final int BAD_QOP = 14;
    public static final int UNAUTHORIZED = 15;
    public static final int UNAVAILABLE = 16;
    public static final int DUPLICATE_ELEMENT = 17;
    public static final int NAME_NOT_MN = 18;
    public static final int DUPLICATE_TOKEN = 19;
    public static final int OLD_TOKEN = 20;
    public static final int UNSEQ_TOKEN = 21;
    public static final int GAP_TOKEN = 22;
    private static String[] messages = {
        "Channel binding mismatch", 
        "Unsupported mechanism requested", 
        "Invalid name provided", 
        "Name of unsupported type provided", 
        "Invalid input status selector", 
        "Token had invalid integrity check", 
        "Specified security context expired", 
        "Expired credentials detected", 
        "Defective credential detected", 
        "Defective token detected", 
        "Failure unspecified at GSS-API level", 
        "Security context init/accept not yet called or context deleted",
        "No valid credentials provided", 
        "Unsupported QOP value", 
        "Operation unauthorized", 
        "Operation unavailable", 
        "Duplicate credential element requested", 
        "Name contains multi-mechanism elements", 
        "The token was a duplicate of an earlier token", 
        "The token's validity period has expired", 
        "A later token has already been processed", 
        "An expected per-message token was not received", 
    };
    private int major;
    private int minor = 0;
    private String minorMessage = null;
    private String majorString = null;
    public GSSException (int majorCode) {
        if (validateMajor(majorCode))
            major = majorCode;
        else
            major = FAILURE;
    }
    GSSException (int majorCode, String majorString) {
        if (validateMajor(majorCode))
            major = majorCode;
        else
            major = FAILURE;
        this.majorString = majorString;
    }
    public GSSException (int majorCode, int minorCode, String minorString) {
        if (validateMajor(majorCode))
            major = majorCode;
        else
            major = FAILURE;
        minor = minorCode;
        minorMessage = minorString;
    }
    public int getMajor() {
        return major;
    }
    public int  getMinor(){
        return minor;
    }
    public String getMajorString() {
        if (majorString != null)
            return majorString;
        else
            return messages[major - 1];
    }
    public String getMinorString() {
        return minorMessage;
    }
    public void setMinor(int minorCode, String message) {
        minor = minorCode;
        minorMessage = message;
    }
    public String toString() {
        return ("GSSException: " + getMessage());
    }
    public String getMessage() {
        if (minor == 0)
            return (getMajorString());
        return (getMajorString()
                + " (Mechanism level: " + getMinorString() + ")");
    }
    private boolean validateMajor(int major) {
        if (major > 0 && major <= messages.length)
            return (true);
        return (false);
    }
}
