public class MouseEvent extends InputEvent {
    private static final long serialVersionUID = -991214153494842848L;
    public static final int MOUSE_FIRST = 500;
    public static final int MOUSE_LAST = 507;
    public static final int MOUSE_CLICKED = 500;
    public static final int MOUSE_PRESSED = 501;
    public static final int MOUSE_RELEASED = 502;
    public static final int MOUSE_MOVED = 503;
    public static final int MOUSE_ENTERED = 504;
    public static final int MOUSE_EXITED = 505;
    public static final int MOUSE_DRAGGED = 506;
    public static final int MOUSE_WHEEL = 507;
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;
    private boolean popupTrigger;
    private int clickCount;
    private int button;
    private int x;
    private int y;
    public static String getMouseModifiersText(int modifiers) {
        final StringBuffer text = new StringBuffer();
        if ((modifiers & META_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.meta", "Meta")).append("+"); 
        }
        if ((modifiers & SHIFT_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.shift", "Shift")).append("+"); 
        }
        if ((modifiers & CTRL_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.control", "Ctrl")).append("+"); 
        }
        if ((modifiers & ALT_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.alt", "Alt")).append("+"); 
        }
        if ((modifiers & ALT_GRAPH_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph")).append("+"); 
        }
        if ((modifiers & BUTTON1_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.button1", "Button1")).append("+"); 
        }
        if ((modifiers & BUTTON2_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.button2", "Button2")).append("+"); 
        }
        if ((modifiers & BUTTON3_MASK) != 0) {
            text.append(Toolkit.getProperty("AWT.button3", "Button3")).append("+"); 
        }
        return text.length() == 0 ? text.toString() : text.substring(0, text
                .length() - 1);
    }
    static String addMouseModifiersExText(String text, int modifiersEx) {
        if ((modifiersEx & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            text += ((text.length() > 0) ? "+" : "") + 
                    Toolkit.getProperty("AWT.button1", "Button1"); 
        }
        if ((modifiersEx & InputEvent.BUTTON2_DOWN_MASK) != 0) {
            text += ((text.length() > 0) ? "+" : "") + 
                    Toolkit.getProperty("AWT.button2", "Button2"); 
        }
        if ((modifiersEx & InputEvent.BUTTON3_DOWN_MASK) != 0) {
            text += ((text.length() > 0) ? "+" : "") + 
                    Toolkit.getProperty("AWT.button3", "Button3"); 
        }
        return text;
    }
    public MouseEvent(Component source, int id, long when,
                      int modifiers, int x, int y,
                      int clickCount, boolean popupTrigger) {
        this(source, id, when, modifiers, x, y,
             clickCount, popupTrigger, NOBUTTON);
    }
    public MouseEvent(Component source, int id, long when,
                      int modifiers, int x, int y,
                      int clickCount, boolean popupTrigger, int button) {
        super(source, id, when, modifiers);
        if ((button != NOBUTTON) && (button != BUTTON1) &&
                (button != BUTTON2) && (button != BUTTON3)) {
            throw new IllegalArgumentException(Messages.getString("awt.18B")); 
        }
        this.popupTrigger = popupTrigger;
        this.clickCount = clickCount;
        this.button = button;
        this.x = x;
        this.y = y;
    }
    public int getButton() {
        return button;
    }
    public int getClickCount() {
        return clickCount;
    }
    public Point getPoint() {
        return new Point(x, y);
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public boolean isPopupTrigger() {
        return popupTrigger;
    }
    public void translatePoint(int x, int y) {
        this.x += x;
        this.y += y;
    }
    @Override
    public String paramString() {
        String idString = null;
        String paramString = null;
        switch (id) {
        case MOUSE_MOVED:
            idString = "MOUSE_MOVED"; 
            break;
        case MOUSE_CLICKED:
            idString = "MOUSE_CLICKED"; 
            break;
        case MOUSE_PRESSED:
            idString = "MOUSE_PRESSED"; 
            break;
        case MOUSE_RELEASED:
            idString = "MOUSE_RELEASED"; 
            break;
        case MOUSE_DRAGGED:
            idString = "MOUSE_DRAGGED"; 
            break;
        case MOUSE_ENTERED:
            idString = "MOUSE_ENTERED"; 
            break;
        case MOUSE_EXITED:
            idString = "MOUSE_EXITED"; 
            break;
        case MOUSE_WHEEL:
            idString = "MOUSE_WHEEL"; 
            break;
        default:
            idString = "unknown type"; 
        }
        paramString = idString + ",(" + getX() + "," + getY() + ")" + 
                ",button=" + button; 
        if (getModifiersEx() > 0) {
            paramString += 
                    ",modifiers=" + getModifiersExText(getModifiersEx()) + 
                    ",extModifiers=" + getModifiersExText(getModifiersEx()); 
        }
        paramString += ",clickCount=" + getClickCount(); 
        return paramString;
    }
}
