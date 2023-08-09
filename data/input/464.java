public class Rint {
    static int testRintCase(double input, double expected) {
        int failures = 0;
        double result;
        failures += Tests.test("Math.rint",  input, Math.rint(input),   expected);
        failures += Tests.test("Math.rint", -input, Math.rint(-input), -expected);
        failures += Tests.test("StrictMath.rint",
                               input, StrictMath.rint(input),   expected);
        failures += Tests.test("StrictMath.rint", -input,
                               StrictMath.rint(-input), -expected);
        return failures;
    }
    public static void main(String args[]) {
        int failures = 0;
        double twoToThe52 = FpUtils.scalb(1.0, 52); 
        double [][] testCases = {
            {0.0,                               0.0},
            {Double.MIN_VALUE,                  0.0},
            {FpUtils.nextDown(DoubleConsts.MIN_NORMAL), 0.0},
            {DoubleConsts.MIN_NORMAL,           0.0},
            {0.2,                               0.0},
            {FpUtils.nextDown(0.5),             0.0},
            {                 0.5,              0.0},
            {  FpUtils.nextUp(0.5),             1.0},
            {0.7,                               1.0},
            {FpUtils.nextDown(1.0),             1.0},
            {                 1.0,              1.0},
            {  FpUtils.nextUp(1.0),             1.0},
            {FpUtils.nextDown(1.5),             1.0},
            {                 1.5,              2.0},
            {  FpUtils.nextUp(1.5),             2.0},
            {4.2,                               4.0},
            {4.5,                               4.0},
            {4.7,                               5.0},
            {7.5,                               8.0},
            {7.2,                               7.0},
            {7.7,                               8.0},
            {150000.75,                         150001.0},
            {300000.5,                          300000.0},
            {FpUtils.nextUp(300000.5),          300001.0},
            {FpUtils.nextDown(300000.75),       300001.0},
            {300000.75,                         300001.0},
            {FpUtils.nextUp(300000.75),         300001.0},
            {300000.99,                         300001.0},
            {262144.75,                         262145.0}, 
            {499998.75,                         499999.0},
            {524287.75,                         524288.0}, 
            {524288.75,                         524289.0},
            {FpUtils.nextDown(twoToThe52),      twoToThe52},
            {twoToThe52,                        twoToThe52},
            {FpUtils.nextUp(twoToThe52),        FpUtils.nextUp(twoToThe52)},
            {Double.MAX_VALUE,          Double.MAX_VALUE},
            {Double.POSITIVE_INFINITY,  Double.POSITIVE_INFINITY},
            {Double.NaN,                        Double.NaN}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures += testRintCase(testCases[i][0], testCases[i][1]);
        }
        for(double d = Double.MIN_VALUE;
            d < Double.POSITIVE_INFINITY; d *= 2) {
            failures += testRintCase(d, ((d<=0.5)?0.0:d));
        }
        if (failures > 0) {
            System.err.println("Testing {Math, StrictMath}.rint incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
}
