public class InvalidTestResultStringException extends Exception {
    private String mTestResultString;
    public InvalidTestResultStringException(String resultString) {
        super();
        mTestResultString = resultString;
    }
    @Override
    public String getMessage() {
        return "Invalid test result string: " + mTestResultString;
    }
}
