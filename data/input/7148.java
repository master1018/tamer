public class Decode {
    private static void check(String val, int expected) {
        int n = (Integer.decode(val)).intValue();
        if (n != expected)
            throw new RuntimeException("Integer.decode failed. String:" +
                                                val + " Result:" + n);
    }
    private static void checkFailure(String val, String message) {
        try {
            int n = (Integer.decode(val)).intValue();
            throw new RuntimeException(message);
        } catch (NumberFormatException e) { }
    }
    public static void main(String[] args) throws Exception {
        check(new String(""+Integer.MIN_VALUE), Integer.MIN_VALUE);
        check(new String(""+Integer.MAX_VALUE), Integer.MAX_VALUE);
        check("10",   10);
        check("0x10", 16);
        check("0X10", 16);
        check("010",  8);
        check("#10",  16);
        check("+10",   10);
        check("+0x10", 16);
        check("+0X10", 16);
        check("+010",  8);
        check("+#10",  16);
        check("-10",   -10);
        check("-0x10", -16);
        check("-0X10", -16);
        check("-010",  -8);
        check("-#10",  -16);
        check(Long.toString(Integer.MIN_VALUE), Integer.MIN_VALUE);
        check(Long.toString(Integer.MAX_VALUE), Integer.MAX_VALUE);
        checkFailure("0x-10",   "Integer.decode allows negative sign in wrong position.");
        checkFailure("0x+10",   "Integer.decode allows positive sign in wrong position.");
        checkFailure("+",       "Raw plus sign allowed.");
        checkFailure("-",       "Raw minus sign allowed.");
        checkFailure(Long.toString((long)Integer.MIN_VALUE - 1L), "Out of range");
        checkFailure(Long.toString((long)Integer.MAX_VALUE + 1L), "Out of range");
        checkFailure("", "Empty String");
        try {
            Integer.decode(null);
            throw new RuntimeException("Integer.decode(null) expected to throw NPE");
        } catch (NullPointerException npe) {}
    }
}
