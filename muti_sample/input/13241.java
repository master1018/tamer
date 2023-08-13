public final class java_awt_Color extends AbstractTest<Color> {
    public static void main(String[] args) {
        new java_awt_Color().test(true);
    }
    protected Color getObject() {
        return new Color(0x88, 0x44, 0x22, 0x11);
    }
    protected Color getAnotherObject() {
        return Color.BLACK;
    }
}
