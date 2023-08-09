public final class TypeResolver {
    public static Type resolveInClass(Class<?> inClass, Type type) {
        return resolve(getActualType(inClass), type);
    }
    public static Type[] resolveInClass(Class<?> inClass, Type[] types) {
        return resolve(getActualType(inClass), types);
    }
    public static Type resolve(Type actual, Type formal) {
        return new TypeResolver(actual).resolve(formal);
    }
    public static Type[] resolve(Type actual, Type[] formals) {
        return new TypeResolver(actual).resolve(formals);
    }
    public static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return (Class<?>) pt.getRawType();
        }
        if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)type;
            Type[] bounds = tv.getBounds();
            return (0 < bounds.length)
                    ? erase(bounds[0])
                    : Object.class;
        }
        if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType)type;
            Type[] bounds = wt.getUpperBounds();
            return (0 < bounds.length)
                    ? erase(bounds[0])
                    : Object.class;
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType)type;
            return Array.newInstance(erase(gat.getGenericComponentType()), 0).getClass();
        }
        throw new IllegalArgumentException("Unknown Type kind: " + type.getClass());
    }
    public static Class[] erase(Type[] types) {
        int length = types.length;
        Class[] classes = new Class[length];
        for (int i = 0; i < length; i++) {
            classes[i] = TypeResolver.erase(types[i]);
        }
        return classes;
    }
    private final Map<TypeVariable<?>, Type> map
        = new HashMap<TypeVariable<?>, Type>();
    private TypeResolver(Type actual) {
        prepare(actual);
    }
    private void prepare(Type type) {
        Class<?> raw = (Class<?>)((type instanceof Class<?>)
                ? type
                : ((ParameterizedType)type).getRawType());
        TypeVariable<?>[] formals = raw.getTypeParameters();
        Type[] actuals = (type instanceof Class<?>)
                ? formals
                : ((ParameterizedType)type).getActualTypeArguments();
        assert formals.length == actuals.length;
        for (int i = 0; i < formals.length; i++) {
            this.map.put(formals[i], actuals[i]);
        }
        Type gSuperclass = raw.getGenericSuperclass();
        if (gSuperclass != null) {
            prepare(gSuperclass);
        }
        for (Type gInterface : raw.getGenericInterfaces()) {
            prepare(gInterface);
        }
        if (type instanceof Class<?> && formals.length > 0) {
            for (Map.Entry<TypeVariable<?>, Type> entry : this.map.entrySet()) {
                entry.setValue(erase(entry.getValue()));
            }
        }
    }
    private Type resolve(Type formal) {
        if (formal instanceof Class) {
            return formal;
        }
        if (formal instanceof GenericArrayType) {
            Type comp = ((GenericArrayType)formal).getGenericComponentType();
            comp = resolve(comp);
            return (comp instanceof Class)
                    ? Array.newInstance((Class<?>)comp, 0).getClass()
                    : GenericArrayTypeImpl.make(comp);
        }
        if (formal instanceof ParameterizedType) {
            ParameterizedType fpt = (ParameterizedType)formal;
            Type[] actuals = resolve(fpt.getActualTypeArguments());
            return ParameterizedTypeImpl.make(
                    (Class<?>)fpt.getRawType(), actuals, fpt.getOwnerType());
        }
        if (formal instanceof WildcardType) {
            WildcardType fwt = (WildcardType)formal;
            Type[] upper = resolve(fwt.getUpperBounds());
            Type[] lower = resolve(fwt.getLowerBounds());
            return new WildcardTypeImpl(upper, lower);
        }
        if (!(formal instanceof TypeVariable)) {
            throw new IllegalArgumentException("Bad Type kind: " + formal.getClass());
        }
        Type actual = this.map.get((TypeVariable) formal);
        if (actual == null || actual.equals(formal)) {
            return formal;
        }
        actual = fixGenericArray(actual);
        return resolve(actual);
    }
    private Type[] resolve(Type[] formals) {
        int length = formals.length;
        Type[] actuals = new Type[length];
        for (int i = 0; i < length; i++) {
            actuals[i] = resolve(formals[i]);
        }
        return actuals;
    }
    private static Type fixGenericArray(Type type) {
        if (type instanceof GenericArrayType) {
            Type comp = ((GenericArrayType)type).getGenericComponentType();
            comp = fixGenericArray(comp);
            if (comp instanceof Class) {
                return Array.newInstance((Class<?>)comp, 0).getClass();
            }
        }
        return type;
    }
    private static Type getActualType(Class<?> inClass) {
        Type[] params = inClass.getTypeParameters();
        return (params.length == 0)
                ? inClass
                : ParameterizedTypeImpl.make(
                        inClass, params, inClass.getEnclosingClass());
    }
}
