abstract class ExceptionCommon implements IJREVersion
{
    public static ResourceBundle addExceptionResource (final Class namespace,
                                                       final String messageResourceBundleName)
    {
        if ((namespace != null) && (messageResourceBundleName != null)
            && (messageResourceBundleName.length () > 0))
        {
            if (! ABSTRACT_EXCEPTION.isAssignableFrom (namespace)
                && ! ABSTACT_RUNTIME_EXCEPTION.isAssignableFrom (namespace))
            {
                throw new Error ("addExceptionResource(): class [" + namespace +
                    "] is not a subclass of " + ABSTRACT_EXCEPTION.getName () +
                    " or " + ABSTACT_RUNTIME_EXCEPTION.getName ());
            }
            ResourceBundle temprb = null;
            String nameInNamespace = null;
            try
            {
                nameInNamespace = getNameInNamespace (namespace, messageResourceBundleName);
                ClassLoader loader = namespace.getClassLoader ();
                if (loader == null) loader = ClassLoader.getSystemClassLoader ();
                temprb = ResourceBundle.getBundle (nameInNamespace, Locale.getDefault (), loader);
            }
            catch (Throwable ignore)
            {
               temprb = null;
            }
            if (temprb != null)
            {
                synchronized (s_exceptionCodeMap)
                {
                    final ResourceBundle currentrb =
                        (ResourceBundle) s_exceptionCodeMap.get (namespace); 
                    if (currentrb != null)
                        return currentrb;
                    else
                    {
                        s_exceptionCodeMap.put (namespace, temprb);
                        return temprb;
                    }
                }
            }
        }
        return null;
    }
    static void printStackTrace (Throwable t, final PrintWriter out)
    {
        if (JRE_1_4_PLUS)
        {
            if (t instanceof IThrowableWrapper)
            {
                final IThrowableWrapper tw = (IThrowableWrapper) t;
                tw.__printStackTrace (out); 
            }
            else
            {
                t.printStackTrace (out);
            }
        }
        else
        {
            for (boolean first = true; t != null; )
            {
                if (first)
                    first = false;
                else
                {
                    out.println ();
                    out.println (NESTED_THROWABLE_HEADER);                
                }
                if (t instanceof IThrowableWrapper)
                {
                    final IThrowableWrapper tw = (IThrowableWrapper) t;
                    tw.__printStackTrace (out);
                    t = tw.getCause (); 
                }
                else
                {
                    t.printStackTrace (out);
                    break;
                }
            }
        }
    }
    static void printStackTrace (Throwable t, final PrintStream out)
    {
        if (JRE_1_4_PLUS)
        {
            if (t instanceof IThrowableWrapper)
            {
                final IThrowableWrapper tw = (IThrowableWrapper) t;
                tw.__printStackTrace (out); 
            }
            else
            {
                t.printStackTrace (out);
            }
        }
        else
        {
            for (boolean first = true; t != null; )
            {
                if (first)
                    first = false;
                else
                {
                    out.println ();
                    out.println (NESTED_THROWABLE_HEADER);                
                }
                if (t instanceof IThrowableWrapper)
                {
                    final IThrowableWrapper tw = (IThrowableWrapper) t;
                    tw.__printStackTrace (out);
                    t = tw.getCause (); 
                }
                else
                {
                    t.printStackTrace (out);
                    break;
                }
            }
        }
    }
    static String getMessage (final Class namespace, final String code)
    {
        if (code == null) return null;
        try
        {                      
            if (code.length () > 0)
            {
                final String msg = lookup (namespace, code);     
                if (msg == null)
                {
                    return code;
                }
                else
                {
                    return EMBED_ERROR_CODE ? "[" + code + "] " + msg : msg;
                }
           }
           else
           {
               return "";
           }
        }
        catch (Throwable t)
        {
            return code;
        }
    }
    static String getMessage (final Class namespace, final String code, final Object [] arguments)
    {
        if (code == null) return null;
        final String pattern = getMessage (namespace, code);
        if ((arguments == null) || (arguments.length == 0))
        {
            return pattern;
        }
        else
        {
            try
            {
                return MessageFormat.format (pattern, arguments);
            }
            catch (Throwable t)
            {
                final StringBuffer msg = new StringBuffer (code + EOL);
                for (int a = 0; a < arguments.length; a ++)
                {
                    msg.append ("\t{" + a + "} = [");
                    final Object arg = arguments [a];
                    try
                    {
                        msg.append (arg.toString ());
                    }
                    catch (Throwable e) 
                    {
                        if (arg != null)
                            msg.append (arg.getClass ().getName ());
                        else
                            msg.append ("null");
                    }
                    msg.append ("]");
                    msg.append (EOL);
                }
                return msg.toString ();
            }
        }
    }
    private ExceptionCommon () {} 
    private static String lookup (Class namespace, final String propertyName)
    {
        if (propertyName == null) return null;
        if (namespace != null)
        {
            ResourceBundle rb;
            while (namespace != ABSTRACT_EXCEPTION && namespace != ABSTACT_RUNTIME_EXCEPTION
                   && namespace != THROWABLE && namespace != null)
            {
                synchronized (s_exceptionCodeMap)
                {
                    rb = (ResourceBundle) s_exceptionCodeMap.get (namespace);
                    if (rb == null)
                    {
                        if ((rb = addExceptionResource (namespace, "exceptions")) == null)
                        {
                            s_exceptionCodeMap.put (namespace, EMPTY_RESOURCE_BUNDLE);
                        }
                    }
                }
                if (rb != null)
                {
                    String propertyValue = null;
                    try
                    {
                        propertyValue = rb.getString (propertyName);
                    }
                    catch (Throwable ignore) {}
                    if (propertyValue != null) return propertyValue;
                }
                namespace = namespace.getSuperclass ();
            }
        }
        if (ROOT_RESOURCE_BUNDLE != null)
        {
            String propertyValue = null;
            try
            {
                propertyValue = ROOT_RESOURCE_BUNDLE.getString (propertyName);
            }
            catch (Throwable ignore) {}
            if (propertyValue != null) return propertyValue;
        }
        return null;
    }
    private static String getNameInNamespace (final Class namespace, final String name)
    {
        if (namespace == null) return name;
        final String namespaceName = namespace.getName ();
        final int lastDot = namespaceName.lastIndexOf ('.');
        if (lastDot <= 0)
            return name;
        else
            return namespaceName.substring (0, lastDot + 1)  + name;
    }
    private static final boolean EMBED_ERROR_CODE = true;
    private static final String ROOT_RESOURCE_BUNDLE_NAME;      
    private static final ResourceBundle ROOT_RESOURCE_BUNDLE;   
    private static final Map  s_exceptionCodeMap = new HashMap ();
    private static final String NESTED_THROWABLE_HEADER = "[NESTED EXCEPTION]:";
    private static final Class THROWABLE                    = Throwable.class;
    private static final Class ABSTRACT_EXCEPTION           = AbstractException.class;
    private static final Class ABSTACT_RUNTIME_EXCEPTION    = AbstractRuntimeException.class;
     static final Enumeration EMPTY_ENUMERATION  = Collections.enumeration (Collections.EMPTY_SET);    
    private static final ResourceBundle EMPTY_RESOURCE_BUNDLE = new ResourceBundle ()
    {
        public Object handleGetObject (final String key)
        {
            return null;
        }
        public Enumeration getKeys ()
        {
            return EMPTY_ENUMERATION;
        }
    };
    private static final String EOL = System.getProperty ("line.separator", "\n");
    static
    {
        ROOT_RESOURCE_BUNDLE_NAME = getNameInNamespace (ExceptionCommon.class, "exceptions");
        ResourceBundle temprb = null;
        try
        {
            temprb = ResourceBundle.getBundle (ROOT_RESOURCE_BUNDLE_NAME);
        }
        catch (Throwable ignore)
        {
        }
        ROOT_RESOURCE_BUNDLE = temprb;        
    }
} 
