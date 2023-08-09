public class Variability implements java.io.Serializable {
    private static final int NATTRIBUTES = 4;
    private static Variability[] map = new Variability[NATTRIBUTES];
    private String name;
    private int value;
    public static final Variability INVALID = new Variability("Invalid",0);
    public static final Variability CONSTANT = new Variability("Constant",1);
    public static final Variability MONOTONIC = new Variability("Monotonic",2);
    public static final Variability VARIABLE = new Variability("Variable",3);
    public String toString() {
        return name;
    }
    public int intValue() {
        return value;
    }
    public static Variability toVariability(int value) {
        if (value < 0 || value >= map.length || map[value] == null) {
            return INVALID;
        }
        return map[value];
    }
    private Variability(String name, int value) {
        this.name = name;
        this.value = value;
        map[value]=this;
    }
    private static final long serialVersionUID = 6992337162326171013L;
}
