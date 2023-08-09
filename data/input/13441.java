abstract class InputMethodPopupMenu implements ActionListener {
    static InputMethodPopupMenu getInstance(Component client, String title) {
        if ((client instanceof JFrame) ||
            (client instanceof JDialog)) {
                return new JInputMethodPopupMenu(title);
        } else {
            return new AWTInputMethodPopupMenu(title);
        }
    }
    abstract void show(Component c, int x, int y);
    abstract void removeAll();
    abstract void addSeparator();
    abstract void addToComponent(Component c);
    abstract Object createSubmenu(String label);
    abstract void add(Object menuItem);
    abstract void addMenuItem(String label, String command, String currentSelection);
    abstract void addMenuItem(Object targetMenu, String label, String command,
                              String currentSelection);
    void addOneInputMethodToMenu(InputMethodLocator locator, String currentSelection) {
        InputMethodDescriptor descriptor = locator.getDescriptor();
        String label = descriptor.getInputMethodDisplayName(null, Locale.getDefault());
        String command = locator.getActionCommandString();
        Locale[] locales = null;
        int localeCount;
        try {
            locales = descriptor.getAvailableLocales();
            localeCount = locales.length;
        } catch (AWTException e) {
            localeCount = 0;
        }
        if (localeCount == 0) {
            addMenuItem(label, null, currentSelection);
        } else if (localeCount == 1) {
            if (descriptor.hasDynamicLocaleList()) {
                label = descriptor.getInputMethodDisplayName(locales[0], Locale.getDefault());
                command = locator.deriveLocator(locales[0]).getActionCommandString();
            }
            addMenuItem(label, command, currentSelection);
        } else {
            Object submenu = createSubmenu(label);
            add(submenu);
            for (int j = 0; j < localeCount; j++) {
                Locale locale = locales[j];
                String subLabel = getLocaleName(locale);
                String subCommand = locator.deriveLocator(locale).getActionCommandString();
                addMenuItem(submenu, subLabel, subCommand, currentSelection);
            }
        }
    }
    static boolean isSelected(String command, String currentSelection) {
        if (command == null || currentSelection == null) {
            return false;
        }
        if (command.equals(currentSelection)) {
            return true;
        }
        int index = currentSelection.indexOf('\n');
        if (index != -1 && currentSelection.substring(0, index).equals(command)) {
            return true;
        }
        return false;
    }
    String getLocaleName(Locale locale) {
        String localeString = locale.toString();
        String localeName = Toolkit.getProperty("AWT.InputMethodLanguage." + localeString, null);
        if (localeName == null) {
            localeName = locale.getDisplayName();
            if (localeName == null || localeName.length() == 0)
                localeName = localeString;
        }
        return localeName;
    }
    public void actionPerformed(ActionEvent event) {
        String choice = event.getActionCommand();
        ((ExecutableInputMethodManager)InputMethodManager.getInstance()).changeInputMethod(choice);
    }
}
class JInputMethodPopupMenu extends InputMethodPopupMenu {
    static JPopupMenu delegate = null;
    JInputMethodPopupMenu(String title) {
        synchronized (this) {
            if (delegate == null) {
                delegate = new JPopupMenu(title);
            }
        }
    }
    void show(Component c, int x, int y) {
        delegate.show(c, x, y);
    }
    void removeAll() {
        delegate.removeAll();
    }
    void addSeparator() {
        delegate.addSeparator();
    }
    void addToComponent(Component c) {
    }
    Object createSubmenu(String label) {
        return new JMenu(label);
    }
    void add(Object menuItem) {
        delegate.add((JMenuItem)menuItem);
    }
    void addMenuItem(String label, String command, String currentSelection) {
        addMenuItem(delegate, label, command, currentSelection);
    }
    void addMenuItem(Object targetMenu, String label, String command, String currentSelection) {
        JMenuItem menuItem;
        if (isSelected(command, currentSelection)) {
            menuItem = new JCheckBoxMenuItem(label, true);
        } else {
            menuItem = new JMenuItem(label);
        }
        menuItem.setActionCommand(command);
        menuItem.addActionListener(this);
        menuItem.setEnabled(command != null);
        if (targetMenu instanceof JMenu) {
            ((JMenu)targetMenu).add(menuItem);
        } else {
            ((JPopupMenu)targetMenu).add(menuItem);
        }
    }
}
class AWTInputMethodPopupMenu extends InputMethodPopupMenu {
    static PopupMenu delegate = null;
    AWTInputMethodPopupMenu(String title) {
        synchronized (this) {
            if (delegate == null) {
                delegate = new PopupMenu(title);
            }
        }
    }
    void show(Component c, int x, int y) {
        delegate.show(c, x, y);
    }
    void removeAll() {
        delegate.removeAll();
    }
    void addSeparator() {
        delegate.addSeparator();
    }
    void addToComponent(Component c) {
        c.add(delegate);
    }
    Object createSubmenu(String label) {
        return new Menu(label);
    }
    void add(Object menuItem) {
        delegate.add((MenuItem)menuItem);
    }
    void addMenuItem(String label, String command, String currentSelection) {
        addMenuItem(delegate, label, command, currentSelection);
    }
    void addMenuItem(Object targetMenu, String label, String command, String currentSelection) {
        MenuItem menuItem;
        if (isSelected(command, currentSelection)) {
            menuItem = new CheckboxMenuItem(label, true);
        } else {
            menuItem = new MenuItem(label);
        }
        menuItem.setActionCommand(command);
        menuItem.addActionListener(this);
        menuItem.setEnabled(command != null);
        ((Menu)targetMenu).add(menuItem);
    }
}
