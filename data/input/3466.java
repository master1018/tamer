public class SynthTextAreaUI extends BasicTextAreaUI implements SynthUI {
    private Handler handler = new Handler();
    private SynthStyle style;
    public static ComponentUI createUI(JComponent ta) {
        return new SynthTextAreaUI();
    }
    @Override
    protected void installDefaults() {
        super.installDefaults();
        updateStyle(getComponent());
        getComponent().addFocusListener(handler);
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(getComponent(), ENABLED);
        getComponent().putClientProperty("caretAspectRatio", null);
        getComponent().removeFocusListener(handler);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        super.uninstallDefaults();
    }
    private void updateStyle(JTextComponent comp) {
        SynthContext context = getContext(comp, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            SynthTextFieldUI.updateStyle(comp, context, getPropertyPrefix());
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintTextAreaBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        super.paint(g, getComponent());
    }
    @Override
    protected void paintBackground(Graphics g) {
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintTextAreaBorder(context, g, x, y, w, h);
    }
    @Override
    protected void propertyChange(PropertyChangeEvent evt) {
        if (SynthLookAndFeel.shouldUpdateStyle(evt)) {
            updateStyle((JTextComponent)evt.getSource());
        }
        super.propertyChange(evt);
    }
    private final class Handler implements FocusListener {
        public void focusGained(FocusEvent e) {
            getComponent().repaint();
        }
        public void focusLost(FocusEvent e) {
            getComponent().repaint();
        }
    }
}
