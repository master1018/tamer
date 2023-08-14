public class TypeNotPresentExceptionProxy extends ExceptionProxy {
    String typeName;
    Throwable cause;
    public TypeNotPresentExceptionProxy(String typeName, Throwable cause) {
        this.typeName = typeName;
        this.cause = cause;
    }
    protected RuntimeException generateException() {
        return new TypeNotPresentException(typeName, cause);
    }
}
