public class SynthMenuUI extends BasicMenuUI
                         implements PropertyChangeListener, SynthUI {
    private SynthStyle style;
    private SynthStyle accStyle;
    public static ComponentUI createUI(JComponent x) {
        return new SynthMenuUI();
    }
    @Override
    protected void installDefaults() {
        updateStyle(menuItem);
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        menuItem.addPropertyChangeListener(this);
    }
    private void updateStyle(JMenuItem mi) {
        SynthStyle oldStyle = style;
        SynthContext context = getContext(mi, ENABLED);
        style = SynthLookAndFeel.updateStyle(context, this);
        if (oldStyle != style) {
            String prefix = getPropertyPrefix();
            defaultTextIconGap = style.getInt(
                           context, prefix + ".textIconGap", 4);
            if (menuItem.getMargin() == null ||
                         (menuItem.getMargin() instanceof UIResource)) {
                Insets insets = (Insets)style.get(context, prefix + ".margin");
                if (insets == null) {
                    insets = SynthLookAndFeel.EMPTY_UIRESOURCE_INSETS;
                }
                menuItem.setMargin(insets);
            }
            acceleratorDelimiter = style.getString(context, prefix +
                                            ".acceleratorDelimiter", "+");
            if (MenuItemLayoutHelper.useCheckAndArrow(menuItem)) {
                checkIcon = style.getIcon(context, prefix + ".checkIcon");
                arrowIcon = style.getIcon(context, prefix + ".arrowIcon");
            } else {
                checkIcon = null;
                arrowIcon = null;
            }
            ((JMenu)menuItem).setDelay(style.getInt(context, prefix +
                                                    ".delay", 200));
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
        SynthContext accContext = getContext(mi, Region.MENU_ITEM_ACCELERATOR,
                                             ENABLED);
        accStyle = SynthLookAndFeel.updateStyle(accContext, this);
        accContext.dispose();
    }
    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        JComponent p = MenuItemLayoutHelper.getMenuItemParent((JMenuItem) c);
        if (p != null) {
            p.putClientProperty(
                    SynthMenuItemLayoutHelper.MAX_ACC_OR_ARROW_WIDTH, null);
        }
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(menuItem, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        SynthContext accContext = getContext(menuItem,
                                     Region.MENU_ITEM_ACCELERATOR, ENABLED);
        accStyle.uninstallDefaults(accContext);
        accContext.dispose();
        accStyle = null;
        super.uninstallDefaults();
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        menuItem.removePropertyChangeListener(this);
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, getComponentState(c));
    }
    SynthContext getContext(JComponent c, int state) {
        Region region = SynthLookAndFeel.getRegion(c);
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }
    SynthContext getContext(JComponent c, Region region) {
        return getContext(c, region, getComponentState(c, region));
    }
    private SynthContext getContext(JComponent c, Region region, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                                       region, accStyle, state);
    }
    private int getComponentState(JComponent c) {
        int state;
        if (!c.isEnabled()) {
            return DISABLED;
        }
        if (menuItem.isArmed()) {
            state = MOUSE_OVER;
        }
        else {
            state = SynthLookAndFeel.getComponentState(c);
        }
        if (menuItem.isSelected()) {
            state |= SELECTED;
        }
        return state;
    }
    private int getComponentState(JComponent c, Region region) {
        return getComponentState(c);
    }
    @Override
    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                     Icon checkIcon,
                                                     Icon arrowIcon,
                                                     int defaultTextIconGap) {
        SynthContext context = getContext(c);
        SynthContext accContext = getContext(c, Region.MENU_ITEM_ACCELERATOR);
        Dimension value = SynthGraphicsUtils.getPreferredMenuItemSize(
                context, accContext, c, checkIcon, arrowIcon,
                defaultTextIconGap, acceleratorDelimiter,
                MenuItemLayoutHelper.useCheckAndArrow(menuItem),
                getPropertyPrefix());
        context.dispose();
        accContext.dispose();
        return value;
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintMenuBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        SynthContext accContext = getContext(menuItem,
                                             Region.MENU_ITEM_ACCELERATOR);
        String prefix = getPropertyPrefix();
        Icon checkIcon = style.getIcon(context, prefix + ".checkIcon");
        Icon arrowIcon = style.getIcon(context, prefix + ".arrowIcon");
        SynthGraphicsUtils.paint(context, accContext, g, checkIcon, arrowIcon,
              acceleratorDelimiter, defaultTextIconGap, getPropertyPrefix());
        accContext.dispose();
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintMenuBorder(context, g, x, y, w, h);
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JMenu)e.getSource());
        }
    }
}
