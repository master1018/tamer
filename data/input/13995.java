public abstract class CommonMenuBar extends JMenuBar
{
    protected CommonMenuBar(ActionManager manager)
    {
        this(manager, StatusBar.getInstance());
    }
    protected CommonMenuBar(ActionManager manager, StatusBar status)
    {
        this.manager = manager;
        statusBar = status;
        configureMenu();
    }
    protected abstract void configureMenu();
    protected void configureToggleMenuItem(JMenuItem menuItem, Action action)
    {
        configureMenuItem(menuItem, action);
        action.addPropertyChangeListener(new ToggleActionPropertyChangeListener(menuItem));
    }
    protected void configureMenuItem(JMenuItem menuItem, Action action)
    {
        menuItem.addMouseListener(statusBar);
    }
    protected JMenu createMenu(String name, char mnemonic)
    {
        JMenu menu = new JMenu(name);
        menu.setMnemonic(mnemonic);
        return menu;
    }
    protected void addMenuItem(JMenu menu, Action action)
    {
        JMenuItem menuItem = menu.add(action);
        configureMenuItem(menuItem, action);
    }
    protected void addCheckBoxMenuItem(JMenu menu, StateChangeAction a)
    {
        addCheckBoxMenuItem(menu, a, false);
    }
    protected void addCheckBoxMenuItem(JMenu menu, StateChangeAction a, boolean selected)
    {
        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(a);
        mi.addItemListener(a);
        mi.setSelected(selected);
        menu.add(mi);
        configureToggleMenuItem(mi, a);
    }
    protected void addRadioButtonMenuItem(JMenu menu, ButtonGroup group, StateChangeAction a)
    {
        addRadioButtonMenuItem(menu, group, a, false);
    }
    protected void addRadioButtonMenuItem(JMenu menu, ButtonGroup group, StateChangeAction a, boolean selected)
    {
        JRadioButtonMenuItem mi = new JRadioButtonMenuItem(a);
        mi.addItemListener(a);
        mi.setSelected(selected);
        menu.add(mi);
        if(group != null)
            group.add(mi);
        configureToggleMenuItem(mi, a);
    }
    protected ActionManager manager;
    private StatusBar statusBar;
}
