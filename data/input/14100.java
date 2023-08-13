public class MetalSplitPaneUI extends BasicSplitPaneUI
{
    public static ComponentUI createUI(JComponent x) {
        return new MetalSplitPaneUI();
    }
    public BasicSplitPaneDivider createDefaultDivider() {
        return new MetalSplitPaneDivider(this);
    }
}
