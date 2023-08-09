public class MonitoringManagerImpl implements MonitoringManager {
    private final MonitoredObject rootMonitoredObject;
    MonitoringManagerImpl( String nameOfTheRoot, String description ) {
        MonitoredObjectFactory f =
            MonitoringFactories.getMonitoredObjectFactory();
        rootMonitoredObject =
            f.createMonitoredObject( nameOfTheRoot, description );
    }
    public void clearState( ) {
        rootMonitoredObject.clearState( );
    }
    public MonitoredObject getRootMonitoredObject( ) {
        return rootMonitoredObject;
    }
}
