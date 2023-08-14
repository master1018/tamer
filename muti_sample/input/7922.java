public class SynthTextFieldUI extends BasicTextFieldUI implements SynthUI {
    private Handler handler = new Handler();
    private SynthStyle style;
    public static ComponentUI createUI(JComponent c) {
        return new SynthTextFieldUI();
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
    static void updateStyle(JTextComponent comp, SynthContext context,
            String prefix) {
        SynthStyle style = context.getStyle();
        Color color = comp.getCaretColor();
        if (color == null || color instanceof UIResource) {
            comp.setCaretColor(
                (Color)style.get(context, prefix + ".caretForeground"));
        }
        Color fg = comp.getForeground();
        if (fg == null || fg instanceof UIResource) {
            fg = style.getColorForState(context, ColorType.TEXT_FOREGROUND);
            if (fg != null) {
                comp.setForeground(fg);
            }
        }
        Object ar = style.get(context, prefix + ".caretAspectRatio");
        if (ar instanceof Number) {
            comp.putClientProperty("caretAspectRatio", ar);
        }
        context.setComponentState(SELECTED | FOCUSED);
        Color s = comp.getSelectionColor();
        if (s == null || s instanceof UIResource) {
            comp.setSelectionColor(
                style.getColor(context, ColorType.TEXT_BACKGROUND));
        }
        Color sfg = comp.getSelectedTextColor();
        if (sfg == null || sfg instanceof UIResource) {
            comp.setSelectedTextColor(
                style.getColor(context, ColorType.TEXT_FOREGROUND));
        }
        context.setComponentState(DISABLED);
        Color dfg = comp.getDisabledTextColor();
        if (dfg == null || dfg instanceof UIResource) {
            comp.setDisabledTextColor(
                style.getColor(context, ColorType.TEXT_FOREGROUND));
        }
        Insets margin = comp.getMargin();
        if (margin == null || margin instanceof UIResource) {
            margin = (Insets)style.get(context, prefix + ".margin");
            if (margin == null) {
                margin = SynthLookAndFeel.EMPTY_UIRESOURCE_INSETS;
            }
            comp.setMargin(margin);
        }
        Caret caret = comp.getCaret();
        if (caret instanceof UIResource) {
            Object o = style.get(context, prefix + ".caretBlinkRate");
            if (o != null && o instanceof Integer) {
                Integer rate = (Integer)o;
                caret.setBlinkRate(rate.intValue());
            }
        }
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
        paintBackground(context, g, c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        super.paint(g, getComponent());
    }
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintTextFieldBackground(context, g, 0, 0,
                                                c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintTextFieldBorder(context, g, x, y, w, h);
    }
    @Override
    protected void paintBackground(Graphics g) {
    }
    @Override
    protected void propertyChange(PropertyChangeEvent evt) {
        if (SynthLookAndFeel.shouldUpdateStyle(evt)) {
            updateStyle((JTextComponent)evt.getSource());
        }
        super.propertyChange(evt);
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
    private final class Handler implements FocusListener {
        public void focusGained(FocusEvent e) {
            getComponent().repaint();
        }
        public void focusLost(FocusEvent e) {
            getComponent().repaint();
        }
    }
}
