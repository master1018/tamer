public abstract class NativeEvent {
    public static final int ID_PLATFORM = 0;
    public static final int ID_BOUNDS_CHANGED = -1;
    public static final int ID_INSETS_CHANGED = -2;
    public static final int ID_CREATED = -3;
    public static final int ID_MOUSE_GRAB_CANCELED = -4;
    public static final int ID_THEME_CHANGED = -5;
    protected long windowId;
    protected int eventId;
    protected long otherWindowId;
    protected Point screenPos;
    protected Point localPos;
    protected Rectangle windowRect;
    protected int modifiers;
    protected int mouseButton;
    protected int wheelRotation;
    protected KeyInfo keyInfo = new KeyInfo();
    protected int windowState = -1;
    protected long time;
    public long getWindowId() {
        return windowId;
    }
    public int getEventId() {
        return eventId;
    }
    public Point getLocalPos() {
        return localPos;
    }
    public Point getScreenPos() {
        return screenPos;
    }
    public Rectangle getWindowRect() {
        return windowRect;
    }
    public int getInputModifiers() {
        return modifiers;
    }
    public int getWindowState() {
        return windowState;
    }
    public int getVKey() {
        return (keyInfo != null) ? keyInfo.vKey : KeyInfo.DEFAULT_VKEY;
    }
    public int getKeyLocation() {
        return (keyInfo != null) ? keyInfo.keyLocation : KeyInfo.DEFAULT_LOCATION;
    }
    public StringBuffer getKeyChars() {
        if (keyInfo == null) {
            return null;
        }
        if (keyInfo.vKey == KeyEvent.VK_ENTER) {
            keyInfo.keyChars.setLength(0);
            keyInfo.setKeyChars('\n');
        }
        return keyInfo.keyChars;
    }
    public char getLastChar() {
        if (keyInfo == null || keyInfo.keyChars.length() == 0) {
            return KeyEvent.CHAR_UNDEFINED;
        }
        return keyInfo.keyChars.charAt(keyInfo.keyChars.length()-1);
    }
    public int getMouseButton() {
        return mouseButton;
    }
    public long getTime() {
        return time;
    }
    public long getOtherWindowId() {
        return otherWindowId;
    }
    public abstract MultiRectArea getClipRects();
    public abstract Rectangle getClipBounds();
    public abstract Insets getInsets();
    public abstract boolean getTrigger();
    public int getWheelRotation() {
        return wheelRotation;
    }
}
