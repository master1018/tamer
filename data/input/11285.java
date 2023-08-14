public class DefaultLayoutStyle extends LayoutStyle {
    private static final DefaultLayoutStyle INSTANCE =
            new DefaultLayoutStyle();
    public static LayoutStyle getInstance() {
        return INSTANCE;
    }
    @Override
    public int getPreferredGap(JComponent component1, JComponent component2,
            ComponentPlacement type, int position, Container parent) {
        if (component1 == null || component2 == null || type == null) {
            throw new NullPointerException();
        }
        if (type == ComponentPlacement.INDENT &&
                (position == SwingConstants.EAST ||
                 position == SwingConstants.WEST)) {
            int indent = getIndent(component1, position);
            if (indent > 0) {
                return indent;
            }
        }
        return (type == ComponentPlacement.UNRELATED) ? 12 : 6;
    }
    @Override
    public int getContainerGap(JComponent component, int position,
                               Container parent) {
        if (component == null) {
            throw new NullPointerException();
        }
        checkPosition(position);
        return 6;
    }
    protected boolean isLabelAndNonlabel(JComponent c1, JComponent c2,
                                         int position) {
        if (position == SwingConstants.EAST ||
                position == SwingConstants.WEST) {
            boolean c1Label = (c1 instanceof JLabel);
            boolean c2Label = (c2 instanceof JLabel);
            return ((c1Label || c2Label) && (c1Label != c2Label));
        }
        return false;
    }
    protected int getButtonGap(JComponent source, JComponent target,
                               int position, int offset) {
        offset -= getButtonGap(source, position);
        if (offset > 0) {
            offset -= getButtonGap(target, flipDirection(position));
        }
        if (offset < 0) {
            return 0;
        }
        return offset;
    }
    protected int getButtonGap(JComponent source, int position, int offset) {
        offset -= getButtonGap(source, position);
        return Math.max(offset, 0);
    }
    public int getButtonGap(JComponent c, int position) {
        String classID = c.getUIClassID();
        if ((classID == "CheckBoxUI" || classID == "RadioButtonUI") &&
                !((AbstractButton)c).isBorderPainted()) {
            Border border = c.getBorder();
            if (border instanceof UIResource) {
                return getInset(c, position);
            }
        }
        return 0;
    }
    private void checkPosition(int position) {
        if (position != SwingConstants.NORTH &&
                position != SwingConstants.SOUTH &&
                position != SwingConstants.WEST &&
                position != SwingConstants.EAST) {
            throw new IllegalArgumentException();
        }
    }
    protected int flipDirection(int position) {
        switch(position) {
        case SwingConstants.NORTH:
            return SwingConstants.SOUTH;
        case SwingConstants.SOUTH:
            return SwingConstants.NORTH;
        case SwingConstants.EAST:
            return SwingConstants.WEST;
        case SwingConstants.WEST:
            return SwingConstants.EAST;
        }
        assert false;
        return 0;
    }
    protected int getIndent(JComponent c, int position) {
        String classID = c.getUIClassID();
        if (classID == "CheckBoxUI" || classID == "RadioButtonUI") {
            AbstractButton button = (AbstractButton)c;
            Insets insets = c.getInsets();
            Icon icon = getIcon(button);
            int gap = button.getIconTextGap();
            if (isLeftAligned(button, position)) {
                return insets.left + icon.getIconWidth() + gap;
            } else if (isRightAligned(button, position)) {
                return insets.right + icon.getIconWidth() + gap;
            }
        }
        return 0;
    }
    private Icon getIcon(AbstractButton button) {
        Icon icon = button.getIcon();
        if (icon != null) {
            return icon;
        }
        String key = null;
        if (button instanceof JCheckBox) {
            key = "CheckBox.icon";
        } else if (button instanceof JRadioButton) {
            key = "RadioButton.icon";
        }
        if (key != null) {
            Object oIcon = UIManager.get(key);
            if (oIcon instanceof Icon) {
                return (Icon)oIcon;
            }
        }
        return null;
    }
    private boolean isLeftAligned(AbstractButton button, int position) {
        if (position == SwingConstants.WEST) {
            boolean ltr = button.getComponentOrientation().isLeftToRight();
            int hAlign = button.getHorizontalAlignment();
            return ((ltr && (hAlign == SwingConstants.LEFT ||
                             hAlign == SwingConstants.LEADING)) ||
                    (!ltr && (hAlign == SwingConstants.TRAILING)));
        }
        return false;
    }
    private boolean isRightAligned(AbstractButton button, int position) {
        if (position == SwingConstants.EAST) {
            boolean ltr = button.getComponentOrientation().isLeftToRight();
            int hAlign = button.getHorizontalAlignment();
            return ((ltr && (hAlign == SwingConstants.RIGHT ||
                             hAlign == SwingConstants.TRAILING)) ||
                    (!ltr && (hAlign == SwingConstants.LEADING)));
        }
        return false;
    }
    private int getInset(JComponent c, int position) {
        return getInset(c.getInsets(), position);
    }
    private int getInset(Insets insets, int position) {
        if (insets == null) {
            return 0;
        }
        switch(position) {
        case SwingConstants.NORTH:
            return insets.top;
        case SwingConstants.SOUTH:
            return insets.bottom;
        case SwingConstants.EAST:
            return insets.right;
        case SwingConstants.WEST:
            return insets.left;
        }
        assert false;
        return 0;
    }
}
