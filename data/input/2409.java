public class MetalComboBoxButton extends JButton {
    protected JComboBox comboBox;
    protected JList listBox;
    protected CellRendererPane rendererPane;
    protected Icon comboIcon;
    protected boolean iconOnly = false;
    public final JComboBox getComboBox() { return comboBox;}
    public final void setComboBox( JComboBox cb ) { comboBox = cb;}
    public final Icon getComboIcon() { return comboIcon;}
    public final void setComboIcon( Icon i ) { comboIcon = i;}
    public final boolean isIconOnly() { return iconOnly;}
    public final void setIconOnly( boolean isIconOnly ) { iconOnly = isIconOnly;}
    MetalComboBoxButton() {
        super( "" );
        DefaultButtonModel model = new DefaultButtonModel() {
            public void setArmed( boolean armed ) {
                super.setArmed( isPressed() ? true : armed );
            }
        };
        setModel( model );
    }
    public MetalComboBoxButton( JComboBox cb, Icon i,
                                CellRendererPane pane, JList list ) {
        this();
        comboBox = cb;
        comboIcon = i;
        rendererPane = pane;
        listBox = list;
        setEnabled( comboBox.isEnabled() );
    }
    public MetalComboBoxButton( JComboBox cb, Icon i, boolean onlyIcon,
                                CellRendererPane pane, JList list ) {
        this( cb, i, pane, list );
        iconOnly = onlyIcon;
    }
    public boolean isFocusTraversable() {
        return false;
    }
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setBackground(comboBox.getBackground());
            setForeground(comboBox.getForeground());
        } else {
            setBackground(UIManager.getColor("ComboBox.disabledBackground"));
            setForeground(UIManager.getColor("ComboBox.disabledForeground"));
        }
    }
    public void paintComponent( Graphics g ) {
        boolean leftToRight = MetalUtils.isLeftToRight(comboBox);
        super.paintComponent( g );
        Insets insets = getInsets();
        int width = getWidth() - (insets.left + insets.right);
        int height = getHeight() - (insets.top + insets.bottom);
        if ( height <= 0 || width <= 0 ) {
            return;
        }
        int left = insets.left;
        int top = insets.top;
        int right = left + (width - 1);
        int bottom = top + (height - 1);
        int iconWidth = 0;
        int iconLeft = (leftToRight) ? right : left;
        if ( comboIcon != null ) {
            iconWidth = comboIcon.getIconWidth();
            int iconHeight = comboIcon.getIconHeight();
            int iconTop = 0;
            if ( iconOnly ) {
                iconLeft = (getWidth() / 2) - (iconWidth / 2);
                iconTop = (getHeight() / 2) - (iconHeight / 2);
            }
            else {
                if (leftToRight) {
                    iconLeft = (left + (width - 1)) - iconWidth;
                }
                else {
                    iconLeft = left;
                }
                iconTop = (top + ((bottom - top) / 2)) - (iconHeight / 2);
            }
            comboIcon.paintIcon( this, g, iconLeft, iconTop );
            if ( comboBox.hasFocus() && (!MetalLookAndFeel.usingOcean() ||
                                         comboBox.isEditable())) {
                g.setColor( MetalLookAndFeel.getFocusColor() );
                g.drawRect( left - 1, top - 1, width + 3, height + 1 );
            }
        }
        if (MetalLookAndFeel.usingOcean()) {
            return;
        }
        if ( ! iconOnly && comboBox != null ) {
            ListCellRenderer renderer = comboBox.getRenderer();
            Component c;
            boolean renderPressed = getModel().isPressed();
            c = renderer.getListCellRendererComponent(listBox,
                                                      comboBox.getSelectedItem(),
                                                      -1,
                                                      renderPressed,
                                                      false);
            c.setFont(rendererPane.getFont());
            if ( model.isArmed() && model.isPressed() ) {
                if ( isOpaque() ) {
                    c.setBackground(UIManager.getColor("Button.select"));
                }
                c.setForeground(comboBox.getForeground());
            }
            else if ( !comboBox.isEnabled() ) {
                if ( isOpaque() ) {
                    c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
                }
                c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
            }
            else {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            }
            int cWidth = width - (insets.right + iconWidth);
            boolean shouldValidate = false;
            if (c instanceof JPanel)  {
                shouldValidate = true;
            }
            if (leftToRight) {
                rendererPane.paintComponent( g, c, this,
                                             left, top, cWidth, height, shouldValidate );
            }
            else {
                rendererPane.paintComponent( g, c, this,
                                             left + iconWidth, top, cWidth, height, shouldValidate );
            }
        }
    }
    public Dimension getMinimumSize() {
        Dimension ret = new Dimension();
        Insets insets = getInsets();
        ret.width = insets.left + getComboIcon().getIconWidth() + insets.right;
        ret.height = insets.bottom + getComboIcon().getIconHeight() + insets.top;
        return ret;
    }
}
