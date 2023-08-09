public class DOMAccessException extends Exception
{
    public DOMAccessException()
    {
        this(null, null);
    }
    public DOMAccessException(String msg)
    {
        this(null, msg);
    }
    public DOMAccessException(Exception e)
    {
        this(e, null);
    }
    public DOMAccessException(Exception e, String msg)
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
