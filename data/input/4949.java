public class EqualsTests {
    public static void main(String argv[]) {
        int failures = 0;
        BigDecimal[][] testValues = {
            {ZERO, ZERO},
            {ONE, TEN},
            {valueOf(Integer.MAX_VALUE), valueOf(Integer.MAX_VALUE)},
            {valueOf(Long.MAX_VALUE), valueOf(-Long.MAX_VALUE)},
            {valueOf(12345678), valueOf(12345678)},
            {valueOf(123456789), valueOf(123456788)},
            {new BigDecimal("123456789123456789123"),
             new BigDecimal(new BigInteger("123456789123456789123"))},
            {new BigDecimal("123456789123456789123"),
             new BigDecimal(new BigInteger("123456789123456789124"))},
            {valueOf(Long.MIN_VALUE), new BigDecimal("-9223372036854775808")},
            {new BigDecimal("9223372036854775808"), valueOf(Long.MAX_VALUE)},
            {valueOf(Math.round(Math.pow(2, 10))), new BigDecimal("1024")},
            {new BigDecimal("1020"), valueOf(Math.pow(2, 11))},
            {new BigDecimal(BigInteger.valueOf(2).pow(65)),
             new BigDecimal("36893488147419103232")},
            {new BigDecimal("36893488147419103231.81"),
             new BigDecimal("36893488147419103231.811"),
            }
        };
        boolean expected = Boolean.TRUE;
        for (BigDecimal[] testValuePair : testValues) {
            failures += equalsTest(testValuePair[0], testValuePair[1], expected);
            expected = !expected;
        }
        if (failures > 0) {
            throw new RuntimeException("Inccured " + failures +
                                       " failures while testing equals.");
        }
    }
    private static int equalsTest(BigDecimal l, BigDecimal r, boolean expected) {
        boolean result = l.equals(r);
        int failed = (result == expected) ? 0 : 1;
        if (failed == 1) {
            System.err.println(l + " .equals(" + r + ") => " + result +
                               "\n\tExpected " + expected);
        }
        return failed;
    }
}
