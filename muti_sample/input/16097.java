public final class java_awt_geom_AffineTransform extends AbstractTest<AffineTransform> {
    public static void main(String[] args) {
        new java_awt_geom_AffineTransform().test(true);
    }
    protected AffineTransform getObject() {
        return new AffineTransform(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f);
    }
    protected AffineTransform getAnotherObject() {
        return new AffineTransform(0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f);
    }
}
