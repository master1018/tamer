public final class java_awt_Rectangle extends AbstractTest<Rectangle> {
    public static void main(String[] args) {
        new java_awt_Rectangle().test(true);
    }
    protected Rectangle getObject() {
        return new Rectangle(1, 2, 3, 4);
    }
    protected Rectangle getAnotherObject() {
        return new Rectangle();
    }
}
