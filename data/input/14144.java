public class NormalDataCollector extends DataCollectorBase {
    private String[] args ;
    public NormalDataCollector( String[] args, Properties props,
        String localHostName, String configurationHostName )
    {
        super( props, localHostName, configurationHostName ) ;
        this.args = args ;
    }
    public boolean isApplet()
    {
        return false ;
    }
    protected void collect()
    {
        checkPropertyDefaults() ;
        findPropertiesFromFile() ;
        findPropertiesFromSystem() ;
        findPropertiesFromProperties() ;
        findPropertiesFromArgs( args ) ;
    }
}
