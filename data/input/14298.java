public class ActivityRequiredException extends java.rmi.RemoteException
{
    public ActivityRequiredException() { super(); }
    public ActivityRequiredException(String message) {
        super(message);
    }
    public ActivityRequiredException(Throwable cause) {
        this("", cause);
    }
    public ActivityRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
