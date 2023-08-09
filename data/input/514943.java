public abstract class CRL {
    private final String type;
    protected CRL(String type) {
        this.type = type;
    }
    public final String getType() {
        return type;
    }
    public abstract boolean isRevoked(Certificate cert);
    public abstract String toString();
}
