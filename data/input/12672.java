public class EuroConverter {
    public static void main(String args[]) throws Exception {
        boolean pass = true;
        char[] map = new char[256]; 
        byte[] bytes = new byte[1]; 
        char[] chars = new char[1]; 
        for (int i=0; i<DATA.length; ) {
            String euroEnc = DATA[i++];
            String parentEnc = DATA[i++];
            System.out.println("Checking encoder " + euroEnc + " against " + parentEnc);
            String currentEnc = parentEnc;
            try {
                for (int j=-128; j<128; ++j) {
                    bytes[0] = (byte)j;
                    char parentValue = new String(bytes, parentEnc).charAt(0);
                    if (j != 0x0025) {
                        chars[0] = parentValue;
                        int parentRoundTrip = new String(chars).getBytes(parentEnc)[0];
                        if (parentRoundTrip != j) {
                            pass = false;
                            System.out.println("Error: Encoder " + parentEnc +
                                           " fails round-trip: " + j +
                                           " -> \\u" + Integer.toHexString(parentValue) +
                                           " -> " + parentRoundTrip);
                        }
                    }
                    map[(j+0x100)&0xFF] = parentValue;
                }
                while (DATA[i] != null) {
                    int codePoint = Integer.valueOf(DATA[i++], 16).intValue();
                    char expectedParentValue = DATA[i++].charAt(0);
                    char expectedEuroValue = DATA[i++].charAt(0);
                    if (map[codePoint] != expectedParentValue) {
                        pass = false;
                        System.out.println("Error: Encoder " + parentEnc +
                                           " " + Integer.toHexString(codePoint) + " -> \\u" +
                                           Integer.toHexString(map[codePoint]) + ", expected \\u" +
                                           Integer.toHexString(expectedParentValue));
                    }
                    map[codePoint] = expectedEuroValue;
                }
                ++i; 
                currentEnc = euroEnc;
                for (int j=-128; j<128; ++j) {
                    bytes[0] = (byte)j;
                    char euroValue = new String(bytes, euroEnc).charAt(0);
                    chars[0] = euroValue;
                    if (j != 0x0015) {
                        int euroRoundTrip = new String(chars).getBytes(euroEnc)[0];
                        if (euroRoundTrip != j) {
                            pass = false;
                            System.out.println("Error: Encoder " + euroEnc +
                                           " fails round-trip at " + j);
                        }
                    }
                    if (euroValue != map[(j+0x100)&0xFF]) {
                        pass = false;
                        System.out.println("Error: Encoder " + euroEnc +
                                           " " + Integer.toHexString((j+0x100)&0xFF) + " -> \\u" +
                                           Integer.toHexString(euroValue) + ", expected \\u" +
                                           Integer.toHexString(map[(j+0x100)&0xFF]));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding " + currentEnc);
                pass = false;
                while (i < DATA.length && DATA[i] != null) ++i;
                ++i; 
            }
        }
        if (!pass) {
            throw new RuntimeException("Bug 4114080 - Euro encoder test failed");
        }
    }
    static String[] DATA = {
        "ISO8859_15_FDIS", "ISO8859_1",
            "A4", "\u00A4", "\u20AC",
            "A6", "\u00A6", "\u0160",
            "A8", "\u00A8", "\u0161",
            "B4", "\u00B4", "\u017D",
            "B8", "\u00B8", "\u017E",
            "BC", "\u00BC", "\u0152",
            "BD", "\u00BD", "\u0153",
            "BE", "\u00BE", "\u0178",
            null,
        "Cp923", "ISO8859_15_FDIS", null,
        "Cp858", "Cp850", "D5", "\u0131", "\u20AC", null,
        "Cp1140", "Cp037", "9F", "\u00A4", "\u20AC", null,
        "Cp1141", "Cp273", "9F", "\u00A4", "\u20AC", null,
        "Cp1142", "Cp277", "5A", "\u00A4", "\u20AC", null,
        "Cp1143", "Cp278", "5A", "\u00A4", "\u20AC", null,
        "Cp1144", "Cp280", "9F", "\u00A4", "\u20AC", null,
        "Cp1145", "Cp284", "9F", "\u00A4", "\u20AC", null,
        "Cp1146", "Cp285", "9F", "\u00A4", "\u20AC", null,
        "Cp1147", "Cp297", "9F", "\u00A4", "\u20AC", null,
        "Cp1148", "Cp500", "9F", "\u00A4", "\u20AC", null,
        "Cp1149", "Cp871", "9F", "\u00A4", "\u20AC", null,
    };
}
