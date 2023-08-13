public class StrippingZerosTest {
    public static void main(String argv[]) {
        BigDecimal [][] testCases = {
            {new BigDecimal("1.00000"),         new BigDecimal("1")},
            {new BigDecimal("1.000"),           new BigDecimal("1")},
            {new BigDecimal("1"),               new BigDecimal("1")},
            {new BigDecimal("0.1234"),          new BigDecimal("0.1234")},
            {new BigDecimal("0.12340"),         new BigDecimal("0.1234")},
            {new BigDecimal("0.12340000000"),   new BigDecimal("0.1234")},
            {new BigDecimal("1234.5678"),       new BigDecimal("1234.5678")},
            {new BigDecimal("1234.56780"),      new BigDecimal("1234.5678")},
            {new BigDecimal("1234.567800000"),  new BigDecimal("1234.5678")},
            {new BigDecimal("0"),               new BigDecimal("0")},
            {new BigDecimal("0e100"),           new BigDecimal("0e100")},
            {new BigDecimal("0e-100"),          new BigDecimal("0e-100")},
            {new BigDecimal("10"),              new BigDecimal("1e1")},
            {new BigDecimal("20"),              new BigDecimal("2e1")},
            {new BigDecimal("100"),             new BigDecimal("1e2")},
            {new BigDecimal("1000000000"),      new BigDecimal("1e9")},
            {new BigDecimal("100000000e1"),     new BigDecimal("1e9")},
            {new BigDecimal("10000000e2"),      new BigDecimal("1e9")},
            {new BigDecimal("1000000e3"),       new BigDecimal("1e9")},
            {new BigDecimal("100000e4"),        new BigDecimal("1e9")},
            {new BigDecimal("1.0000000000000000000000000000"),    new BigDecimal("1")},
            {new BigDecimal("-1.0000000000000000000000000000"),   new BigDecimal("-1")},
            {new BigDecimal("1.00000000000000000000000000001"),   new BigDecimal("1.00000000000000000000000000001")},
            {new BigDecimal("1000000000000000000000000000000e4"), new BigDecimal("1e34")},
        };
        for(int i = 0; i < testCases.length; i++) {
            if (!(testCases[i][0]).stripTrailingZeros().equals(testCases[i][1])) {
                throw new RuntimeException("For input " + testCases[i][0].toString() +
                                           " did not received expected result " +
                                           testCases[i][1].toString() + ",  got " +
                                           testCases[i][0].stripTrailingZeros());
            }
            testCases[i][0] = testCases[i][0].negate();
            testCases[i][1] = testCases[i][1].negate();
            if (!(testCases[i][0]).stripTrailingZeros().equals(testCases[i][1])) {
                throw new RuntimeException("For input " + testCases[i][0].toString() +
                                           " did not received expected result " +
                                           testCases[i][1].toString() + ",  got " +
                                           testCases[i][0].stripTrailingZeros());
            }
        }
    }
}
