public class MonitoredAttributeInfoFactoryImpl
    implements MonitoredAttributeInfoFactory
{
    public MonitoredAttributeInfo createMonitoredAttributeInfo(
        String description, Class type, boolean isWritable,
        boolean isStatistic  )
    {
        return new MonitoredAttributeInfoImpl( description, type,
            isWritable, isStatistic );
    }
}
