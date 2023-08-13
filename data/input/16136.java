public class SynthRadioButtonMenuItemUI extends SynthMenuItemUI {
    public static ComponentUI createUI(JComponent b) {
        return new SynthRadioButtonMenuItemUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "RadioButtonMenuItem";
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintRadioButtonMenuItemBackground(context, g, 0,
                             0, c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintRadioButtonMenuItemBorder(context, g, x,
                                                            y, w, h);
    }
}
