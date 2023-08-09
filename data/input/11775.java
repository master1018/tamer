public class DefaultMetalTheme extends MetalTheme {
    private static final boolean PLAIN_FONTS;
    private static final String[] fontNames = {
        Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG,Font.DIALOG
    };
    private static final int[] fontStyles = {
        Font.BOLD, Font.PLAIN, Font.PLAIN, Font.BOLD, Font.BOLD, Font.PLAIN
    };
    private static final int[] fontSizes = {
        12, 12, 12, 12, 12, 10
    };
    private static final String[] defaultNames = {
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.systemFont",
        "swing.plaf.metal.userFont",
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.controlFont",
        "swing.plaf.metal.smallFont"
    };
    static String getDefaultFontName(int key) {
        return fontNames[key];
    }
    static int getDefaultFontSize(int key) {
        return fontSizes[key];
    }
    static int getDefaultFontStyle(int key) {
        if (key != WINDOW_TITLE_FONT) {
            Object boldMetal = null;
            if (AppContext.getAppContext().get(
                    SwingUtilities2.LAF_STATE_KEY) != null) {
                boldMetal = UIManager.get("swing.boldMetal");
            }
            if (boldMetal != null) {
                if (Boolean.FALSE.equals(boldMetal)) {
                    return Font.PLAIN;
                }
            }
            else if (PLAIN_FONTS) {
                return Font.PLAIN;
            }
        }
        return fontStyles[key];
    }
    static String getDefaultPropertyName(int key) {
        return defaultNames[key];
    }
    static {
        Object boldProperty = java.security.AccessController.doPrivileged(
            new GetPropertyAction("swing.boldMetal"));
        if (boldProperty == null || !"false".equals(boldProperty)) {
            PLAIN_FONTS = false;
        }
        else {
            PLAIN_FONTS = true;
        }
    }
    private static final ColorUIResource primary1 = new ColorUIResource(
                              102, 102, 153);
    private static final ColorUIResource primary2 = new ColorUIResource(153,
                              153, 204);
    private static final ColorUIResource primary3 = new ColorUIResource(
                              204, 204, 255);
    private static final ColorUIResource secondary1 = new ColorUIResource(
                              102, 102, 102);
    private static final ColorUIResource secondary2 = new ColorUIResource(
                              153, 153, 153);
    private static final ColorUIResource secondary3 = new ColorUIResource(
                              204, 204, 204);
    private FontDelegate fontDelegate;
    public String getName() { return "Steel"; }
    public DefaultMetalTheme() {
        install();
    }
    protected ColorUIResource getPrimary1() { return primary1; }
    protected ColorUIResource getPrimary2() { return primary2; }
    protected ColorUIResource getPrimary3() { return primary3; }
    protected ColorUIResource getSecondary1() { return secondary1; }
    protected ColorUIResource getSecondary2() { return secondary2; }
    protected ColorUIResource getSecondary3() { return secondary3; }
    public FontUIResource getControlTextFont() {
        return getFont(CONTROL_TEXT_FONT);
    }
    public FontUIResource getSystemTextFont() {
        return getFont(SYSTEM_TEXT_FONT);
    }
    public FontUIResource getUserTextFont() {
        return getFont(USER_TEXT_FONT);
    }
    public FontUIResource getMenuTextFont() {
        return getFont(MENU_TEXT_FONT);
    }
    public FontUIResource getWindowTitleFont() {
        return getFont(WINDOW_TITLE_FONT);
    }
    public FontUIResource getSubTextFont() {
        return getFont(SUB_TEXT_FONT);
    }
    private FontUIResource getFont(int key) {
        return fontDelegate.getFont(key);
    }
    void install() {
        if (MetalLookAndFeel.isWindows() &&
                             MetalLookAndFeel.useSystemFonts()) {
            fontDelegate = new WindowsFontDelegate();
        }
        else {
            fontDelegate = new FontDelegate();
        }
    }
    boolean isSystemTheme() {
        return (getClass() == DefaultMetalTheme.class);
    }
    private static class FontDelegate {
        private static int[] defaultMapping = {
            CONTROL_TEXT_FONT, SYSTEM_TEXT_FONT,
            USER_TEXT_FONT, CONTROL_TEXT_FONT,
            CONTROL_TEXT_FONT, SUB_TEXT_FONT
        };
        FontUIResource fonts[];
        public FontDelegate() {
            fonts = new FontUIResource[6];
        }
        public FontUIResource getFont(int type) {
            int mappedType = defaultMapping[type];
            if (fonts[type] == null) {
                Font f = getPrivilegedFont(mappedType);
                if (f == null) {
                    f = new Font(getDefaultFontName(type),
                             getDefaultFontStyle(type),
                             getDefaultFontSize(type));
                }
                fonts[type] = new FontUIResource(f);
            }
            return fonts[type];
        }
        protected Font getPrivilegedFont(final int key) {
            return java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Font>() {
                    public Font run() {
                        return Font.getFont(getDefaultPropertyName(key));
                    }
                }
                );
        }
    }
    private static class WindowsFontDelegate extends FontDelegate {
        private MetalFontDesktopProperty[] props;
        private boolean[] checkedPriviledged;
        public WindowsFontDelegate() {
            props = new MetalFontDesktopProperty[6];
            checkedPriviledged = new boolean[6];
        }
        public FontUIResource getFont(int type) {
            if (fonts[type] != null) {
                return fonts[type];
            }
            if (!checkedPriviledged[type]) {
                Font f = getPrivilegedFont(type);
                checkedPriviledged[type] = true;
                if (f != null) {
                    fonts[type] = new FontUIResource(f);
                    return fonts[type];
                }
            }
            if (props[type] == null) {
                props[type] = new MetalFontDesktopProperty(type);
            }
            return (FontUIResource)props[type].createValue(null);
        }
    }
}
