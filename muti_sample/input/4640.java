public class MonitoredObjectFactoryImpl implements MonitoredObjectFactory {
    public MonitoredObject createMonitoredObject( String name,
        String description )
    {
        return new MonitoredObjectImpl( name, description );
    }
}
