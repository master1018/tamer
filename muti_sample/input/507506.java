public class ErrorManager {
    public static final int GENERIC_FAILURE = 0;
    public static final int WRITE_FAILURE = 1;
    public static final int FLUSH_FAILURE = 2;
    public static final int CLOSE_FAILURE = 3;
    public static final int OPEN_FAILURE = 4;
    public static final int FORMAT_FAILURE = 5;
    @SuppressWarnings("nls")
    private static final String[] FAILURES = new String[] { "GENERIC_FAILURE",
            "WRITE_FAILURE", "FLUSH_FAILURE", "CLOSE_FAILURE", "OPEN_FAILURE",
            "FORMAT_FAILURE" };
    private boolean called;
    public ErrorManager() {
        super();
    }
    public void error(String message, Exception exception, int errorCode) {
        synchronized (this) {
            if (called) {
                return;
            }
            called = true;
        }
        System.err.println(this.getClass().getName()
                + ": " + FAILURES[errorCode]); 
        if (message != null) {
            System.err.println(Messages.getString("logging.1E", message)); 
        }
        if (exception != null) {
            System.err.println(Messages.getString("logging.1F", exception)); 
        }
    }
}
