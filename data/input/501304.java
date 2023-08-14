public abstract class CommonGraphics2DFactory implements GraphicsFactory {
    public static CommonGraphics2DFactory inst;
    public FontMetrics getFontMetrics(Font font) {
        FontMetrics fm;
        for (FontMetrics element : cacheFM) {
            fm = element;
            if (fm == null){
                break;
            }
            if (fm.getFont().equals(font)){
                return fm;
            }
        }
        fm = new FontMetricsImpl(font);
        System.arraycopy(cacheFM, 0, cacheFM, 1, cacheFM.length -1);
        cacheFM[0] = fm;
        return fm;
    }
    public FontPeer getFontPeer(Font font) {
        return getFontManager().getFontPeer(font.getName(), font.getStyle(), font.getSize());
    }
    public abstract Font embedFont(String fontFilePath);
}