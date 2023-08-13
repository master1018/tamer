public class X11FontMetrics extends FontMetrics {
    int widths[];
    int ascent;
    int descent;
    int leading;
    int height;
    int maxAscent;
    int maxDescent;
    int maxHeight;
    int maxAdvance;
    static {
        initIDs();
    }
    private static native void initIDs();
    public X11FontMetrics(Font font) {
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
    public int stringWidth(String string) {
        return charsWidth(string.toCharArray(), 0, string.length());
    }
    public int charsWidth(char chars[], int offset, int length) {
        Font font = getFont();
        PlatformFont pf = ((PlatformFont) font.getPeer());
        if (pf.mightHaveMultiFontMetrics()) {
            return getMFCharsWidth(chars, offset, length, font);
        } else {
            if (widths != null) {
                int w = 0;
                for (int i = offset; i < offset + length; i++) {
                    int ch = chars[i];
                    if (ch < 0 || ch >= widths.length) {
                        w += maxAdvance;
                    } else {
                        w += widths[ch];
                    }
                }
                return w;
            } else {
                return maxAdvance * length;
            }
        }
    }
    private native int getMFCharsWidth(char chars[], int offset, int length, Font font);
    public native int bytesWidth(byte data[], int off, int len);
    public int[] getWidths() {
        return widths;
    }
    native void init();
    static Hashtable table = new Hashtable();
    static synchronized FontMetrics getFontMetrics(Font font) {
        FontMetrics fm = (FontMetrics)table.get(font);
        if (fm == null) {
            table.put(font, fm = new X11FontMetrics(font));
        }
        return fm;
    }
}
