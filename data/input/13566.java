public abstract class TabbedPaneUI extends ComponentUI {
    public abstract int tabForCoordinate(JTabbedPane pane, int x, int y);
    public abstract Rectangle getTabBounds(JTabbedPane pane, int index);
    public abstract int getTabRunCount(JTabbedPane pane);
}
