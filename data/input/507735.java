public class LogAbortException extends Exception {
    private final String mFormat;
    private final Object[] mArgs;
    public LogAbortException(String format, Object... args) {
        mFormat = format;
        mArgs = args;
    }
    public void error(Log log) {
        log.error(mFormat, mArgs);
    }
}
