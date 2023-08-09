public abstract class DataCollectorFactory {
    private DataCollectorFactory() {}
    public static DataCollector create( Applet app, Properties props,
        String localHostName )
    {
        String appletHost = localHostName ;
        if (app != null) {
            URL appletCodeBase = app.getCodeBase() ;
            if (appletCodeBase != null)
                appletHost = appletCodeBase.getHost() ;
        }
        return new AppletDataCollector( app, props, localHostName,
            appletHost ) ;
    }
    public static DataCollector create( String[] args, Properties props,
        String localHostName )
    {
        return new NormalDataCollector( args, props, localHostName,
            localHostName ) ;
    }
    public static DataCollector create( Properties props,
        String localHostName )
    {
        return new PropertyOnlyDataCollector( props, localHostName,
            localHostName ) ;
    }
}
