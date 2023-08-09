public final class Void extends Object {
    public static final Class<Void> TYPE = lookupType();
    @SuppressWarnings("unchecked")
    private static Class<Void> lookupType() {
        Class<?> voidType = null;
        try {
            Method method = Runnable.class.getMethod("run", new Class[0]); 
            voidType = method.getReturnType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (Class<Void>) voidType;
    }
	private Void() {
	}
}
