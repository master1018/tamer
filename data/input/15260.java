public class PropertyEditorManager {
    private static final Object FINDER_KEY = new Object();
    public static void registerEditor(Class<?> targetType, Class<?> editorClass) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        getFinder().register(targetType, editorClass);
    }
    public static PropertyEditor findEditor(Class<?> targetType) {
        return getFinder().find(targetType);
    }
    public static String[] getEditorSearchPath() {
        return getFinder().getPackages();
    }
    public static void setEditorSearchPath(String[] path) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        getFinder().setPackages(path);
    }
    private static PropertyEditorFinder getFinder() {
        AppContext context = AppContext.getAppContext();
        Object object = context.get(FINDER_KEY);
        if (object instanceof PropertyEditorFinder) {
            return (PropertyEditorFinder) object;
        }
        PropertyEditorFinder finder = new PropertyEditorFinder();
        context.put(FINDER_KEY, finder);
        return finder;
    }
}
