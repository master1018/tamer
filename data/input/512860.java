public class CanvasViewInfo {
    private static final int SELECTION_MIN_SIZE = 6;
    private final Rectangle mAbsRect;
    private final Rectangle mSelectionRect;
    private final String mName;
    private final UiViewElementNode mUiViewKey;
    private final CanvasViewInfo mParent;
    private final ArrayList<CanvasViewInfo> mChildren = new ArrayList<CanvasViewInfo>();
    public CanvasViewInfo(ILayoutViewInfo viewInfo) {
        this(viewInfo, null , 0 , 0 );
    }
    private CanvasViewInfo(ILayoutViewInfo viewInfo, CanvasViewInfo parent, int parentX, int parentY) {
        mParent = parent;
        mName = viewInfo.getName();
        mUiViewKey  = (UiViewElementNode) viewInfo.getViewKey();
        int x = viewInfo.getLeft();
        int y = viewInfo.getTop();
        int w = viewInfo.getRight() - x;
        int h = viewInfo.getBottom() - y;
        if (parent != null) {
            x += parentX;
            y += parentY;
        }
        mAbsRect = new Rectangle(x, y, w - 1, h - 1);
        if (viewInfo.getChildren() != null) {
            for (ILayoutViewInfo child : viewInfo.getChildren()) {
                if (child.getViewKey() instanceof UiViewElementNode) {
                    mChildren.add(new CanvasViewInfo(child, this, x, y));
                }
            }
        }
        if (w < SELECTION_MIN_SIZE) {
            int d = (SELECTION_MIN_SIZE - w) / 2;
            x -= d;
            w += SELECTION_MIN_SIZE - w;
        }
        if (h < SELECTION_MIN_SIZE) {
            int d = (SELECTION_MIN_SIZE - h) / 2;
            y -= d;
            h += SELECTION_MIN_SIZE - h;
        }
        mSelectionRect = new Rectangle(x, y, w - 1, h - 1);
    }
    public Rectangle getAbsRect() {
        return mAbsRect;
    }
    public Rectangle getSelectionRect() {
        return mSelectionRect;
    }
    public UiViewElementNode getUiViewKey() {
        return mUiViewKey;
    }
    public CanvasViewInfo getParent() {
        return mParent;
    }
    public ArrayList<CanvasViewInfo> getChildren() {
        return mChildren;
    }
    public String getName() {
        return mName;
    }
}
