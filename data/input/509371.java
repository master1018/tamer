public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public NotImplementedException() {
        this(System.err);
    }
    @SuppressWarnings("nls")
    public NotImplementedException(PrintStream stream) {
        super();
        stream.println("*** NOT IMPLEMENTED EXCEPTION ***");
        StackTraceElement thrower = getStackTrace()[0];
        stream.println("*** thrown from class  -> " + thrower.getClassName());
        stream.println("***             method -> " + thrower.getMethodName());
        stream.print("*** defined in         -> ");
        if (thrower.isNativeMethod()) {
            stream.println("a native method");
        } else {
            String fileName = thrower.getFileName();
            if (fileName == null) {
                stream.println("an unknown source");
            } else {
                int lineNumber = thrower.getLineNumber();
                stream.print("the file \"" + fileName + "\"");
                if (lineNumber >= 0) {
                    stream.print(" on line #" + lineNumber);
                }
                stream.println();
            }
        }
    }
    public NotImplementedException(String detailMessage) {
        super(detailMessage);
    }
    public NotImplementedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public NotImplementedException(Throwable throwable) {
        super(throwable);
    }
}
