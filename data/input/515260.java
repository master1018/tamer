public final class ExceptionUtils {
    static private final Method INIT_CAUSE_METHOD = getInitCauseMethod();
    static private Method getInitCauseMethod() {
        try {
            Class[] paramsClasses = new Class[] { Throwable.class };
            return Throwable.class.getMethod("initCause", paramsClasses);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    public static void initCause(Throwable throwable, Throwable cause) {
        if (INIT_CAUSE_METHOD != null) {
            try {
                INIT_CAUSE_METHOD.invoke(throwable, new Object[] { cause });
            } catch (Exception e) {
            }
        }
    }
    private ExceptionUtils() {
    }
}
