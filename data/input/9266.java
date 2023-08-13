public class MultiplyTests {
    private static int multiplyTests() {
        int failures = 0;
        BigDecimal[] bd1 = {
            new BigDecimal("123456789"),
            new BigDecimal("1234567898"),
            new BigDecimal("12345678987")
        };
        BigDecimal[] bd2 = {
            new BigDecimal("987654321"),
            new BigDecimal("8987654321"),
            new BigDecimal("78987654321")
        };
        BigDecimal[][] expectedResults = {
            {new BigDecimal("121932631112635269"),
             new BigDecimal("1109586943112635269"),
             new BigDecimal("9751562173112635269")
            },
            { new BigDecimal("1219326319027587258"),
              new BigDecimal("11095869503027587258"),
              new BigDecimal("97515622363027587258")
            },
            { new BigDecimal("12193263197189452827"),
              new BigDecimal("110958695093189452827"),
              new BigDecimal("975156224183189452827")
            }
        };
        for (int i = 0; i < bd1.length; i++) {
            for (int j = 0; j < bd2.length; j++) {
                if (!bd1[i].multiply(bd2[j]).equals(expectedResults[i][j])) {
                    failures++;
                }
            }
        }
        BigDecimal x = BigDecimal.valueOf(8L, 1);
        BigDecimal xPower = BigDecimal.valueOf(-1L);
        try {
            for (int i = 0; i < 100; i++) {
                xPower = xPower.multiply(x);
            }
        } catch (Exception ex) {
            failures++;
        }
        return failures;
    }
    public static void main(String[] args) {
        int failures = 0;
        failures += multiplyTests();
        if (failures > 0) {
            throw new RuntimeException("Incurred " + failures +
                                       " failures while testing multiply.");
        }
    }
}
