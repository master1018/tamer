public class RoundTests {
    public static void main(String... args) {
        int failures = 0;
        failures += testNearFloatHalfCases();
        failures += testNearDoubleHalfCases();
        if (failures > 0) {
            System.err.println("Testing {Math, StrictMath}.round incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
    private static int testNearDoubleHalfCases() {
        int failures = 0;
        double [][] testCases = {
            {+0x1.fffffffffffffp-2,  0.0},
            {+0x1.0p-1,              1.0}, 
            {+0x1.0000000000001p-1,  1.0},
            {-0x1.fffffffffffffp-2,  0.0},
            {-0x1.0p-1,              0.0}, 
            {-0x1.0000000000001p-1, -1.0},
        };
        for(double[] testCase : testCases) {
            failures += testNearHalfCases(testCase[0], (long)testCase[1]);
        }
        return failures;
    }
    private static int testNearHalfCases(double input, double expected) {
        int failures = 0;
        failures += Tests.test("Math.round",        input, Math.round(input),       expected);
        failures += Tests.test("StrictMath.round",  input, StrictMath.round(input), expected);
        return failures;
    }
    private static int testNearFloatHalfCases() {
        int failures = 0;
        float [][] testCases = {
            {+0x1.fffffep-2f,  0.0f},
            {+0x1.0p-1f,       1.0f}, 
            {+0x1.000002p-1f,  1.0f},
            {-0x1.fffffep-2f,  0.0f},
            {-0x1.0p-1f,       0.0f}, 
            {-0x1.000002p-1f, -1.0f},
        };
        for(float[] testCase : testCases) {
            failures += testNearHalfCases(testCase[0], (int)testCase[1]);
        }
        return failures;
    }
    private static int testNearHalfCases(float input, float expected) {
        int failures = 0;
        failures += Tests.test("Math.round",        input, Math.round(input),       expected);
        failures += Tests.test("StrictMath.round",  input, StrictMath.round(input), expected);
        return failures;
    }
}
