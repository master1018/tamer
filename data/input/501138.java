public class Throwable implements java.io.Serializable {
    private static final long serialVersionUID = -3042686055658047285L;
    private String detailMessage;
    private Throwable cause = this;
    private volatile Object stackState;
    private StackTraceElement[] stackTrace;
    public Throwable() {
        super();
        fillInStackTrace();
    }
    public Throwable(String detailMessage) {
        this();
        this.detailMessage = detailMessage;
    }
    public Throwable(String detailMessage, Throwable throwable) {
        this();
        this.detailMessage = detailMessage;
        cause = throwable;
    }
    public Throwable(Throwable throwable) {
        this();
        this.detailMessage = throwable == null ? null : throwable.toString();
        cause = throwable;
    }
    public Throwable fillInStackTrace() {
        stackState = nativeFillInStackTrace();
        stackTrace = null;
        return this;
    }
    public String getMessage() {
        return detailMessage;
    }
    public String getLocalizedMessage() {
        return getMessage();
    }
    public StackTraceElement[] getStackTrace() {
        return getInternalStackTrace().clone();
    }
    public void setStackTrace(StackTraceElement[] trace) {
        StackTraceElement[] newTrace = trace.clone();
        for (java.lang.StackTraceElement element : newTrace) {
            if (element == null) {
                throw new NullPointerException();
            }
        }
        stackTrace = newTrace;
    }
    public void printStackTrace() {
        printStackTrace(System.err);
    }
    private static int countDuplicates(StackTraceElement[] currentStack,
            StackTraceElement[] parentStack) {
        int duplicates = 0;
        int parentIndex = parentStack.length;
        for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
            StackTraceElement parentFrame = parentStack[parentIndex];
            if (parentFrame.equals(currentStack[i])) {
                duplicates++;
            } else {
                break;
            }
        }
        return duplicates;
    }
    private StackTraceElement[] getInternalStackTrace() {
        if (stackTrace == null) {
            stackTrace = nativeGetStackTrace(stackState);
            stackState = null; 
        }
        return stackTrace;
    }
    public void printStackTrace(PrintStream err) {
        err.println(toString());
        StackTraceElement[] stack = getInternalStackTrace();
        for (java.lang.StackTraceElement element : stack) {
            err.println("\tat " + element);
        }
        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack = throwable.getInternalStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }
    public void printStackTrace(PrintWriter err) {
        err.println(toString());
        StackTraceElement[] stack = getInternalStackTrace();
        for (java.lang.StackTraceElement element : stack) {
            err.println("\tat " + element);
        }
        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack = throwable.getInternalStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }
    @Override
    public String toString() {
        String msg = getLocalizedMessage();
        String name = getClass().getName();
        if (msg == null) {
            return name;
        }
        return new StringBuffer(name.length() + 2 + msg.length()).append(name).append(": ")
                .append(msg).toString();
    }
    public Throwable initCause(Throwable throwable) {
        if (cause == this) {
            if (throwable != this) {
                cause = throwable;
                return this;
            }
            throw new IllegalArgumentException("Cause cannot be the receiver");
        }
        throw new IllegalStateException("Cause already initialized");
    }
    public Throwable getCause() {
        if (cause == this) {
            return null;
        }
        return cause;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        getInternalStackTrace();
        s.defaultWriteObject();
    }
    native private static Object nativeFillInStackTrace();
    native private static StackTraceElement[] nativeGetStackTrace(Object stackState);
}
