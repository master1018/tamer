public class PropertyOnlyDataCollector extends DataCollectorBase
{
    public PropertyOnlyDataCollector( Properties props,
        String localHostName, String configurationHostName )
    {
        super( props, localHostName, configurationHostName ) ;
    }
    public boolean isApplet()
    {
        return false ;
    }
    protected void collect()
    {
        checkPropertyDefaults() ;
        findPropertiesFromProperties() ;
    }
}
