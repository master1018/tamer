public class Messages {
    private static final String sResource =
        "org.apache.harmony.x.imageio.internal.nls.messages"; 
    static public String getString(String msg) {
        return MsgHelp.getString(sResource, msg);
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
        return MsgHelp.getString(sResource, msg, args);
    }
}
