public class NormalParserData extends ParserDataBase {
    private String testData ;
    public NormalParserData( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, String testData )
    {
        super( propertyName, operation, fieldName, defaultValue, testValue ) ;
        this.testData = testData ;
    }
    public void addToParser( PropertyParser parser )
    {
        parser.add( getPropertyName(), getOperation(), getFieldName() ) ;
    }
    public void addToProperties( Properties props )
    {
        props.setProperty( getPropertyName(), testData ) ;
    }
}
