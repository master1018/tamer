public class PropagatedException extends RuntimeException {
    static final long serialVersionUID = -6065309339888775367L;
    public PropagatedException(RuntimeException cause) {
        super(cause);
    }
    @Override
    public RuntimeException getCause() {
        return (RuntimeException)super.getCause();
    }
}
