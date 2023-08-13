public class Units implements java.io.Serializable {
    private static final int NUNITS=8;
    private static Units[] map = new Units[NUNITS];
    private final String name;
    private final int value;
    public static final Units INVALID = new Units("Invalid", 0);
    public static final Units NONE = new Units("None", 1);
    public static final Units BYTES = new Units("Bytes", 2);
    public static final Units TICKS = new Units("Ticks", 3);
    public static final Units EVENTS = new Units("Events", 4);
    public static final Units STRING = new Units("String", 5);
    public static final Units HERTZ = new Units("Hertz", 6);
    public String toString() {
        return name;
    }
    public int intValue() {
        return value;
    }
    public static Units toUnits(int value) {
        if (value < 0 || value >= map.length || map[value] == null) {
            return INVALID;
        }
        return map[value];
    }
    private Units(String name, int value) {
        this.name = name;
        this.value = value;
        map[value] = this;
    }
    private static final long serialVersionUID = 6992337162326171013L;
}
