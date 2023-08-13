public class Settings {
    public final static String NODE_TEXT = "nodeText";
    public final static String NODE_TEXT_DEFAULT = "[idx] [name]";
    public final static String NODE_WIDTH = "nodeWidth";
    public final static String NODE_WIDTH_DEFAULT = "100";
    public final static String PORT = "port";
    public final static String PORT_DEFAULT = "4444";
    public final static String DIRECTORY = "directory";
    public final static String DIRECTORY_DEFAULT = System.getProperty("user.dir");
    public static Preferences get() {
        return Preferences.userNodeForPackage(Settings.class);
    }
}
