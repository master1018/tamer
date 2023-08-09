public class InstantiationError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -4885810657349421204L;
    public InstantiationError() {
        super();
    }
    public InstantiationError(String detailMessage) {
        super(detailMessage);
    }
    InstantiationError(Class<?> clazz) {
        super(clazz.getName());
    }
}
