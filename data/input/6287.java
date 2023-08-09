public class StringConstructor {
    public static void main(String [] argv) {
        constructWithoutError("0", 0L);
        constructWithoutError("000000000000000000", 0L);
        constructWithoutError("1", 1L);
        constructWithoutError("-1", -1L);
        constructWithoutError("+1", +1L);
        constructWithoutError( "123456789123456789", 123456789123456789L);
        constructWithoutError("+123456789123456789", 123456789123456789L);
        constructWithoutError("-123456789123456789", -123456789123456789L);
        constructWithoutError(Integer.toString(Integer.MIN_VALUE),
                              (long)Integer.MIN_VALUE);
        constructWithoutError(Integer.toString(Integer.MAX_VALUE),
                              (long)Integer.MAX_VALUE);
        constructWithoutError(Long.toString(Long.MIN_VALUE),
                              Long.MIN_VALUE);
        constructWithoutError(Long.toString(Long.MAX_VALUE),
                              Long.MAX_VALUE);
        constructWithError("");
        constructWithError("-");
        constructWithError("+");
        constructWithError("--");
        constructWithError("++");
        constructWithError("-000-0");
        constructWithError("+000+0");
        constructWithError("+000-0");
        constructWithError("--1234567890");
        constructWithError("++1234567890");
        constructWithError("-0-12345678");
        constructWithError("+0+12345678");
        constructWithError("--12345678-12345678-12345678");
        constructWithError("++12345678+12345678+12345678");
        constructWithError("12345-");
        constructWithError("12345+");
    }
    private static void constructWithError(String badString) {
        try {
            BigInteger bi = new BigInteger(badString);
            throw new RuntimeException(badString + " accepted");
        } catch(NumberFormatException e) {
        }
    }
    private static void constructWithoutError(String goodString, long value) {
            BigInteger bi = new BigInteger(goodString);
            if(bi.longValue() != value) {
                System.err.printf("From ``%s'' expected %d, got %s.\n", goodString, value, bi);
                throw new RuntimeException();
            }
    }
}
