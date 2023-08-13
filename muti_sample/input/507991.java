public class AwtFactory {
    private static GraphicsFactory gf;
    public static Graphics2D getAwtGraphics(Canvas c, Paint p) {
        if (null == gf) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            gf = tk.getGraphicsFactory();
        }
        return gf.getGraphics2D(c, p);
    }
}
