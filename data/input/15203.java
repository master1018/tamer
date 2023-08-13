public class ParseBoolean {
    public static void main(String[] args) throws Exception {
        checkTrue(Boolean.parseBoolean("TRUE"));
        checkTrue(Boolean.parseBoolean("true"));
        checkTrue(Boolean.parseBoolean("TrUe"));
        checkFalse(Boolean.parseBoolean("false"));
        checkFalse(Boolean.parseBoolean("FALSE"));
        checkFalse(Boolean.parseBoolean("FaLse"));
        checkFalse(Boolean.parseBoolean(null));
        checkFalse(Boolean.parseBoolean("garbage"));
        checkFalse(Boolean.parseBoolean("TRUEE"));
    }
    static void checkTrue(boolean b) {
        if (!b)
            throw new RuntimeException("test failed");
    }
    static void checkFalse(boolean b) {
        if (b)
            throw new RuntimeException("test failed");
    }
}
