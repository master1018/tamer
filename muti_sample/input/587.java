public abstract class LongMonitoredAttributeBase extends MonitoredAttributeBase {
    public  LongMonitoredAttributeBase(String name, String description) {
        super( name );
        MonitoredAttributeInfoFactory f =
            MonitoringFactories.getMonitoredAttributeInfoFactory();
        MonitoredAttributeInfo maInfo = f.createMonitoredAttributeInfo(
                description, Long.class, false, false );
        this.setMonitoredAttributeInfo( maInfo );
    } 
} 
