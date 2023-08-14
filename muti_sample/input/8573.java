public class LongValueExactTests {
    private static int longValueExactTests() {
        int failures = 0;
        String[] testStrings = {
            "9223372036854775807",
            "9223372036854775807.0",
            "9223372036854775807.00",
            "-9223372036854775808",
            "-9223372036854775808.0",
            "-9223372036854775808.00",
        };
        for (String longValue : testStrings) {
            try {
                BigDecimal bd = new BigDecimal(longValue);
                long longValueExact = bd.longValueExact();
            } catch (Exception e) {
                failures++;
            }
        }
        String[] testStrings2 = {
            "9223372036854775808",
            "9223372036854775808.0",
            "9223372036854775808.00",
            "-9223372036854775809",
            "-9223372036854775808.1",
            "-9223372036854775808.01",
        };
        for (String bigValue : testStrings2) {
            try {
                BigDecimal bd = new BigDecimal(bigValue);
                long longValueExact = bd.longValueExact();
                failures++;
            } catch (ArithmeticException e) {
            }
        }
        return failures;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += longValueExactTests();
        if (failures > 0) {
            throw new RuntimeException("Incurred " + failures +
                                       " failures while testing longValueExact.");
        }
    }
}
