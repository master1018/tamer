public class WindowEvent extends ComponentEvent {
    private static final long serialVersionUID = -1567959133147912127L;
    public static final int WINDOW_FIRST = 200;
    public static final int WINDOW_OPENED = 200;
    public static final int WINDOW_CLOSING = 201;
    public static final int WINDOW_CLOSED = 202;
    public static final int WINDOW_ICONIFIED = 203;
    public static final int WINDOW_DEICONIFIED = 204;
    public static final int WINDOW_ACTIVATED = 205;
    public static final int WINDOW_DEACTIVATED = 206;
    public static final int WINDOW_GAINED_FOCUS = 207;
    public static final int WINDOW_LOST_FOCUS = 208;
    public static final int WINDOW_STATE_CHANGED = 209;
    public static final int WINDOW_LAST = 209;
    private int oldState;
    private int newState;
    public WindowEvent() {
        super(null, 0);
    }
    public int getNewState() {
        return newState;
    }
    public int getOldState() {
        return oldState;
    }
    @Override
    public String paramString() {
        String typeString = null;
        switch (id) {
        case WINDOW_OPENED:
            typeString = "WINDOW_OPENED"; 
            break;
        case WINDOW_CLOSING:
            typeString = "WINDOW_CLOSING"; 
            break;
        case WINDOW_CLOSED:
            typeString = "WINDOW_CLOSED"; 
            break;
        case WINDOW_ICONIFIED:
            typeString = "WINDOW_ICONIFIED"; 
            break;
        case WINDOW_DEICONIFIED:
            typeString = "WINDOW_DEICONIFIED"; 
            break;
        case WINDOW_ACTIVATED:
            typeString = "WINDOW_ACTIVATED"; 
            break;
        case WINDOW_DEACTIVATED:
            typeString = "WINDOW_DEACTIVATED"; 
            break;
        case WINDOW_GAINED_FOCUS:
            typeString = "WINDOW_GAINED_FOCUS"; 
            break;
        case WINDOW_LOST_FOCUS:
            typeString = "WINDOW_LOST_FOCUS"; 
            break;
        case WINDOW_STATE_CHANGED:
            typeString = "WINDOW_STATE_CHANGED"; 
            break;
        default:
            typeString = "unknown type"; 
        }
        return typeString;
    }
}
