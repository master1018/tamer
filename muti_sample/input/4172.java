public abstract class AbstractBorder implements Border, Serializable
{
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    }
    public Insets getBorderInsets(Component c)       {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 0;
        return insets;
    }
    public boolean isBorderOpaque() { return false; }
    public Rectangle getInteriorRectangle(Component c, int x, int y, int width, int height) {
        return getInteriorRectangle(c, this, x, y, width, height);
    }
    public static Rectangle getInteriorRectangle(Component c, Border b, int x, int y, int width, int height) {
        Insets insets;
        if(b != null)
            insets = b.getBorderInsets(c);
        else
            insets = new Insets(0, 0, 0, 0);
        return new Rectangle(x + insets.left,
                                    y + insets.top,
                                    width - insets.right - insets.left,
                                    height - insets.top - insets.bottom);
    }
    public int getBaseline(Component c, int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        return -1;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            Component c) {
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
    static boolean isLeftToRight( Component c ) {
        return c.getComponentOrientation().isLeftToRight();
    }
}
