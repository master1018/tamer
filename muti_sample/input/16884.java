public class RoundingModeTests {
    public static void main(String [] argv) {
        for(RoundingMode rm: RoundingMode.values()) {
            if (rm != RoundingMode.valueOf(rm.toString())) {
                throw new RuntimeException("Bad roundtrip conversion of " +
                                           rm.toString());
            }
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_CEILING) !=
                RoundingMode.CEILING) {
                throw new RuntimeException("Bad mapping for ROUND_CEILING");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_DOWN) !=
                RoundingMode.DOWN) {
                throw new RuntimeException("Bad mapping for ROUND_DOWN");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_FLOOR) !=
                RoundingMode.FLOOR) {
                throw new RuntimeException("Bad mapping for ROUND_FLOOR");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_DOWN) !=
                RoundingMode.HALF_DOWN) {
                throw new RuntimeException("Bad mapping for ROUND_HALF_DOWN");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_EVEN) !=
                RoundingMode.HALF_EVEN) {
                throw new RuntimeException("Bad mapping for ROUND_HALF_EVEN");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP) !=
                RoundingMode.HALF_UP) {
                throw new RuntimeException("Bad mapping for ROUND_HALF_UP");
        }
        if (RoundingMode.valueOf(BigDecimal.ROUND_UNNECESSARY) !=
                RoundingMode.UNNECESSARY) {
                throw new RuntimeException("Bad mapping for ROUND_UNNECESARY");
        }
    }
}
