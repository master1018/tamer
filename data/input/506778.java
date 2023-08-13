public class InvalidClassException extends ObjectStreamException {
    private static final long serialVersionUID = -4333316296251054416L;
    public String classname;
    public InvalidClassException(String detailMessage) {
        super(detailMessage);
    }
    public InvalidClassException(String className, String detailMessage) {
        super(detailMessage);
        this.classname = className;
    }
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (classname != null) {
            msg = classname + "; " + msg; 
        }
        return msg;
    }
}
