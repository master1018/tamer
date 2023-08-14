public class SynthRadioButtonUI extends SynthToggleButtonUI {
    public static ComponentUI createUI(JComponent b) {
        return new SynthRadioButtonUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "RadioButton.";
    }
    @Override
    protected Icon getSizingIcon(AbstractButton b) {
        return getIcon(b);
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintRadioButtonBackground(context, g, 0, 0,
                                                c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintRadioButtonBorder(context, g, x, y, w, h);
    }
}
