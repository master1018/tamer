public class WindowsSplitPaneDivider extends BasicSplitPaneDivider
{
    public WindowsSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
    }
    public void paint(Graphics g) {
        Color bgColor = (splitPane.hasFocus()) ?
                            UIManager.getColor("SplitPane.shadow") :
                            getBackground();
        Dimension size = getSize();
        if(bgColor != null) {
            g.setColor(bgColor);
            g.fillRect(0, 0, size.width, size.height);
        }
        super.paint(g);
    }
}
