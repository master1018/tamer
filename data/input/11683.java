public class MotifTreeUI extends BasicTreeUI
{
    static final int HALF_SIZE = 7;
    static final int SIZE = 14;
    public MotifTreeUI() {
        super();
    }
    public void installUI(JComponent c) {
        super.installUI(c);
    }
    protected void paintVerticalLine( Graphics g, JComponent c, int x, int top, int bottom )
      {
          if (tree.getComponentOrientation().isLeftToRight()) {
              g.fillRect( x, top, 2, bottom - top + 2 );
          } else {
              g.fillRect( x - 1, top, 2, bottom - top + 2 );
          }
      }
    protected void paintHorizontalLine( Graphics g, JComponent c, int y, int left, int right )
      {
          g.fillRect( left, y, right - left + 1, 2 );
      }
    public static class MotifExpandedIcon implements Icon, Serializable {
        static Color bg;
        static Color fg;
        static Color highlight;
        static Color shadow;
        public MotifExpandedIcon() {
            bg = UIManager.getColor("Tree.iconBackground");
            fg = UIManager.getColor("Tree.iconForeground");
            highlight = UIManager.getColor("Tree.iconHighlight");
            shadow = UIManager.getColor("Tree.iconShadow");
        }
        public static Icon createExpandedIcon() {
            return new MotifExpandedIcon();
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(highlight);
            g.drawLine(x, y, x+SIZE-1, y);
            g.drawLine(x, y+1, x, y+SIZE-1);
            g.setColor(shadow);
            g.drawLine(x+SIZE-1, y+1, x+SIZE-1, y+SIZE-1);
            g.drawLine(x+1, y+SIZE-1, x+SIZE-1, y+SIZE-1);
            g.setColor(bg);
            g.fillRect(x+1, y+1, SIZE-2, SIZE-2);
            g.setColor(fg);
            g.drawLine(x+3, y+HALF_SIZE-1, x+SIZE-4, y+HALF_SIZE-1);
            g.drawLine(x+3, y+HALF_SIZE, x+SIZE-4, y+HALF_SIZE);
        }
        public int getIconWidth() { return SIZE; }
        public int getIconHeight() { return SIZE; }
    }
    public static class MotifCollapsedIcon extends MotifExpandedIcon {
        public static Icon createCollapsedIcon() {
            return new MotifCollapsedIcon();
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.drawLine(x + HALF_SIZE-1, y + 3, x + HALF_SIZE-1, y + (SIZE - 4));
            g.drawLine(x + HALF_SIZE, y + 3, x + HALF_SIZE, y + (SIZE - 4));
        }
    }
    public static ComponentUI createUI(JComponent x) {
        return new MotifTreeUI();
    }
    public TreeCellRenderer createDefaultCellRenderer() {
        return new MotifTreeCellRenderer();
    }
}
