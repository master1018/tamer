public class Uninitialized {
    private static final Object UNINITIALIZED;
    static {
        UNINITIALIZED = Proxy.newProxyInstance(Uninitialized.class
                .getClassLoader(), new Class[] {
                ITypeReference.class, IPackage.class, IClassDefinition.class,
                IClassReference.class, ITypeVariableReference.class,
                ITypeVariableDefinition.class, IAnnotationField.class,
                Set.class, List.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                if (method.getName().equals("toString")) {
                    return "Uninitialized";
                }
                throw new UnsupportedOperationException();
            }
        });
    }
    @SuppressWarnings("unchecked")
    public static <T> T unset() {
        return (T) UNINITIALIZED;
    }
    public static boolean isInitialized(Object o) {
        return o != UNINITIALIZED && o != Kind.UNINITIALIZED;
    }
}
