public final class javax_swing_border_TitledBorder extends AbstractTest<TitledBorder> {
    public static void main(String[] args) {
        new javax_swing_border_TitledBorder().test(true);
    }
    protected TitledBorder getObject() {
        return new TitledBorder(
                new EmptyBorder(1, 2, 3, 4),
                "TITLE",
                TitledBorder.CENTER,
                TitledBorder.ABOVE_TOP,
                new Font("Arial", Font.ITALIC, 12),
                Color.RED);
    }
    protected TitledBorder getAnotherObject() {
        return null; 
    }
}
