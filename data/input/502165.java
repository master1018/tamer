class EMMAException extends AbstractException
{
    public EMMAException ()
    {
    }
    public EMMAException (final String message)
    {
        super (message);
    }
    public EMMAException (final String message, final Object [] arguments)
    {
        super (message, arguments);
    }
    public EMMAException (final Throwable cause)
    {
        super (cause);
    }
    public EMMAException (final String message, final Throwable cause)
    {
        super (message, cause);
    }
    public EMMAException (final String message, final Object [] arguments, final Throwable cause)
    {
        super (message, arguments, cause);
    }
} 
