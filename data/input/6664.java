public class MediaTray extends Media implements Attribute {
    private static final long serialVersionUID = -982503611095214703L;
    public static final MediaTray TOP = new MediaTray(0);
    public static final MediaTray MIDDLE = new MediaTray(1);
    public static final MediaTray BOTTOM = new MediaTray(2);
    public static final MediaTray ENVELOPE = new MediaTray(3);
    public static final MediaTray MANUAL = new MediaTray(4);
    public static final MediaTray LARGE_CAPACITY = new MediaTray(5);
    public static final MediaTray MAIN = new MediaTray(6);
    public static final MediaTray SIDE = new MediaTray(7);
    protected MediaTray(int value) {
        super (value);
    }
    private static final String[] myStringTable ={
        "top",
        "middle",
        "bottom",
        "envelope",
        "manual",
        "large-capacity",
        "main",
        "side"
    };
    private static final MediaTray[] myEnumValueTable = {
        TOP,
        MIDDLE,
        BOTTOM,
        ENVELOPE,
        MANUAL,
        LARGE_CAPACITY,
        MAIN,
        SIDE
    };
    protected String[] getStringTable()
    {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
}
