public class ParsingTest {
    public static void main(String... argv) {
        check("+100", +100);
        check("-100", -100);
        check("+0", 0);
        check("-0", 0);
        check("+00000", 0);
        check("-00000", 0);
        check("0", 0);
        check("1", 1);
        check("9", 9);
        checkFailure("\u0000");
        checkFailure("\u002f");
        checkFailure("+");
        checkFailure("-");
        checkFailure("++");
        checkFailure("+-");
        checkFailure("-+");
        checkFailure("--");
        checkFailure("++100");
        checkFailure("--100");
        checkFailure("+-6");
        checkFailure("-+6");
        checkFailure("*100");
    }
    private static void check(String val, int expected) {
        int n = Integer.parseInt(val);
        if (n != expected)
            throw new RuntimeException("Integer.parsedInt failed. String:" +
                                                val + " Result:" + n);
    }
    private static void checkFailure(String val) {
        int n = 0;
        try {
            n = Integer.parseInt(val);
            System.err.println("parseInt(" + val + ") incorrectly returned " + n);
            throw new RuntimeException();
        } catch (NumberFormatException nfe) {
            ; 
        }
    }
}
