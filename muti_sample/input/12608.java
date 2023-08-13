public class AttributeInUseException extends NamingException {
    public AttributeInUseException(String explanation) {
        super(explanation);
    }
    public AttributeInUseException() {
        super();
    }
    private static final long serialVersionUID = 4437710305529322564L;
}
