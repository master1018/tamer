public class FocusEvent extends ComponentEvent {
    private static final long serialVersionUID = 523753786457416396L;
    public static final int FOCUS_FIRST = 1004;
    public static final int FOCUS_LAST = 1005;
    public static final int FOCUS_GAINED = 1004;
    public static final int FOCUS_LOST = 1005;
    private boolean temporary;
    private Component opposite;
    public FocusEvent(Component source, int id) {
        this(source, id, false);
    }
    public FocusEvent(Component source, int id, boolean temporary) {
        this(source, id, temporary, null);
    }
    public FocusEvent(Component source, int id, boolean temporary, Component opposite) {
        super(source, id);
        this.temporary = temporary;
        this.opposite = opposite;
    }
    public Component getOppositeComponent() {
        return opposite;
    }
    public boolean isTemporary() {
        return temporary;
    }
    @Override
    public String paramString() {
        String idString = null;
        switch (id) {
        case FOCUS_GAINED:
            idString = "FOCUS_GAINED"; 
            break;
        case FOCUS_LOST:
            idString = "FOCUS_LOST"; 
            break;
        default:
            idString = "unknown type"; 
        }
        return (idString +
                (temporary ? ",temporary" : ",permanent") + 
                ",opposite=" + opposite); 
    }
}
