public final class SystemColor extends Color implements java.io.Serializable {
    public final static int DESKTOP = 0;
    public final static int ACTIVE_CAPTION = 1;
    public final static int ACTIVE_CAPTION_TEXT = 2;
    public final static int ACTIVE_CAPTION_BORDER = 3;
    public final static int INACTIVE_CAPTION = 4;
    public final static int INACTIVE_CAPTION_TEXT = 5;
    public final static int INACTIVE_CAPTION_BORDER = 6;
    public final static int WINDOW = 7;
    public final static int WINDOW_BORDER = 8;
    public final static int WINDOW_TEXT = 9;
    public final static int MENU = 10;
    public final static int MENU_TEXT = 11;
    public final static int TEXT = 12;
    public final static int TEXT_TEXT = 13;
    public final static int TEXT_HIGHLIGHT = 14;
    public final static int TEXT_HIGHLIGHT_TEXT = 15;
    public final static int TEXT_INACTIVE_TEXT = 16;
    public final static int CONTROL = 17;
    public final static int CONTROL_TEXT = 18;
    public final static int CONTROL_HIGHLIGHT = 19;
    public final static int CONTROL_LT_HIGHLIGHT = 20;
    public final static int CONTROL_SHADOW = 21;
    public final static int CONTROL_DK_SHADOW = 22;
    public final static int SCROLLBAR = 23;
    public final static int INFO = 24;
    public final static int INFO_TEXT = 25;
    public final static int NUM_COLORS = 26;
    private static int[] systemColors = {
        0xFF005C5C,  
        0xFF000080,  
        0xFFFFFFFF,  
        0xFFC0C0C0,  
        0xFF808080,  
        0xFFC0C0C0,  
        0xFFC0C0C0,  
        0xFFFFFFFF,  
        0xFF000000,  
        0xFF000000,  
        0xFFC0C0C0,  
        0xFF000000,  
        0xFFC0C0C0,  
        0xFF000000,  
        0xFF000080,  
        0xFFFFFFFF,  
        0xFF808080,  
        0xFFC0C0C0,  
        0xFF000000,  
        0xFFFFFFFF,  
        0xFFE0E0E0,  
        0xFF808080,  
        0xFF000000,  
        0xFFE0E0E0,  
        0xFFE0E000,  
        0xFF000000,  
    };
    public final static SystemColor desktop = new SystemColor((byte)DESKTOP);
    public final static SystemColor activeCaption = new SystemColor((byte)ACTIVE_CAPTION);
    public final static SystemColor activeCaptionText = new SystemColor((byte)ACTIVE_CAPTION_TEXT);
    public final static SystemColor activeCaptionBorder = new SystemColor((byte)ACTIVE_CAPTION_BORDER);
    public final static SystemColor inactiveCaption = new SystemColor((byte)INACTIVE_CAPTION);
    public final static SystemColor inactiveCaptionText = new SystemColor((byte)INACTIVE_CAPTION_TEXT);
    public final static SystemColor inactiveCaptionBorder = new SystemColor((byte)INACTIVE_CAPTION_BORDER);
    public final static SystemColor window = new SystemColor((byte)WINDOW);
    public final static SystemColor windowBorder = new SystemColor((byte)WINDOW_BORDER);
    public final static SystemColor windowText = new SystemColor((byte)WINDOW_TEXT);
    public final static SystemColor menu = new SystemColor((byte)MENU);
    public final static SystemColor menuText = new SystemColor((byte)MENU_TEXT);
    public final static SystemColor text = new SystemColor((byte)TEXT);
    public final static SystemColor textText = new SystemColor((byte)TEXT_TEXT);
    public final static SystemColor textHighlight = new SystemColor((byte)TEXT_HIGHLIGHT);
    public final static SystemColor textHighlightText = new SystemColor((byte)TEXT_HIGHLIGHT_TEXT);
    public final static SystemColor textInactiveText = new SystemColor((byte)TEXT_INACTIVE_TEXT);
    public final static SystemColor control = new SystemColor((byte)CONTROL);
    public final static SystemColor controlText = new SystemColor((byte)CONTROL_TEXT);
    public final static SystemColor controlHighlight = new SystemColor((byte)CONTROL_HIGHLIGHT);
    public final static SystemColor controlLtHighlight = new SystemColor((byte)CONTROL_LT_HIGHLIGHT);
    public final static SystemColor controlShadow = new SystemColor((byte)CONTROL_SHADOW);
    public final static SystemColor controlDkShadow = new SystemColor((byte)CONTROL_DK_SHADOW);
    public final static SystemColor scrollbar = new SystemColor((byte)SCROLLBAR);
    public final static SystemColor info = new SystemColor((byte)INFO);
    public final static SystemColor infoText = new SystemColor((byte)INFO_TEXT);
    private static final long serialVersionUID = 4503142729533789064L;
    private transient int index;
    private static SystemColor systemColorObjects [] = {
        SystemColor.desktop,
        SystemColor.activeCaption,
        SystemColor.activeCaptionText,
        SystemColor.activeCaptionBorder,
        SystemColor.inactiveCaption,
        SystemColor.inactiveCaptionText,
        SystemColor.inactiveCaptionBorder,
        SystemColor.window,
        SystemColor.windowBorder,
        SystemColor.windowText,
        SystemColor.menu,
        SystemColor.menuText,
        SystemColor.text,
        SystemColor.textText,
        SystemColor.textHighlight,
        SystemColor.textHighlightText,
        SystemColor.textInactiveText,
        SystemColor.control,
        SystemColor.controlText,
        SystemColor.controlHighlight,
        SystemColor.controlLtHighlight,
        SystemColor.controlShadow,
        SystemColor.controlDkShadow,
        SystemColor.scrollbar,
        SystemColor.info,
        SystemColor.infoText
    };
    static {
      updateSystemColors();
    }
    private static void updateSystemColors() {
        if (!GraphicsEnvironment.isHeadless()) {
            Toolkit.getDefaultToolkit().loadSystemColors(systemColors);
        }
        for (int i = 0; i < systemColors.length; i++) {
            systemColorObjects[i].value = systemColors[i];
        }
    }
    private SystemColor(byte index) {
        super(systemColors[index]);
        this.index = index;
    }
    public String toString() {
        return getClass().getName() + "[i=" + (index) + "]";
    }
    private Object readResolve() {
        return systemColorObjects[value];
    }
    private Object writeReplace() throws ObjectStreamException
    {
        SystemColor color = new SystemColor((byte)index);
        color.value = index;
        return color;
    }
}
