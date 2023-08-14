public final class javax_swing_border_EmptyBorder extends AbstractTest<EmptyBorder> {
    public static void main(String[] args) {
        new javax_swing_border_EmptyBorder().test(true);
    }
    protected EmptyBorder getObject() {
        return new EmptyBorder(1, 2, 3, 4);
    }
    protected EmptyBorder getAnotherObject() {
        return null; 
    }
}
