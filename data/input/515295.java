public class ItemEvent extends AWTEvent {
    private static final long serialVersionUID = -608708132447206933L;
    public static final int ITEM_FIRST = 701;
    public static final int ITEM_LAST = 701;
    public static final int ITEM_STATE_CHANGED = 701;
    public static final int SELECTED = 1;
    public static final int DESELECTED = 2;
    private Object item;
    private int stateChange;
    public ItemEvent(ItemSelectable source, int id, Object item, int stateChange) {
        super(source, id);
        this.item = item;
        this.stateChange = stateChange;
    }
    public Object getItem() {
        return item;
    }
    public int getStateChange() {
        return stateChange;
    }
    public ItemSelectable getItemSelectable() {
        return (ItemSelectable) source;
    }
    @Override
    public String paramString() {
        String stateString = null;
        switch (stateChange) {
        case SELECTED:
            stateString = "SELECTED"; 
            break;
        case DESELECTED:
            stateString = "DESELECTED"; 
            break;
        default:
            stateString = "unknown type"; 
        }
        return ((id == ITEM_STATE_CHANGED ? "ITEM_STATE_CHANGED" : "unknown type") + 
                ",item=" + item + ",stateChange=" + stateString); 
    }
}
