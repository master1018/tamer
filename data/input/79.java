public class DummyContext implements Context {
    private static final long serialVersionUID = 1L;
    public void configure(SessionManagerConfig config) throws HibersapException {
    }
    public Connection getConnection() {
        return null;
    }
    public void close() {
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DummyContext) return true;
        return false;
    }
}
