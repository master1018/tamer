public class DefaultLookup {
    private static final Object DEFAULT_LOOKUP_KEY = new
                                        StringBuffer("DefaultLookup");
    private static Thread currentDefaultThread;
    private static DefaultLookup currentDefaultLookup;
    private static boolean isLookupSet;
    public static void setDefaultLookup(DefaultLookup lookup) {
        synchronized(DefaultLookup.class) {
            if (!isLookupSet && lookup == null) {
                return;
            }
            else if (lookup == null) {
                lookup = new DefaultLookup();
            }
            isLookupSet = true;
            AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
            currentDefaultThread = Thread.currentThread();
            currentDefaultLookup = lookup;
        }
    }
    public static Object get(JComponent c, ComponentUI ui, String key) {
        boolean lookupSet;
        synchronized(DefaultLookup.class) {
            lookupSet = isLookupSet;
        }
        if (!lookupSet) {
            return UIManager.get(key, c.getLocale());
        }
        Thread thisThread = Thread.currentThread();
        DefaultLookup lookup;
        synchronized(DefaultLookup.class) {
            if (thisThread == currentDefaultThread) {
                lookup = currentDefaultLookup;
            }
            else {
                lookup = (DefaultLookup)AppContext.getAppContext().get(
                                                   DEFAULT_LOOKUP_KEY);
                if (lookup == null) {
                    lookup = new DefaultLookup();
                    AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
                }
                currentDefaultThread = thisThread;
                currentDefaultLookup = lookup;
            }
        }
        return lookup.getDefault(c, ui, key);
    }
    public static int getInt(JComponent c, ComponentUI ui, String key,
                             int defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Number)) {
            return defaultValue;
        }
        return ((Number)iValue).intValue();
    }
    public static int getInt(JComponent c, ComponentUI ui, String key) {
        return getInt(c, ui, key, -1);
    }
    public static Insets getInsets(JComponent c, ComponentUI ui, String key,
                                   Insets defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Insets)) {
            return defaultValue;
        }
        return (Insets)iValue;
    }
    public static Insets getInsets(JComponent c, ComponentUI ui, String key) {
        return getInsets(c, ui, key, null);
    }
    public static boolean getBoolean(JComponent c, ComponentUI ui, String key,
                                     boolean defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Boolean)) {
            return defaultValue;
        }
        return ((Boolean)iValue).booleanValue();
    }
    public static boolean getBoolean(JComponent c, ComponentUI ui, String key) {
        return getBoolean(c, ui, key, false);
    }
    public static Color getColor(JComponent c, ComponentUI ui, String key,
                                 Color defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Color)) {
            return defaultValue;
        }
        return (Color)iValue;
    }
    public static Color getColor(JComponent c, ComponentUI ui, String key) {
        return getColor(c, ui, key, null);
    }
    public static Icon getIcon(JComponent c, ComponentUI ui, String key,
            Icon defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Icon)) {
            return defaultValue;
        }
        return (Icon)iValue;
    }
    public static Icon getIcon(JComponent c, ComponentUI ui, String key) {
        return getIcon(c, ui, key, null);
    }
    public static Border getBorder(JComponent c, ComponentUI ui, String key,
            Border defaultValue) {
        Object iValue = get(c, ui, key);
        if (iValue == null || !(iValue instanceof Border)) {
            return defaultValue;
        }
        return (Border)iValue;
    }
    public static Border getBorder(JComponent c, ComponentUI ui, String key) {
        return getBorder(c, ui, key, null);
    }
    public Object getDefault(JComponent c, ComponentUI ui, String key) {
        return UIManager.get(key, c.getLocale());
    }
}
