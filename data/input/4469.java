public class WindowsSplitPaneUI extends BasicSplitPaneUI
{
    public WindowsSplitPaneUI() {
        super();
    }
    public static ComponentUI createUI(JComponent x) {
        return new WindowsSplitPaneUI();
    }
    public BasicSplitPaneDivider createDefaultDivider() {
        return new WindowsSplitPaneDivider(this);
    }
}
