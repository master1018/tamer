public class JPEGQTable {
    private final static int SIZE = 64;
    private final static int BASELINE_MAX = 255;
    private final static int MAX = 32767;
    private int[] theTable;
    private static final int[] K1LumTable = new int[] {
            16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55, 14, 13, 16, 24, 40, 57,
            69, 56, 14, 17, 22, 29, 51, 87, 80, 62, 18, 22, 37, 56, 68, 109, 103, 77, 24, 35, 55,
            64, 81, 104, 113, 92, 49, 64, 78, 87, 103, 121, 120, 101, 72, 92, 95, 98, 112, 100,
            103, 99
    };
    private static final int[] K2ChrTable = new int[] {
            17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99, 24, 26, 56, 99, 99, 99,
            99, 99, 47, 66, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99
    };
    public static final JPEGQTable K1Luminance = new JPEGQTable(K1LumTable);
    public static final JPEGQTable K1Div2Luminance = K1Luminance.getScaledInstance(0.5f, true);
    public static final JPEGQTable K2Chrominance = new JPEGQTable(K2ChrTable);
    public static final JPEGQTable K2Div2Chrominance = K2Chrominance.getScaledInstance(0.5f, true);;
    public JPEGQTable(int[] table) {
        if (table == null) {
            throw new IllegalArgumentException("table should not be NULL");
        }
        if (table.length != SIZE) {
            throw new IllegalArgumentException("illegal table size: " + table.length);
        }
        theTable = table.clone();
    }
    public int[] getTable() {
        return theTable.clone();
    }
    public JPEGQTable getScaledInstance(float scaleFactor, boolean forceBaseline) {
        int table[] = new int[SIZE];
        int maxValue = forceBaseline ? BASELINE_MAX : MAX;
        for (int i = 0; i < theTable.length; i++) {
            int rounded = Math.round(theTable[i] * scaleFactor);
            if (rounded < 1) {
                rounded = 1;
            }
            if (rounded > maxValue) {
                rounded = maxValue;
            }
            table[i] = rounded;
        }
        return new JPEGQTable(table);
    }
    @Override
    public String toString() {
        return "JPEGQTable";
    }
}
