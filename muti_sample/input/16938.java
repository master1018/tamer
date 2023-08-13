public final class java_awt_BasicStroke extends AbstractTest<BasicStroke> {
    public static void main(String[] args) {
        new java_awt_BasicStroke().test(true);
    }
    protected BasicStroke getObject() {
        return new BasicStroke();
    }
    protected BasicStroke getAnotherObject() {
        float[] f = {1.0f, 2.0f, 3.0f, 4.0f};
        return new BasicStroke(f[1], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, f[2], f, f[3]);
    }
}
