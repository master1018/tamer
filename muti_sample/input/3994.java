public class ParserDataFactory {
    public static ParserData make( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, String testData )
    {
        return new NormalParserData( propertyName, operation, fieldName,
            defaultValue, testValue, testData ) ;
    }
    public static ParserData make( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, StringPair[] testData, Class componentType )
    {
        return new PrefixParserData( propertyName, operation, fieldName,
            defaultValue, testValue, testData, componentType ) ;
    }
}
