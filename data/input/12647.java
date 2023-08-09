public class MissingResourceCauseTest {
    public static void main(String[] args) {
        callGetBundle("NonResourceBundle", ClassCastException.class);
        callGetBundle("MalformedDataRB", Locale.ENGLISH, IllegalArgumentException.class);
        callGetBundle("PrivateConstructorRB", IllegalAccessException.class);
        callGetBundle("AbstractRB", InstantiationException.class);
        callGetBundle("BadStaticInitRB", ExceptionInInitializerError.class);
        callGetBundle("UnreadableRB", IOException.class);
    }
    private static void callGetBundle(String baseName,
                                      Class<? extends Throwable> expectedCause) {
        callGetBundle(baseName, Locale.getDefault(), expectedCause);
    }
    private static void callGetBundle(String baseName, Locale locale,
                                      Class<? extends Throwable> expectedCause) {
        ResourceBundle rb;
        try {
            rb = ResourceBundle.getBundle(baseName, locale);
            throw new RuntimeException("getBundle(\""+baseName+"\") doesn't throw "
                                      + expectedCause);
        } catch (MissingResourceException e) {
            Throwable cause = e.getCause();
            if (!expectedCause.isInstance(cause)) {
                throw new RuntimeException("getBundle(\""+baseName+"\") throws "
                                           + cause + ", expected " + expectedCause);
            }
        }
    }
}
