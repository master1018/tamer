public abstract class InputEvent extends ComponentEvent {
    private static final PlatformLogger logger = PlatformLogger.getLogger("java.awt.event.InputEvent");
    public static final int SHIFT_MASK = Event.SHIFT_MASK;
    public static final int CTRL_MASK = Event.CTRL_MASK;
    public static final int META_MASK = Event.META_MASK;
    public static final int ALT_MASK = Event.ALT_MASK;
    public static final int ALT_GRAPH_MASK = 1 << 5;
    public static final int BUTTON1_MASK = 1 << 4;
    public static final int BUTTON2_MASK = Event.ALT_MASK;
    public static final int BUTTON3_MASK = Event.META_MASK;
    public static final int SHIFT_DOWN_MASK = 1 << 6;
    public static final int CTRL_DOWN_MASK = 1 << 7;
    public static final int META_DOWN_MASK = 1 << 8;
    public static final int ALT_DOWN_MASK = 1 << 9;
    public static final int BUTTON1_DOWN_MASK = 1 << 10;
    public static final int BUTTON2_DOWN_MASK = 1 << 11;
    public static final int BUTTON3_DOWN_MASK = 1 << 12;
    public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;
    private static final int [] BUTTON_DOWN_MASK = new int [] { BUTTON1_DOWN_MASK,
                                                               BUTTON2_DOWN_MASK,
                                                               BUTTON3_DOWN_MASK,
                                                               1<<14, 
                                                               1<<15, 
                                                               1<<16,
                                                               1<<17,
                                                               1<<18,
                                                               1<<19,
                                                               1<<20,
                                                               1<<21,
                                                               1<<22,
                                                               1<<23,
                                                               1<<24,
                                                               1<<25,
                                                               1<<26,
                                                               1<<27,
                                                               1<<28,
                                                               1<<29,
                                                               1<<30};
    private static int [] getButtonDownMasks(){
        return Arrays.copyOf(BUTTON_DOWN_MASK, BUTTON_DOWN_MASK.length);
    }
    public static int getMaskForButton(int button) {
        if (button <= 0 || button > BUTTON_DOWN_MASK.length) {
            throw new IllegalArgumentException("button doesn\'t exist " + button);
        }
        return BUTTON_DOWN_MASK[button - 1];
    }
    static final int FIRST_HIGH_BIT = 1 << 31;
    static final int JDK_1_3_MODIFIERS = SHIFT_DOWN_MASK - 1;
    static final int HIGH_MODIFIERS = ~( FIRST_HIGH_BIT - 1 );
    long when;
    int modifiers;
    private transient boolean canAccessSystemClipboard;
    static {
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setInputEventAccessor(
            new AWTAccessor.InputEventAccessor() {
                public int[] getButtonDownMasks() {
                    return InputEvent.getButtonDownMasks();
                }
            });
    }
    private static native void initIDs();
    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
        canAccessSystemClipboard = canAccessSystemClipboard();
    }
    private boolean canAccessSystemClipboard() {
        boolean b = false;
        if (!GraphicsEnvironment.isHeadless()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    sm.checkSystemClipboardAccess();
                    b = true;
                } catch (SecurityException se) {
                    if (logger.isLoggable(PlatformLogger.FINE)) {
                        logger.fine("InputEvent.canAccessSystemClipboard() got SecurityException ", se);
                    }
                }
            } else {
                b = true;
            }
        }
        return b;
    }
    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }
    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }
    public boolean isMetaDown() {
        return (modifiers & META_MASK) != 0;
    }
    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }
    public boolean isAltGraphDown() {
        return (modifiers & ALT_GRAPH_MASK) != 0;
    }
    public long getWhen() {
        return when;
    }
    public int getModifiers() {
        return modifiers & (JDK_1_3_MODIFIERS | HIGH_MODIFIERS);
    }
    public int getModifiersEx() {
        return modifiers & ~JDK_1_3_MODIFIERS;
    }
    public void consume() {
        consumed = true;
    }
    public boolean isConsumed() {
        return consumed;
    }
    static final long serialVersionUID = -2482525981698309786L;
    public static String getModifiersExText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        int buttonNumber = 1;
        for (int mask : InputEvent.BUTTON_DOWN_MASK){
            if ((modifiers & mask) != 0) {
                buf.append(Toolkit.getProperty("AWT.button"+buttonNumber, "Button"+buttonNumber));
                buf.append("+");
            }
            buttonNumber++;
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length()-1); 
        }
        return buf.toString();
    }
}
