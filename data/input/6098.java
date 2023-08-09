public class InvalidActivityException extends java.rmi.RemoteException
{
    public InvalidActivityException() { super(); }
    public InvalidActivityException(String message) {
        super(message);
    }
    public InvalidActivityException(Throwable cause) {
        this("", cause);
    }
    public InvalidActivityException(String message, Throwable cause) {
        super(message, cause);
    }
}
