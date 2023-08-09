public class SynthInternalFrameUI extends BasicInternalFrameUI
                                  implements SynthUI, PropertyChangeListener {
    private SynthStyle style;
    public static ComponentUI createUI(JComponent b) {
        return new SynthInternalFrameUI((JInternalFrame)b);
    }
    protected SynthInternalFrameUI(JInternalFrame b) {
        super(b);
    }
    @Override
    public void installDefaults() {
        frame.setLayout(internalFrameLayout = createLayoutManager());
        updateStyle(frame);
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        frame.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallComponents() {
        if (frame.getComponentPopupMenu() instanceof UIResource) {
            frame.setComponentPopupMenu(null);
        }
        super.uninstallComponents();
    }
    @Override
    protected void uninstallListeners() {
        frame.removePropertyChangeListener(this);
        super.uninstallListeners();
    }
    private void updateStyle(JComponent c) {
        SynthContext context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            Icon frameIcon = frame.getFrameIcon();
            if (frameIcon == null || frameIcon instanceof UIResource) {
                frame.setFrameIcon(context.getStyle().getIcon(
                                   context, "InternalFrame.icon"));
            }
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(frame, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        if(frame.getLayout() == internalFrameLayout) {
            frame.setLayout(null);
        }
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                            SynthLookAndFeel.getRegion(c), style, state);
    }
    private int getComponentState(JComponent c) {
        return SynthLookAndFeel.getComponentState(c);
    }
    @Override
    protected JComponent createNorthPane(JInternalFrame w) {
        titlePane = new SynthInternalFrameTitlePane(w);
        titlePane.setName("InternalFrame.northPane");
        return titlePane;
    }
    @Override
    protected ComponentListener createComponentListener() {
        if (UIManager.getBoolean("InternalFrame.useTaskBar")) {
            return new ComponentHandler() {
                @Override public void componentResized(ComponentEvent e) {
                    if (frame != null && frame.isMaximum()) {
                        JDesktopPane desktop = (JDesktopPane)e.getSource();
                        for (Component comp : desktop.getComponents()) {
                            if (comp instanceof SynthDesktopPaneUI.TaskBar) {
                                frame.setBounds(0, 0,
                                                desktop.getWidth(),
                                                desktop.getHeight() - comp.getHeight());
                                frame.revalidate();
                                break;
                            }
                        }
                    }
                    JInternalFrame f = frame;
                    frame = null;
                    super.componentResized(e);
                    frame = f;
                }
            };
        } else {
            return super.createComponentListener();
        }
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintInternalFrameBackground(context,
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
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintInternalFrameBorder(context,
                                                            g, x, y, w, h);
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SynthStyle oldStyle = style;
        JInternalFrame f = (JInternalFrame)evt.getSource();
        String prop = evt.getPropertyName();
        if (SynthLookAndFeel.shouldUpdateStyle(evt)) {
            updateStyle(f);
        }
        if (style == oldStyle &&
            (prop == JInternalFrame.IS_MAXIMUM_PROPERTY ||
             prop == JInternalFrame.IS_SELECTED_PROPERTY)) {
            SynthContext context = getContext(f, ENABLED);
            style.uninstallDefaults(context);
            style.installDefaults(context, this);
        }
    }
}
