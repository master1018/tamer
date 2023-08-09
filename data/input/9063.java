public class EnumConstantNotPresentExceptionProxy extends ExceptionProxy {
    Class<? extends Enum> enumType;
    String constName;
    public EnumConstantNotPresentExceptionProxy(Class<? extends Enum> enumType,
                                                String constName) {
        this.enumType = enumType;
        this.constName = constName;
    }
    protected RuntimeException generateException() {
        return new EnumConstantNotPresentException(enumType, constName);
    }
}
