public class AuthenticationFailedException extends MessagingException {
    public static final long serialVersionUID = -1;
    public AuthenticationFailedException(String message) {
        super(MessagingException.AUTHENTICATION_FAILED, message);
    }
    public AuthenticationFailedException(String message, Throwable throwable) {
        super(message, throwable);
        mExceptionType = MessagingException.AUTHENTICATION_FAILED;
     }
}
