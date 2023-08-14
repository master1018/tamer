public class CompareToTests {
    private static int compareToTests() {
        int failures = 0;
        final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
        BigDecimal [][] testCases = {
            {valueOf(0),        valueOf(0),     ZERO},
            {valueOf(0),        valueOf(1),     MINUS_ONE},
            {valueOf(1),        valueOf(2),     MINUS_ONE},
            {valueOf(2),        valueOf(1),     ONE},
            {valueOf(10),       valueOf(10),    ZERO},
            {valueOf(2,1),      valueOf(2),     MINUS_ONE},
            {valueOf(2,-1),     valueOf(2),     ONE},
            {valueOf(1,1),      valueOf(2),     MINUS_ONE},
            {valueOf(1,-1),     valueOf(2),     ONE},
            {valueOf(5,-1),     valueOf(2),     ONE},
            {valueOf(Long.MAX_VALUE),   valueOf(Long.MAX_VALUE),        ZERO},
            {valueOf(Long.MAX_VALUE-1), valueOf(Long.MAX_VALUE),        MINUS_ONE},
            {valueOf(Long.MIN_VALUE),   valueOf(Long.MAX_VALUE),        MINUS_ONE},
            {valueOf(Long.MIN_VALUE+1), valueOf(Long.MAX_VALUE),        MINUS_ONE},
            {valueOf(Long.MIN_VALUE),   valueOf(Long.MIN_VALUE),        ZERO},
            {valueOf(Long.MIN_VALUE+1), valueOf(Long.MAX_VALUE),        ONE},
        };
        for (BigDecimal[] testCase : testCases) {
            BigDecimal a = testCase[0];
            BigDecimal a_negate = a.negate();
            BigDecimal b = testCase[1];
            BigDecimal b_negate = b.negate();
            int expected = testCase[2].intValue();
            failures += compareToTest(a,        b,         expected);
            failures += compareToTest(a_negate, b,        -1);
            failures += compareToTest(a,        b_negate,  1);
            failures += compareToTest(a_negate, b_negate, -expected);
        }
        return failures;
    }
    private static int compareToTest(BigDecimal a, BigDecimal b, int expected) {
        int result = a.compareTo(b);
        int failed = (result==expected) ? 0 : 1;
        if (result == 1) {
            System.err.println("(" + a + ").compareTo(" + b + ") => " + result +
                               "\n\tExpected " + expected);
        }
        return result;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += compareToTests();
        if (failures > 0) {
            throw new RuntimeException("Incurred " + failures +
                                       " failures while testing exact compareTo.");
        }
    }
}
