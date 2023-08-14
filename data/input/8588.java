public final class java_awt_Point extends AbstractTest<Point> {
    public static void main(String[] args) {
        new java_awt_Point().test(true);
    }
    protected Point getObject() {
        return new Point(-5, 5);
    }
    protected Point getAnotherObject() {
        return new Point();
    }
}
