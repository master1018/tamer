abstract class AbstractException extends Exception implements ICodedException, IThrowableWrapper
{
    public AbstractException ()
    {
        m_cause = null;
        m_arguments = null;
    }
    public AbstractException (final String message)
    {
        super (message);
        m_cause = null;
        m_arguments = null;
    }
    public AbstractException (final String message, final Object [] arguments)
    {
        super (message);
        m_cause = null;
        m_arguments = arguments == null ? null : (Object []) arguments.clone ();
    }
    public AbstractException (final Throwable cause)
    {
        super ();
        m_cause = cause;
        m_arguments = null;
    }
    public AbstractException (final String message, final Throwable cause)
    {
        super (message);
        m_cause = cause;
        m_arguments = null;
    }
    public AbstractException (final String message, final Object [] arguments, final Throwable cause)
    {
        super (message);
        m_cause = cause;
        m_arguments = arguments == null ? null : (Object []) arguments.clone ();
    }
    public final String getMessage ()
    {
        if (m_message == null) 
        {
            String msg;
            final String supermsg = super.getMessage ();
            final Class _class = getClass ();
            if (m_arguments == null)
            {
                msg = ExceptionCommon.getMessage (_class, supermsg);
            }
            else
            {
                msg = ExceptionCommon.getMessage (_class, supermsg, m_arguments);
            }
            if (msg == null)
            {
                final String className = _class.getName ();
                msg = (supermsg != null) ? (className + ": " + supermsg) : className;
            }
            m_message = msg;
        }
        return m_message;
    }
    public final String getLocalizedMessage ()
    {
        return getMessage ();
    }
    public final void printStackTrace ()
    {
        ExceptionCommon.printStackTrace (this, System.out);
    }
    public final void printStackTrace (final PrintStream s)
    {
        ExceptionCommon.printStackTrace (this, s);
    }
    public final void printStackTrace (final PrintWriter s)
    {
        ExceptionCommon.printStackTrace (this, s);
    }
    public final String getErrorCode ()
    {
        return super.getMessage ();
    }
    public final Throwable getCause ()
    {
        return m_cause;
    }
    public void __printStackTrace (final PrintStream ps)
    {
        super.printStackTrace (ps);
    }
    public void __printStackTrace (final PrintWriter pw)
    {
        super.printStackTrace (pw);
    }
    public static void addExceptionResource (final Class namespace,
                                             final String messageResourceBundleName)
    {
        ExceptionCommon.addExceptionResource (namespace, messageResourceBundleName);
    }
    private void writeObject (final ObjectOutputStream out)
        throws IOException
    {
        getMessage (); 
        out.defaultWriteObject ();
    }
    private String m_message; 
    private transient final Object [] m_arguments;
    private final Throwable m_cause;
} 
