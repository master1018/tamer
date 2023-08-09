class WFontMetrics extends FontMetrics {
    static {
        initIDs();
    }
    int widths[];
    int ascent;
    int descent;
    int leading;
    int height;
    int maxAscent;
    int maxDescent;
    int maxHeight;
    int maxAdvance;
    public WFontMetrics(Font font) {
        super(font);
        init();
    }
    public int getLeading() {
        return leading;
    }
    public int getAscent() {
        return ascent;
    }
    public int getDescent() {
        return descent;
    }
    public int getHeight() {
        return height;
    }
    public int getMaxAscent() {
        return maxAscent;
    }
    public int getMaxDescent() {
        return maxDescent;
    }
    public int getMaxAdvance() {
        return maxAdvance;
    }
    public native int stringWidth(String str);
    public native int charsWidth(char data[], int off, int len);
    public native int bytesWidth(byte data[], int off, int len);
    public int[] getWidths() {
        return widths;
    }
    native void init();
    static Hashtable table = new Hashtable();
    static FontMetrics getFontMetrics(Font font) {
        FontMetrics fm = (FontMetrics)table.get(font);
        if (fm == null) {
            table.put(font, fm = new WFontMetrics(font));
        }
        return fm;
    }
    private static native void initIDs();
}
