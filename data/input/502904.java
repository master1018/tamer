public class PaintEvent extends ComponentEvent {
    private static final long serialVersionUID = 1267492026433337593L;
    public static final int PAINT_FIRST = 800;
    public static final int PAINT_LAST = 801;
    public static final int PAINT = 800;
    public static final int UPDATE = 801;
    private Rectangle updateRect;
    public PaintEvent(Component source, int id, Rectangle updateRect) {
        super(source, id);
        this.updateRect = updateRect;
    }
    public Rectangle getUpdateRect() {
        return updateRect;
    }
    public void setUpdateRect(Rectangle updateRect) {
        this.updateRect = updateRect;
    }
    @Override
    public String paramString() {
        String typeString = null;
        switch (id) {
        case PAINT:
            typeString = "PAINT"; 
            break;
        case UPDATE:
            typeString = "UPDATE"; 
            break;
        default:
            typeString = "unknown type"; 
        }
        return typeString + ",updateRect=" + updateRect; 
    }
}
