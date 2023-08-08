public class ParserException extends Exception {
    private String errorMsg = null;
    public ParserException(String message) {
        errorMsg = "\nParser failed:\n";
        errorMsg = errorMsg + message;
    }
    public String getMessage() {
        return errorMsg;
    }
}
