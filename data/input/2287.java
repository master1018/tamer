public class MotifPopupMenuUI extends BasicPopupMenuUI {
    private static Border border = null;
    private Font titleFont = null;
    public static ComponentUI createUI(JComponent x) {
        return new MotifPopupMenuUI();
    }
    public Dimension getPreferredSize(JComponent c) {
        LayoutManager layout = c.getLayout();
        Dimension d = layout.preferredLayoutSize(c);
        String title = ((JPopupMenu)c).getLabel();
        if (titleFont == null) {
            UIDefaults table = UIManager.getLookAndFeelDefaults();
            titleFont = table.getFont("PopupMenu.font");
        }
        FontMetrics fm = c.getFontMetrics(titleFont);
        int         stringWidth = 0;
        if (title!=null) {
            stringWidth += SwingUtilities2.stringWidth(c, fm, title);
        }
        if (d.width < stringWidth) {
            d.width = stringWidth + 8;
            Insets i = c.getInsets();
            if (i!=null) {
                d.width += i.left + i.right;
            }
            if (border != null) {
                i = border.getBorderInsets(c);
                d.width += i.left + i.right;
            }
            return d;
        }
        return null;
    }
    protected ChangeListener createChangeListener(JPopupMenu m) {
        return new ChangeListener() {
            public void stateChanged(ChangeEvent e) {}
        };
    }
    public boolean isPopupTrigger(MouseEvent e) {
        return ((e.getID()==MouseEvent.MOUSE_PRESSED)
                && ((e.getModifiers() & MouseEvent.BUTTON3_MASK)!=0));
    }
}
