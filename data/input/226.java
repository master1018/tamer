public class DebugClassHelper {
    private static final String DEBUG_CLASS_ATTRIBUTE = "dc";
    private static boolean debugClassEnabled = false;
    private DebugClassHelper() {
    }
    public static final void addDebugClass(UIObject uiObject, String debugClass) {
        Preconditions.checkNotNull(uiObject, "uiObject cannot be null");
        addDebugClass(uiObject.getElement(), debugClass);
    }
    public static final void addDebugClass(Element elem, String debugClass) {
        Preconditions.checkNotNull(elem, "addDebugClass: Element must not be null");
        if (debugClassEnabled) {
            Set<String> debugClasses = getDebugClasses(elem);
            if (debugClasses.add(debugClass)) {
                elem.setAttribute(DEBUG_CLASS_ATTRIBUTE, joinDebugClasses(debugClasses));
            }
        }
    }
    public static final void replaceDebugClass(Element elem, String oldClassName, String newClassName) {
        Preconditions.checkNotNull(elem, "replaceDebugClass: Element must not be null");
        if (debugClassEnabled) {
            Set<String> debugClasses = getDebugClasses(elem);
            boolean removed = debugClasses.remove(oldClassName);
            if (debugClasses.add(newClassName) || removed) {
                elem.setAttribute(DEBUG_CLASS_ATTRIBUTE, joinDebugClasses(debugClasses));
            }
        }
    }
    public static final void removeDebugClass(UIObject uiObject, String debugClass) {
        Preconditions.checkNotNull(uiObject, "uiObject cannot be null");
        removeDebugClass(uiObject.getElement(), debugClass);
    }
    public static final void removeDebugClass(Element elem, String debugClass) {
        if (debugClassEnabled && null != elem) {
            Set<String> debugClasses = getDebugClasses(elem);
            if (debugClasses.remove(debugClass)) {
                elem.setAttribute(DEBUG_CLASS_ATTRIBUTE, joinDebugClasses(debugClasses));
            }
        }
    }
    public static void clearDebugClasses(UIObject uiObject) {
        clearDebugClasses(uiObject.getElement());
    }
    public static void clearDebugClasses(Element elem) {
        elem.removeAttribute(DEBUG_CLASS_ATTRIBUTE);
    }
    private static final String joinDebugClasses(Set<String> debugClasses) {
        StringBuilder result = new StringBuilder();
        for (String debugClass : debugClasses) {
            result.append(debugClass);
            result.append(" ");
        }
        return result.toString().trim();
    }
    private static final Set<String> getDebugClasses(Element elem) {
        Set<String> result = new HashSet<String>();
        String debugClasses = elem.getAttribute(DEBUG_CLASS_ATTRIBUTE);
        result.addAll(Arrays.asList(debugClasses.split(" ")));
        return result;
    }
    public static void setDebugClassEnabled(boolean enableDebugClass) {
        debugClassEnabled = enableDebugClass;
    }
}
