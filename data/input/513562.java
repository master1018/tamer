public class UnifiedContentBorder extends AbstractBorder {
    private static final Color BORDER_TOP_COLOR1 = new Color(0x575757);
    private static final Color BORDER_BOTTOM_COLOR1 = new Color(0x404040);
    private static final Color BORDER_BOTTOM_COLOR2 = new Color(0xd8d8d8);
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(BORDER_TOP_COLOR1);
        g.drawLine(x, y, x + width, y);
        g.setColor(BORDER_BOTTOM_COLOR1);
        g.drawLine(x, y + height - 2, x + width, y + height - 2);
        g.setColor(BORDER_BOTTOM_COLOR2);
        g.drawLine(x, y + height - 1, x + width, y + height - 1);
    }
    public Insets getBorderInsets(Component component) {
        return new Insets(1, 0, 2, 0);
    }
    public boolean isBorderOpaque() {
        return true;
    }
}
