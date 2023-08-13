public class InstantiationException extends Exception {
    private static final long serialVersionUID = -8441929162975509110L;
    public InstantiationException() {
        super();
    }
    public InstantiationException(String detailMessage) {
        super(detailMessage);
    }
    InstantiationException(Class<?> clazz) {
        super(clazz.getName());
    }
}
