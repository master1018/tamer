final class RhinoWrapFactory extends WrapFactory {
    private RhinoWrapFactory() {}
    private static RhinoWrapFactory theInstance;
    static synchronized WrapFactory getInstance() {
        if (theInstance == null) {
            theInstance = new RhinoWrapFactory();
        }
        return theInstance;
    }
    private static class RhinoJavaObject extends NativeJavaObject {
        RhinoJavaObject(Scriptable scope, Object obj, Class type) {
            super(scope, null, type);
            javaObject = obj;
        }
    }
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
            Object javaObject, Class staticType) {
        SecurityManager sm = System.getSecurityManager();
        ClassShutter classShutter = RhinoClassShutter.getInstance();
        if (javaObject instanceof ClassLoader) {
            if (sm != null) {
                sm.checkPermission(GET_CLASSLOADER_PERMISSION);
            }
            return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
        } else {
            String name = null;
            if (javaObject instanceof Class) {
                name = ((Class)javaObject).getName();
            } else if (javaObject instanceof Member) {
                Member member = (Member) javaObject;
                if (sm != null && !Modifier.isPublic(member.getModifiers())) {
                    return null;
                }
                name = member.getDeclaringClass().getName();
            }
            if (name != null) {
                if (!classShutter.visibleToScripts(name)) {
                    return null;
                } else {
                    return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
                }
            }
        }
        Class dynamicType = javaObject.getClass();
        String name = dynamicType.getName();
        if (!classShutter.visibleToScripts(name)) {
            Class type = null;
            if (staticType != null && staticType.isInterface()) {
                type = staticType;
            } else {
                while (dynamicType != null) {
                    dynamicType = dynamicType.getSuperclass();
                    name = dynamicType.getName();
                    if (classShutter.visibleToScripts(name)) {
                         type = dynamicType; break;
                    }
                }
                assert type != null:
                       "even java.lang.Object is not accessible?";
            }
            return new RhinoJavaObject(scope, javaObject, type);
        } else {
            return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
        }
    }
}
