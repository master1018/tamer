public class SampleTreeCellRenderer extends JLabel implements TreeCellRenderer {
    protected static Font defaultFont;
    protected static ImageIcon collapsedIcon;
    protected static ImageIcon expandedIcon;
    protected static final Color SELECTED_BACKGROUND_COLOR;
    static {
        if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
            SELECTED_BACKGROUND_COLOR = new Color(0, 0,
                0, 0);
        } else {
            SELECTED_BACKGROUND_COLOR = Color.YELLOW;
        }
        try {
            defaultFont = new Font("SansSerif", 0, 12);
        } catch (Exception e) {
        }
        try {
            collapsedIcon = new ImageIcon(SampleTreeCellRenderer.class.
                    getResource("/resources/images/collapsed.gif"));
            expandedIcon = new ImageIcon(SampleTreeCellRenderer.class.
                    getResource("/resources/images/expanded.gif"));
        } catch (Exception e) {
            System.out.println("Couldn't load images: " + e);
        }
    }
    protected boolean selected;
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded,
            boolean leaf, int row,
            boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, selected,
                expanded, leaf, row, hasFocus);
        setText(stringValue);
        setToolTipText(stringValue);
        if (expanded) {
            setIcon(expandedIcon);
        } else if (!leaf) {
            setIcon(collapsedIcon);
        } else {
            setIcon(null);
        }
        SampleData userObject = (SampleData) ((DefaultMutableTreeNode) value).
                getUserObject();
        if (hasFocus) {
            setForeground(UIManager.getColor("Tree.selectionForeground"));
        } else {
            setForeground(userObject.getColor());
        }
        if (userObject.getFont() == null) {
            setFont(defaultFont);
        } else {
            setFont(userObject.getFont());
        }
        this.selected = selected;
        return this;
    }
    @Override
    public void paint(Graphics g) {
        Color bColor;
        Icon currentI = getIcon();
        if (selected) {
            bColor = SELECTED_BACKGROUND_COLOR;
        } else if (getParent() != null)  {
            bColor = getParent().getBackground();
        } else {
            bColor = getBackground();
        }
        g.setColor(bColor);
        if (currentI != null && getText() != null) {
            int offset = (currentI.getIconWidth() + getIconTextGap());
            if (getComponentOrientation().isLeftToRight()) {
                g.fillRect(offset, 0, getWidth() - 1 - offset,
                        getHeight() - 1);
            } else {
                g.fillRect(0, 0, getWidth() - 1 - offset, getHeight() - 1);
            }
        } else {
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
        super.paint(g);
    }
}
