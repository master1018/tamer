public class OpenTypeConverter {
    private static final WeakHashMap<Class,OpenType> convertedTypes =
        new WeakHashMap<Class,OpenType>();
    private static final OpenType[] simpleTypes = {
        BIGDECIMAL, BIGINTEGER, BOOLEAN, BYTE, CHARACTER, DATE,
        DOUBLE, FLOAT, INTEGER, LONG, OBJECTNAME, SHORT, STRING,
        VOID,
    };
    static {
        for (int i = 0; i < simpleTypes.length; i++) {
            final OpenType t = simpleTypes[i];
            Class c;
            try {
                c = Class.forName(t.getClassName(), false,
                                  String.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                assert(false);
                c = null; 
            }
            convertedTypes.put(c, t);
            if (c.getName().startsWith("java.lang.")) {
                try {
                    final Field typeField = c.getField("TYPE");
                    final Class primitiveType = (Class) typeField.get(null);
                    convertedTypes.put(primitiveType, t);
                } catch (NoSuchFieldException e) {
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
    private static class InProgress extends OpenType {
        private static final String description =
                  "Marker to detect recursive type use -- internal use only!";
        InProgress() throws OpenDataException {
            super("java.lang.String", "java.lang.String", description);
        }
        public String toString() {
            return description;
        }
        public int hashCode() {
            return 0;
        }
        public boolean equals(Object o) {
            return false;
        }
        public boolean isValue(Object o) {
            return false;
        }
    }
    private static final OpenType inProgress;
    static {
        OpenType t;
        try {
            t = new InProgress();
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
        inProgress = t;
    }
    public static synchronized OpenType toOpenType(Class c)
            throws OpenDataException {
        OpenType t;
        t = convertedTypes.get(c);
        if (t != null) {
            if (t instanceof InProgress)
                throw new OpenDataException("Recursive data structure");
            return t;
        }
        convertedTypes.put(c, inProgress);
        if (Enum.class.isAssignableFrom(c))
            t = STRING;
        else if (c.isArray())
            t = makeArrayType(c);
        else
            t = makeCompositeType(c);
        convertedTypes.put(c, t);
        return t;
    }
    private static OpenType makeArrayType(Class c) throws OpenDataException {
        int dim;
        for (dim = 0; c.isArray(); dim++)
            c = c.getComponentType();
        return new ArrayType(dim, toOpenType(c));
    }
    private static OpenType makeCompositeType(Class c)
            throws OpenDataException {
        final Method[] methods = c.getMethods();
        final List<String> names = new ArrayList<String>();
        final List<OpenType> types = new ArrayList<OpenType>();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            final String name = method.getName();
            final Class type = method.getReturnType();
            final String rest;
            if (name.startsWith("get"))
                rest = name.substring(3);
            else if (name.startsWith("is") && type == boolean.class)
                rest = name.substring(2);
            else if (name.startsWith("has") && type == boolean.class)
                rest = name.substring(3);
            else
                continue;
            if (rest.equals("") || method.getParameterTypes().length > 0
                || type == void.class || rest.equals("Class"))
                continue;
            names.add(decapitalize(rest));
            types.add(toOpenType(type));
        }
        final String[] nameArray = names.toArray(new String[0]);
        return new CompositeType(c.getName(),
                                 c.getName(),
                                 nameArray, 
                                 nameArray, 
                                 types.toArray(new OpenType[0]));
    }
    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                        Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
