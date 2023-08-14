public final class ConstructorFinder extends AbstractFinder<Constructor<?>> {
    private static final WeakCache<Signature, Constructor<?>> CACHE = new WeakCache<Signature, Constructor<?>>();
    public static Constructor<?> findConstructor(Class<?> type, Class<?>...args) throws NoSuchMethodException {
        if (type.isPrimitive()) {
            throw new NoSuchMethodException("Primitive wrapper does not contain constructors");
        }
        if (type.isInterface()) {
            throw new NoSuchMethodException("Interface does not contain constructors");
        }
        if (Modifier.isAbstract(type.getModifiers())) {
            throw new NoSuchMethodException("Abstract class cannot be instantiated");
        }
        if (!Modifier.isPublic(type.getModifiers())) {
            throw new NoSuchMethodException("Class is not accessible");
        }
        PrimitiveWrapperMap.replacePrimitivesWithWrappers(args);
        Signature signature = new Signature(type, args);
        Constructor<?> constructor = CACHE.get(signature);
        if (constructor != null) {
            return constructor;
        }
        constructor = new ConstructorFinder(args).find(type.getConstructors());
        CACHE.put(signature, constructor);
        return constructor;
    }
    private ConstructorFinder(Class<?>[] args) {
        super(args);
    }
    @Override
    protected Class<?>[] getParameters(Constructor<?> constructor) {
        return constructor.getParameterTypes();
    }
    @Override
    protected boolean isVarArgs(Constructor<?> constructor) {
        return constructor.isVarArgs();
    }
    @Override
    protected boolean isValid(Constructor<?> constructor) {
        return Modifier.isPublic(constructor.getModifiers());
    }
}
