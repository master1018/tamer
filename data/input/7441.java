public class bug6312358 {
    public static void main(String[] args) throws Exception {
        try {
            Method getInstanceMethod = Locale.class.getDeclaredMethod(
                "getInstance", String.class, String.class, String.class
            );
            getInstanceMethod.setAccessible(true);
            getInstanceMethod.invoke(null, "null", "GB", "");
            try {
                getInstanceMethod.invoke(null, null, "GB", "");
                throw new RuntimeException("Should NPE with language set to null");
            } catch (InvocationTargetException exc) {
                Throwable cause = exc.getCause();
                if (!(cause instanceof NullPointerException)) {
                    throw new RuntimeException(cause+" is thrown with language set to null");
                }
            }
            getInstanceMethod.invoke(null, "en", "null", "");
            try {
                getInstanceMethod.invoke(null, "en", null, "");
                throw new RuntimeException("Should NPE with country set to null");
            } catch (InvocationTargetException exc) {
                Throwable cause = exc.getCause();
                if (!(cause instanceof NullPointerException)) {
                    throw new RuntimeException(cause+" is thrown with country set to null");
                }
            }
            getInstanceMethod.invoke(null, "en", "GB", "null");
            try {
                getInstanceMethod.invoke(null, "en", "GB", null);
                throw new RuntimeException("Should NPE with variant set to null");
            } catch (InvocationTargetException exc) {
                Throwable cause = exc.getCause();
                if (!(cause instanceof NullPointerException)) {
                    throw new RuntimeException(cause+" is thrown with variant set to null");
                }
            }
        } catch (java.lang.NoSuchMethodException exc) {
        }
    }
}
