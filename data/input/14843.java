public final class javax_swing_plaf_FontUIResource extends AbstractTest<FontUIResource> {
    public static void main(String[] args) {
        new javax_swing_plaf_FontUIResource().test(true);
    }
    protected FontUIResource getObject() {
        return new FontUIResource(
                new Font(
                        Collections.singletonMap(
                                TextAttribute.STRIKETHROUGH,
                                TextAttribute.STRIKETHROUGH_ON)));
    }
    protected FontUIResource getAnotherObject() {
        return new FontUIResource(null, Font.ITALIC, 11);
    }
}
