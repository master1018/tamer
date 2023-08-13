public final class javax_swing_border_BevelBorder extends AbstractTest<BevelBorder> {
    public static void main(String[] args) {
        new javax_swing_border_BevelBorder().test(true);
    }
    protected BevelBorder getObject() {
        return new BevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
    }
    protected BevelBorder getAnotherObject() {
        return null; 
    }
}
