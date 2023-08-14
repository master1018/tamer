public final class javax_swing_border_LineBorder extends AbstractTest<LineBorder> {
    public static void main(String[] args) {
        new javax_swing_border_LineBorder().test(true);
    }
    protected LineBorder getObject() {
        return new LineBorder(Color.RED, 2, true);
    }
    protected LineBorder getAnotherObject() {
        return null; 
    }
}
