public class JPEGQTable {
    private static final int[] k1 = {
        16,  11,  10,  16,  24,  40,  51,  61,
        12,  12,  14,  19,  26,  58,  60,  55,
        14,  13,  16,  24,  40,  57,  69,  56,
        14,  17,  22,  29,  51,  87,  80,  62,
        18,  22,  37,  56,  68,  109, 103, 77,
        24,  35,  55,  64,  81,  104, 113, 92,
        49,  64,  78,  87,  103, 121, 120, 101,
        72,  92,  95,  98,  112, 100, 103, 99,
    };
    private static final int[] k1div2 = {
        8,   6,   5,   8,   12,  20,  26,  31,
        6,   6,   7,   10,  13,  29,  30,  28,
        7,   7,   8,   12,  20,  29,  35,  28,
        7,   9,   11,  15,  26,  44,  40,  31,
        9,   11,  19,  28,  34,  55,  52,  39,
        12,  18,  28,  32,  41,  52,  57,  46,
        25,  32,  39,  44,  52,  61,  60,  51,
        36,  46,  48,  49,  56,  50,  52,  50,
    };
    private static final int[] k2 = {
        17,  18,  24,  47,  99,  99,  99,  99,
        18,  21,  26,  66,  99,  99,  99,  99,
        24,  26,  56,  99,  99,  99,  99,  99,
        47,  66,  99,  99,  99,  99,  99,  99,
        99,  99,  99,  99,  99,  99,  99,  99,
        99,  99,  99,  99,  99,  99,  99,  99,
        99,  99,  99,  99,  99,  99,  99,  99,
        99,  99,  99,  99,  99,  99,  99,  99,
    };
    private static final int[] k2div2 = {
        9,   9,   12,  24,  50,  50,  50,  50,
        9,   11,  13,  33,  50,  50,  50,  50,
        12,  13,  28,  50,  50,  50,  50,  50,
        24,  33,  50,  50,  50,  50,  50,  50,
        50,  50,  50,  50,  50,  50,  50,  50,
        50,  50,  50,  50,  50,  50,  50,  50,
        50,  50,  50,  50,  50,  50,  50,  50,
        50,  50,  50,  50,  50,  50,  50,  50,
    };
    public static final JPEGQTable
        K1Luminance = new JPEGQTable(k1, false);
    public static final JPEGQTable
        K1Div2Luminance = new JPEGQTable(k1div2, false);
    public static final JPEGQTable K2Chrominance =
        new JPEGQTable(k2, false);
    public static final JPEGQTable K2Div2Chrominance =
        new JPEGQTable(k2div2, false);
    private int[] qTable;
    private JPEGQTable(int[] table, boolean copy) {
        qTable = (copy) ? Arrays.copyOf(table, table.length) : table;
    }
    public JPEGQTable(int[] table) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null.");
        }
        if (table.length != 64) {
            throw new IllegalArgumentException("table.length != 64");
        }
        qTable = Arrays.copyOf(table, table.length);
    }
    public int[] getTable() {
        return Arrays.copyOf(qTable, qTable.length);
    }
    public JPEGQTable getScaledInstance(float scaleFactor,
                                        boolean forceBaseline) {
        int max = (forceBaseline) ? 255 : 32767;
        int[] scaledTable = new int[qTable.length];
        for (int i=0; i<qTable.length; i++) {
            int sv = (int)((qTable[i] * scaleFactor)+0.5f);
            if (sv < 1) {
                sv = 1;
            }
            if (sv > max) {
                sv = max;
            }
            scaledTable[i] = sv;
        }
        return new JPEGQTable(scaledTable);
    }
    public String toString() {
        String ls = System.getProperty("line.separator", "\n");
        StringBuilder sb = new StringBuilder("JPEGQTable:"+ls);
        for (int i=0; i < qTable.length; i++) {
            if (i % 8 == 0) {
                sb.append('\t');
            }
            sb.append(qTable[i]);
            sb.append(((i % 8) == 7) ? ls : ' ');
        }
        return sb.toString();
    }
}
