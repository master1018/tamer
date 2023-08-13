public final class javax_swing_border_MatteBorder extends AbstractTest<MatteBorder> {
    public static void main(String[] args) {
        new javax_swing_border_MatteBorder().test(true);
    }
    protected MatteBorder getObject() {
        return new MatteBorder(1, 2, 3, 4, Color.RED);
    }
    protected MatteBorder getAnotherObject() {
        return null; 
    }
}
