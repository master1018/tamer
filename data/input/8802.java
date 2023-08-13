class XWindowAttributesData {
    static int NORMAL           = 0;
    static int ICONIC           = 1;
    static int MAXIMIZED        = 2;
    static int AWT_DECOR_NONE        = 0;
    static int AWT_DECOR_ALL         = 1;
    static int AWT_DECOR_BORDER      = 2;
    static int AWT_DECOR_RESIZEH     = 4;
    static int AWT_DECOR_TITLE       = 8;
    static int AWT_DECOR_MENU        = 0x10;
    static int AWT_DECOR_MINIMIZE    = 0x20;
    static int AWT_DECOR_MAXIMIZE    = 0x40;
    static int AWT_UNOBSCURED        = 0;   
    static int AWT_PARTIALLY_OBSCURED = 1;  
    static int AWT_FULLY_OBSCURED    =  2;  
    static int AWT_UNKNOWN_OBSCURITY = 3;
    boolean nativeDecor;
    boolean initialFocus;
    boolean isResizable;
    int initialState;
    boolean initialResizability;
    int visibilityState; 
    String title;
    java.util.List<XIconInfo> icons;
    boolean iconsInherited;
    int decorations;            
    int functions; 
    XWindowAttributesData() {
        nativeDecor = false;
        initialFocus = false;
        isResizable = false;
        initialState = NORMAL;
        visibilityState = AWT_UNKNOWN_OBSCURITY;
        title = null;
        icons = null;
        iconsInherited = true;
        decorations = 0;
        functions = 0;
        initialResizability = true;
    }
}
