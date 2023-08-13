public abstract class LogWrapperBase {
    protected Logger logger ;
    protected String loggerName ;
    protected LogWrapperBase( Logger logger )
    {
        this.logger = logger ;
        this.loggerName = logger.getName( );
    }
    protected void doLog( Level level, String key, Object[] params, Class wrapperClass,
        Throwable thr )
    {
        LogRecord lrec = new LogRecord( level, key ) ;
        if (params != null)
            lrec.setParameters( params ) ;
        inferCaller( wrapperClass, lrec ) ;
        lrec.setThrown( thr ) ;
        lrec.setLoggerName( loggerName );
        lrec.setResourceBundle( logger.getResourceBundle() ) ;
        logger.log( lrec ) ;
    }
    private void inferCaller( Class wrapperClass, LogRecord lrec )
    {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        StackTraceElement frame = null ;
        String wcname = wrapperClass.getName() ;
        String baseName = LogWrapperBase.class.getName() ;
        int ix = 0;
        while (ix < stack.length) {
            frame = stack[ix];
            String cname = frame.getClassName();
            if (!cname.equals(wcname) && !cname.equals(baseName))  {
                break;
            }
            ix++;
        }
        if (ix < stack.length) {
            lrec.setSourceClassName(frame.getClassName());
            lrec.setSourceMethodName(frame.getMethodName());
        }
    }
    protected void doLog( Level level, String key, Class wrapperClass, Throwable thr )
    {
        doLog( level, key, null, wrapperClass, thr ) ;
    }
}
