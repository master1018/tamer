public class GSSExceptionImpl extends GSSException {
    private static final long serialVersionUID = 4251197939069005575L;
    private String majorMessage;
    GSSExceptionImpl(int majorCode, Oid mech) {
        super(majorCode);
        this.majorMessage = super.getMajorString() + ": " + mech;
    }
    public GSSExceptionImpl(int majorCode, String majorMessage) {
        super(majorCode);
        this.majorMessage = majorMessage;
    }
    public GSSExceptionImpl(int majorCode, Exception cause) {
        super(majorCode);
        initCause(cause);
    }
    public GSSExceptionImpl(int majorCode, String majorMessage,
        Exception cause) {
        this(majorCode, majorMessage);
        initCause(cause);
    }
    public String getMessage() {
        if (majorMessage != null)
            return majorMessage;
        else
            return super.getMessage();
    }
}
