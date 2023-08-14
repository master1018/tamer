public abstract class ParserDataBase implements ParserData {
    private String  propertyName ;
    private Operation operation ;
    private String fieldName ;
    private Object defaultValue ;
    private Object testValue ;
    protected ParserDataBase( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue )
    {
        this.propertyName = propertyName  ;
        this.operation = operation  ;
        this.fieldName = fieldName  ;
        this.defaultValue = defaultValue  ;
        this.testValue = testValue  ;
    }
    public String  getPropertyName() { return propertyName ; }
    public Operation getOperation() { return operation ; }
    public String getFieldName() { return fieldName ; }
    public Object getDefaultValue() { return defaultValue ; }
    public Object getTestValue() { return testValue ; }
}
