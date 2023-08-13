public class InvocationEvent extends AWTEvent implements ActiveEvent {
    private static final long serialVersionUID = 436056344909459450L;
    public static final int INVOCATION_FIRST = 1200;
    public static final int INVOCATION_DEFAULT = 1200;
    public static final int INVOCATION_LAST = 1200;
    protected Runnable runnable;
    protected Object notifier;
    protected boolean catchExceptions;
    private long when;
    private Throwable throwable;
    public InvocationEvent(Object source, Runnable runnable) {
        this(source, runnable, null, false);
    }
    public InvocationEvent(Object source, Runnable runnable, 
                           Object notifier, boolean catchExceptions) {
        this(source, INVOCATION_DEFAULT, runnable, notifier, catchExceptions);
    }
    protected InvocationEvent(Object source, int id, Runnable runnable,
            Object notifier, boolean catchExceptions)
    {
        super(source, id);
        assert runnable != null : Messages.getString("awt.18C"); 
        if (source == null) {
            throw new IllegalArgumentException(Messages.getString("awt.18D")); 
        }
        this.runnable = runnable;
        this.notifier = notifier;
        this.catchExceptions = catchExceptions;
        throwable = null;
        when = System.currentTimeMillis();
    }
    public void dispatch() {
        if (!catchExceptions) {
            runAndNotify();
        } else {
            try {
                runAndNotify();
            } catch (Throwable t) {
                throwable = t;
            }
        }
    }
    private void runAndNotify() {
        if (notifier != null) {
            synchronized(notifier) {
                try {
                    runnable.run();
                } finally {
                    notifier.notifyAll();
                }
            }
        } else {
            runnable.run();
        }
    }
    public Exception getException() {
        return (throwable != null && throwable instanceof Exception) ?
                (Exception)throwable : null;
    }
    public Throwable getThrowable() {
        return throwable;
    }
    public long getWhen() {
        return when;
    }
    @Override
    public String paramString() {
        return ((id == INVOCATION_DEFAULT ? "INVOCATION_DEFAULT" : "unknown type") + 
                ",runnable=" + runnable + 
                ",notifier=" + notifier + 
                ",catchExceptions=" + catchExceptions + 
                ",when=" + when); 
    }
}
