public class MotifSplitPaneUI extends BasicSplitPaneUI
{
    public MotifSplitPaneUI() {
        super();
    }
    public static ComponentUI createUI(JComponent x) {
        return new MotifSplitPaneUI();
    }
    public BasicSplitPaneDivider createDefaultDivider() {
        return new MotifSplitPaneDivider(this);
    }
}
