public class PowTests {
    private PowTests(){}
    static final double infinityD = Double.POSITIVE_INFINITY;
    static int testPowCase(double input1, double input2, double expected) {
        int failures = 0;
        failures += Tests.test("StrictMath.pow(double, double)", input1, input2,
                               StrictMath.pow(input1, input2), expected);
        failures += Tests.test("Math.pow(double, double)", input1, input2,
                               Math.pow(input1, input2), expected);
        return failures;
    }
    static int testStrictPowCase(double input1, double input2, double expected) {
        int failures = 0;
        failures += Tests.test("StrictMath.pow(double, double)", input1, input2,
                               StrictMath.pow(input1, input2), expected);
        return failures;
    }
    static int testNonstrictPowCase(double input1, double input2, double expected) {
        int failures = 0;
        failures += Tests.test("Math.pow(double, double)", input1, input2,
                               Math.pow(input1, input2), expected);
        return failures;
    }
    static int testPow() {
        int failures = 0;
        double [][] testCases = {
            {-0.0,               3.0,   -0.0},
            {-0.0,               4.0,    0.0},
            {-infinityD,        -3.0,   -0.0},
            {-infinityD,        -4.0,    0.0},
        };
        for (double[] testCase : testCases) {
            failures+=testPowCase(testCase[0], testCase[1], testCase[2]);
        }
        return failures;
    }
    static int testCrossProduct() {
        int failures = 0;
        double testData[] = {
                                Double.NEGATIVE_INFINITY,
                     -Double.MAX_VALUE,
                            (double)Long.MIN_VALUE,
                            (double) -((1L<<53)+2L),
                            (double) -((1L<<53)),
                            (double) -((1L<<53)-1L),
                            -((double)Integer.MAX_VALUE + 4.0),
                            (double)Integer.MIN_VALUE - 1.0,
                            (double)Integer.MIN_VALUE,
                            (double)Integer.MIN_VALUE + 1.0,
                            -Math.PI,
                            -3.0,
                            -Math.E,
                            -2.0,
                            -1.0000000000000004,
                    -1.0000000000000002, 
                                -1.0,
                    -0.9999999999999999, 
                    -0.9999999999999998,
                            -0.5,
                            -1.0/3.0,
                     -Double.MIN_VALUE,
                                -0.0,
                                +0.0,
                     +Double.MIN_VALUE,
                            +1.0/3.0,
                            +0.5,
                            +0.9999999999999998,
                    +0.9999999999999999, 
                                +1.0,
                     +1.0000000000000002, 
                            +1.0000000000000004,
                            +2.0,
                            +Math.E,
                            +3.0,
                            +Math.PI,
                            -(double)Integer.MIN_VALUE - 1.0,
                            -(double)Integer.MIN_VALUE,
                            -(double)Integer.MIN_VALUE + 1.0,
                            (double)Integer.MAX_VALUE + 4.0,
                            (double) ((1L<<53)-1L),
                            (double) ((1L<<53)),
                            (double) ((1L<<53)+2L),
                            -(double)Long.MIN_VALUE,
                      Double.MAX_VALUE,
                                Double.POSITIVE_INFINITY,
                                Double.NaN
    };
        double NaN = Double.NaN;
        for(double x: testData) {
            for(double y: testData) {
                boolean testPass = false;
                double expected=NaN;
                double actual;
                if( Double.isNaN(y)) {
                    expected = NaN;
                } else if (y == 0.0) {
                    expected = 1.0;
                } else if (Double.isInfinite(y) ) {
                    if(y > 0) { 
                        if (Math.abs(x) > 1.0) {
                            expected = Double.POSITIVE_INFINITY;
                        } else if (Math.abs(x) == 1.0) {
                            expected = NaN;
                        } else if (Math.abs(x) < 1.0) {
                            expected = +0.0;
                        } else { 
                            assert Double.isNaN(x);
                            expected = NaN;
                        }
                    } else { 
                        if (Math.abs(x) > 1.0) {
                            expected = +0.0;
                        } else if (Math.abs(x) == 1.0) {
                            expected = NaN;
                        } else if (Math.abs(x) < 1.0) {
                            expected = Double.POSITIVE_INFINITY;
                        } else { 
                            assert Double.isNaN(x);
                            expected = NaN;
                        }
                    } 
                } else if (y == 1.0) {
                    expected = x;
                } else if (Double.isNaN(x)) { 
                    assert y != 0.0;
                    expected = NaN;
                } else if (x == Double.NEGATIVE_INFINITY) {
                    expected = (y < 0.0) ? f2(y) :f1(y);
                } else if (x == Double.POSITIVE_INFINITY) {
                    expected = (y < 0.0) ? +0.0 : Double.POSITIVE_INFINITY;
                } else if (equivalent(x, +0.0)) {
                    assert y != 0.0;
                    expected = (y < 0.0) ? Double.POSITIVE_INFINITY: +0.0;
                } else if (equivalent(x, -0.0)) {
                    assert y != 0.0;
                    expected = (y < 0.0) ? f1(y): f2(y);
                } else if( x < 0.0) {
                    assert y != 0.0;
                    failures += testStrictPowCase(x, y, f3(x, y));
                    failures += testNonstrictPowCase(x, y, f3ns(x, y));
                    continue;
                } else {
                    expected = NaN;
                    continue;
                }
                failures += testPowCase(x, y, expected);
            } 
        } 
        return failures;
    }
    static boolean equivalent(double a, double b) {
        return Double.compare(a, b) == 0;
    }
    static double f1(double y) {
        return (intClassify(y) == 1)?
            Double.NEGATIVE_INFINITY:
            Double.POSITIVE_INFINITY;
    }
    static double f2(double y) {
        return (intClassify(y) == 1)?-0.0:0.0;
    }
    static double f3(double x, double y) {
        switch( intClassify(y) ) {
        case 0:
            return StrictMath.pow(Math.abs(x), y);
        case 1:
            return -StrictMath.pow(Math.abs(x), y);
        case -1:
            return Double.NaN;
        default:
            throw new AssertionError("Bad classification.");
        }
    }
    static double f3ns(double x, double y) {
        switch( intClassify(y) ) {
        case 0:
            return Math.pow(Math.abs(x), y);
        case 1:
            return -Math.pow(Math.abs(x), y);
        case -1:
            return Double.NaN;
        default:
            throw new AssertionError("Bad classification.");
        }
    }
    static boolean isFinite(double a) {
        return (0.0*a  == 0);
    }
    static int intClassify(double a) {
        if(!isFinite(a) || 
           (a != Math.floor(a) )) { 
                return -1;
        }
        else {
            a = StrictMath.abs(a); 
            if(a+1.0 == a) { 
                return 0; 
            }
            else { 
                long ell = (long)  a;
                return ((ell & 0x1L) == (long)1)?1:0;
            }
        }
    }
    public static void main(String [] argv) {
        int failures = 0;
        failures += testPow();
        failures += testCrossProduct();
        if (failures > 0) {
            System.err.println("Testing pow incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
}
