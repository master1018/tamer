public class WindowsTreeUI extends BasicTreeUI {
    public static ComponentUI createUI( JComponent c )
      {
        return new WindowsTreeUI();
      }
    protected void ensureRowsAreVisible(int beginRow, int endRow) {
        if(tree != null && beginRow >= 0 && endRow < getRowCount(tree)) {
            Rectangle visRect = tree.getVisibleRect();
            if(beginRow == endRow) {
                Rectangle     scrollBounds = getPathBounds(tree, getPathForRow
                                                           (tree, beginRow));
                if(scrollBounds != null) {
                    scrollBounds.x = visRect.x;
                    scrollBounds.width = visRect.width;
                    tree.scrollRectToVisible(scrollBounds);
                }
            }
            else {
                Rectangle   beginRect = getPathBounds(tree, getPathForRow
                                                      (tree, beginRow));
                Rectangle   testRect = beginRect;
                int         beginY = beginRect.y;
                int         maxY = beginY + visRect.height;
                for(int counter = beginRow + 1; counter <= endRow; counter++) {
                    testRect = getPathBounds(tree,
                                             getPathForRow(tree, counter));
                    if((testRect.y + testRect.height) > maxY)
                        counter = endRow;
                }
                tree.scrollRectToVisible(new Rectangle(visRect.x, beginY, 1,
                                                  testRect.y + testRect.height-
                                                  beginY));
            }
        }
    }
    static protected final int HALF_SIZE = 4;
    static protected final int SIZE = 9;
    protected TreeCellRenderer createDefaultCellRenderer() {
        return new WindowsTreeCellRenderer();
    }
    public static class ExpandedIcon implements Icon, Serializable {
        static public Icon createExpandedIcon() {
            return new ExpandedIcon();
        }
        Skin getSkin(Component c) {
            XPStyle xp = XPStyle.getXP();
            return (xp != null) ? xp.getSkin(c, Part.TVP_GLYPH) : null;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Skin skin = getSkin(c);
            if (skin != null) {
                skin.paintSkin(g, x, y, State.OPENED);
                return;
            }
            Color     backgroundColor = c.getBackground();
            if(backgroundColor != null)
                g.setColor(backgroundColor);
            else
                g.setColor(Color.white);
            g.fillRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.gray);
            g.drawRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.black);
            g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
        }
        public int getIconWidth() {
            Skin skin = getSkin(null);
            return (skin != null) ? skin.getWidth() : SIZE;
        }
        public int getIconHeight() {
            Skin skin = getSkin(null);
            return (skin != null) ? skin.getHeight() : SIZE;
        }
    }
    public static class CollapsedIcon extends ExpandedIcon {
        static public Icon createCollapsedIcon() {
            return new CollapsedIcon();
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Skin skin = getSkin(c);
            if (skin != null) {
                skin.paintSkin(g, x, y, State.CLOSED);
            } else {
            super.paintIcon(c, g, x, y);
            g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
            }
        }
    }
    public class WindowsTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf, int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,
                                               expanded, leaf, row,
                                               hasFocus);
            if (!tree.isEnabled()) {
                setEnabled(false);
                if (leaf) {
                    setDisabledIcon(getLeafIcon());
                } else if (sel) {
                    setDisabledIcon(getOpenIcon());
                } else {
                    setDisabledIcon(getClosedIcon());
                }
            }
            else {
                setEnabled(true);
                if (leaf) {
                    setIcon(getLeafIcon());
                } else if (sel) {
                    setIcon(getOpenIcon());
                } else {
                    setIcon(getClosedIcon());
                }
            }
            return this;
        }
    }
}
