public class SynthCheckBoxMenuItemUI extends SynthMenuItemUI {
    public static ComponentUI createUI(JComponent c) {
        return new SynthCheckBoxMenuItemUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "CheckBoxMenuItem";
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintCheckBoxMenuItemBackground(context, g, 0, 0,
                                                  c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintCheckBoxMenuItemBorder(context, g, x, y, w, h);
    }
}
