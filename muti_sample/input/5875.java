public final class javax_swing_border_StrokeBorder extends AbstractTest<StrokeBorder> {
    public static void main(String[] args) {
        new javax_swing_border_StrokeBorder().test(true);
    }
    protected StrokeBorder getObject() {
        return new StrokeBorder(new BasicStroke(0), Color.WHITE);
    }
    protected StrokeBorder getAnotherObject() {
        return null; 
    }
}
