public class InvalidNameSpaceException extends Exception {
	private String mNamespace;
    public InvalidNameSpaceException(String namespace) {
        super();
        mNamespace = namespace;
    }
    @Override
    public String getMessage() {
        return "Invalid namespace: " + mNamespace;
    }
}
