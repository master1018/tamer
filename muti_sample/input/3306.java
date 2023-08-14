public class ActivityCompletedException extends java.rmi.RemoteException
{
    public ActivityCompletedException() { super(); }
    public ActivityCompletedException(String message) {
        super(message);
    }
    public ActivityCompletedException(Throwable cause) {
        this("", cause);
    }
    public ActivityCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
