class EMMARuntimeException extends AbstractRuntimeException
{
    public EMMARuntimeException ()
    {
    }
    public EMMARuntimeException (final String message)
    {
        super (message);
    }
    public EMMARuntimeException (final String message, final Object [] arguments)
    {
        super (message, arguments);
    }
    public EMMARuntimeException (final Throwable cause)
    {
        super (cause);
    }
    public EMMARuntimeException (final String message, final Throwable cause)
    {
        super (message, cause);
    }
    public EMMARuntimeException (final String message, final Object [] arguments, final Throwable cause)
    {
        super (message, arguments, cause);
    }
} 
