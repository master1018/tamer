public abstract class DataCollectorBase implements DataCollector {
    private PropertyParser parser ;
    private Set propertyNames ;
    private Set propertyPrefixes ;
    private Set URLPropertyNames ;
    protected String localHostName ;
    protected String configurationHostName ;
    private boolean setParserCalled ;
    private Properties originalProps ;
    private Properties resultProps ;
    public DataCollectorBase( Properties props, String localHostName,
        String configurationHostName )
    {
        URLPropertyNames = new HashSet() ;
        URLPropertyNames.add( ORBConstants.INITIAL_SERVICES_PROPERTY ) ;
        propertyNames = new HashSet() ;
        propertyNames.add( ORBConstants.ORB_INIT_REF_PROPERTY ) ;
        propertyPrefixes = new HashSet() ;
        this.originalProps = props ;
        this.localHostName = localHostName ;
        this.configurationHostName = configurationHostName ;
        setParserCalled = false ;
        resultProps = new Properties() ;
    }
    public boolean initialHostIsLocal()
    {
        checkSetParserCalled() ;
        return localHostName.equals( resultProps.getProperty(
            ORBConstants.INITIAL_HOST_PROPERTY ) ) ;
    }
    public void setParser( PropertyParser parser )
    {
        Iterator iter = parser.iterator() ;
        while (iter.hasNext()) {
            ParserAction pa = (ParserAction)(iter.next()) ;
            if (pa.isPrefix())
                propertyPrefixes.add( pa.getPropertyName() ) ;
            else
                propertyNames.add( pa.getPropertyName() ) ;
        }
        collect() ;
        setParserCalled = true ;
    }
    public Properties getProperties()
    {
        checkSetParserCalled() ;
        return resultProps ;
    }
    public abstract boolean isApplet() ;
    protected abstract void collect() ;
    protected void checkPropertyDefaults()
    {
        String host =
            resultProps.getProperty( ORBConstants.INITIAL_HOST_PROPERTY ) ;
        if ((host == null) || (host.equals("")))
            setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
                configurationHostName );
        String serverHost =
            resultProps.getProperty( ORBConstants.SERVER_HOST_PROPERTY ) ;
        if (serverHost == null ||
            serverHost.equals("") ||
            serverHost.equals("0.0.0.0") ||
            serverHost.equals("::") ||
            serverHost.toLowerCase().equals("::ffff:0.0.0.0"))
        {
            setProperty(ORBConstants.SERVER_HOST_PROPERTY,
                        localHostName);
            setProperty(ORBConstants.LISTEN_ON_ALL_INTERFACES,
                        ORBConstants.LISTEN_ON_ALL_INTERFACES);
        }
    }
    protected void findPropertiesFromArgs( String[] params )
    {
        if (params == null)
            return;
        String name ;
        String value ;
        for ( int i=0; i<params.length; i++ ) {
            value = null ;
            name = null ;
            if ( params[i] != null && params[i].startsWith("-ORB") ) {
                String argName = params[i].substring( 1 ) ;
                name = findMatchingPropertyName( propertyNames, argName ) ;
                if (name != null)
                    if ( i+1 < params.length && params[i+1] != null ) {
                        value = params[++i];
                    }
            }
            if (value != null) {
                setProperty( name, value ) ;
            }
        }
    }
    protected void findPropertiesFromApplet( final Applet app )
    {
        if (app == null)
            return;
        PropertyCallback callback = new PropertyCallback() {
            public String get(String name) {
                return app.getParameter(name);
            }
        } ;
        findPropertiesByName( propertyNames.iterator(), callback ) ;
        PropertyCallback URLCallback = new PropertyCallback() {
            public String get( String name ) {
                String value = resultProps.getProperty(name);
                if (value == null)
                    return null ;
                try {
                    URL url = new URL( app.getDocumentBase(), value ) ;
                    return url.toExternalForm() ;
                } catch (java.net.MalformedURLException exc) {
                    return value ;
                }
            }
        } ;
        findPropertiesByName( URLPropertyNames.iterator(),
            URLCallback ) ;
    }
    private void doProperties( final Properties props )
    {
        PropertyCallback callback =  new PropertyCallback() {
            public String get(String name) {
                return props.getProperty(name);
            }
        } ;
        findPropertiesByName( propertyNames.iterator(), callback ) ;
        findPropertiesByPrefix( propertyPrefixes,
            makeIterator( props.propertyNames()), callback );
    }
    protected void findPropertiesFromFile()
    {
        final Properties fileProps = getFileProperties() ;
        if (fileProps==null)
            return ;
        doProperties( fileProps ) ;
    }
    protected void findPropertiesFromProperties()
    {
        if (originalProps == null)
            return;
        doProperties( originalProps ) ;
    }
    protected void findPropertiesFromSystem()
    {
        Set normalNames = getCORBAPrefixes( propertyNames ) ;
        Set prefixNames = getCORBAPrefixes( propertyPrefixes ) ;
        PropertyCallback callback = new PropertyCallback() {
            public String get(String name) {
                return getSystemProperty(name);
            }
        } ;
        findPropertiesByName( normalNames.iterator(), callback ) ;
        findPropertiesByPrefix( prefixNames,
            getSystemPropertyNames(), callback ) ;
    }
    private void setProperty( String name, String value )
    {
        if( name.equals( ORBConstants.ORB_INIT_REF_PROPERTY ) ) {
            StringTokenizer st = new StringTokenizer( value, "=" ) ;
            if (st.countTokens() != 2)
                throw new IllegalArgumentException() ;
            String refName = st.nextToken() ;
            String refValue = st.nextToken() ;
            resultProps.setProperty( name + "." + refName, refValue ) ;
        } else {
            resultProps.setProperty( name, value ) ;
        }
    }
    private void checkSetParserCalled()
    {
        if (!setParserCalled)
            throw new IllegalStateException( "setParser not called." ) ;
    }
    private void findPropertiesByPrefix( Set prefixes,
        Iterator propertyNames, PropertyCallback getProperty )
    {
        while (propertyNames.hasNext()) {
            String name = (String)(propertyNames.next()) ;
            Iterator iter = prefixes.iterator() ;
            while (iter.hasNext()) {
                String prefix = (String)(iter.next()) ;
                if (name.startsWith( prefix )) {
                    String value = getProperty.get( name ) ;
                    setProperty( name, value ) ;
                }
            }
        }
    }
    private void findPropertiesByName( Iterator names,
        PropertyCallback getProperty )
    {
        while (names.hasNext()) {
            String name = (String)(names.next()) ;
            String value = getProperty.get( name ) ;
            if (value != null)
                setProperty( name, value ) ;
        }
    }
    private static String getSystemProperty(final String name)
    {
        return (String)AccessController.doPrivileged(
            new GetPropertyAction(name));
    }
    private String findMatchingPropertyName( Set names,
        String suffix )
    {
        Iterator iter = names.iterator() ;
        while (iter.hasNext()) {
            String name = (String)(iter.next()) ;
            if (name.endsWith( suffix ))
                return name ;
        }
        return null ;
    }
    private static Iterator makeIterator( final Enumeration enumeration )
    {
        return new Iterator() {
            public boolean hasNext() { return enumeration.hasMoreElements() ; }
            public Object next() { return enumeration.nextElement() ; }
            public void remove() { throw new UnsupportedOperationException() ; }
        } ;
    }
    private static Iterator getSystemPropertyNames()
    {
        Enumeration enumeration = (Enumeration)
            AccessController.doPrivileged(
                new PrivilegedAction() {
                      public java.lang.Object run() {
                          return System.getProperties().propertyNames();
                      }
                }
            );
        return makeIterator( enumeration ) ;
    }
    private void getPropertiesFromFile( Properties props, String fileName )
    {
        try {
            File file = new File( fileName ) ;
            if (!file.exists())
                return ;
            FileInputStream in = new FileInputStream( file ) ;
            try {
                props.load( in ) ;
            } finally {
                in.close() ;
            }
        } catch (Exception exc) {
        }
    }
    private Properties getFileProperties()
    {
        Properties defaults = new Properties() ;
        String javaHome = getSystemProperty( "java.home" ) ;
        String fileName = javaHome + File.separator + "lib" + File.separator +
            "orb.properties" ;
        getPropertiesFromFile( defaults, fileName ) ;
        Properties results = new Properties( defaults ) ;
        String userHome = getSystemProperty( "user.home" ) ;
        fileName = userHome + File.separator + "orb.properties" ;
        getPropertiesFromFile( results, fileName ) ;
        return results ;
    }
    private boolean hasCORBAPrefix( String prefix )
    {
        return prefix.startsWith( ORBConstants.ORG_OMG_PREFIX ) ||
            prefix.startsWith( ORBConstants.SUN_PREFIX ) ||
            prefix.startsWith( ORBConstants.SUN_LC_PREFIX ) ||
            prefix.startsWith( ORBConstants.SUN_LC_VERSION_PREFIX ) ;
    }
    private Set getCORBAPrefixes( final Set prefixes )
    {
        Set result = new HashSet() ;
        Iterator iter = prefixes.iterator() ;
        while (iter.hasNext()) {
            String element = (String)(iter.next()) ;
            if (hasCORBAPrefix( element ))
                result.add( element ) ;
        }
        return result ;
    }
}
abstract class PropertyCallback
{
    abstract public String get(String name);
}
