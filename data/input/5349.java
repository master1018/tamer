public class Atan2Tests {
    private Atan2Tests(){}
    static int testAtan2Case(double input1, double input2, double expected) {
        int failures = 0;
        failures += Tests.test("StrictMath.atan2(double, double)", input1, input2,
                               StrictMath.atan2(input1, input2), expected);
        failures += Tests.test("Math.atan2(double, double)", input1, input2,
                               Math.atan2(input1, input2), expected);
        return failures;
    }
    static int testAtan2() {
        int failures = 0;
        double [][] testCases = {
            {-3.0,      Double.POSITIVE_INFINITY,       -0.0},
        };
        for (double[] testCase : testCases) {
            failures+=testAtan2Case(testCase[0], testCase[1], testCase[2]);
        }
        return failures;
    }
    public static void main(String [] argv) {
        int failures = 0;
        failures += testAtan2();
        if (failures > 0) {
            System.err.println("Testing atan2 incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
}
