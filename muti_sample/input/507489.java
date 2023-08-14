public class StackFrame {
    public static final int NO_LINE_NUMBER          =   0;
    public static final int UNKNOWN_LOCATION        =   -1;
    public static final int COMPILED_METHOD         =   -2;
    public static final int NATIVE_METHOD           =   -3;
    long mId;
    String mMethodName;
    String mSignature;
    String mFilename;
    int mSerialNumber;
    int mLineNumber;
    public StackFrame(long id, String method, String sig, String file, 
            int serial, int line) {
        mId = id;
        mMethodName = method;
        mSignature = sig;
        mFilename = file;
        mSerialNumber = serial;
        mLineNumber = line;
    }
    private final String lineNumberString() {
        switch (mLineNumber) {
            case NO_LINE_NUMBER:    return "No line number";
            case UNKNOWN_LOCATION:  return "Unknown line number";
            case COMPILED_METHOD:   return "Compiled method";
            case NATIVE_METHOD:     return "Native method";
            default:                return String.valueOf(mLineNumber);
        }
    }
    public final String toString() {
        return mMethodName 
            + mSignature.replace('/', '.') 
            + " - " 
            + mFilename + ":" 
            + lineNumberString();
    }
}
