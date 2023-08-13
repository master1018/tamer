public final class java_awt_Font extends AbstractTest<Font> {
    public static void main(String[] args) {
        new java_awt_Font().test(true);
    }
    protected Font getObject() {
        Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
        map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        return new Font(map);
    }
    protected Font getAnotherObject() {
        return new Font("SansSerif", Font.BOLD, 10).deriveFont(12.12f);
    }
}
