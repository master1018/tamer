public class DefaultTreeCellRenderer extends JLabel implements TreeCellRenderer
{
    private JTree tree;
    protected boolean selected;
    protected boolean hasFocus;
    private boolean drawsFocusBorderAroundIcon;
    private boolean drawDashedFocusIndicator;
    private Color treeBGColor;
    private Color focusBGColor;
    transient protected Icon closedIcon;
    transient protected Icon leafIcon;
    transient protected Icon openIcon;
    protected Color textSelectionColor;
    protected Color textNonSelectionColor;
    protected Color backgroundSelectionColor;
    protected Color backgroundNonSelectionColor;
    protected Color borderSelectionColor;
    private boolean isDropCell;
    private boolean fillBackground = true;
    private boolean inited;
    public DefaultTreeCellRenderer() {
        inited = true;
    }
    public void updateUI() {
        super.updateUI();
        if (!inited || (getLeafIcon() instanceof UIResource)) {
            setLeafIcon(DefaultLookup.getIcon(this, ui, "Tree.leafIcon"));
        }
        if (!inited || (getClosedIcon() instanceof UIResource)) {
            setClosedIcon(DefaultLookup.getIcon(this, ui, "Tree.closedIcon"));
        }
        if (!inited || (getOpenIcon() instanceof UIManager)) {
            setOpenIcon(DefaultLookup.getIcon(this, ui, "Tree.openIcon"));
        }
        if (!inited || (getTextSelectionColor() instanceof UIResource)) {
            setTextSelectionColor(
                    DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
        }
        if (!inited || (getTextNonSelectionColor() instanceof UIResource)) {
            setTextNonSelectionColor(
                    DefaultLookup.getColor(this, ui, "Tree.textForeground"));
        }
        if (!inited || (getBackgroundSelectionColor() instanceof UIResource)) {
            setBackgroundSelectionColor(
                    DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
        }
        if (!inited ||
                (getBackgroundNonSelectionColor() instanceof UIResource)) {
            setBackgroundNonSelectionColor(
                    DefaultLookup.getColor(this, ui, "Tree.textBackground"));
        }
        if (!inited || (getBorderSelectionColor() instanceof UIResource)) {
            setBorderSelectionColor(
                    DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
        }
        drawsFocusBorderAroundIcon = DefaultLookup.getBoolean(
                this, ui, "Tree.drawsFocusBorderAroundIcon", false);
        drawDashedFocusIndicator = DefaultLookup.getBoolean(
                this, ui, "Tree.drawDashedFocusIndicator", false);
        fillBackground = DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
        Insets margins = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
        if (margins != null) {
            setBorder(new EmptyBorder(margins.top, margins.left,
                    margins.bottom, margins.right));
        }
        setName("Tree.cellRenderer");
    }
    public Icon getDefaultOpenIcon() {
        return DefaultLookup.getIcon(this, ui, "Tree.openIcon");
    }
    public Icon getDefaultClosedIcon() {
        return DefaultLookup.getIcon(this, ui, "Tree.closedIcon");
    }
    public Icon getDefaultLeafIcon() {
        return DefaultLookup.getIcon(this, ui, "Tree.leafIcon");
    }
    public void setOpenIcon(Icon newIcon) {
        openIcon = newIcon;
    }
    public Icon getOpenIcon() {
        return openIcon;
    }
    public void setClosedIcon(Icon newIcon) {
        closedIcon = newIcon;
    }
    public Icon getClosedIcon() {
        return closedIcon;
    }
    public void setLeafIcon(Icon newIcon) {
        leafIcon = newIcon;
    }
    public Icon getLeafIcon() {
        return leafIcon;
    }
    public void setTextSelectionColor(Color newColor) {
        textSelectionColor = newColor;
    }
    public Color getTextSelectionColor() {
        return textSelectionColor;
    }
    public void setTextNonSelectionColor(Color newColor) {
        textNonSelectionColor = newColor;
    }
    public Color getTextNonSelectionColor() {
        return textNonSelectionColor;
    }
    public void setBackgroundSelectionColor(Color newColor) {
        backgroundSelectionColor = newColor;
    }
    public Color getBackgroundSelectionColor() {
        return backgroundSelectionColor;
    }
    public void setBackgroundNonSelectionColor(Color newColor) {
        backgroundNonSelectionColor = newColor;
    }
    public Color getBackgroundNonSelectionColor() {
        return backgroundNonSelectionColor;
    }
    public void setBorderSelectionColor(Color newColor) {
        borderSelectionColor = newColor;
    }
    public Color getBorderSelectionColor() {
        return borderSelectionColor;
    }
    public void setFont(Font font) {
        if(font instanceof FontUIResource)
            font = null;
        super.setFont(font);
    }
    public Font getFont() {
        Font font = super.getFont();
        if (font == null && tree != null) {
            font = tree.getFont();
        }
        return font;
    }
    public void setBackground(Color color) {
        if(color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        String         stringValue = tree.convertValueToText(value, sel,
                                          expanded, leaf, row, hasFocus);
        this.tree = tree;
        this.hasFocus = hasFocus;
        setText(stringValue);
        Color fg = null;
        isDropCell = false;
        JTree.DropLocation dropLocation = tree.getDropLocation();
        if (dropLocation != null
                && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {
            Color col = DefaultLookup.getColor(this, ui, "Tree.dropCellForeground");
            if (col != null) {
                fg = col;
            } else {
                fg = getTextSelectionColor();
            }
            isDropCell = true;
        } else if (sel) {
            fg = getTextSelectionColor();
        } else {
            fg = getTextNonSelectionColor();
        }
        setForeground(fg);
        Icon icon = null;
        if (leaf) {
            icon = getLeafIcon();
        } else if (expanded) {
            icon = getOpenIcon();
        } else {
            icon = getClosedIcon();
        }
        if (!tree.isEnabled()) {
            setEnabled(false);
            LookAndFeel laf = UIManager.getLookAndFeel();
            Icon disabledIcon = laf.getDisabledIcon(tree, icon);
            if (disabledIcon != null) icon = disabledIcon;
            setDisabledIcon(icon);
        } else {
            setEnabled(true);
            setIcon(icon);
        }
        setComponentOrientation(tree.getComponentOrientation());
        selected = sel;
        return this;
    }
    public void paint(Graphics g) {
        Color bColor;
        if (isDropCell) {
            bColor = DefaultLookup.getColor(this, ui, "Tree.dropCellBackground");
            if (bColor == null) {
                bColor = getBackgroundSelectionColor();
            }
        } else if (selected) {
            bColor = getBackgroundSelectionColor();
        } else {
            bColor = getBackgroundNonSelectionColor();
            if (bColor == null) {
                bColor = getBackground();
            }
        }
        int imageOffset = -1;
        if (bColor != null && fillBackground) {
            imageOffset = getLabelStart();
            g.setColor(bColor);
            if(getComponentOrientation().isLeftToRight()) {
                g.fillRect(imageOffset, 0, getWidth() - imageOffset,
                           getHeight());
            } else {
                g.fillRect(0, 0, getWidth() - imageOffset,
                           getHeight());
            }
        }
        if (hasFocus) {
            if (drawsFocusBorderAroundIcon) {
                imageOffset = 0;
            }
            else if (imageOffset == -1) {
                imageOffset = getLabelStart();
            }
            if(getComponentOrientation().isLeftToRight()) {
                paintFocus(g, imageOffset, 0, getWidth() - imageOffset,
                           getHeight(), bColor);
            } else {
                paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight(), bColor);
            }
        }
        super.paint(g);
    }
    private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
        Color       bsColor = getBorderSelectionColor();
        if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }
        if (drawDashedFocusIndicator && notColor != null) {
            if (treeBGColor != notColor) {
                treeBGColor = notColor;
                focusBGColor = new Color(~notColor.getRGB());
            }
            g.setColor(focusBGColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }
    private int getLabelStart() {
        Icon currentI = getIcon();
        if(currentI != null && getText() != null) {
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
        }
        return 0;
    }
    public Dimension getPreferredSize() {
        Dimension        retDimension = super.getPreferredSize();
        if(retDimension != null)
            retDimension = new Dimension(retDimension.width + 3,
                                         retDimension.height);
        return retDimension;
    }
    public void validate() {}
    public void invalidate() {}
    public void revalidate() {}
    public void repaint(long tm, int x, int y, int width, int height) {}
    public void repaint(Rectangle r) {}
    public void repaint() {}
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyName == "text"
                || ((propertyName == "font" || propertyName == "foreground")
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
