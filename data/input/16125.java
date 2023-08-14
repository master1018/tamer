public class MetalComboBoxUI extends BasicComboBoxUI {
    public static ComponentUI createUI(JComponent c) {
        return new MetalComboBoxUI();
    }
    public void paint(Graphics g, JComponent c) {
        if (MetalLookAndFeel.usingOcean()) {
            super.paint(g, c);
        }
    }
    public void paintCurrentValue(Graphics g, Rectangle bounds,
                                  boolean hasFocus) {
        if (MetalLookAndFeel.usingOcean()) {
            bounds.x += 2;
            bounds.width -= 3;
            if (arrowButton != null) {
                Insets buttonInsets = arrowButton.getInsets();
                bounds.y += buttonInsets.top;
                bounds.height -= (buttonInsets.top + buttonInsets.bottom);
            }
            else {
                bounds.y += 2;
                bounds.height -= 4;
            }
            super.paintCurrentValue(g, bounds, hasFocus);
        }
        else if (g == null || bounds == null) {
            throw new NullPointerException(
                "Must supply a non-null Graphics and Rectangle");
        }
    }
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds,
                                            boolean hasFocus) {
        if (MetalLookAndFeel.usingOcean()) {
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height - 1);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2,
                       bounds.height - 3);
            if (hasFocus && !isPopupVisible(comboBox) &&
                    arrowButton != null) {
                g.setColor(listBox.getSelectionBackground());
                Insets buttonInsets = arrowButton.getInsets();
                if (buttonInsets.top > 2) {
                    g.fillRect(bounds.x + 2, bounds.y + 2, bounds.width - 3,
                               buttonInsets.top - 2);
                }
                if (buttonInsets.bottom > 2) {
                    g.fillRect(bounds.x + 2, bounds.y + bounds.height -
                               buttonInsets.bottom, bounds.width - 3,
                               buttonInsets.bottom - 2);
                }
            }
        }
        else if (g == null || bounds == null) {
            throw new NullPointerException(
                "Must supply a non-null Graphics and Rectangle");
        }
    }
    public int getBaseline(JComponent c, int width, int height) {
        int baseline;
        if (MetalLookAndFeel.usingOcean() && height >= 4) {
            height -= 4;
            baseline = super.getBaseline(c, width, height);
            if (baseline >= 0) {
                baseline += 2;
            }
        }
        else {
            baseline = super.getBaseline(c, width, height);
        }
        return baseline;
    }
    protected ComboBoxEditor createEditor() {
        return new MetalComboBoxEditor.UIResource();
    }
    protected ComboPopup createPopup() {
        return super.createPopup();
    }
    protected JButton createArrowButton() {
        boolean iconOnly = (comboBox.isEditable() ||
                            MetalLookAndFeel.usingOcean());
        JButton button = new MetalComboBoxButton( comboBox,
                                                  new MetalComboBoxIcon(),
                                                  iconOnly,
                                                  currentValuePane,
                                                  listBox );
        button.setMargin( new Insets( 0, 1, 1, 3 ) );
        if (MetalLookAndFeel.usingOcean()) {
            button.putClientProperty(MetalBorders.NO_BUTTON_ROLLOVER,
                                     Boolean.TRUE);
        }
        updateButtonForOcean(button);
        return button;
    }
    private void updateButtonForOcean(JButton button) {
        if (MetalLookAndFeel.usingOcean()) {
            button.setFocusPainted(comboBox.isEditable());
        }
    }
    public PropertyChangeListener createPropertyChangeListener() {
        return new MetalPropertyChangeListener();
    }
    public class MetalPropertyChangeListener extends BasicComboBoxUI.PropertyChangeHandler {
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange( e );
            String propertyName = e.getPropertyName();
            if ( propertyName == "editable" ) {
                if(arrowButton instanceof MetalComboBoxButton) {
                            MetalComboBoxButton button = (MetalComboBoxButton)arrowButton;
                            button.setIconOnly( comboBox.isEditable() ||
                                    MetalLookAndFeel.usingOcean() );
                }
                        comboBox.repaint();
                updateButtonForOcean(arrowButton);
            } else if ( propertyName == "background" ) {
                Color color = (Color)e.getNewValue();
                arrowButton.setBackground(color);
                listBox.setBackground(color);
            } else if ( propertyName == "foreground" ) {
                Color color = (Color)e.getNewValue();
                arrowButton.setForeground(color);
                listBox.setForeground(color);
            }
        }
    }
    @Deprecated
    protected void editablePropertyChanged( PropertyChangeEvent e ) { }
    protected LayoutManager createLayoutManager() {
        return new MetalComboBoxLayoutManager();
    }
    public class MetalComboBoxLayoutManager extends BasicComboBoxUI.ComboBoxLayoutManager {
        public void layoutContainer( Container parent ) {
            layoutComboBox( parent, this );
        }
        public void superLayout( Container parent ) {
            super.layoutContainer( parent );
        }
    }
    public void layoutComboBox( Container parent, MetalComboBoxLayoutManager manager ) {
        if (comboBox.isEditable() && !MetalLookAndFeel.usingOcean()) {
            manager.superLayout( parent );
            return;
        }
        if (arrowButton != null) {
            if (MetalLookAndFeel.usingOcean() ) {
                Insets insets = comboBox.getInsets();
                int buttonWidth = arrowButton.getMinimumSize().width;
                arrowButton.setBounds(MetalUtils.isLeftToRight(comboBox)
                                ? (comboBox.getWidth() - insets.right - buttonWidth)
                                : insets.left,
                            insets.top, buttonWidth,
                            comboBox.getHeight() - insets.top - insets.bottom);
            }
            else {
                Insets insets = comboBox.getInsets();
                int width = comboBox.getWidth();
                int height = comboBox.getHeight();
                arrowButton.setBounds( insets.left, insets.top,
                                       width - (insets.left + insets.right),
                                       height - (insets.top + insets.bottom) );
            }
        }
        if (editor != null && MetalLookAndFeel.usingOcean()) {
            Rectangle cvb = rectangleForCurrentValue();
            editor.setBounds(cvb);
        }
    }
    @Deprecated
    protected void removeListeners() {
        if ( propertyChangeListener != null ) {
            comboBox.removePropertyChangeListener( propertyChangeListener );
        }
    }
    public void configureEditor() {
        super.configureEditor();
    }
    public void unconfigureEditor() {
        super.unconfigureEditor();
    }
    public Dimension getMinimumSize( JComponent c ) {
        if ( !isMinimumSizeDirty ) {
            return new Dimension( cachedMinimumSize );
        }
        Dimension size = null;
        if ( !comboBox.isEditable() &&
             arrowButton != null) {
            Insets buttonInsets = arrowButton.getInsets();
            Insets insets = comboBox.getInsets();
            size = getDisplaySize();
            size.width += insets.left + insets.right;
            size.width += buttonInsets.right;
            size.width += arrowButton.getMinimumSize().width;
            size.height += insets.top + insets.bottom;
            size.height += buttonInsets.top + buttonInsets.bottom;
        }
        else if ( comboBox.isEditable() &&
                  arrowButton != null &&
                  editor != null ) {
            size = super.getMinimumSize( c );
            Insets margin = arrowButton.getMargin();
            size.height += margin.top + margin.bottom;
            size.width += margin.left + margin.right;
        }
        else {
            size = super.getMinimumSize( c );
        }
        cachedMinimumSize.setSize( size.width, size.height );
        isMinimumSizeDirty = false;
        return new Dimension( cachedMinimumSize );
    }
    @Deprecated
    public class MetalComboPopup extends BasicComboPopup {
        public MetalComboPopup( JComboBox cBox) {
            super( cBox );
        }
        public void delegateFocus(MouseEvent e) {
            super.delegateFocus(e);
        }
    }
}
