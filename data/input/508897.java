public class EnumConstantNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -6046998521960521108L;
    @SuppressWarnings("unchecked")
    private final Class<? extends Enum> enumType;
    private final String constantName;
    @SuppressWarnings("unchecked")
    public EnumConstantNotPresentException(Class<? extends Enum> enumType,
            String constantName) {
        super(Msg.getString("luni.03", enumType.getName(), constantName)); 
        this.enumType = enumType;
        this.constantName = constantName;
    }
    @SuppressWarnings("unchecked")
    public Class<? extends Enum> enumType() {
        return enumType;
    }
    public String constantName() {
        return constantName;
    }
}
