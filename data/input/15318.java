public class EscapeUnescapeTests {
    public static void main(String args[]) throws Exception {
        String[] invalids = new String[] {"\\", "\\\\\\" };
        String[] valids = new String[] {"\\\\", "\\\\\\\\"};
        String val;
        Object unescVal = null;
        System.out.println("##### Unescape value tests #####");
        for (int i = 0; i < valids.length; i++) {
            unescVal = Rdn.unescapeValue(valids[i]);
            System.out.println("Orig val: " + valids[i] +
                                "       Unescaped val: " + unescVal);
        }
        boolean isExcepThrown = false;
        for (int i = 0; i < invalids.length; i++) {
            val = "Juicy" + invalids[i] + "Fruit";
            try {
                unescVal = Rdn.unescapeValue(val);
            } catch (IllegalArgumentException e) {
                System.out.println("Caught the right exception: " + e);
                isExcepThrown = true;
            }
            if (!isExcepThrown) {
                throw new Exception(
                        "Unescaped successfully an invalid string "
                        + val + " as Rdn: " + unescVal);
            }
            isExcepThrown = false;
        }
        String[] values = new String[] {";", "<<<", "###", "=="};
        System.out.println("##### Escape value tests #####");
        printEscapedVal(values);
        values = new String[] {"  leading space", "trailing space  "};
        printEscapedVal(values);
        byte[] bytes = new byte[] {1, 2, 3, 4};
        String escVal = Rdn.escapeValue(bytes);
        System.out.println("Orig val: " + bytes +
                                "       Escaped val: " + escVal);
    }
    static void printEscapedVal(Object[] values) {
        String escVal;
        for (int i = 0; i < values.length; i++) {
            escVal = Rdn.escapeValue(values[i]);
            System.out.println("Orig val: " + values[i] +
                                "       Escaped val: " + escVal);
        }
    }
}
