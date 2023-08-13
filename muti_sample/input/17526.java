public class MonitoredAttributeInfoImpl implements MonitoredAttributeInfo {
    private final String description;
    private final Class type;
    private final boolean writableFlag, statisticFlag;
    MonitoredAttributeInfoImpl( String description, Class type,
        boolean isWritable, boolean isStatistic )
    {
        this.description = description;
        this.type = type;
        this.writableFlag = isWritable;
        this.statisticFlag = isStatistic;
    }
    public String getDescription( ) {
        return this.description;
    }
    public Class type( ) {
        return this.type;
    }
    public boolean isWritable( ) {
         return this.writableFlag;
    }
    public boolean isStatistic( ) {
         return this.statisticFlag;
    }
}
