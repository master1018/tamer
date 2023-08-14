public class PropertyParser {
    private List actions ;
    public PropertyParser( )
    {
        actions = new LinkedList() ;
    }
    public PropertyParser add( String propName,
        Operation action, String fieldName )
    {
        actions.add( ParserActionFactory.makeNormalAction( propName,
            action, fieldName ) ) ;
        return this ;
    }
    public PropertyParser addPrefix( String propName,
        Operation action, String fieldName, Class componentType )
    {
        actions.add( ParserActionFactory.makePrefixAction( propName,
            action, fieldName, componentType ) ) ;
        return this ;
    }
    public Map parse( Properties props )
    {
        Map map = new HashMap() ;
        Iterator iter = actions.iterator() ;
        while (iter.hasNext()) {
            ParserAction act = (ParserAction)(iter.next()) ;
            Object result = act.apply( props ) ;
            if (result != null)
                map.put( act.getFieldName(), result ) ;
        }
        return map ;
    }
    public Iterator iterator()
    {
        return actions.iterator() ;
    }
}
