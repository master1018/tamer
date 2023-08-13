public final class java_awt_Dimension extends AbstractTest<Dimension> {
    public static void main(String[] args) {
        new java_awt_Dimension().test(true);
    }
    protected Dimension getObject() {
        return new Dimension();
    }
    protected Dimension getAnotherObject() {
        return new Dimension(-5, 5);
    }
}
