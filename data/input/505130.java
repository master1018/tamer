 class ClassCache<T> {
    private static final EnumComparator ENUM_COMPARATOR =
        new EnumComparator();
     static final ReflectionAccess REFLECT = getReflectionAccess();
    private final Class<T> clazz;
    private volatile Method[] declaredMethods;
    private volatile Method[] declaredPublicMethods;
    private volatile Method[] allMethods;
    private volatile Method[] allPublicMethods;
    private volatile Field[] declaredFields;
    private volatile Field[] declaredPublicFields;
    private volatile Field[] allFields;
    private volatile Field[] allPublicFields;
    private volatile T[] enumValuesInOrder;
    private volatile T[] enumValuesByName;
    static {
        Field field;
        try {
            field = EnumSet.class.getDeclaredField("LANG_BOOTSTRAP");
            REFLECT.setAccessibleNoCheck(field, true);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
        try {
            field.set(null, LangAccessImpl.THE_ONE);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
        LangAccess.setInstance(LangAccessImpl.THE_ONE);
    }
     ClassCache(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz == null");
        }
        this.clazz = clazz;
        this.declaredMethods = null;
        this.declaredPublicMethods = null;
        this.allMethods = null;
        this.allPublicMethods = null;
        this.enumValuesInOrder = null;
        this.enumValuesByName = null;
        this.declaredFields = null;
        this.declaredPublicFields = null;
        this.allFields = null;
        this.allPublicFields = null;
    }
    public Method[] getDeclaredMethods() {
        if (declaredMethods == null) {
            declaredMethods = Class.getDeclaredMethods(clazz, false);
        }
        return declaredMethods;
    }
    public Method[] getDeclaredPublicMethods() {
        if (declaredPublicMethods == null) {
            declaredPublicMethods = Class.getDeclaredMethods(clazz, true);
        }
        return declaredPublicMethods;
    }
    public Method[] getDeclaredMethods(boolean publicOnly) {
        return publicOnly ? getDeclaredPublicMethods() : getDeclaredMethods();
    }
    public Method[] getAllMethods() {
        if (allMethods == null) {
            allMethods = getFullListOfMethods(false);
        }
        return allMethods;
    }
    public Method[] getAllPublicMethods() {
        if (allPublicMethods == null) {
            allPublicMethods = getFullListOfMethods(true);
        }
        return allPublicMethods;
    }
    private Method[] getFullListOfMethods(boolean publicOnly) {
        ArrayList<Method> methods = new ArrayList<Method>();
        HashSet<String> seen = new HashSet<String>();
        findAllMethods(clazz, methods, seen, publicOnly);
        return methods.toArray(new Method[methods.size()]);
    }
    private static void findAllMethods(Class<?> clazz,
            ArrayList<Method> methods, HashSet<String> seen,
            boolean publicOnly) {
        StringBuilder builder = new StringBuilder();
        Class<?> origClass = clazz;
        while (clazz != null) {
            Method[] declaredMethods =
                clazz.getClassCache().getDeclaredMethods(publicOnly);
            int length = declaredMethods.length;
            if (length != 0) {
                for (Method method : declaredMethods) {
                    builder.setLength(0);
                    builder.append(method.getName());
                    builder.append('(');
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length != 0) {
                        builder.append(types[0].getName());
                        for (int j = 1; j < types.length; j++) {
                            builder.append(',');
                            builder.append(types[j].getName());
                        }
                    }
                    builder.append(')');
                    String signature = builder.toString();
                    if (!seen.contains(signature)) {
                        methods.add(method);
                        seen.add(signature);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        Class<?>[] interfaces = origClass.getInterfaces();
        for (Class<?> intf : interfaces) {
            findAllMethods(intf, methods, seen, publicOnly);
        }
    }
    public static Method findMethodByName(Method[] list, String name,
            Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (name == null) {
            throw new NullPointerException("Method name must not be null.");
        }
        for (int i = list.length - 1; i >= 0; i--) {
            Method method = list[i];
            if (method.getName().equals(name) 
                    && compareClassLists(
                            method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }
    public static boolean compareClassLists(Class<?>[] a, Class<?>[] b) {
        if (a == null) {
            return (b == null) || (b.length == 0);
        }
        int length = a.length;
        if (b == null) {
            return (length == 0);
        }
        if (length != b.length) {
            return false;
        }
        for (int i = length - 1; i >= 0; i--) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
    public static Method[] deepCopy(Method[] orig) {
        int length = orig.length;
        Method[] result = new Method[length];
        for (int i = length - 1; i >= 0; i--) {
            result[i] = REFLECT.clone(orig[i]);
        }
        return result;
    }
    public Field[] getDeclaredFields() {
        if (declaredFields == null) {
            declaredFields = Class.getDeclaredFields(clazz, false);
        }
        return declaredFields;
    }
    public Field[] getDeclaredPublicFields() {
        if (declaredPublicFields == null) {
            declaredPublicFields = Class.getDeclaredFields(clazz, true);
        }
        return declaredPublicFields;
    }
    public Field[] getDeclaredFields(boolean publicOnly) {
        return publicOnly ? getDeclaredPublicFields() : getDeclaredFields();
    }
    public Field[] getAllFields() {
        if (allFields == null) {
            allFields = getFullListOfFields(false);
        }
        return allFields;
    }
    public Field[] getAllPublicFields() {
        if (allPublicFields == null) {
            allPublicFields = getFullListOfFields(true);
        }
        return allPublicFields;
    }
    private Field[] getFullListOfFields(boolean publicOnly) {
        ArrayList<Field> fields = new ArrayList<Field>();
        HashSet<String> seen = new HashSet<String>();
        findAllfields(clazz, fields, seen, publicOnly);
        return fields.toArray(new Field[fields.size()]);
    }
    private static void findAllfields(Class<?> clazz,
            ArrayList<Field> fields, HashSet<String> seen,
            boolean publicOnly) {
        while (clazz != null) {
            Field[] declaredFields =
                    clazz.getClassCache().getDeclaredFields(publicOnly);
            for (Field field : declaredFields) {                    
                String signature = field.toString();
                if (!seen.contains(signature)) {
                    fields.add(field);
                    seen.add(signature);
                }
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> intf : interfaces) {
                findAllfields(intf, fields, seen, publicOnly);
            }
            clazz = clazz.getSuperclass();
        }
    }
    public static Field findFieldByName(Field[] list, String name)
            throws NoSuchFieldException {
        if (name == null) {
            throw new NullPointerException("Field name must not be null.");
        }
        for (int i = 0; i < list.length; i++) {
            Field field = list[i];
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new NoSuchFieldException(name);
    }
    public static Field[] deepCopy(Field[] orig) {
        int length = orig.length;
        Field[] result = new Field[length];
        for (int i = length - 1; i >= 0; i--) {
            result[i] = REFLECT.clone(orig[i]);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public T getEnumValue(String name) {
        Enum[] values = (Enum[]) getEnumValuesByName();
        if (values == null) {
            return null;
        }
        int min = 0;
        int max = values.length - 1;
        while (min <= max) {
            int guessIdx = min + ((max - min) >> 1);
            Enum guess = values[guessIdx];
            int cmp = name.compareTo(guess.name());
            if (cmp < 0) {
                max = guessIdx - 1;
            } else if (cmp > 0) {
                min = guessIdx + 1;
            } else {
                return (T) guess;
            }
        }
        return null;
    }
    public T[] getEnumValuesByName() {
        if (enumValuesByName == null) {
            T[] values = getEnumValuesInOrder();
            if (values != null) {
                values = (T[]) values.clone();
                Arrays.sort((Enum<?>[]) values, ENUM_COMPARATOR);
                enumValuesByName = values;
            }
        }
        return enumValuesByName;
    }
    public T[] getEnumValuesInOrder() {
        if ((enumValuesInOrder == null) && clazz.isEnum()) {
            enumValuesInOrder = callEnumValues();
        }
        return enumValuesInOrder;
    }
    @SuppressWarnings("unchecked")
    private T[] callEnumValues() {
        Method method;
        try {
            Method[] methods = getDeclaredPublicMethods();
            method = findMethodByName(methods, "values", (Class[]) null);
            method = REFLECT.accessibleClone(method);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException(ex);
        }
        try {
            return (T[]) method.invoke((Object[]) null);
        } catch (IllegalAccessException ex) {
            throw new Error(ex);
        } catch (InvocationTargetException ex) {
            Throwable te = ex.getTargetException();
            if (te instanceof RuntimeException) {
                throw (RuntimeException) te;
            } else if (te instanceof Error) {
                throw (Error) te;
            } else {
                throw new Error(te);
            }
        }
    }
    private static ReflectionAccess getReflectionAccess() {
        Method[] methods =
            Class.getDeclaredMethods(AccessibleObject.class, false);
        try {
            Method method = findMethodByName(methods, "getReflectionAccess",
                    (Class[]) null);
            Class.setAccessibleNoCheck(method, true);
            return (ReflectionAccess) method.invoke((Object[]) null);
        } catch (NoSuchMethodException ex) {
            throw new Error(ex);
        } catch (IllegalAccessException ex) {
            throw new Error(ex);
        } catch (InvocationTargetException ex) {
            throw new Error(ex);
        }
    }
    private static class EnumComparator implements Comparator<Enum<?>> {
        public int compare(Enum<?> e1, Enum<?> e2) {
            return e1.name().compareTo(e2.name());
        }
    }
}
