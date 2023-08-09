public class AndroidGraphicsFactory extends CommonGraphics2DFactory {
    public GraphicsEnvironment createGraphicsEnvironment(WindowFactory wf) {
        return null;
    }
    public Font embedFont(String fontFilePath) {
        return null;
    }
    public FontManager getFontManager() {
        return AndroidFontManager.inst;
    }
    public FontMetrics getFontMetrics(Font font) {
        return new FontMetricsImpl(font);
    }
    public FontPeer getFontPeer(Font font) {
        return new AndroidFont(font.getName(), font.getStyle(), font.getSize());
    }
    public Graphics2D getGraphics2D(NativeWindow win, int translateX,
            int translateY, MultiRectArea clip) {
        return null;
    }
    public Graphics2D getGraphics2D(NativeWindow win, int translateX,
            int translateY, int width, int height) {
        return null;
    }
    public Graphics2D getGraphics2D(Context ctx, Canvas c, Paint p) {
        return AndroidGraphics2D.getInstance(ctx, c, p);
    }
    public Graphics2D getGraphics2D(Canvas c, Paint p) {
        throw new RuntimeException("Not supported!");
    }
    public Graphics2D getGraphics2D() {
        return AndroidGraphics2D.getInstance();
    }
}
