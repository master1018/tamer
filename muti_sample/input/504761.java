public class GLException extends RuntimeException {
    public GLException(final int error) {
        super(getErrorString(error));
        mError = error;
    }
    public GLException(final int error, final String string) {
        super(string);
        mError = error;
    }
    private static String getErrorString(int error) {
        String errorString = GLU.gluErrorString(error);
        if ( errorString == null ) {
            errorString = "Unknown error 0x" + Integer.toHexString(error);
        }
        return errorString;
    }
    int getError() {
        return mError;
    }
    private final int mError;
}
