public final class MsgHelp {
    private static final String RESOURCE_NAME =
        "/org/apache/harmony/awt/internal/nls/messages.properties";
    private static final ResourceBundle THE_BUNDLE;
    static {
        ResourceBundle rb = null;
        try {
            InputStream in = MsgHelp.class.getResourceAsStream(
                    RESOURCE_NAME);
            rb = new PropertyResourceBundle(in);
        } catch (IOException ex) {
            Logger.global.warning("Couldn't read resource bundle: " +
                    ex);
        } catch (RuntimeException ex) {
            Logger.global.warning("Couldn't find resource bundle: " +
                    ex);
        }
        THE_BUNDLE = rb;
    }
    public static String getString(String msg) {
        if (THE_BUNDLE == null) {
            return msg;
        }
        try {
            return THE_BUNDLE.getString(msg);
        } catch (MissingResourceException e) {
            return "Missing message: " + msg;
        }
    }
    static public String getString(String msg, Object[] args) {
        String format = msg;
        if (THE_BUNDLE != null) {
            try {
                format = THE_BUNDLE.getString(msg);
            } catch (MissingResourceException e) {
            }
        }
        return org.apache.harmony.luni.util.MsgHelp.format(format, args);
    }
}
