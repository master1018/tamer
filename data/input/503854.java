public class IllegalFormatConversionException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 17000126L;
    private char c;
    private Class<?> arg;
    public IllegalFormatConversionException(char c, Class<?> arg) {
        this.c = c;
        if (arg == null) {
            throw new NullPointerException();
        }
        this.arg = arg;
    }
    public Class<?> getArgumentClass() {
        return arg;
    }
    public char getConversion() {
        return c;
    }
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(c);
        buffer.append(" is incompatible with ");
        buffer.append(arg.getName());
        return buffer.toString();
    }
}
