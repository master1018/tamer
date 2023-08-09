public final class TransformAttribute implements Serializable {
    private static final long serialVersionUID = 3356247357827709530L;
    private AffineTransform fTransform;
    public TransformAttribute(AffineTransform transform) {
        if (transform == null) {
            throw new IllegalArgumentException(Messages.getString("awt.94")); 
        }
        if (!transform.isIdentity()) {
            this.fTransform = new AffineTransform(transform);
        }
    }
    public AffineTransform getTransform() {
        if (fTransform != null) {
            return new AffineTransform(fTransform);
        }
        return new AffineTransform();
    }
    public boolean isIdentity() {
        return (fTransform == null);
    }
}
