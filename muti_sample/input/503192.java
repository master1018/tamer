public final class ComponentOrientation implements Serializable {
    private static final long serialVersionUID = -4113291392143563828L;
    public static final ComponentOrientation LEFT_TO_RIGHT = new ComponentOrientation(true, true);
    public static final ComponentOrientation RIGHT_TO_LEFT = new ComponentOrientation(true, false);
    public static final ComponentOrientation UNKNOWN = new ComponentOrientation(true, true);
    private static final Set<String> rlLangs = new HashSet<String>(); 
    private final boolean horizontal;
    private final boolean left2right;
    static {
        rlLangs.add("ar"); 
        rlLangs.add("fa"); 
        rlLangs.add("iw"); 
        rlLangs.add("ur"); 
    }
    @Deprecated
    public static ComponentOrientation getOrientation(ResourceBundle bdl) {
        Object obj = null;
        try {
            obj = bdl.getObject("Orientation"); 
        } catch (MissingResourceException mre) {
            obj = null;
        }
        if (obj instanceof ComponentOrientation) {
            return (ComponentOrientation)obj;
        }
        Locale locale = bdl.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return getOrientation(locale);
    }
    public static ComponentOrientation getOrientation(Locale locale) {
        String lang = locale.getLanguage();
        return rlLangs.contains(lang) ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
    }
    private ComponentOrientation(boolean hor, boolean l2r) {
        horizontal = hor;
        left2right = l2r;
    }
    public boolean isHorizontal() {
        return horizontal;
    }
    public boolean isLeftToRight() {
        return left2right;
    }
}
