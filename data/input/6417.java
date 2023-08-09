public final class java_awt_GradientPaint extends AbstractTest<GradientPaint> {
    public static void main(String[] args) {
        new java_awt_GradientPaint().test(true);
    }
    protected GradientPaint getObject() {
        return new GradientPaint(0.1f, 0.2f, Color.BLACK, 0.3f, 0.4f, Color.WHITE, true);
    }
    protected GradientPaint getAnotherObject() {
        return null; 
    }
}
