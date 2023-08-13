public class Looper {
    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final ThreadLocal sThreadLocal = new ThreadLocal();
    final MessageQueue mQueue;
    volatile boolean mRun;
    Thread mThread;
    private Printer mLogging = null;
    private static Looper mMainLooper = null;
    public static final void prepare() {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper());
    }
    public static final void prepareMainLooper() {
        prepare();
        setMainLooper(myLooper());
        if (Process.supportsProcesses()) {
            myLooper().mQueue.mQuitAllowed = false;
        }
    }
    private synchronized static void setMainLooper(Looper looper) {
        mMainLooper = looper;
    }
    public synchronized static final Looper getMainLooper() {
        return mMainLooper;
    }
    public static final void loop() {
        Looper me = myLooper();
        MessageQueue queue = me.mQueue;
        while (true) {
            Message msg = queue.next(); 
            if (msg != null) {
                if (msg.target == null) {
                    return;
                }
                if (me.mLogging!= null) me.mLogging.println(
                        ">>>>> Dispatching to " + msg.target + " "
                        + msg.callback + ": " + msg.what
                        );
                msg.target.dispatchMessage(msg);
                if (me.mLogging!= null) me.mLogging.println(
                        "<<<<< Finished to    " + msg.target + " "
                        + msg.callback);
                msg.recycle();
            }
        }
    }
    public static final Looper myLooper() {
        return (Looper)sThreadLocal.get();
    }
    public void setMessageLogging(Printer printer) {
        mLogging = printer;
    }
    public static final MessageQueue myQueue() {
        return myLooper().mQueue;
    }
    private Looper() {
        mQueue = new MessageQueue();
        mRun = true;
        mThread = Thread.currentThread();
    }
    public void quit() {
        Message msg = Message.obtain();
        mQueue.enqueueMessage(msg, 0);
    }
    public Thread getThread() {
        return mThread;
    }
    public void dump(Printer pw, String prefix) {
        pw.println(prefix + this);
        pw.println(prefix + "mRun=" + mRun);
        pw.println(prefix + "mThread=" + mThread);
        pw.println(prefix + "mQueue=" + ((mQueue != null) ? mQueue : "(null"));
        if (mQueue != null) {
            synchronized (mQueue) {
                Message msg = mQueue.mMessages;
                int n = 0;
                while (msg != null) {
                    pw.println(prefix + "  Message " + n + ": " + msg);
                    n++;
                    msg = msg.next;
                }
                pw.println(prefix + "(Total messages: " + n + ")");
            }
        }
    }
    public String toString() {
        return "Looper{"
            + Integer.toHexString(System.identityHashCode(this))
            + "}";
    }
    static class HandlerException extends Exception {
        HandlerException(Message message, Throwable cause) {
            super(createMessage(cause), cause);
        }
        static String createMessage(Throwable cause) {
            String causeMsg = cause.getMessage();
            if (causeMsg == null) {
                causeMsg = cause.toString();
            }
            return causeMsg;
        }
    }
}
