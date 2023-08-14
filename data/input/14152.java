public class IconUIResource implements Icon, UIResource, Serializable
{
    private Icon delegate;
    public IconUIResource(Icon delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("null delegate icon argument");
        }
        this.delegate = delegate;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        delegate.paintIcon(c, g, x, y);
    }
    public int getIconWidth() {
        return delegate.getIconWidth();
    }
    public int getIconHeight() {
        return delegate.getIconHeight();
    }
}
