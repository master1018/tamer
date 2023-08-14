public class SynthCheckBoxUI extends SynthRadioButtonUI {
    public static ComponentUI createUI(JComponent b) {
        return new SynthCheckBoxUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "CheckBox.";
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintCheckBoxBackground(context, g, 0, 0,
                                                  c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintCheckBoxBorder(context, g, x, y, w, h);
    }
}
