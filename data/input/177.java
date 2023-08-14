public class ColorIcon implements Icon {
    private Color color;
    public ColorIcon(Color c) {
        color = c;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, 16, 16);
    }
    public int getIconWidth() {
        return 16;
    }
    public int getIconHeight() {
        return 16;
    }
}
