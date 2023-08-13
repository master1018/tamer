public class ParserActionFactory{
    private ParserActionFactory() {}
    public static ParserAction makeNormalAction( String propertyName,
        Operation operation, String fieldName )
    {
        return new NormalParserAction( propertyName, operation, fieldName ) ;
    }
    public static ParserAction makePrefixAction( String propertyName,
        Operation operation, String fieldName, Class componentType )
    {
        return new PrefixParserAction( propertyName, operation, fieldName, componentType ) ;
    }
}
