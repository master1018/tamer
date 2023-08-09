public class BasicMenuItemUI extends MenuItemUI
{
    protected JMenuItem menuItem = null;
    protected Color selectionBackground;
    protected Color selectionForeground;
    protected Color disabledForeground;
    protected Color acceleratorForeground;
    protected Color acceleratorSelectionForeground;
    protected String acceleratorDelimiter;
    protected int defaultTextIconGap;
    protected Font acceleratorFont;
    protected MouseInputListener mouseInputListener;
    protected MenuDragMouseListener menuDragMouseListener;
    protected MenuKeyListener menuKeyListener;
    protected PropertyChangeListener propertyChangeListener;
    Handler handler;
    protected Icon arrowIcon = null;
    protected Icon checkIcon = null;
    protected boolean oldBorderPainted;
    private static final boolean TRACE =   false; 
    private static final boolean VERBOSE = false; 
    private static final boolean DEBUG =   false;  
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.CLICK));
        BasicLookAndFeel.installAudioActionMap(map);
    }
    public static ComponentUI createUI(JComponent c) {
        return new BasicMenuItemUI();
    }
    public void installUI(JComponent c) {
        menuItem = (JMenuItem) c;
        installDefaults();
        installComponents(menuItem);
        installListeners();
        installKeyboardActions();
    }
    protected void installDefaults() {
        String prefix = getPropertyPrefix();
        acceleratorFont = UIManager.getFont("MenuItem.acceleratorFont");
        if (acceleratorFont == null) {
            acceleratorFont = UIManager.getFont("MenuItem.font");
        }
        Object opaque = UIManager.get(getPropertyPrefix() + ".opaque");
        if (opaque != null) {
            LookAndFeel.installProperty(menuItem, "opaque", opaque);
        }
        else {
            LookAndFeel.installProperty(menuItem, "opaque", Boolean.TRUE);
        }
        if(menuItem.getMargin() == null ||
           (menuItem.getMargin() instanceof UIResource)) {
            menuItem.setMargin(UIManager.getInsets(prefix + ".margin"));
        }
        LookAndFeel.installProperty(menuItem, "iconTextGap", Integer.valueOf(4));
        defaultTextIconGap = menuItem.getIconTextGap();
        LookAndFeel.installBorder(menuItem, prefix + ".border");
        oldBorderPainted = menuItem.isBorderPainted();
        LookAndFeel.installProperty(menuItem, "borderPainted",
                                    UIManager.getBoolean(prefix + ".borderPainted"));
        LookAndFeel.installColorsAndFont(menuItem,
                                         prefix + ".background",
                                         prefix + ".foreground",
                                         prefix + ".font");
        if (selectionBackground == null ||
            selectionBackground instanceof UIResource) {
            selectionBackground =
                UIManager.getColor(prefix + ".selectionBackground");
        }
        if (selectionForeground == null ||
            selectionForeground instanceof UIResource) {
            selectionForeground =
                UIManager.getColor(prefix + ".selectionForeground");
        }
        if (disabledForeground == null ||
            disabledForeground instanceof UIResource) {
            disabledForeground =
                UIManager.getColor(prefix + ".disabledForeground");
        }
        if (acceleratorForeground == null ||
            acceleratorForeground instanceof UIResource) {
            acceleratorForeground =
                UIManager.getColor(prefix + ".acceleratorForeground");
        }
        if (acceleratorSelectionForeground == null ||
            acceleratorSelectionForeground instanceof UIResource) {
            acceleratorSelectionForeground =
                UIManager.getColor(prefix + ".acceleratorSelectionForeground");
        }
        acceleratorDelimiter =
            UIManager.getString("MenuItem.acceleratorDelimiter");
        if (acceleratorDelimiter == null) { acceleratorDelimiter = "+"; }
        if (arrowIcon == null ||
            arrowIcon instanceof UIResource) {
            arrowIcon = UIManager.getIcon(prefix + ".arrowIcon");
        }
        if (checkIcon == null ||
            checkIcon instanceof UIResource) {
            checkIcon = UIManager.getIcon(prefix + ".checkIcon");
            boolean isColumnLayout = MenuItemLayoutHelper.isColumnLayout(
                    BasicGraphicsUtils.isLeftToRight(menuItem), menuItem);
            if (isColumnLayout) {
                MenuItemCheckIconFactory iconFactory =
                    (MenuItemCheckIconFactory) UIManager.get(prefix
                        + ".checkIconFactory");
                if (iconFactory != null
                        && MenuItemLayoutHelper.useCheckAndArrow(menuItem)
                        && iconFactory.isCompatible(checkIcon, prefix)) {
                    checkIcon = iconFactory.getIcon(menuItem);
                }
            }
        }
    }
    protected void installComponents(JMenuItem menuItem){
        BasicHTML.updateRenderer(menuItem, menuItem.getText());
    }
    protected String getPropertyPrefix() {
        return "MenuItem";
    }
    protected void installListeners() {
        if ((mouseInputListener = createMouseInputListener(menuItem)) != null) {
            menuItem.addMouseListener(mouseInputListener);
            menuItem.addMouseMotionListener(mouseInputListener);
        }
        if ((menuDragMouseListener = createMenuDragMouseListener(menuItem)) != null) {
            menuItem.addMenuDragMouseListener(menuDragMouseListener);
        }
        if ((menuKeyListener = createMenuKeyListener(menuItem)) != null) {
            menuItem.addMenuKeyListener(menuKeyListener);
        }
        if ((propertyChangeListener = createPropertyChangeListener(menuItem)) != null) {
            menuItem.addPropertyChangeListener(propertyChangeListener);
        }
    }
    protected void installKeyboardActions() {
        installLazyActionMap();
        updateAcceleratorBinding();
    }
    void installLazyActionMap() {
        LazyActionMap.installLazyActionMap(menuItem, BasicMenuItemUI.class,
                                           getPropertyPrefix() + ".actionMap");
    }
    public void uninstallUI(JComponent c) {
        menuItem = (JMenuItem)c;
        uninstallDefaults();
        uninstallComponents(menuItem);
        uninstallListeners();
        uninstallKeyboardActions();
        MenuItemLayoutHelper.clearUsedParentClientProperties(menuItem);
        menuItem = null;
    }
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(menuItem);
        LookAndFeel.installProperty(menuItem, "borderPainted", oldBorderPainted);
        if (menuItem.getMargin() instanceof UIResource)
            menuItem.setMargin(null);
        if (arrowIcon instanceof UIResource)
            arrowIcon = null;
        if (checkIcon instanceof UIResource)
            checkIcon = null;
    }
    protected void uninstallComponents(JMenuItem menuItem){
        BasicHTML.updateRenderer(menuItem, "");
    }
    protected void uninstallListeners() {
        if (mouseInputListener != null) {
            menuItem.removeMouseListener(mouseInputListener);
            menuItem.removeMouseMotionListener(mouseInputListener);
        }
        if (menuDragMouseListener != null) {
            menuItem.removeMenuDragMouseListener(menuDragMouseListener);
        }
        if (menuKeyListener != null) {
            menuItem.removeMenuKeyListener(menuKeyListener);
        }
        if (propertyChangeListener != null) {
            menuItem.removePropertyChangeListener(propertyChangeListener);
        }
        mouseInputListener = null;
        menuDragMouseListener = null;
        menuKeyListener = null;
        propertyChangeListener = null;
        handler = null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(menuItem, null);
        SwingUtilities.replaceUIInputMap(menuItem, JComponent.
                                         WHEN_IN_FOCUSED_WINDOW, null);
    }
    protected MouseInputListener createMouseInputListener(JComponent c) {
        return getHandler();
    }
    protected MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
        return getHandler();
    }
    protected MenuKeyListener createMenuKeyListener(JComponent c) {
        return null;
    }
    protected PropertyChangeListener
                                  createPropertyChangeListener(JComponent c) {
        return getHandler();
    }
    Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    InputMap createInputMap(int condition) {
        if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
            return new ComponentInputMapUIResource(menuItem);
        }
        return null;
    }
    void updateAcceleratorBinding() {
        KeyStroke accelerator = menuItem.getAccelerator();
        InputMap windowInputMap = SwingUtilities.getUIInputMap(
                       menuItem, JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (windowInputMap != null) {
            windowInputMap.clear();
        }
        if (accelerator != null) {
            if (windowInputMap == null) {
                windowInputMap = createInputMap(JComponent.
                                                WHEN_IN_FOCUSED_WINDOW);
                SwingUtilities.replaceUIInputMap(menuItem,
                           JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
            }
            windowInputMap.put(accelerator, "doClick");
        }
    }
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = null;
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d = getPreferredSize(c);
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }
    public Dimension getPreferredSize(JComponent c) {
        return getPreferredMenuItemSize(c,
                                        checkIcon,
                                        arrowIcon,
                                        defaultTextIconGap);
    }
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = null;
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d = getPreferredSize(c);
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }
    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                 Icon checkIcon,
                                                 Icon arrowIcon,
                                                 int defaultTextIconGap) {
        JMenuItem mi = (JMenuItem) c;
        MenuItemLayoutHelper lh = new MenuItemLayoutHelper(mi, checkIcon,
                arrowIcon, MenuItemLayoutHelper.createMaxRect(), defaultTextIconGap,
                acceleratorDelimiter, BasicGraphicsUtils.isLeftToRight(mi),
                mi.getFont(), acceleratorFont,
                MenuItemLayoutHelper.useCheckAndArrow(menuItem),
                getPropertyPrefix());
        Dimension result = new Dimension();
        result.width = lh.getLeadingGap();
        MenuItemLayoutHelper.addMaxWidth(lh.getCheckSize(),
                lh.getAfterCheckIconGap(), result);
        if ((!lh.isTopLevelMenu())
                && (lh.getMinTextOffset() > 0)
                && (result.width < lh.getMinTextOffset())) {
            result.width = lh.getMinTextOffset();
        }
        MenuItemLayoutHelper.addMaxWidth(lh.getLabelSize(), lh.getGap(), result);
        MenuItemLayoutHelper.addMaxWidth(lh.getAccSize(), lh.getGap(), result);
        MenuItemLayoutHelper.addMaxWidth(lh.getArrowSize(), lh.getGap(), result);
        result.height = MenuItemLayoutHelper.max(lh.getCheckSize().getHeight(),
                lh.getLabelSize().getHeight(), lh.getAccSize().getHeight(),
                lh.getArrowSize().getHeight());
        Insets insets = lh.getMenuItem().getInsets();
        if(insets != null) {
            result.width += insets.left + insets.right;
            result.height += insets.top + insets.bottom;
        }
        if(result.width%2 == 0) {
            result.width++;
        }
        if(result.height%2 == 0
                && Boolean.TRUE !=
                    UIManager.get(getPropertyPrefix() + ".evenHeight")) {
            result.height++;
        }
        return result;
    }
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }
    public void paint(Graphics g, JComponent c) {
        paintMenuItem(g, c, checkIcon, arrowIcon,
                      selectionBackground, selectionForeground,
                      defaultTextIconGap);
    }
    protected void paintMenuItem(Graphics g, JComponent c,
                                     Icon checkIcon, Icon arrowIcon,
                                     Color background, Color foreground,
                                     int defaultTextIconGap) {
        Font holdf = g.getFont();
        Color holdc = g.getColor();
        JMenuItem mi = (JMenuItem) c;
        g.setFont(mi.getFont());
        Rectangle viewRect = new Rectangle(0, 0, mi.getWidth(), mi.getHeight());
        applyInsets(viewRect, mi.getInsets());
        MenuItemLayoutHelper lh = new MenuItemLayoutHelper(mi, checkIcon,
                arrowIcon, viewRect, defaultTextIconGap, acceleratorDelimiter,
                BasicGraphicsUtils.isLeftToRight(mi), mi.getFont(),
                acceleratorFont, MenuItemLayoutHelper.useCheckAndArrow(menuItem),
                getPropertyPrefix());
        MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();
        paintBackground(g, mi, background);
        paintCheckIcon(g, lh, lr, holdc, foreground);
        paintIcon(g, lh, lr, holdc);
        paintText(g, lh, lr);
        paintAccText(g, lh, lr);
        paintArrowIcon(g, lh, lr, foreground);
        g.setColor(holdc);
        g.setFont(holdf);
    }
    private void paintIcon(Graphics g, MenuItemLayoutHelper lh,
                           MenuItemLayoutHelper.LayoutResult lr, Color holdc) {
        if (lh.getIcon() != null) {
            Icon icon;
            ButtonModel model = lh.getMenuItem().getModel();
            if (!model.isEnabled()) {
                icon = lh.getMenuItem().getDisabledIcon();
            } else if (model.isPressed() && model.isArmed()) {
                icon = lh.getMenuItem().getPressedIcon();
                if (icon == null) {
                    icon = lh.getMenuItem().getIcon();
                }
            } else {
                icon = lh.getMenuItem().getIcon();
            }
            if (icon != null) {
                icon.paintIcon(lh.getMenuItem(), g, lr.getIconRect().x,
                        lr.getIconRect().y);
                g.setColor(holdc);
            }
        }
    }
    private void paintCheckIcon(Graphics g, MenuItemLayoutHelper lh,
                                MenuItemLayoutHelper.LayoutResult lr,
                                Color holdc, Color foreground) {
        if (lh.getCheckIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || (lh.getMenuItem() instanceof JMenu
                    && model.isSelected())) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            if (lh.useCheckAndArrow()) {
                lh.getCheckIcon().paintIcon(lh.getMenuItem(), g,
                        lr.getCheckRect().x, lr.getCheckRect().y);
            }
            g.setColor(holdc);
        }
    }
    private void paintAccText(Graphics g, MenuItemLayoutHelper lh,
                              MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getAccText().equals("")) {
            ButtonModel model = lh.getMenuItem().getModel();
            g.setFont(lh.getAccFontMetrics().getFont());
            if (!model.isEnabled()) {
                if (disabledForeground != null) {
                    g.setColor(disabledForeground);
                    SwingUtilities2.drawString(lh.getMenuItem(), g,
                        lh.getAccText(), lr.getAccRect().x,
                        lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                } else {
                    g.setColor(lh.getMenuItem().getBackground().brighter());
                    SwingUtilities2.drawString(lh.getMenuItem(), g,
                        lh.getAccText(), lr.getAccRect().x,
                        lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                    g.setColor(lh.getMenuItem().getBackground().darker());
                    SwingUtilities2.drawString(lh.getMenuItem(), g,
                        lh.getAccText(), lr.getAccRect().x - 1,
                        lr.getAccRect().y + lh.getFontMetrics().getAscent() - 1);
                }
            } else {
                if (model.isArmed()
                        || (lh.getMenuItem() instanceof JMenu
                        && model.isSelected())) {
                    g.setColor(acceleratorSelectionForeground);
                } else {
                    g.setColor(acceleratorForeground);
                }
                SwingUtilities2.drawString(lh.getMenuItem(), g, lh.getAccText(),
                        lr.getAccRect().x, lr.getAccRect().y +
                        lh.getAccFontMetrics().getAscent());
            }
        }
    }
    private void paintText(Graphics g, MenuItemLayoutHelper lh,
                           MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getText().equals("")) {
            if (lh.getHtmlView() != null) {
                lh.getHtmlView().paint(g, lr.getTextRect());
            } else {
                paintText(g, lh.getMenuItem(), lr.getTextRect(), lh.getText());
            }
        }
    }
    private void paintArrowIcon(Graphics g, MenuItemLayoutHelper lh,
                                MenuItemLayoutHelper.LayoutResult lr,
                                Color foreground) {
        if (lh.getArrowIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || (lh.getMenuItem() instanceof JMenu
                                && model.isSelected())) {
                g.setColor(foreground);
            }
            if (lh.useCheckAndArrow()) {
                lh.getArrowIcon().paintIcon(lh.getMenuItem(), g,
                        lr.getArrowRect().x, lr.getArrowRect().y);
            }
        }
    }
    private void applyInsets(Rectangle rect, Insets insets) {
        if(insets != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= (insets.right + rect.x);
            rect.height -= (insets.bottom + rect.y);
        }
    }
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        int menuWidth = menuItem.getWidth();
        int menuHeight = menuItem.getHeight();
        if(menuItem.isOpaque()) {
            if (model.isArmed()|| (menuItem instanceof JMenu && model.isSelected())) {
                g.setColor(bgColor);
                g.fillRect(0,0, menuWidth, menuHeight);
            } else {
                g.setColor(menuItem.getBackground());
                g.fillRect(0,0, menuWidth, menuHeight);
            }
            g.setColor(oldColor);
        }
        else if (model.isArmed() || (menuItem instanceof JMenu &&
                                     model.isSelected())) {
            g.setColor(bgColor);
            g.fillRect(0,0, menuWidth, menuHeight);
            g.setColor(oldColor);
        }
    }
    protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
        ButtonModel model = menuItem.getModel();
        FontMetrics fm = SwingUtilities2.getFontMetrics(menuItem, g);
        int mnemIndex = menuItem.getDisplayedMnemonicIndex();
        if(!model.isEnabled()) {
            if ( UIManager.get("MenuItem.disabledForeground") instanceof Color ) {
                g.setColor( UIManager.getColor("MenuItem.disabledForeground") );
                SwingUtilities2.drawStringUnderlineCharAt(menuItem, g,text,
                          mnemIndex, textRect.x,  textRect.y + fm.getAscent());
            } else {
                g.setColor(menuItem.getBackground().brighter());
                SwingUtilities2.drawStringUnderlineCharAt(menuItem, g, text,
                           mnemIndex, textRect.x, textRect.y + fm.getAscent());
                g.setColor(menuItem.getBackground().darker());
                SwingUtilities2.drawStringUnderlineCharAt(menuItem, g,text,
                           mnemIndex,  textRect.x - 1, textRect.y +
                           fm.getAscent() - 1);
            }
        } else {
            if (model.isArmed()|| (menuItem instanceof JMenu && model.isSelected())) {
                g.setColor(selectionForeground); 
            }
            SwingUtilities2.drawStringUnderlineCharAt(menuItem, g,text,
                           mnemIndex, textRect.x, textRect.y + fm.getAscent());
        }
    }
    public MenuElement[] getPath() {
        MenuSelectionManager m = MenuSelectionManager.defaultManager();
        MenuElement oldPath[] = m.getSelectedPath();
        MenuElement newPath[];
        int i = oldPath.length;
        if (i == 0)
            return new MenuElement[0];
        Component parent = menuItem.getParent();
        if (oldPath[i-1].getComponent() == parent) {
            newPath = new MenuElement[i+1];
            System.arraycopy(oldPath, 0, newPath, 0, i);
            newPath[i] = menuItem;
        } else {
            int j;
            for (j = oldPath.length-1; j >= 0; j--) {
                if (oldPath[j].getComponent() == parent)
                    break;
            }
            newPath = new MenuElement[j+2];
            System.arraycopy(oldPath, 0, newPath, 0, j+1);
            newPath[j+1] = menuItem;
        }
        return newPath;
    }
    void printMenuElementArray(MenuElement path[], boolean dumpStack) {
        System.out.println("Path is(");
        int i, j;
        for(i=0,j=path.length; i<j ;i++){
            for (int k=0; k<=i; k++)
                System.out.print("  ");
            MenuElement me = path[i];
            if(me instanceof JMenuItem)
                System.out.println(((JMenuItem)me).getText() + ", ");
            else if (me == null)
                System.out.println("NULL , ");
            else
                System.out.println("" + me + ", ");
        }
        System.out.println(")");
        if (dumpStack == true)
            Thread.dumpStack();
    }
    protected class MouseInputHandler implements MouseInputListener {
        public void mouseClicked(MouseEvent e) {
            getHandler().mouseClicked(e);
        }
        public void mousePressed(MouseEvent e) {
            getHandler().mousePressed(e);
        }
        public void mouseReleased(MouseEvent e) {
            getHandler().mouseReleased(e);
        }
        public void mouseEntered(MouseEvent e) {
            getHandler().mouseEntered(e);
        }
        public void mouseExited(MouseEvent e) {
            getHandler().mouseExited(e);
        }
        public void mouseDragged(MouseEvent e) {
            getHandler().mouseDragged(e);
        }
        public void mouseMoved(MouseEvent e) {
            getHandler().mouseMoved(e);
        }
    }
    private static class Actions extends UIAction {
        private static final String CLICK = "doClick";
        Actions(String key) {
            super(key);
        }
        public void actionPerformed(ActionEvent e) {
            JMenuItem mi = (JMenuItem)e.getSource();
            MenuSelectionManager.defaultManager().clearSelectedPath();
            mi.doClick();
        }
    }
    protected void doClick(MenuSelectionManager msm) {
        if (! isInternalFrameSystemMenu()) {
            BasicLookAndFeel.playSound(menuItem, getPropertyPrefix() +
                                       ".commandSound");
        }
        if (msm == null) {
            msm = MenuSelectionManager.defaultManager();
        }
        msm.clearSelectedPath();
        menuItem.doClick(0);
    }
    private boolean isInternalFrameSystemMenu() {
        String actionCommand = menuItem.getActionCommand();
        if ((actionCommand == "Close") ||
            (actionCommand == "Minimize") ||
            (actionCommand == "Restore") ||
            (actionCommand == "Maximize")) {
          return true;
        } else {
          return false;
        }
    }
    class Handler implements MenuDragMouseListener,
                          MouseInputListener, PropertyChangeListener {
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            if (!menuItem.isEnabled()) {
                return;
            }
            MenuSelectionManager manager =
                MenuSelectionManager.defaultManager();
            Point p = e.getPoint();
            if(p.x >= 0 && p.x < menuItem.getWidth() &&
               p.y >= 0 && p.y < menuItem.getHeight()) {
                doClick(manager);
            } else {
                manager.processMouseEvent(e);
            }
        }
        public void mouseEntered(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
            int modifiers = e.getModifiers();
            if ((modifiers & (InputEvent.BUTTON1_MASK |
                              InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) !=0 ) {
                MenuSelectionManager.defaultManager().processMouseEvent(e);
            } else {
            manager.setSelectedPath(getPath());
             }
        }
        public void mouseExited(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
            int modifiers = e.getModifiers();
            if ((modifiers & (InputEvent.BUTTON1_MASK |
                              InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) !=0 ) {
                MenuSelectionManager.defaultManager().processMouseEvent(e);
            } else {
                MenuElement path[] = manager.getSelectedPath();
                if (path.length > 1 && path[path.length-1] == menuItem) {
                    MenuElement newPath[] = new MenuElement[path.length-1];
                    int i,c;
                    for(i=0,c=path.length-1;i<c;i++)
                        newPath[i] = path[i];
                    manager.setSelectedPath(newPath);
                }
                }
        }
        public void mouseDragged(MouseEvent e) {
            MenuSelectionManager.defaultManager().processMouseEvent(e);
        }
        public void mouseMoved(MouseEvent e) {
        }
        public void menuDragMouseEntered(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            manager.setSelectedPath(path);
        }
        public void menuDragMouseDragged(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            manager.setSelectedPath(path);
        }
        public void menuDragMouseExited(MenuDragMouseEvent e) {}
        public void menuDragMouseReleased(MenuDragMouseEvent e) {
            if (!menuItem.isEnabled()) {
                return;
            }
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            Point p = e.getPoint();
            if (p.x >= 0 && p.x < menuItem.getWidth() &&
                    p.y >= 0 && p.y < menuItem.getHeight()) {
                doClick(manager);
            } else {
                manager.clearSelectedPath();
            }
        }
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name == "labelFor" || name == "displayedMnemonic" ||
                name == "accelerator") {
                updateAcceleratorBinding();
            } else if (name == "text" || "font" == name ||
                       "foreground" == name) {
                JMenuItem lbl = ((JMenuItem) e.getSource());
                String text = lbl.getText();
                BasicHTML.updateRenderer(lbl, text);
            } else if (name  == "iconTextGap") {
                defaultTextIconGap = ((Number)e.getNewValue()).intValue();
            }
        }
    }
}
