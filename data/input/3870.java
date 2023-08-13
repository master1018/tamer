public abstract class ParserImplBase {
    private ORBUtilSystemException wrapper ;
    protected abstract PropertyParser makeParser() ;
    protected void complete()
    {
    }
    public ParserImplBase()
    {
        wrapper = ORBUtilSystemException.get(
            CORBALogDomains.ORB_LIFECYCLE ) ;
    }
    public void init( DataCollector coll )
    {
        PropertyParser parser = makeParser() ;
        coll.setParser( parser ) ;
        Properties props = coll.getProperties() ;
        Map map = parser.parse( props ) ;
        setFields( map ) ;
    }
    private Field getAnyField( String name )
    {
        Field result = null ;
        try {
            Class cls = this.getClass() ;
            result = cls.getDeclaredField( name ) ;
            while (result == null) {
                cls = cls.getSuperclass() ;
                if (cls == null)
                    break ;
                result = cls.getDeclaredField( name ) ;
            }
        } catch (Exception exc) {
            throw wrapper.fieldNotFound( exc, name ) ;
        }
        if (result == null)
            throw wrapper.fieldNotFound( name ) ;
        return result ;
    }
    protected void setFields( Map map )
    {
        Set entries = map.entrySet() ;
        Iterator iter = entries.iterator() ;
        while (iter.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry)(iter.next()) ;
            final String name = (String)(entry.getKey()) ;
            final Object value = entry.getValue() ;
            try {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws IllegalAccessException,
                            IllegalArgumentException
                        {
                            Field field = getAnyField( name ) ;
                            field.setAccessible( true ) ;
                            field.set( ParserImplBase.this, value ) ;
                            return null ;
                        }
                    }
                ) ;
            } catch (PrivilegedActionException exc) {
                throw wrapper.errorSettingField( exc.getCause(), name,
                    value.toString() ) ;
            }
        }
        complete() ;
    }
}
