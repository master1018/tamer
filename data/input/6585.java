class MetalTitlePane extends JComponent {
    private static final Border handyEmptyBorder = new EmptyBorder(0,0,0,0);
    private static final int IMAGE_HEIGHT = 16;
    private static final int IMAGE_WIDTH = 16;
    private PropertyChangeListener propertyChangeListener;
    private JMenuBar menuBar;
    private Action closeAction;
    private Action iconifyAction;
    private Action restoreAction;
    private Action maximizeAction;
    private JButton toggleButton;
    private JButton iconifyButton;
    private JButton closeButton;
    private Icon maximizeIcon;
    private Icon minimizeIcon;
    private Image systemIcon;
    private WindowListener windowListener;
    private Window window;
    private JRootPane rootPane;
    private int buttonsWidth;
    private int state;
    private MetalRootPaneUI rootPaneUI;
    private Color inactiveBackground = UIManager.getColor("inactiveCaption");
    private Color inactiveForeground = UIManager.getColor("inactiveCaptionText");
    private Color inactiveShadow = UIManager.getColor("inactiveCaptionBorder");
    private Color activeBumpsHighlight = MetalLookAndFeel.getPrimaryControlHighlight();
    private Color activeBumpsShadow = MetalLookAndFeel.getPrimaryControlDarkShadow();
    private Color activeBackground = null;
    private Color activeForeground = null;
    private Color activeShadow = null;
    private MetalBumps activeBumps
        = new MetalBumps( 0, 0,
                          activeBumpsHighlight,
                          activeBumpsShadow,
                          MetalLookAndFeel.getPrimaryControl() );
    private MetalBumps inactiveBumps
        = new MetalBumps( 0, 0,
                          MetalLookAndFeel.getControlHighlight(),
                          MetalLookAndFeel.getControlDarkShadow(),
                          MetalLookAndFeel.getControl() );
    public MetalTitlePane(JRootPane root, MetalRootPaneUI ui) {
        this.rootPane = root;
        rootPaneUI = ui;
        state = -1;
        installSubcomponents();
        determineColors();
        installDefaults();
        setLayout(createLayout());
    }
    private void uninstall() {
        uninstallListeners();
        window = null;
        removeAll();
    }
    private void installListeners() {
        if (window != null) {
            windowListener = createWindowListener();
            window.addWindowListener(windowListener);
            propertyChangeListener = createWindowPropertyChangeListener();
            window.addPropertyChangeListener(propertyChangeListener);
        }
    }
    private void uninstallListeners() {
        if (window != null) {
            window.removeWindowListener(windowListener);
            window.removePropertyChangeListener(propertyChangeListener);
        }
    }
    private WindowListener createWindowListener() {
        return new WindowHandler();
    }
    private PropertyChangeListener createWindowPropertyChangeListener() {
        return new PropertyChangeHandler();
    }
    public JRootPane getRootPane() {
        return rootPane;
    }
    private int getWindowDecorationStyle() {
        return getRootPane().getWindowDecorationStyle();
    }
    public void addNotify() {
        super.addNotify();
        uninstallListeners();
        window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            if (window instanceof Frame) {
                setState(((Frame)window).getExtendedState());
            }
            else {
                setState(0);
            }
            setActive(window.isActive());
            installListeners();
            updateSystemIcon();
        }
    }
    public void removeNotify() {
        super.removeNotify();
        uninstallListeners();
        window = null;
    }
    private void installSubcomponents() {
        int decorationStyle = getWindowDecorationStyle();
        if (decorationStyle == JRootPane.FRAME) {
            createActions();
            menuBar = createMenuBar();
            add(menuBar);
            createButtons();
            add(iconifyButton);
            add(toggleButton);
            add(closeButton);
        } else if (decorationStyle == JRootPane.PLAIN_DIALOG ||
                decorationStyle == JRootPane.INFORMATION_DIALOG ||
                decorationStyle == JRootPane.ERROR_DIALOG ||
                decorationStyle == JRootPane.COLOR_CHOOSER_DIALOG ||
                decorationStyle == JRootPane.FILE_CHOOSER_DIALOG ||
                decorationStyle == JRootPane.QUESTION_DIALOG ||
                decorationStyle == JRootPane.WARNING_DIALOG) {
            createActions();
            createButtons();
            add(closeButton);
        }
    }
    private void determineColors() {
        switch (getWindowDecorationStyle()) {
        case JRootPane.FRAME:
            activeBackground = UIManager.getColor("activeCaption");
            activeForeground = UIManager.getColor("activeCaptionText");
            activeShadow = UIManager.getColor("activeCaptionBorder");
            break;
        case JRootPane.ERROR_DIALOG:
            activeBackground = UIManager.getColor(
                "OptionPane.errorDialog.titlePane.background");
            activeForeground = UIManager.getColor(
                "OptionPane.errorDialog.titlePane.foreground");
            activeShadow = UIManager.getColor(
                "OptionPane.errorDialog.titlePane.shadow");
            break;
        case JRootPane.QUESTION_DIALOG:
        case JRootPane.COLOR_CHOOSER_DIALOG:
        case JRootPane.FILE_CHOOSER_DIALOG:
            activeBackground = UIManager.getColor(
                "OptionPane.questionDialog.titlePane.background");
            activeForeground = UIManager.getColor(
                "OptionPane.questionDialog.titlePane.foreground");
            activeShadow = UIManager.getColor(
                "OptionPane.questionDialog.titlePane.shadow");
            break;
        case JRootPane.WARNING_DIALOG:
            activeBackground = UIManager.getColor(
                "OptionPane.warningDialog.titlePane.background");
            activeForeground = UIManager.getColor(
                "OptionPane.warningDialog.titlePane.foreground");
            activeShadow = UIManager.getColor(
                "OptionPane.warningDialog.titlePane.shadow");
            break;
        case JRootPane.PLAIN_DIALOG:
        case JRootPane.INFORMATION_DIALOG:
        default:
            activeBackground = UIManager.getColor("activeCaption");
            activeForeground = UIManager.getColor("activeCaptionText");
            activeShadow = UIManager.getColor("activeCaptionBorder");
            break;
        }
        activeBumps.setBumpColors(activeBumpsHighlight, activeBumpsShadow,
                                  activeBackground);
    }
    private void installDefaults() {
        setFont(UIManager.getFont("InternalFrame.titleFont", getLocale()));
    }
    private void uninstallDefaults() {
    }
    protected JMenuBar createMenuBar() {
        menuBar = new SystemMenuBar();
        menuBar.setFocusable(false);
        menuBar.setBorderPainted(true);
        menuBar.add(createMenu());
        return menuBar;
    }
    private void close() {
        Window window = getWindow();
        if (window != null) {
            window.dispatchEvent(new WindowEvent(
                                 window, WindowEvent.WINDOW_CLOSING));
        }
    }
    private void iconify() {
        Frame frame = getFrame();
        if (frame != null) {
            frame.setExtendedState(state | Frame.ICONIFIED);
        }
    }
    private void maximize() {
        Frame frame = getFrame();
        if (frame != null) {
            frame.setExtendedState(state | Frame.MAXIMIZED_BOTH);
        }
    }
    private void restore() {
        Frame frame = getFrame();
        if (frame == null) {
            return;
        }
        if ((state & Frame.ICONIFIED) != 0) {
            frame.setExtendedState(state & ~Frame.ICONIFIED);
        } else {
            frame.setExtendedState(state & ~Frame.MAXIMIZED_BOTH);
        }
    }
    private void createActions() {
        closeAction = new CloseAction();
        if (getWindowDecorationStyle() == JRootPane.FRAME) {
            iconifyAction = new IconifyAction();
            restoreAction = new RestoreAction();
            maximizeAction = new MaximizeAction();
        }
    }
    private JMenu createMenu() {
        JMenu menu = new JMenu("");
        if (getWindowDecorationStyle() == JRootPane.FRAME) {
            addMenuItems(menu);
        }
        return menu;
    }
    private void addMenuItems(JMenu menu) {
        Locale locale = getRootPane().getLocale();
        JMenuItem mi = menu.add(restoreAction);
        int mnemonic = MetalUtils.getInt("MetalTitlePane.restoreMnemonic", -1);
        if (mnemonic != -1) {
            mi.setMnemonic(mnemonic);
        }
        mi = menu.add(iconifyAction);
        mnemonic = MetalUtils.getInt("MetalTitlePane.iconifyMnemonic", -1);
        if (mnemonic != -1) {
            mi.setMnemonic(mnemonic);
        }
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(
                Frame.MAXIMIZED_BOTH)) {
            mi = menu.add(maximizeAction);
            mnemonic =
                MetalUtils.getInt("MetalTitlePane.maximizeMnemonic", -1);
            if (mnemonic != -1) {
                mi.setMnemonic(mnemonic);
            }
        }
        menu.add(new JSeparator());
        mi = menu.add(closeAction);
        mnemonic = MetalUtils.getInt("MetalTitlePane.closeMnemonic", -1);
        if (mnemonic != -1) {
            mi.setMnemonic(mnemonic);
        }
    }
    private JButton createTitleButton() {
        JButton button = new JButton();
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        return button;
    }
    private void createButtons() {
        closeButton = createTitleButton();
        closeButton.setAction(closeAction);
        closeButton.setText(null);
        closeButton.putClientProperty("paintActive", Boolean.TRUE);
        closeButton.setBorder(handyEmptyBorder);
        closeButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                      "Close");
        closeButton.setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
        if (getWindowDecorationStyle() == JRootPane.FRAME) {
            maximizeIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
            minimizeIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
            iconifyButton = createTitleButton();
            iconifyButton.setAction(iconifyAction);
            iconifyButton.setText(null);
            iconifyButton.putClientProperty("paintActive", Boolean.TRUE);
            iconifyButton.setBorder(handyEmptyBorder);
            iconifyButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                            "Iconify");
            iconifyButton.setIcon(UIManager.getIcon("InternalFrame.iconifyIcon"));
            toggleButton = createTitleButton();
            toggleButton.setAction(restoreAction);
            toggleButton.putClientProperty("paintActive", Boolean.TRUE);
            toggleButton.setBorder(handyEmptyBorder);
            toggleButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                           "Maximize");
            toggleButton.setIcon(maximizeIcon);
        }
    }
    private LayoutManager createLayout() {
        return new TitlePaneLayout();
    }
    private void setActive(boolean isActive) {
        Boolean activeB = isActive ? Boolean.TRUE : Boolean.FALSE;
        closeButton.putClientProperty("paintActive", activeB);
        if (getWindowDecorationStyle() == JRootPane.FRAME) {
            iconifyButton.putClientProperty("paintActive", activeB);
            toggleButton.putClientProperty("paintActive", activeB);
        }
        getRootPane().repaint();
    }
    private void setState(int state) {
        setState(state, false);
    }
    private void setState(int state, boolean updateRegardless) {
        Window w = getWindow();
        if (w != null && getWindowDecorationStyle() == JRootPane.FRAME) {
            if (this.state == state && !updateRegardless) {
                return;
            }
            Frame frame = getFrame();
            if (frame != null) {
                JRootPane rootPane = getRootPane();
                if (((state & Frame.MAXIMIZED_BOTH) != 0) &&
                        (rootPane.getBorder() == null ||
                        (rootPane.getBorder() instanceof UIResource)) &&
                            frame.isShowing()) {
                    rootPane.setBorder(null);
                }
                else if ((state & Frame.MAXIMIZED_BOTH) == 0) {
                    rootPaneUI.installBorder(rootPane);
                }
                if (frame.isResizable()) {
                    if ((state & Frame.MAXIMIZED_BOTH) != 0) {
                        updateToggleButton(restoreAction, minimizeIcon);
                        maximizeAction.setEnabled(false);
                        restoreAction.setEnabled(true);
                    }
                    else {
                        updateToggleButton(maximizeAction, maximizeIcon);
                        maximizeAction.setEnabled(true);
                        restoreAction.setEnabled(false);
                    }
                    if (toggleButton.getParent() == null ||
                        iconifyButton.getParent() == null) {
                        add(toggleButton);
                        add(iconifyButton);
                        revalidate();
                        repaint();
                    }
                    toggleButton.setText(null);
                }
                else {
                    maximizeAction.setEnabled(false);
                    restoreAction.setEnabled(false);
                    if (toggleButton.getParent() != null) {
                        remove(toggleButton);
                        revalidate();
                        repaint();
                    }
                }
            }
            else {
                maximizeAction.setEnabled(false);
                restoreAction.setEnabled(false);
                iconifyAction.setEnabled(false);
                remove(toggleButton);
                remove(iconifyButton);
                revalidate();
                repaint();
            }
            closeAction.setEnabled(true);
            this.state = state;
        }
    }
    private void updateToggleButton(Action action, Icon icon) {
        toggleButton.setAction(action);
        toggleButton.setIcon(icon);
        toggleButton.setText(null);
    }
    private Frame getFrame() {
        Window window = getWindow();
        if (window instanceof Frame) {
            return (Frame)window;
        }
        return null;
    }
    private Window getWindow() {
        return window;
    }
    private String getTitle() {
        Window w = getWindow();
        if (w instanceof Frame) {
            return ((Frame)w).getTitle();
        }
        else if (w instanceof Dialog) {
            return ((Dialog)w).getTitle();
        }
        return null;
    }
    public void paintComponent(Graphics g)  {
        if (getFrame() != null) {
            setState(getFrame().getExtendedState());
        }
        JRootPane rootPane = getRootPane();
        Window window = getWindow();
        boolean leftToRight = (window == null) ?
                               rootPane.getComponentOrientation().isLeftToRight() :
                               window.getComponentOrientation().isLeftToRight();
        boolean isSelected = (window == null) ? true : window.isActive();
        int width = getWidth();
        int height = getHeight();
        Color background;
        Color foreground;
        Color darkShadow;
        MetalBumps bumps;
        if (isSelected) {
            background = activeBackground;
            foreground = activeForeground;
            darkShadow = activeShadow;
            bumps = activeBumps;
        } else {
            background = inactiveBackground;
            foreground = inactiveForeground;
            darkShadow = inactiveShadow;
            bumps = inactiveBumps;
        }
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        g.setColor( darkShadow );
        g.drawLine ( 0, height - 1, width, height -1);
        g.drawLine ( 0, 0, 0 ,0);
        g.drawLine ( width - 1, 0 , width -1, 0);
        int xOffset = leftToRight ? 5 : width - 5;
        if (getWindowDecorationStyle() == JRootPane.FRAME) {
            xOffset += leftToRight ? IMAGE_WIDTH + 5 : - IMAGE_WIDTH - 5;
        }
        String theTitle = getTitle();
        if (theTitle != null) {
            FontMetrics fm = SwingUtilities2.getFontMetrics(rootPane, g);
            g.setColor(foreground);
            int yOffset = ( (height - fm.getHeight() ) / 2 ) + fm.getAscent();
            Rectangle rect = new Rectangle(0, 0, 0, 0);
            if (iconifyButton != null && iconifyButton.getParent() != null) {
                rect = iconifyButton.getBounds();
            }
            int titleW;
            if( leftToRight ) {
                if (rect.x == 0) {
                    rect.x = window.getWidth() - window.getInsets().right-2;
                }
                titleW = rect.x - xOffset - 4;
                theTitle = SwingUtilities2.clipStringIfNecessary(
                                rootPane, fm, theTitle, titleW);
            } else {
                titleW = xOffset - rect.x - rect.width - 4;
                theTitle = SwingUtilities2.clipStringIfNecessary(
                                rootPane, fm, theTitle, titleW);
                xOffset -= SwingUtilities2.stringWidth(rootPane, fm,
                                                       theTitle);
            }
            int titleLength = SwingUtilities2.stringWidth(rootPane, fm,
                                                          theTitle);
            SwingUtilities2.drawString(rootPane, g, theTitle, xOffset,
                                       yOffset );
            xOffset += leftToRight ? titleLength + 5  : -5;
        }
        int bumpXOffset;
        int bumpLength;
        if( leftToRight ) {
            bumpLength = width - buttonsWidth - xOffset - 5;
            bumpXOffset = xOffset;
        } else {
            bumpLength = xOffset - buttonsWidth - 5;
            bumpXOffset = buttonsWidth + 5;
        }
        int bumpYOffset = 3;
        int bumpHeight = getHeight() - (2 * bumpYOffset);
        bumps.setBumpArea( bumpLength, bumpHeight );
        bumps.paintIcon(this, g, bumpXOffset, bumpYOffset);
    }
    private class CloseAction extends AbstractAction {
        public CloseAction() {
            super(UIManager.getString("MetalTitlePane.closeTitle",
                                      getLocale()));
        }
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    private class IconifyAction extends AbstractAction {
        public IconifyAction() {
            super(UIManager.getString("MetalTitlePane.iconifyTitle",
                                      getLocale()));
        }
        public void actionPerformed(ActionEvent e) {
            iconify();
        }
    }
    private class RestoreAction extends AbstractAction {
        public RestoreAction() {
            super(UIManager.getString
                  ("MetalTitlePane.restoreTitle", getLocale()));
        }
        public void actionPerformed(ActionEvent e) {
            restore();
        }
    }
    private class MaximizeAction extends AbstractAction {
        public MaximizeAction() {
            super(UIManager.getString("MetalTitlePane.maximizeTitle",
                                      getLocale()));
        }
        public void actionPerformed(ActionEvent e) {
            maximize();
        }
    }
    private class SystemMenuBar extends JMenuBar {
        public void paint(Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (systemIcon != null) {
                g.drawImage(systemIcon, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
            } else {
                Icon icon = UIManager.getIcon("InternalFrame.icon");
                if (icon != null) {
                    icon.paintIcon(this, g, 0, 0);
                }
            }
        }
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(Math.max(IMAGE_WIDTH, size.width),
                                 Math.max(size.height, IMAGE_HEIGHT));
        }
    }
    private class TitlePaneLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component c) {}
        public void removeLayoutComponent(Component c) {}
        public Dimension preferredLayoutSize(Container c)  {
            int height = computeHeight();
            return new Dimension(height, height);
        }
        public Dimension minimumLayoutSize(Container c) {
            return preferredLayoutSize(c);
        }
        private int computeHeight() {
            FontMetrics fm = rootPane.getFontMetrics(getFont());
            int fontHeight = fm.getHeight();
            fontHeight += 7;
            int iconHeight = 0;
            if (getWindowDecorationStyle() == JRootPane.FRAME) {
                iconHeight = IMAGE_HEIGHT;
            }
            int finalHeight = Math.max( fontHeight, iconHeight );
            return finalHeight;
        }
        public void layoutContainer(Container c) {
            boolean leftToRight = (window == null) ?
                getRootPane().getComponentOrientation().isLeftToRight() :
                window.getComponentOrientation().isLeftToRight();
            int w = getWidth();
            int x;
            int y = 3;
            int spacing;
            int buttonHeight;
            int buttonWidth;
            if (closeButton != null && closeButton.getIcon() != null) {
                buttonHeight = closeButton.getIcon().getIconHeight();
                buttonWidth = closeButton.getIcon().getIconWidth();
            }
            else {
                buttonHeight = IMAGE_HEIGHT;
                buttonWidth = IMAGE_WIDTH;
            }
            x = leftToRight ? w : 0;
            spacing = 5;
            x = leftToRight ? spacing : w - buttonWidth - spacing;
            if (menuBar != null) {
                menuBar.setBounds(x, y, buttonWidth, buttonHeight);
            }
            x = leftToRight ? w : 0;
            spacing = 4;
            x += leftToRight ? -spacing -buttonWidth : spacing;
            if (closeButton != null) {
                closeButton.setBounds(x, y, buttonWidth, buttonHeight);
            }
            if( !leftToRight ) x += buttonWidth;
            if (getWindowDecorationStyle() == JRootPane.FRAME) {
                if (Toolkit.getDefaultToolkit().isFrameStateSupported(
                        Frame.MAXIMIZED_BOTH)) {
                    if (toggleButton.getParent() != null) {
                        spacing = 10;
                        x += leftToRight ? -spacing -buttonWidth : spacing;
                        toggleButton.setBounds(x, y, buttonWidth, buttonHeight);
                        if (!leftToRight) {
                            x += buttonWidth;
                        }
                    }
                }
                if (iconifyButton != null && iconifyButton.getParent() != null) {
                    spacing = 2;
                    x += leftToRight ? -spacing -buttonWidth : spacing;
                    iconifyButton.setBounds(x, y, buttonWidth, buttonHeight);
                    if (!leftToRight) {
                        x += buttonWidth;
                    }
                }
            }
            buttonsWidth = leftToRight ? w - x : x;
        }
    }
    private class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent pce) {
            String name = pce.getPropertyName();
            if ("resizable".equals(name) || "state".equals(name)) {
                Frame frame = getFrame();
                if (frame != null) {
                    setState(frame.getExtendedState(), true);
                }
                if ("resizable".equals(name)) {
                    getRootPane().repaint();
                }
            }
            else if ("title".equals(name)) {
                repaint();
            }
            else if ("componentOrientation" == name) {
                revalidate();
                repaint();
            }
            else if ("iconImage" == name) {
                updateSystemIcon();
                revalidate();
                repaint();
            }
        }
    }
    private void updateSystemIcon() {
        Window window = getWindow();
        if (window == null) {
            systemIcon = null;
            return;
        }
        java.util.List<Image> icons = window.getIconImages();
        assert icons != null;
        if (icons.size() == 0) {
            systemIcon = null;
        }
        else if (icons.size() == 1) {
            systemIcon = icons.get(0);
        }
        else {
            systemIcon = SunToolkit.getScaledIconImage(icons,
                                                       IMAGE_WIDTH,
                                                       IMAGE_HEIGHT);
        }
    }
    private class WindowHandler extends WindowAdapter {
        public void windowActivated(WindowEvent ev) {
            setActive(true);
        }
        public void windowDeactivated(WindowEvent ev) {
            setActive(false);
        }
    }
}
