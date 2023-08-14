public class MonitoringFactories {
    private static final MonitoredObjectFactoryImpl monitoredObjectFactory =
        new MonitoredObjectFactoryImpl( );
    private static final MonitoredAttributeInfoFactoryImpl
        monitoredAttributeInfoFactory =
        new MonitoredAttributeInfoFactoryImpl( );
    private static final MonitoringManagerFactoryImpl monitoringManagerFactory =
        new MonitoringManagerFactoryImpl( );
    public static MonitoredObjectFactory getMonitoredObjectFactory( ) {
        return monitoredObjectFactory;
    }
    public static MonitoredAttributeInfoFactory
        getMonitoredAttributeInfoFactory( )
    {
        return monitoredAttributeInfoFactory;
    }
    public static MonitoringManagerFactory getMonitoringManagerFactory( ) {
        return monitoringManagerFactory;
    }
}
