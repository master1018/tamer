public class SynthSplitPaneUI extends BasicSplitPaneUI
                              implements PropertyChangeListener, SynthUI {
    private static Set<KeyStroke> managingFocusForwardTraversalKeys;
    private static Set<KeyStroke> managingFocusBackwardTraversalKeys;
    private SynthStyle style;
    private SynthStyle dividerStyle;
    public static ComponentUI createUI(JComponent x) {
        return new SynthSplitPaneUI();
    }
    @Override
    protected void installDefaults() {
        updateStyle(splitPane);
        setOrientation(splitPane.getOrientation());
        setContinuousLayout(splitPane.isContinuousLayout());
        resetLayoutManager();
        if(nonContinuousLayoutDivider == null) {
            setNonContinuousLayoutDivider(
                                createDefaultNonContinuousLayoutDivider(),
                                true);
        } else {
            setNonContinuousLayoutDivider(nonContinuousLayoutDivider, true);
        }
        if (managingFocusForwardTraversalKeys==null) {
            managingFocusForwardTraversalKeys = new HashSet<KeyStroke>();
            managingFocusForwardTraversalKeys.add(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        }
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                        managingFocusForwardTraversalKeys);
        if (managingFocusBackwardTraversalKeys==null) {
            managingFocusBackwardTraversalKeys = new HashSet<KeyStroke>();
            managingFocusBackwardTraversalKeys.add(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        }
        splitPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                        managingFocusBackwardTraversalKeys);
    }
    private void updateStyle(JSplitPane splitPane) {
        SynthContext context = getContext(splitPane, Region.SPLIT_PANE_DIVIDER,
                                          ENABLED);
        SynthStyle oldDividerStyle = dividerStyle;
        dividerStyle = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
        context = getContext(splitPane, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            Object value = style.get(context, "SplitPane.size");
            if (value == null) {
                value = Integer.valueOf(6);
            }
            LookAndFeel.installProperty(splitPane, "dividerSize", value);
            value = style.get(context, "SplitPane.oneTouchExpandable");
            if (value != null) {
                LookAndFeel.installProperty(splitPane, "oneTouchExpandable", value);
            }
            if (divider != null) {
                splitPane.remove(divider);
                divider.setDividerSize(splitPane.getDividerSize());
            }
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        if (style != oldStyle || dividerStyle != oldDividerStyle) {
            if (divider != null) {
                splitPane.remove(divider);
            }
            divider = createDefaultDivider();
            divider.setBasicSplitPaneUI(this);
            splitPane.add(divider, JSplitPane.DIVIDER);
        }
        context.dispose();
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        splitPane.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(splitPane, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        context = getContext(splitPane, Region.SPLIT_PANE_DIVIDER, ENABLED);
        dividerStyle.uninstallDefaults(context);
        context.dispose();
        dividerStyle = null;
        super.uninstallDefaults();
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        splitPane.removePropertyChangeListener(this);
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }
    SynthContext getContext(JComponent c, Region region) {
        return getContext(c, region, getComponentState(c, region));
    }
    private SynthContext getContext(JComponent c, Region region, int state) {
        if (region == Region.SPLIT_PANE_DIVIDER) {
            return SynthContext.getContext(SynthContext.class, c, region,
                                           dividerStyle, state);
        }
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }
    private int getComponentState(JComponent c, Region subregion) {
        int state = SynthLookAndFeel.getComponentState(c);
        if (divider.isMouseOver()) {
            state |= MOUSE_OVER;
        }
        return state;
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JSplitPane)e.getSource());
        }
    }
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        SynthSplitPaneDivider divider = new SynthSplitPaneDivider(this);
        divider.setDividerSize(splitPane.getDividerSize());
        return divider;
    }
    @Override
    protected Component createDefaultNonContinuousLayoutDivider() {
        return new Canvas() {
            public void paint(Graphics g) {
                paintDragDivider(g, 0, 0, getWidth(), getHeight());
            }
        };
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintSplitPaneBackground(context,
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
        super.paint(g, splitPane);
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintSplitPaneBorder(context, g, x, y, w, h);
    }
    private void paintDragDivider(Graphics g, int x, int y, int w, int h) {
        SynthContext context = getContext(splitPane,Region.SPLIT_PANE_DIVIDER);
        context.setComponentState(((context.getComponentState() | MOUSE_OVER) ^
                                   MOUSE_OVER) | PRESSED);
        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        context.getPainter().paintSplitPaneDragDivider(context, g, x, y, w, h,
                                           splitPane.getOrientation());
        g.setClip(oldClip);
        context.dispose();
    }
    @Override
    public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
        if(jc == splitPane && getLastDragLocation() != -1 &&
                              !isContinuousLayout() && !draggingHW) {
            if(jc.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                paintDragDivider(g, getLastDragLocation(), 0, dividerSize - 1,
                                 splitPane.getHeight() - 1);
            } else {
                paintDragDivider(g, 0, getLastDragLocation(),
                                 splitPane.getWidth() - 1, dividerSize - 1);
            }
        }
    }
}
