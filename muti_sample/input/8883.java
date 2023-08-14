public class NegateTests {
    static BigDecimal negateThenRound(BigDecimal bd, MathContext mc) {
        return bd.negate().plus(mc);
    }
    static BigDecimal absThenRound(BigDecimal bd, MathContext mc) {
        return bd.abs().plus(mc);
    }
    static int negateTest(BigDecimal[][] testCases,  MathContext mc) {
        int failures = 0;
        for (BigDecimal [] testCase : testCases) {
            BigDecimal bd = testCase[0];
            BigDecimal neg1 = bd.negate(mc);
            BigDecimal neg2 = negateThenRound(bd, mc);
            BigDecimal expected = testCase[1];
            if (! neg1.equals(expected) ) {
                failures++;
                System.err.println("(" + bd + ").negate(" + mc + ") => " +
                                   neg1 + " != expected " + expected);
            }
            if (! neg1.equals(neg2) ) {
                failures++;
                System.err.println("(" + bd + ").negate(" + mc + ")  => " +
                                   neg1 + " != ntr " + neg2);
            }
            BigDecimal abs = bd.abs(mc);
            BigDecimal expectedAbs = absThenRound(bd,mc);
            if (! abs.equals(expectedAbs) ) {
                failures++;
                System.err.println("(" + bd + ").abs(" + mc + ")  => " +
                                   abs + " != atr " +  expectedAbs);
            }
        }
        return failures;
    }
    static int negateTests() {
        int failures = 0;
        BigDecimal [][] testCasesCeiling = {
            {new BigDecimal("1.3"),     new BigDecimal("-1")},
            {new BigDecimal("-1.3"),    new BigDecimal("2")},
        };
        failures += negateTest(testCasesCeiling,
                               new MathContext(1, RoundingMode.CEILING));
        BigDecimal [][] testCasesFloor = {
            {new BigDecimal("1.3"),     new BigDecimal("-2")},
            {new BigDecimal("-1.3"),    new BigDecimal("1")},
        };
        failures += negateTest(testCasesFloor,
                               new MathContext(1, RoundingMode.FLOOR));
        return failures;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += negateTests();
        if (failures > 0 )
            throw new RuntimeException("Incurred " + failures + " failures" +
                                       " testing the negate and/or abs.");
    }
}
