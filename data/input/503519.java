public class AsyncResult
{
    public Object userObj;
    public Throwable exception;
    public Object result;
    public static AsyncResult 
    forMessage(Message m, Object r, Throwable ex)
    {
        AsyncResult ret;
        ret = new AsyncResult (m.obj, r, ex);
        m.obj = ret; 
        return ret;
    }
    public static AsyncResult 
    forMessage(Message m)
    {
        AsyncResult ret;
        ret = new AsyncResult (m.obj, null, null);
        m.obj = ret; 
        return ret;
    }
    public 
    AsyncResult (Object uo, Object r, Throwable ex)
    {
        userObj = uo;
        result = r;
        exception = ex;
    }
}
