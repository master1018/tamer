public class SimpleLog implements Log, Serializable {
    static protected final String systemPrefix =
        "org.apache.commons.logging.simplelog.";
    static protected final Properties simpleLogProps = new Properties();
    static protected final String DEFAULT_DATE_TIME_FORMAT =
        "yyyy/MM/dd HH:mm:ss:SSS zzz";
    static protected boolean showLogName = false;
    static protected boolean showShortName = true;
    static protected boolean showDateTime = false;
    static protected String dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
    static protected DateFormat dateFormatter = null;
    public static final int LOG_LEVEL_TRACE  = 1;
    public static final int LOG_LEVEL_DEBUG  = 2;
    public static final int LOG_LEVEL_INFO   = 3;
    public static final int LOG_LEVEL_WARN   = 4;
    public static final int LOG_LEVEL_ERROR  = 5;
    public static final int LOG_LEVEL_FATAL  = 6;
    public static final int LOG_LEVEL_ALL    = (LOG_LEVEL_TRACE - 1);
    public static final int LOG_LEVEL_OFF    = (LOG_LEVEL_FATAL + 1);
    private static String getStringProperty(String name) {
        String prop = null;
	try {
	    prop = System.getProperty(name);
	} catch (SecurityException e) {
	    ; 
	}
        return (prop == null) ? simpleLogProps.getProperty(name) : prop;
    }
    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : prop;
    }
    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : "true".equalsIgnoreCase(prop);
    }
    static {
        InputStream in = getResourceAsStream("simplelog.properties");
        if(null != in) {
            try {
                simpleLogProps.load(in);
                in.close();
            } catch(java.io.IOException e) {
            }
        }
        showLogName = getBooleanProperty( systemPrefix + "showlogname", showLogName);
        showShortName = getBooleanProperty( systemPrefix + "showShortLogname", showShortName);
        showDateTime = getBooleanProperty( systemPrefix + "showdatetime", showDateTime);
        if(showDateTime) {
            dateTimeFormat = getStringProperty(systemPrefix + "dateTimeFormat",
                                               dateTimeFormat);
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            } catch(IllegalArgumentException e) {
                dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
        }
    }
    protected String logName = null;
    protected int currentLogLevel;
    private String shortLogName = null;
    public SimpleLog(String name) {
        logName = name;
        setLevel(SimpleLog.LOG_LEVEL_INFO);
        String lvl = getStringProperty(systemPrefix + "log." + logName);
        int i = String.valueOf(name).lastIndexOf(".");
        while(null == lvl && i > -1) {
            name = name.substring(0,i);
            lvl = getStringProperty(systemPrefix + "log." + name);
            i = String.valueOf(name).lastIndexOf(".");
        }
        if(null == lvl) {
            lvl =  getStringProperty(systemPrefix + "defaultlog");
        }
        if("all".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ALL);
        } else if("trace".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_TRACE);
        } else if("debug".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        } else if("info".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_INFO);
        } else if("warn".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_WARN);
        } else if("error".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ERROR);
        } else if("fatal".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_FATAL);
        } else if("off".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_OFF);
        }
    }
    public void setLevel(int currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }
    public int getLevel() {
        return currentLogLevel;
    }
    protected void log(int type, Object message, Throwable t) {
        StringBuffer buf = new StringBuffer();
        if(showDateTime) {
            buf.append(dateFormatter.format(new Date()));
            buf.append(" ");
        }
        switch(type) {
            case SimpleLog.LOG_LEVEL_TRACE: buf.append("[TRACE] "); break;
            case SimpleLog.LOG_LEVEL_DEBUG: buf.append("[DEBUG] "); break;
            case SimpleLog.LOG_LEVEL_INFO:  buf.append("[INFO] ");  break;
            case SimpleLog.LOG_LEVEL_WARN:  buf.append("[WARN] ");  break;
            case SimpleLog.LOG_LEVEL_ERROR: buf.append("[ERROR] "); break;
            case SimpleLog.LOG_LEVEL_FATAL: buf.append("[FATAL] "); break;
        }
 	if( showShortName) {
            if( shortLogName==null ) {
                shortLogName = logName.substring(logName.lastIndexOf(".") + 1);
                shortLogName =
                    shortLogName.substring(shortLogName.lastIndexOf("/") + 1);
            }
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if(showLogName) {
            buf.append(String.valueOf(logName)).append(" - ");
        }
        buf.append(String.valueOf(message));
        if(t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");
            java.io.StringWriter sw= new java.io.StringWriter(1024);
            java.io.PrintWriter pw= new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }
        write(buf);
    }
    protected void write(StringBuffer buffer) {
        System.err.println(buffer.toString());
    }
    protected boolean isLevelEnabled(int logLevel) {
        return (logLevel >= currentLogLevel);
    }
    public final void debug(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, null);
        }
    }
    public final void debug(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, t);
        }
    }
    public final void trace(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, null);
        }
    }
    public final void trace(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, t);
        }
    }
    public final void info(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO,message,null);
        }
    }
    public final void info(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO, message, t);
        }
    }
    public final void warn(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, null);
        }
    }
    public final void warn(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, t);
        }
    }
    public final void error(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, null);
        }
    }
    public final void error(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, t);
        }
    }
    public final void fatal(Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, null);
        }
    }
    public final void fatal(Object message, Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, t);
        }
    }
    public final boolean isDebugEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG);
    }
    public final boolean isErrorEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR);
    }
    public final boolean isFatalEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL);
    }
    public final boolean isInfoEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_INFO);
    }
    public final boolean isTraceEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE);
    }
    public final boolean isWarnEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_WARN);
    }
    private static ClassLoader getContextClassLoader()
    {
        ClassLoader classLoader = null;
        if (classLoader == null) {
            try {
                Method method = Thread.class.getMethod("getContextClassLoader",
                        (Class[]) null);
                try {
                    classLoader = (ClassLoader)method.invoke(Thread.currentThread(), 
                            (Object[]) null);
                } catch (IllegalAccessException e) {
                    ;  
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof SecurityException) {
                        ;  
                    } else {
                        throw new LogConfigurationException
                            ("Unexpected InvocationTargetException", e.getTargetException());
                    }
                }
            } catch (NoSuchMethodException e) {
                ;  
            }
        }
        if (classLoader == null) {
            classLoader = SimpleLog.class.getClassLoader();
        }
        return classLoader;
    }
    private static InputStream getResourceAsStream(final String name)
    {
        return (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    ClassLoader threadCL = getContextClassLoader();
                    if (threadCL != null) {
                        return threadCL.getResourceAsStream(name);
                    } else {
                        return ClassLoader.getSystemResourceAsStream(name);
                    }
                }
            });
    }
}
