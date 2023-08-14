public final class AttExceptions extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "Exceptions";
    private final TypeList exceptions;
    public AttExceptions(TypeList exceptions) {
        super(ATTRIBUTE_NAME);
        try {
            if (exceptions.isMutable()) {
                throw new MutabilityException("exceptions.isMutable()");
            }
        } catch (NullPointerException ex) {
            throw new NullPointerException("exceptions == null");
        }
        this.exceptions = exceptions;
    }
    public int byteLength() {
        return 8 + exceptions.size() * 2;
    }
    public TypeList getExceptions() {
        return exceptions;
    }
}
