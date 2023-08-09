public class AnnotationTypeMismatchException extends RuntimeException {
    private static final long serialVersionUID = 8125925355765570191L;
    private Method element;
    private String foundType;
    public AnnotationTypeMismatchException(Method element, String foundType) {
        super(Messages.getString("annotation.1", element, foundType)); 
        this.element = element;
        this.foundType = foundType;
    }
    public Method element() {
        return element;
    }
    public String foundType() {
        return foundType;
    }
}
