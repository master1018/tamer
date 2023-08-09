public final class javax_swing_plaf_BorderUIResource_TitledBorderUIResource extends AbstractTest<TitledBorderUIResource> {
    public static void main(String[] args) {
        new javax_swing_plaf_BorderUIResource_TitledBorderUIResource().test(true);
    }
    protected TitledBorderUIResource getObject() {
        return new TitledBorderUIResource(
                new EmptyBorder(1, 2, 3, 4),
                "TITLE",
                TitledBorder.CENTER,
                TitledBorder.ABOVE_TOP,
                new Font("Serif", Font.ITALIC, 12),
                Color.RED);
    }
    protected TitledBorderUIResource getAnotherObject() {
        return null; 
    }
}
