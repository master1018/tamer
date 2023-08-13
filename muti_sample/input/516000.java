public class PropertyEditorManager {
    private static String[] path = { "org.apache.harmony.beans.editors" }; 
    private static final Map<Class<?>, Class<?>> registeredEditors = new HashMap<Class<?>, Class<?>>();
    public PropertyEditorManager() {
    }
    public static void registerEditor(Class<?> targetType, Class<?> editorClass) {
        if (targetType == null) {
            throw new NullPointerException();
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        if (editorClass != null) {
            registeredEditors.put(targetType, editorClass);
        } else {
            registeredEditors.remove(targetType);
        }
    }
    public static synchronized PropertyEditor findEditor(Class<?> targetType) {
        if (targetType == null) {
            throw new NullPointerException();
        }
        Class<?> editorClass = null;
        PropertyEditor editor = null;
        editorClass = registeredEditors.get(targetType);
        if (editorClass == null) {
            String editorClassName = targetType.getName() + "Editor"; 
            ClassLoader loader = targetType.getClassLoader();
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }
            try {
                editorClass = Class.forName(editorClassName, true, loader);
            } catch (ClassNotFoundException cnfe) {
                String shortEditorClassName = editorClassName
                        .substring(editorClassName.lastIndexOf(".") + 1); 
                if (targetType.isPrimitive()) {
                    shortEditorClassName = shortEditorClassName.substring(0, 1)
                            .toUpperCase()
                            + shortEditorClassName.substring(1);
                }
                for (String element : path) {
                    editorClassName = element + "." + shortEditorClassName; 
                    try {
                        editorClass = Class.forName(editorClassName, true,
                                loader);
                        break;
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
        if (editorClass != null) {
            try {
                editor = (PropertyEditor) editorClass.newInstance();
            } catch (Exception e) {
            }
        }
        return editor;
    }
    public static synchronized void setEditorSearchPath(String[] apath) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        path = apath;
    }
    public static synchronized String[] getEditorSearchPath() {
        return path;
    }
}
