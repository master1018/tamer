public final class TransformAttribute implements Serializable {
    private AffineTransform transform;
    public TransformAttribute(AffineTransform transform) {
        if (transform != null && !transform.isIdentity()) {
            this.transform = new AffineTransform(transform);
        }
    }
    public AffineTransform getTransform() {
        AffineTransform at = transform;
        return (at == null) ? new AffineTransform() : new AffineTransform(at);
    }
    public boolean isIdentity() {
        return transform == null;
    }
    public static final TransformAttribute IDENTITY = new TransformAttribute(null);
    private void writeObject(java.io.ObjectOutputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        if (this.transform == null) {
            this.transform = new AffineTransform();
        }
        s.defaultWriteObject();
    }
    private Object readResolve() throws ObjectStreamException {
        if (transform == null || transform.isIdentity()) {
            return IDENTITY;
        }
        return this;
    }
    static final long serialVersionUID = 3356247357827709530L;
    public int hashCode() {
        return transform == null ? 0 : transform.hashCode();
    }
    public boolean equals(Object rhs) {
        try {
            TransformAttribute that = (TransformAttribute)rhs;
            if (transform == null) {
                return that.transform == null;
            }
            return transform.equals(that.transform);
        }
        catch (ClassCastException e) {
        }
        return false;
    }
}
