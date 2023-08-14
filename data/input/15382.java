public class BasicMenuBarUI extends MenuBarUI  {
    protected JMenuBar              menuBar = null;
    protected ContainerListener     containerListener;
    protected ChangeListener        changeListener;
    private Handler handler;
    public static ComponentUI createUI(JComponent x) {
        return new BasicMenuBarUI();
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.TAKE_FOCUS));
    }
    public void installUI(JComponent c) {
        menuBar = (JMenuBar) c;
        installDefaults();
        installListeners();
        installKeyboardActions();
    }
    protected void installDefaults() {
        if (menuBar.getLayout() == null ||
            menuBar.getLayout() instanceof UIResource) {
            menuBar.setLayout(new DefaultMenuLayout(menuBar,BoxLayout.LINE_AXIS));
        }
        LookAndFeel.installProperty(menuBar, "opaque", Boolean.TRUE);
        LookAndFeel.installBorder(menuBar,"MenuBar.border");
        LookAndFeel.installColorsAndFont(menuBar,
                                              "MenuBar.background",
                                              "MenuBar.foreground",
                                              "MenuBar.font");
    }
    protected void installListeners() {
        containerListener = createContainerListener();
        changeListener = createChangeListener();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu!=null)
                menu.getModel().addChangeListener(changeListener);
        }
        menuBar.addContainerListener(containerListener);
    }
    protected void installKeyboardActions() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingUtilities.replaceUIInputMap(menuBar,
                           JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
        LazyActionMap.installLazyActionMap(menuBar, BasicMenuBarUI.class,
                                           "MenuBar.actionMap");
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
            Object[] bindings = (Object[])DefaultLookup.get
                                (menuBar, this, "MenuBar.windowBindings");
            if (bindings != null) {
                return LookAndFeel.makeComponentInputMap(menuBar, bindings);
            }
        }
        return null;
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults();
        uninstallListeners();
        uninstallKeyboardActions();
        menuBar = null;
    }
    protected void uninstallDefaults() {
        if (menuBar!=null) {
            LookAndFeel.uninstallBorder(menuBar);
        }
    }
    protected void uninstallListeners() {
        menuBar.removeContainerListener(containerListener);
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu !=null)
                menu.getModel().removeChangeListener(changeListener);
        }
        containerListener = null;
        changeListener = null;
        handler = null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(menuBar, JComponent.
                                       WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIActionMap(menuBar, null);
    }
    protected ContainerListener createContainerListener() {
        return getHandler();
    }
    protected ChangeListener createChangeListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    public Dimension getMinimumSize(JComponent c) {
        return null;
    }
    public Dimension getMaximumSize(JComponent c) {
        return null;
    }
    private class Handler implements ChangeListener, ContainerListener {
        public void stateChanged(ChangeEvent e) {
            int i,c;
            for(i=0,c = menuBar.getMenuCount() ; i < c ; i++) {
                JMenu menu = menuBar.getMenu(i);
                if(menu !=null && menu.isSelected()) {
                    menuBar.getSelectionModel().setSelectedIndex(i);
                    break;
                }
            }
        }
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            if (c instanceof JMenu)
                ((JMenu)c).getModel().addChangeListener(changeListener);
        }
        public void componentRemoved(ContainerEvent e) {
            Component c = e.getChild();
            if (c instanceof JMenu)
                ((JMenu)c).getModel().removeChangeListener(changeListener);
        }
    }
    private static class Actions extends UIAction {
        private static final String TAKE_FOCUS = "takeFocus";
        Actions(String key) {
            super(key);
        }
        public void actionPerformed(ActionEvent e) {
            JMenuBar menuBar = (JMenuBar)e.getSource();
            MenuSelectionManager defaultManager = MenuSelectionManager.defaultManager();
            MenuElement me[];
            MenuElement subElements[];
            JMenu menu = menuBar.getMenu(0);
            if (menu!=null) {
                    me = new MenuElement[3];
                    me[0] = (MenuElement) menuBar;
                    me[1] = (MenuElement) menu;
                    me[2] = (MenuElement) menu.getPopupMenu();
                    defaultManager.setSelectedPath(me);
            }
        }
    }
}
