public class AccessibleObject implements AnnotatedElement {
    boolean flag = false;
    private static final String DIMENSION_1 = "[]";
    private static final String DIMENSION_2 = "[][]";
    private static final String DIMENSION_3 = "[][][]";
    static Hashtable<String, String> trans;
    static {
        trans = new Hashtable<String, String>(9);
        trans.put("byte", "B");
        trans.put("char", "C");
        trans.put("short", "S");
        trans.put("int", "I");
        trans.put("long", "J");
        trans.put("float", "F");
        trans.put("double", "D");
        trans.put("void", "V");
        trans.put("boolean", "Z");
    }
    public static void setAccessible(AccessibleObject[] objects, boolean flag)
            throws SecurityException {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkPermission(new ReflectPermission("suppressAccessChecks"));
        }
        synchronized(AccessibleObject.class) {
            for (int i = 0; i < objects.length; i++) {
                objects[i].flag = flag;
            }
        }
    }
    protected AccessibleObject() {
        super();
    }
    public boolean isAccessible() {
        return flag;
    }
    public void setAccessible(boolean flag) throws SecurityException {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkPermission(new ReflectPermission("suppressAccessChecks"));
        }
        this.flag = flag;
    }
     void setAccessibleNoCheck(boolean flag) {
        this.flag = flag;
    }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }
    public Annotation[] getDeclaredAnnotations() {
        throw new RuntimeException("subclass must override this method");
    }
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException();
        }
        Annotation[] annos = getAnnotations();
        for (int i = annos.length-1; i >= 0; --i) {
            if (annos[i].annotationType() == annotationType) {
                return (T) annos[i];
            }
        }
        return null;
    }
    String getSignature(Class<?> clazz) {
        String result = "";
        String nextType = clazz.getName();
        if(trans.containsKey(nextType)) {
            result = trans.get(nextType);
        } else {
            if(clazz.isArray()) {
                result = "[" + getSignature(clazz.getComponentType());   
            } else {
                result = "L" + nextType + ";";
            }
        }
        return result;
    }
    String toString(Class<?>[] types) {
        StringBuilder result = new StringBuilder();
        if (types.length != 0) {
            result.append(types[0].getCanonicalName());
            for (int i = 1; i < types.length; i++) {
                result.append(',');
                result.append(types[i].getCanonicalName());
            }
        }
        return result.toString();
    }
     String getSignatureAttribute() {
        throw new UnsupportedOperationException();
    }
     static String getClassSignatureAttribute(Class clazz) {
        Object[] annotation = getClassSignatureAnnotation(clazz);
        if (annotation == null) {
            return null;
        }
        return StringUtils.combineStrings(annotation);
    }
    private static native Object[] getClassSignatureAnnotation(Class clazz);
    static  ReflectionAccess getReflectionAccess() {
        return ReflectionAccessImpl.THE_ONE;
    }
    void appendArrayType(StringBuilder sb, Class<?> obj) {
        if (!obj.isArray()) {
            sb.append(obj.getName());
            return;
        }
        int dimensions = 1;
        Class simplified = obj.getComponentType();
        obj = simplified;
        while (simplified.isArray()) {
            obj = simplified;
            dimensions++;
        }
        sb.append(obj.getName());
        switch (dimensions) {
        case 1:
            sb.append(DIMENSION_1);
            break;
        case 2:
            sb.append(DIMENSION_2);
            break;
        case 3:
            sb.append(DIMENSION_3);
            break;
        default:
            for (; dimensions > 0; dimensions--) {
                sb.append(DIMENSION_1);
            }
        }
    }
    void appendArrayType(StringBuilder sb, Class[] objs) {
        if (objs.length > 0) {
            appendArrayType(sb, objs[0]);
            for (int i = 1; i < objs.length; i++) {
                sb.append(',');
                appendArrayType(sb, objs[i]);
            }
        }
    }
    void appendArrayGenericType(StringBuilder sb, Type[] objs) {
        if (objs.length > 0) {
            appendGenericType(sb, objs[0]);
            for (int i = 1; i < objs.length; i++) {
                sb.append(',');
                appendGenericType(sb, objs[i]);
            }
        }
    }
    void appendGenericType(StringBuilder sb, Type obj) {
        if (obj instanceof TypeVariable) {
            sb.append(((TypeVariable)obj).getName());
        } else if (obj instanceof ParameterizedType) {
            sb.append(obj.toString());
        } else if (obj instanceof GenericArrayType) { 
            Type simplified = ((GenericArrayType)obj).getGenericComponentType();
            appendGenericType(sb, simplified);
            sb.append("[]");
        } else if (obj instanceof Class) {
            Class c = ((Class<?>)obj);
            if (c.isArray()){
                String as[] = c.getName().split("\\[");
                int len = as.length-1;
                if (as[len].length() > 1){
                    sb.append(as[len].substring(1, as[len].length()-1));
                } else {
                    char ch = as[len].charAt(0);
                    if (ch == 'I')
                        sb.append("int");
                    else if (ch == 'B')
                        sb.append("byte");
                    else if (ch == 'J')
                        sb.append("long");
                    else if (ch == 'F')
                        sb.append("float");
                    else if (ch == 'D')
                        sb.append("double");
                    else if (ch == 'S')
                        sb.append("short");
                    else if (ch == 'C')
                        sb.append("char");
                    else if (ch == 'Z')
                        sb.append("boolean");
                    else if (ch == 'V') 
                        sb.append("void");
                }
                for (int i = 0; i < len; i++){
                    sb.append("[]");
                }
            } else {
                sb.append(c.getName());
            }
        }
    }
    void appendSimpleType(StringBuilder sb, Class<?>[] objs) {
        if (objs.length > 0) {
            sb.append(objs[0].getName());
            for (int i = 1; i < objs.length; i++) {
                sb.append(',');
                sb.append(objs[i].getName());
            }
        }
    }
}
