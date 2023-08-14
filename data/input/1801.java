public final class javax_swing_OverlayLayout extends AbstractTest<OverlayLayout> {
    public static void main(String[] args) {
        new javax_swing_OverlayLayout().test(true);
    }
    protected OverlayLayout getObject() {
        return new OverlayLayout(new JLabel("TEST"));
    }
    protected OverlayLayout getAnotherObject() {
        return null; 
    }
}
