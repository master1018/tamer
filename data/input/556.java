public class DOMUnsupportedException extends Exception
{
    public DOMUnsupportedException()
    {
        this(null, null);
    }
    public DOMUnsupportedException(String msg)
    {
        this(null, msg);
    }
    public DOMUnsupportedException(Exception e)
    {
        this(e, null);
    }
    public DOMUnsupportedException(Exception e, String msg)
    {
        this.ex = e;
        this.msg = msg;
    }
    public String getMessage()
    {
        return msg;
    }
    public Throwable getCause()
    {
        return ex;
    }
    private Throwable ex;
    private String msg;
}
