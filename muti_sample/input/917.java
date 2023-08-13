public final class java_awt_LinearGradientPaint extends AbstractTest<LinearGradientPaint> {
    public static void main(String[] args) {
        new java_awt_LinearGradientPaint().test(true);
    }
    protected LinearGradientPaint getObject() {
        float[] f = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f };
        Color[] c = { Color.BLUE, Color.GREEN, Color.RED, Color.BLUE, Color.GREEN, Color.RED };
        return new LinearGradientPaint(f[0], f[1], f[2], f[3], f, c);
    }
    protected LinearGradientPaint getAnotherObject() {
        return null; 
    }
}
