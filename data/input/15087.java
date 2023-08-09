public class RoundingTests {
    private static int roundingTests() {
        int failures = 0;
        BigDecimal bd1 = BigDecimal.valueOf(11, Integer.MIN_VALUE);
        BigDecimal bd2 = null;
        MathContext mc = new MathContext(1);
        try {
                bd2 = bd1.round(mc); 
                failures++;
                System.err.printf("Did not get expected overflow rounding %s to %d digits, got %s%n",
                                   bd1, mc.getPrecision(), bd2);
        } catch(ArithmeticException e) {
            ; 
        }
        return failures;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += roundingTests();
        if (failures > 0) {
            System.err.println("Encountered " + failures +
                               " failures while testing rounding.");
            throw new RuntimeException();
        }
    }
}
