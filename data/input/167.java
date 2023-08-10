public class UserInfoException extends RuntimeException {
    public UserInfoException() {
        super();
    }
    public UserInfoException(String msg) {
        super(msg);
    }
    public UserInfoException(Exception cause) {
        super(cause);
    }
    public UserInfoException(String msg, Exception cause) {
        super(msg, cause);
    }
}
