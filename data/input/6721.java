public final class javax_swing_border_SoftBevelBorder extends AbstractTest<SoftBevelBorder> {
    public static void main(String[] args) {
        new javax_swing_border_SoftBevelBorder().test(true);
    }
    protected SoftBevelBorder getObject() {
        return new SoftBevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
    }
    protected SoftBevelBorder getAnotherObject() {
        return null; 
    }
}
