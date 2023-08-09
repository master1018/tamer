public class EmptyBorder extends AbstractBorder implements Serializable
{
    protected int left, right, top, bottom;
    public EmptyBorder(int top, int left, int bottom, int right)   {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }
    @ConstructorProperties({"borderInsets"})
    public EmptyBorder(Insets borderInsets)   {
        this.top = borderInsets.top;
        this.right = borderInsets.right;
        this.bottom = borderInsets.bottom;
        this.left = borderInsets.left;
    }
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    }
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = left;
        insets.top = top;
        insets.right = right;
        insets.bottom = bottom;
        return insets;
    }
    public Insets getBorderInsets() {
        return new Insets(top, left, bottom, right);
    }
    public boolean isBorderOpaque() { return false; }
}
