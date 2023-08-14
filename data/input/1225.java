public class InvocationException extends Exception
{
    ObjectReference exception;
    public InvocationException(ObjectReference exception)
    {
        super("Exception occurred in target VM");
        this.exception = exception;
    }
    public ObjectReference exception()
    {
        return exception;
    }
}
