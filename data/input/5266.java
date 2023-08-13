public abstract class ParserImplTableBase extends ParserImplBase {
    private final ParserData[] entries ;
    public ParserImplTableBase( ParserData[] entries )
    {
        this.entries = entries ;
        setDefaultValues() ;
    }
    protected PropertyParser makeParser()
    {
        PropertyParser result = new PropertyParser() ;
        for (int ctr=0; ctr<entries.length; ctr++ ) {
            ParserData entry = entries[ctr] ;
            entry.addToParser( result ) ;
        }
        return result ;
    }
    private static final class MapEntry implements Map.Entry {
        private Object key ;
        private Object value ;
        public MapEntry( Object key )
        {
            this.key = key ;
        }
        public Object getKey()
        {
            return key ;
        }
        public Object getValue()
        {
            return value ;
        }
        public Object setValue( Object value )
        {
            Object result = this.value ;
            this.value = value ;
            return result ;
        }
        public boolean equals( Object obj )
        {
            if (!(obj instanceof MapEntry))
                return false ;
            MapEntry other = (MapEntry)obj ;
            return (key.equals( other.key )) &&
                (value.equals( other.value )) ;
        }
        public int hashCode()
        {
            return key.hashCode() ^ value.hashCode() ;
        }
    }
    private static class FieldMap extends AbstractMap {
        private final ParserData[] entries ;
        private final boolean useDefault ;
        public FieldMap( ParserData[] entries, boolean useDefault )
        {
            this.entries = entries ;
            this.useDefault = useDefault ;
        }
        public Set entrySet()
        {
            return new AbstractSet()
            {
                public Iterator iterator()
                {
                    return new Iterator() {
                        int ctr = 0 ;
                        public boolean hasNext()
                        {
                            return ctr < entries.length ;
                        }
                        public Object next()
                        {
                            ParserData pd = entries[ctr++] ;
                            Map.Entry result = new MapEntry( pd.getFieldName() ) ;
                            if (useDefault)
                                result.setValue( pd.getDefaultValue() ) ;
                            else
                                result.setValue( pd.getTestValue() ) ;
                            return result ;
                        }
                        public void remove()
                        {
                            throw new UnsupportedOperationException() ;
                        }
                    } ;
                }
                public int size()
                {
                    return entries.length ;
                }
            } ;
        }
    } ;
    protected void setDefaultValues()
    {
        Map map = new FieldMap( entries, true ) ;
        setFields( map ) ;
    }
    public void setTestValues()
    {
        Map map = new FieldMap( entries, false ) ;
        setFields( map ) ;
    }
}
