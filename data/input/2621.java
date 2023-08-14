public final class sun_swing_PrintColorUIResource extends AbstractTest<Color> {
    public static void main(String[] args) {
        new sun_swing_PrintColorUIResource().test(true);
    }
    protected Color getObject() {
        return UIManager.getColor("TitledBorder.titleColor");
    }
    protected Color getAnotherObject() {
        return new PrintColorUIResource(0, Color.WHITE);
    }
    protected void validate(Color before, Color after) {
        before.equals(after);
    }
}
