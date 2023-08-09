public class WindowsComboBoxUI extends BasicComboBoxUI {
    private static final MouseListener rolloverListener =
        new MouseAdapter() {
            private void handleRollover(MouseEvent e, boolean isRollover) {
                JComboBox comboBox = getComboBox(e);
                WindowsComboBoxUI comboBoxUI = getWindowsComboBoxUI(e);
                if (comboBox == null || comboBoxUI == null) {
                    return;
                }
                if (! comboBox.isEditable()) {
                    ButtonModel m = null;
                    if (comboBoxUI.arrowButton != null) {
                        m = comboBoxUI.arrowButton.getModel();
                    }
                    if (m != null ) {
                        m.setRollover(isRollover);
                    }
                }
                comboBoxUI.isRollover = isRollover;
                comboBox.repaint();
            }
            public void mouseEntered(MouseEvent e) {
                handleRollover(e, true);
            }
            public void mouseExited(MouseEvent e) {
                handleRollover(e, false);
            }
            private JComboBox getComboBox(MouseEvent event) {
                Object source = event.getSource();
                JComboBox rv = null;
                if (source instanceof JComboBox) {
                    rv = (JComboBox) source;
                } else if (source instanceof XPComboBoxButton) {
                    rv = ((XPComboBoxButton) source)
                        .getWindowsComboBoxUI().comboBox;
                }
                return rv;
            }
            private WindowsComboBoxUI getWindowsComboBoxUI(MouseEvent event) {
                JComboBox comboBox = getComboBox(event);
                WindowsComboBoxUI rv = null;
                if (comboBox != null
                    && comboBox.getUI() instanceof WindowsComboBoxUI) {
                    rv = (WindowsComboBoxUI) comboBox.getUI();
                }
                return rv;
            }
        };
    private boolean isRollover = false;
    private static final PropertyChangeListener componentOrientationListener =
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String propertyName = e.getPropertyName();
                Object source = null;
                if ("componentOrientation" == propertyName
                    && (source = e.getSource()) instanceof JComboBox
                    && ((JComboBox) source).getUI() instanceof
                      WindowsComboBoxUI) {
                    JComboBox comboBox = (JComboBox) source;
                    WindowsComboBoxUI comboBoxUI = (WindowsComboBoxUI) comboBox.getUI();
                    if (comboBoxUI.arrowButton instanceof XPComboBoxButton) {
                        ((XPComboBoxButton) comboBoxUI.arrowButton).setPart(
                                    (comboBox.getComponentOrientation() ==
                                       ComponentOrientation.RIGHT_TO_LEFT)
                                    ? Part.CP_DROPDOWNBUTTONLEFT
                                    : Part.CP_DROPDOWNBUTTONRIGHT);
                            }
                        }
                    }
                };
    public static ComponentUI createUI(JComponent c) {
        return new WindowsComboBoxUI();
    }
    public void installUI( JComponent c ) {
        super.installUI( c );
        isRollover = false;
        comboBox.setRequestFocusEnabled( true );
        if (XPStyle.getXP() != null && arrowButton != null) {
            comboBox.addMouseListener(rolloverListener);
            arrowButton.addMouseListener(rolloverListener);
        }
    }
    public void uninstallUI(JComponent c ) {
        comboBox.removeMouseListener(rolloverListener);
        if(arrowButton != null) {
            arrowButton.removeMouseListener(rolloverListener);
        }
        super.uninstallUI( c );
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        XPStyle xp = XPStyle.getXP();
        if (xp != null
              && xp.isSkinDefined(comboBox, Part.CP_DROPDOWNBUTTONRIGHT)) {
            comboBox.addPropertyChangeListener("componentOrientation",
                                               componentOrientationListener);
        }
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        comboBox.removePropertyChangeListener("componentOrientation",
                                              componentOrientationListener);
    }
    protected void configureEditor() {
        super.configureEditor();
        if (XPStyle.getXP() != null) {
            editor.addMouseListener(rolloverListener);
        }
    }
    protected void unconfigureEditor() {
        super.unconfigureEditor();
        editor.removeMouseListener(rolloverListener);
    }
    public void paint(Graphics g, JComponent c) {
        if (XPStyle.getXP() != null) {
            paintXPComboBoxBackground(g, c);
        }
        super.paint(g, c);
    }
    State getXPComboBoxState(JComponent c) {
        State state = State.NORMAL;
        if (!c.isEnabled()) {
            state = State.DISABLED;
        } else if (isPopupVisible(comboBox)) {
            state = State.PRESSED;
        } else if (isRollover) {
            state = State.HOT;
        }
        return state;
    }
    private void paintXPComboBoxBackground(Graphics g, JComponent c) {
        XPStyle xp = XPStyle.getXP();
        State state = getXPComboBoxState(c);
        Skin skin = null;
        if (! comboBox.isEditable()
              && xp.isSkinDefined(c, Part.CP_READONLY)) {
            skin = xp.getSkin(c, Part.CP_READONLY);
        }
        if (skin == null) {
            skin = xp.getSkin(c, Part.CP_COMBOBOX);
        }
        skin.paintSkin(g, 0, 0, c.getWidth(), c.getHeight(), state);
    }
    public void paintCurrentValue(Graphics g, Rectangle bounds,
                                  boolean hasFocus) {
        XPStyle xp = XPStyle.getXP();
        if ( xp != null) {
            bounds.x += 2;
            bounds.y += 2;
            bounds.width -= 4;
            bounds.height -= 4;
        } else {
            bounds.x += 1;
            bounds.y += 1;
            bounds.width -= 2;
            bounds.height -= 2;
        }
        if (! comboBox.isEditable()
            && xp != null
            && xp.isSkinDefined(comboBox, Part.CP_READONLY)) {
            ListCellRenderer renderer = comboBox.getRenderer();
            Component c;
            if ( hasFocus && !isPopupVisible(comboBox) ) {
                c = renderer.getListCellRendererComponent(
                        listBox,
                        comboBox.getSelectedItem(),
                        -1,
                        true,
                        false );
            } else {
                c = renderer.getListCellRendererComponent(
                        listBox,
                        comboBox.getSelectedItem(),
                        -1,
                        false,
                        false );
            }
            c.setFont(comboBox.getFont());
            if ( comboBox.isEnabled() ) {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            } else {
                c.setForeground(DefaultLookup.getColor(
                         comboBox, this, "ComboBox.disabledForeground", null));
                c.setBackground(DefaultLookup.getColor(
                         comboBox, this, "ComboBox.disabledBackground", null));
            }
            boolean shouldValidate = false;
            if (c instanceof JPanel)  {
                shouldValidate = true;
            }
            currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y,
                                            bounds.width, bounds.height, shouldValidate);
        } else {
            super.paintCurrentValue(g, bounds, hasFocus);
        }
    }
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds,
                                            boolean hasFocus) {
        if (XPStyle.getXP() == null) {
            super.paintCurrentValueBackground(g, bounds, hasFocus);
        }
    }
    public Dimension getMinimumSize( JComponent c ) {
        Dimension d = super.getMinimumSize(c);
        if (XPStyle.getXP() != null) {
            d.width += 5;
        } else {
            d.width += 4;
        }
        d.height += 2;
        return d;
    }
    protected LayoutManager createLayoutManager() {
        return new BasicComboBoxUI.ComboBoxLayoutManager() {
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);
                if (XPStyle.getXP() != null && arrowButton != null) {
                    Dimension d = parent.getSize();
                    Insets insets = getInsets();
                    int buttonWidth = arrowButton.getPreferredSize().width;
                    arrowButton.setBounds(WindowsGraphicsUtils.isLeftToRight((JComboBox)parent)
                                          ? (d.width - insets.right - buttonWidth)
                                          : insets.left,
                                          insets.top,
                                          buttonWidth, d.height - insets.top - insets.bottom);
                }
            }
        };
    }
    protected void installKeyboardActions() {
        super.installKeyboardActions();
    }
    protected ComboPopup createPopup() {
        return super.createPopup();
    }
    protected ComboBoxEditor createEditor() {
        return new WindowsComboBoxEditor();
    }
    @Override
    protected ListCellRenderer createRenderer() {
        XPStyle xp = XPStyle.getXP();
        if (xp != null && xp.isSkinDefined(comboBox, Part.CP_READONLY)) {
            return new WindowsComboBoxRenderer();
        } else {
            return super.createRenderer();
        }
    }
    protected JButton createArrowButton() {
        if (XPStyle.getXP() != null) {
            return new XPComboBoxButton();
        } else {
            return super.createArrowButton();
        }
    }
    private class XPComboBoxButton extends XPStyle.GlyphButton {
        public XPComboBoxButton() {
            super(null,
                  (! XPStyle.getXP().isSkinDefined(comboBox, Part.CP_DROPDOWNBUTTONRIGHT))
                   ? Part.CP_DROPDOWNBUTTON
                   : (comboBox.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
                     ? Part.CP_DROPDOWNBUTTONLEFT
                     : Part.CP_DROPDOWNBUTTONRIGHT
                  );
            setRequestFocusEnabled(false);
        }
        @Override
        protected State getState() {
            State rv;
            rv = super.getState();
            if (rv != State.DISABLED
                && comboBox != null && ! comboBox.isEditable()
                && XPStyle.getXP().isSkinDefined(comboBox,
                                                 Part.CP_DROPDOWNBUTTONRIGHT)) {
                rv = State.NORMAL;
            }
            return rv;
        }
        public Dimension getPreferredSize() {
            return new Dimension(17, 21);
        }
        void setPart(Part part) {
            setPart(comboBox, part);
        }
        WindowsComboBoxUI getWindowsComboBoxUI() {
            return WindowsComboBoxUI.this;
        }
    }
    @Deprecated
    protected class WindowsComboPopup extends BasicComboPopup {
        public WindowsComboPopup( JComboBox cBox ) {
            super( cBox );
        }
        protected KeyListener createKeyListener() {
            return new InvocationKeyHandler();
        }
        protected class InvocationKeyHandler extends BasicComboPopup.InvocationKeyHandler {
            protected InvocationKeyHandler() {
                WindowsComboPopup.this.super();
            }
        }
    }
    public static class WindowsComboBoxEditor
        extends BasicComboBoxEditor.UIResource {
        protected JTextField createEditorComponent() {
            JTextField editor = super.createEditorComponent();
            Border border = (Border)UIManager.get("ComboBox.editorBorder");
            if (border != null) {
                editor.setBorder(border);
            }
            editor.setOpaque(false);
            return editor;
        }
        public void setItem(Object item) {
            super.setItem(item);
            if (editor.hasFocus()) {
                editor.selectAll();
            }
        }
    }
    private static class WindowsComboBoxRenderer
          extends BasicComboBoxRenderer.UIResource {
        private static final Object BORDER_KEY
            = new StringUIClientPropertyKey("BORDER_KEY");
        private static final Border NULL_BORDER = new EmptyBorder(0, 0, 0, 0);
        @Override
        public Component getListCellRendererComponent(
                                                 JList list,
                                                 Object value,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {
            Component rv =
                super.getListCellRendererComponent(list, value, index,
                                                   isSelected, cellHasFocus);
            if (rv instanceof JComponent) {
                JComponent component = (JComponent) rv;
                if (index == -1 && isSelected) {
                    Border border = component.getBorder();
                    Border dashedBorder =
                        new WindowsBorders.DashedBorder(list.getForeground());
                    component.setBorder(dashedBorder);
                    if (component.getClientProperty(BORDER_KEY) == null) {
                        component.putClientProperty(BORDER_KEY,
                                       (border == null) ? NULL_BORDER : border);
                    }
                } else {
                    if (component.getBorder() instanceof
                          WindowsBorders.DashedBorder) {
                        Object storedBorder = component.getClientProperty(BORDER_KEY);
                        if (storedBorder instanceof Border) {
                            component.setBorder(
                                (storedBorder == NULL_BORDER) ? null
                                    : (Border) storedBorder);
                        }
                        component.putClientProperty(BORDER_KEY, null);
                    }
                }
                if (index == -1) {
                    component.setOpaque(false);
                    component.setForeground(list.getForeground());
                } else {
                    component.setOpaque(true);
                }
            }
            return rv;
        }
    }
}
