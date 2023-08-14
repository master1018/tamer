public class Msg {
    private static final String sResource =
        "org.apache.harmony.luni.util.ExternalMessages";
    static public String getString(String msg) {
        ResourceBundle bundle = MsgHelp.loadBundle(sResource);
        if (bundle == null) {
            return msg;
        }
        try {
            return bundle.getString(msg);
        } catch (MissingResourceException e) {
            return msg;
        }
    }
    static public String getString(String msg, Object arg) {
        return getString(msg, new Object[] { arg });
    }
    static public String getString(String msg, int arg) {
        return getString(msg, new Object[] { Integer.toString(arg) });
    }
    static public String getString(String msg, char arg) {
        return getString(msg, new Object[] { String.valueOf(arg) });
    }
    static public String getString(String msg, Object arg1, Object arg2) {
        return getString(msg, new Object[] { arg1, arg2 });
    }
    static public String getString(String msg, Object[] args) {
        String format = msg;
        ResourceBundle bundle = MsgHelp.loadBundle(sResource);
        if (bundle != null) {
            try {
                format = bundle.getString(msg);
            } catch (MissingResourceException e) {
            }
        }
        return MsgHelp.format(format, args);
    }
}
