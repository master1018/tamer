public class UnknownException extends org.omg.CORBA.SystemException {
    public Throwable originalEx;
    public UnknownException(Throwable ex) {
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx = ex;
    }
}
