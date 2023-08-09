public class ImException extends Exception {
    private ImErrorInfo mError;
    public ImException(String message) {
        super(message);
    }
    public ImException(ImErrorInfo error) {
        super(error.getDescription());
        mError = error;
    }
    public ImException(Throwable cause) {
        super(cause);
    }
    public ImException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImException(int imErrorCode, String description) {
        this(new ImErrorInfo(imErrorCode, description));
    }
    public ImErrorInfo getImError() {
        return mError;
    }
    public void printStackTrace() {
        System.err.println("ImError: " + mError);
        super.printStackTrace();
    }
}
