public class SynthFormattedTextFieldUI extends SynthTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new SynthFormattedTextFieldUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "FormattedTextField";
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintFormattedTextFieldBackground(context, g, 0,
                             0, c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintFormattedTextFieldBorder(context, g, x, y,
                                                           w, h);
    }
}
