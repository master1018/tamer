public final class java_awt_Insets extends AbstractTest<Insets> {
    public static void main(String[] args) {
        new java_awt_Insets().test(true);
    }
    protected Insets getObject() {
        return new Insets(1, 2, 3, 4);
    }
    protected Insets getAnotherObject() {
        return new Insets(0, 0, 0, 0);
    }
}
