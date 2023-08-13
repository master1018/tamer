public class LogPrinter implements Printer {
    private final int mPriority;
    private final String mTag;
    private final int mBuffer;
    public LogPrinter(int priority, String tag) {
        mPriority = priority;
        mTag = tag;
        mBuffer = Log.LOG_ID_MAIN;
    }
    public LogPrinter(int priority, String tag, int buffer) {
        mPriority = priority;
        mTag = tag;
        mBuffer = buffer;
    }
    public void println(String x) {
        Log.println_native(mBuffer, mPriority, mTag, x);
    }
}
