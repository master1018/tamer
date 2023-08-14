public class HierarchyEvent extends AWTEvent {
    private static final long serialVersionUID = -5337576970038043990L;
    public static final int HIERARCHY_FIRST = 1400;
    public static final int HIERARCHY_CHANGED = 1400;
    public static final int ANCESTOR_MOVED = 1401;
    public static final int ANCESTOR_RESIZED = 1402;
    public static final int HIERARCHY_LAST = 1402;
    public static final int PARENT_CHANGED = 1;
    public static final int DISPLAYABILITY_CHANGED = 2;
    public static final int SHOWING_CHANGED = 4;
    private Component changed;
    private long changeFlag;
    public HierarchyEvent(Component source, int id, Component changed,
            Object changedParent, long changeFlags) {
        super(source, id);
    }
    public Component getComponent() {
        return (Component) source;
    }
    public long getChangeFlags() {
        return changeFlag;
    }
    public Component getChanged() {
        return changed;
    }
    @Override
    public String paramString() {
        String paramString = null;
        switch (id) {
        case HIERARCHY_CHANGED:
            paramString = "HIERARCHY_CHANGED"; 
            break;
        case ANCESTOR_MOVED:
            paramString = "ANCESTOR_MOVED"; 
            break;
        case ANCESTOR_RESIZED:
            paramString = "ANCESTOR_RESIZED"; 
            break;
        default:
            paramString = "unknown type"; 
        }
        paramString += " ("; 
        if (id == HIERARCHY_CHANGED) {
            if ((changeFlag & PARENT_CHANGED) > 0) {
                paramString += "PARENT_CHANGED,"; 
            }
            if ((changeFlag & DISPLAYABILITY_CHANGED) > 0) {
                paramString += "DISPLAYABILITY_CHANGED,"; 
            }
            if ((changeFlag & SHOWING_CHANGED) > 0) {
                paramString += "SHOWING_CHANGED,"; 
            }
        }
        return paramString;
    }
}
