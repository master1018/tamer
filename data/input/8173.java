public abstract class SplitPaneUI extends ComponentUI
{
    public abstract void resetToPreferredSizes(JSplitPane jc);
    public abstract void setDividerLocation(JSplitPane jc, int location);
    public abstract int getDividerLocation(JSplitPane jc);
    public abstract int getMinimumDividerLocation(JSplitPane jc);
    public abstract int getMaximumDividerLocation(JSplitPane jc);
    public abstract void finishedPaintingChildren(JSplitPane jc, Graphics g);
}
