public class IeeeRecommendedTests {
    private IeeeRecommendedTests(){}
    static final float  NaNf = Float.NaN;
    static final double NaNd = Double.NaN;
    static final float  infinityF = Float.POSITIVE_INFINITY;
    static final double infinityD = Double.POSITIVE_INFINITY;
    static final float  Float_MAX_VALUEmm       = 0x1.fffffcP+127f;
    static final float  Float_MAX_SUBNORMAL     = 0x0.fffffeP-126f;
    static final float  Float_MAX_SUBNORMALmm   = 0x0.fffffcP-126f;
    static final double Double_MAX_VALUEmm      = 0x1.ffffffffffffeP+1023;
    static final double Double_MAX_SUBNORMAL    = 0x0.fffffffffffffP-1022;
    static final double Double_MAX_SUBNORMALmm  = 0x0.ffffffffffffeP-1022;
    static java.util.Random rand = new java.util.Random();
    static double powerOfTwoD(int n) {
        return Double.longBitsToDouble((((long)n + (long)DoubleConsts.MAX_EXPONENT) <<
                                        (DoubleConsts.SIGNIFICAND_WIDTH-1))
                                       & DoubleConsts.EXP_BIT_MASK);
    }
    static float powerOfTwoF(int n) {
        return Float.intBitsToFloat(((n + FloatConsts.MAX_EXPONENT) <<
                                     (FloatConsts.SIGNIFICAND_WIDTH-1))
                                    & FloatConsts.EXP_BIT_MASK);
    }
    static int testGetExponentCase(float f, int expected) {
        float minus_f = -f;
        int failures=0;
        failures+=Tests.test("Math.getExponent(float)", f,
                             Math.getExponent(f), expected);
        failures+=Tests.test("Math.getExponent(float)", minus_f,
                             Math.getExponent(minus_f), expected);
        failures+=Tests.test("StrictMath.getExponent(float)", f,
                             StrictMath.getExponent(f), expected);
        failures+=Tests.test("StrictMath.getExponent(float)", minus_f,
                             StrictMath.getExponent(minus_f), expected);
        return failures;
    }
    static int testGetExponentCase(double d, int expected) {
        double minus_d = -d;
        int failures=0;
        failures+=Tests.test("Math.getExponent(double)", d,
                             Math.getExponent(d), expected);
        failures+=Tests.test("Math.getExponent(double)", minus_d,
                             Math.getExponent(minus_d), expected);
        failures+=Tests.test("StrictMath.getExponent(double)", d,
                             StrictMath.getExponent(d), expected);
        failures+=Tests.test("StrictMath.getExponent(double)", minus_d,
                             StrictMath.getExponent(minus_d), expected);
        return failures;
    }
    public static int testFloatGetExponent() {
        int failures = 0;
        float [] specialValues = {NaNf,
                                   Float.POSITIVE_INFINITY,
                                   +0.0f,
                                  +1.0f,
                                  +2.0f,
                                  +16.0f,
                                  +Float.MIN_VALUE,
                                  +Float_MAX_SUBNORMAL,
                                  +FloatConsts.MIN_NORMAL,
                                  +Float.MAX_VALUE
        };
        int [] specialResults = {Float.MAX_EXPONENT + 1, 
                                 Float.MAX_EXPONENT + 1, 
                                 Float.MIN_EXPONENT - 1, 
                                 0,
                                 1,
                                 4,
                                 FloatConsts.MIN_EXPONENT - 1,
                                 -FloatConsts.MAX_EXPONENT,
                                 FloatConsts.MIN_EXPONENT,
                                 FloatConsts.MAX_EXPONENT
        };
        for(int i = 0; i < specialValues.length; i++) {
            failures += testGetExponentCase(specialValues[i], specialResults[i]);
        }
        for(int i = FloatConsts.MIN_EXPONENT; i <= FloatConsts.MAX_EXPONENT; i++) {
            int result;
            float po2 = powerOfTwoF(i);
            failures += testGetExponentCase(po2, i);
            for(int j = 0; j < 10; j++) {
                int randSignif = rand.nextInt();
                float randFloat;
                randFloat = Float.intBitsToFloat( 
                                                 (Float.floatToIntBits(po2)&
                                                  (~FloatConsts.SIGNIF_BIT_MASK)) |
                                                 (randSignif &
                                                  FloatConsts.SIGNIF_BIT_MASK) );
                failures += testGetExponentCase(randFloat, i);
            }
            if (i > FloatConsts.MIN_EXPONENT) {
                float po2minus = FpUtils.nextAfter(po2,
                                                 Float.NEGATIVE_INFINITY);
                failures += testGetExponentCase(po2minus, i-1);
            }
        }
        float top=Float.MIN_VALUE;
        for( int i = 1;
            i < FloatConsts.SIGNIFICAND_WIDTH;
            i++, top *= 2.0f) {
            failures += testGetExponentCase(top,
                                            FloatConsts.MIN_EXPONENT - 1);
            if (i >= 3) {
                testGetExponentCase(FpUtils.nextAfter(top, 0.0f),
                                    FloatConsts.MIN_EXPONENT - 1);
                if( i >= 10) {
                    int mask = ~((~0)<<(i-1));
                    float randFloat = Float.intBitsToFloat( 
                                                 Float.floatToIntBits(top) |
                                                 (rand.nextInt() & mask ) ) ;
                    failures += testGetExponentCase(randFloat,
                                                    FloatConsts.MIN_EXPONENT - 1);
                }
            }
        }
        return failures;
    }
    public static int testDoubleGetExponent() {
        int failures = 0;
        double [] specialValues = {NaNd,
                                   infinityD,
                                   +0.0,
                                   +1.0,
                                   +2.0,
                                   +16.0,
                                   +Double.MIN_VALUE,
                                   +Double_MAX_SUBNORMAL,
                                   +DoubleConsts.MIN_NORMAL,
                                   +Double.MAX_VALUE
        };
        int [] specialResults = {Double.MAX_EXPONENT + 1, 
                                 Double.MAX_EXPONENT + 1, 
                                 Double.MIN_EXPONENT - 1, 
                                 0,
                                 1,
                                 4,
                                 DoubleConsts.MIN_EXPONENT - 1,
                                 -DoubleConsts.MAX_EXPONENT,
                                 DoubleConsts.MIN_EXPONENT,
                                 DoubleConsts.MAX_EXPONENT
        };
        for(int i = 0; i < specialValues.length; i++) {
            failures += testGetExponentCase(specialValues[i], specialResults[i]);
        }
        for(int i = DoubleConsts.MIN_EXPONENT; i <= DoubleConsts.MAX_EXPONENT; i++) {
            int result;
            double po2 = powerOfTwoD(i);
            failures += testGetExponentCase(po2, i);
            for(int j = 0; j < 10; j++) {
                long randSignif = rand.nextLong();
                double randFloat;
                randFloat = Double.longBitsToDouble( 
                                                 (Double.doubleToLongBits(po2)&
                                                  (~DoubleConsts.SIGNIF_BIT_MASK)) |
                                                 (randSignif &
                                                  DoubleConsts.SIGNIF_BIT_MASK) );
                failures += testGetExponentCase(randFloat, i);
            }
            if (i > DoubleConsts.MIN_EXPONENT) {
                double po2minus = FpUtils.nextAfter(po2,
                                                    Double.NEGATIVE_INFINITY);
                failures += testGetExponentCase(po2minus, i-1);
            }
        }
        double top=Double.MIN_VALUE;
        for( int i = 1;
            i < DoubleConsts.SIGNIFICAND_WIDTH;
            i++, top *= 2.0f) {
            failures += testGetExponentCase(top,
                                            DoubleConsts.MIN_EXPONENT - 1);
            if (i >= 3) {
                testGetExponentCase(FpUtils.nextAfter(top, 0.0),
                                    DoubleConsts.MIN_EXPONENT - 1);
                if( i >= 10) {
                    long mask = ~((~0L)<<(i-1));
                    double randFloat = Double.longBitsToDouble( 
                                                 Double.doubleToLongBits(top) |
                                                 (rand.nextLong() & mask ) ) ;
                    failures += testGetExponentCase(randFloat,
                                                    DoubleConsts.MIN_EXPONENT - 1);
                }
            }
        }
        return failures;
    }
    static int testNextAfterCase(float start, double direction, float expected) {
        int failures=0;
        float minus_start = -start;
        double minus_direction = -direction;
        float minus_expected = -expected;
        failures+=Tests.test("Math.nextAfter(float,double)", start, direction,
                             Math.nextAfter(start, direction), expected);
        failures+=Tests.test("Math.nextAfter(float,double)", minus_start, minus_direction,
                             Math.nextAfter(minus_start, minus_direction), minus_expected);
        failures+=Tests.test("StrictMath.nextAfter(float,double)", start, direction,
                             StrictMath.nextAfter(start, direction), expected);
        failures+=Tests.test("StrictMath.nextAfter(float,double)", minus_start, minus_direction,
                             StrictMath.nextAfter(minus_start, minus_direction), minus_expected);
        return failures;
    }
    static int testNextAfterCase(double start, double direction, double expected) {
        int failures=0;
        double minus_start = -start;
        double minus_direction = -direction;
        double minus_expected = -expected;
        failures+=Tests.test("Math.nextAfter(double,double)", start, direction,
                             Math.nextAfter(start, direction), expected);
        failures+=Tests.test("Math.nextAfter(double,double)", minus_start, minus_direction,
                             Math.nextAfter(minus_start, minus_direction), minus_expected);
        failures+=Tests.test("StrictMath.nextAfter(double,double)", start, direction,
                             StrictMath.nextAfter(start, direction), expected);
        failures+=Tests.test("StrictMath.nextAfter(double,double)", minus_start, minus_direction,
                             StrictMath.nextAfter(minus_start, minus_direction), minus_expected);
        return failures;
    }
    public static int testFloatNextAfter() {
        int failures=0;
        float [][] testCases  = {
            {NaNf,              NaNf,                   NaNf},
            {NaNf,              0.0f,                   NaNf},
            {0.0f,              NaNf,                   NaNf},
            {NaNf,              infinityF,              NaNf},
            {infinityF,         NaNf,                   NaNf},
            {infinityF,         infinityF,              infinityF},
            {infinityF,         -infinityF,             Float.MAX_VALUE},
            {infinityF,         0.0f,                   Float.MAX_VALUE},
            {Float.MAX_VALUE,   infinityF,              infinityF},
            {Float.MAX_VALUE,   -infinityF,             Float_MAX_VALUEmm},
            {Float.MAX_VALUE,   Float.MAX_VALUE,        Float.MAX_VALUE},
            {Float.MAX_VALUE,   0.0f,                   Float_MAX_VALUEmm},
            {Float_MAX_VALUEmm, Float.MAX_VALUE,        Float.MAX_VALUE},
            {Float_MAX_VALUEmm, infinityF,              Float.MAX_VALUE},
            {Float_MAX_VALUEmm, Float_MAX_VALUEmm,      Float_MAX_VALUEmm},
            {FloatConsts.MIN_NORMAL,    infinityF,              FloatConsts.MIN_NORMAL+
                                                                Float.MIN_VALUE},
            {FloatConsts.MIN_NORMAL,    -infinityF,             Float_MAX_SUBNORMAL},
            {FloatConsts.MIN_NORMAL,    1.0f,                   FloatConsts.MIN_NORMAL+
                                                                Float.MIN_VALUE},
            {FloatConsts.MIN_NORMAL,    -1.0f,                  Float_MAX_SUBNORMAL},
            {FloatConsts.MIN_NORMAL,    FloatConsts.MIN_NORMAL, FloatConsts.MIN_NORMAL},
            {Float_MAX_SUBNORMAL,       FloatConsts.MIN_NORMAL, FloatConsts.MIN_NORMAL},
            {Float_MAX_SUBNORMAL,       Float_MAX_SUBNORMAL,    Float_MAX_SUBNORMAL},
            {Float_MAX_SUBNORMAL,       0.0f,                   Float_MAX_SUBNORMALmm},
            {Float_MAX_SUBNORMALmm,     Float_MAX_SUBNORMAL,    Float_MAX_SUBNORMAL},
            {Float_MAX_SUBNORMALmm,     0.0f,                   Float_MAX_SUBNORMALmm-Float.MIN_VALUE},
            {Float_MAX_SUBNORMALmm,     Float_MAX_SUBNORMALmm,  Float_MAX_SUBNORMALmm},
            {Float.MIN_VALUE,   0.0f,                   0.0f},
            {-Float.MIN_VALUE,  0.0f,                   -0.0f},
            {Float.MIN_VALUE,   Float.MIN_VALUE,        Float.MIN_VALUE},
            {Float.MIN_VALUE,   1.0f,                   2*Float.MIN_VALUE},
            {0.0f,              0.0f,                   0.0f},
            {0.0f,              -0.0f,                  -0.0f},
            {-0.0f,             0.0f,                   0.0f},
            {-0.0f,             -0.0f,                  -0.0f},
            {0.0f,              infinityF,              Float.MIN_VALUE},
            {0.0f,              -infinityF,             -Float.MIN_VALUE},
            {-0.0f,             infinityF,              Float.MIN_VALUE},
            {-0.0f,             -infinityF,             -Float.MIN_VALUE},
            {0.0f,              Float.MIN_VALUE,        Float.MIN_VALUE},
            {0.0f,              -Float.MIN_VALUE,       -Float.MIN_VALUE},
            {-0.0f,             Float.MIN_VALUE,        Float.MIN_VALUE},
            {-0.0f,             -Float.MIN_VALUE,       -Float.MIN_VALUE}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures += testNextAfterCase(testCases[i][0], testCases[i][1],
                                          testCases[i][2]);
        }
        return failures;
    }
    public static int testDoubleNextAfter() {
        int failures =0;
        double [][] testCases  = {
            {NaNd,              NaNd,                   NaNd},
            {NaNd,              0.0d,                   NaNd},
            {0.0d,              NaNd,                   NaNd},
            {NaNd,              infinityD,              NaNd},
            {infinityD,         NaNd,                   NaNd},
            {infinityD,         infinityD,              infinityD},
            {infinityD,         -infinityD,             Double.MAX_VALUE},
            {infinityD,         0.0d,                   Double.MAX_VALUE},
            {Double.MAX_VALUE,  infinityD,              infinityD},
            {Double.MAX_VALUE,  -infinityD,             Double_MAX_VALUEmm},
            {Double.MAX_VALUE,  Double.MAX_VALUE,       Double.MAX_VALUE},
            {Double.MAX_VALUE,  0.0d,                   Double_MAX_VALUEmm},
            {Double_MAX_VALUEmm,        Double.MAX_VALUE,       Double.MAX_VALUE},
            {Double_MAX_VALUEmm,        infinityD,              Double.MAX_VALUE},
            {Double_MAX_VALUEmm,        Double_MAX_VALUEmm,     Double_MAX_VALUEmm},
            {DoubleConsts.MIN_NORMAL,   infinityD,              DoubleConsts.MIN_NORMAL+
                                                                Double.MIN_VALUE},
            {DoubleConsts.MIN_NORMAL,   -infinityD,             Double_MAX_SUBNORMAL},
            {DoubleConsts.MIN_NORMAL,   1.0f,                   DoubleConsts.MIN_NORMAL+
                                                                Double.MIN_VALUE},
            {DoubleConsts.MIN_NORMAL,   -1.0f,                  Double_MAX_SUBNORMAL},
            {DoubleConsts.MIN_NORMAL,   DoubleConsts.MIN_NORMAL,DoubleConsts.MIN_NORMAL},
            {Double_MAX_SUBNORMAL,      DoubleConsts.MIN_NORMAL,DoubleConsts.MIN_NORMAL},
            {Double_MAX_SUBNORMAL,      Double_MAX_SUBNORMAL,   Double_MAX_SUBNORMAL},
            {Double_MAX_SUBNORMAL,      0.0d,                   Double_MAX_SUBNORMALmm},
            {Double_MAX_SUBNORMALmm,    Double_MAX_SUBNORMAL,   Double_MAX_SUBNORMAL},
            {Double_MAX_SUBNORMALmm,    0.0d,                   Double_MAX_SUBNORMALmm-Double.MIN_VALUE},
            {Double_MAX_SUBNORMALmm,    Double_MAX_SUBNORMALmm, Double_MAX_SUBNORMALmm},
            {Double.MIN_VALUE,  0.0d,                   0.0d},
            {-Double.MIN_VALUE, 0.0d,                   -0.0d},
            {Double.MIN_VALUE,  Double.MIN_VALUE,       Double.MIN_VALUE},
            {Double.MIN_VALUE,  1.0f,                   2*Double.MIN_VALUE},
            {0.0d,              0.0d,                   0.0d},
            {0.0d,              -0.0d,                  -0.0d},
            {-0.0d,             0.0d,                   0.0d},
            {-0.0d,             -0.0d,                  -0.0d},
            {0.0d,              infinityD,              Double.MIN_VALUE},
            {0.0d,              -infinityD,             -Double.MIN_VALUE},
            {-0.0d,             infinityD,              Double.MIN_VALUE},
            {-0.0d,             -infinityD,             -Double.MIN_VALUE},
            {0.0d,              Double.MIN_VALUE,       Double.MIN_VALUE},
            {0.0d,              -Double.MIN_VALUE,      -Double.MIN_VALUE},
            {-0.0d,             Double.MIN_VALUE,       Double.MIN_VALUE},
            {-0.0d,             -Double.MIN_VALUE,      -Double.MIN_VALUE}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures += testNextAfterCase(testCases[i][0], testCases[i][1],
                                          testCases[i][2]);
        }
        return failures;
    }
    public static int testFloatNextUp() {
        int failures=0;
        float testCases [][] = {
            {NaNf,                      NaNf},
            {-infinityF,                -Float.MAX_VALUE},
            {-Float.MAX_VALUE,          -Float_MAX_VALUEmm},
            {-FloatConsts.MIN_NORMAL,   -Float_MAX_SUBNORMAL},
            {-Float_MAX_SUBNORMAL,      -Float_MAX_SUBNORMALmm},
            {-Float.MIN_VALUE,          -0.0f},
            {-0.0f,                     Float.MIN_VALUE},
            {+0.0f,                     Float.MIN_VALUE},
            {Float.MIN_VALUE,           Float.MIN_VALUE*2},
            {Float_MAX_SUBNORMALmm,     Float_MAX_SUBNORMAL},
            {Float_MAX_SUBNORMAL,       FloatConsts.MIN_NORMAL},
            {FloatConsts.MIN_NORMAL,    FloatConsts.MIN_NORMAL+Float.MIN_VALUE},
            {Float_MAX_VALUEmm,         Float.MAX_VALUE},
            {Float.MAX_VALUE,           infinityF},
            {infinityF,                 infinityF}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("Math.nextUp(float)",
                                 testCases[i][0], Math.nextUp(testCases[i][0]), testCases[i][1]);
            failures+=Tests.test("StrictMath.nextUp(float)",
                                 testCases[i][0], StrictMath.nextUp(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static int testDoubleNextUp() {
        int failures=0;
        double testCases [][] = {
            {NaNd,                      NaNd},
            {-infinityD,                -Double.MAX_VALUE},
            {-Double.MAX_VALUE,         -Double_MAX_VALUEmm},
            {-DoubleConsts.MIN_NORMAL,  -Double_MAX_SUBNORMAL},
            {-Double_MAX_SUBNORMAL,     -Double_MAX_SUBNORMALmm},
            {-Double.MIN_VALUE,         -0.0d},
            {-0.0d,                     Double.MIN_VALUE},
            {+0.0d,                     Double.MIN_VALUE},
            {Double.MIN_VALUE,          Double.MIN_VALUE*2},
            {Double_MAX_SUBNORMALmm,    Double_MAX_SUBNORMAL},
            {Double_MAX_SUBNORMAL,      DoubleConsts.MIN_NORMAL},
            {DoubleConsts.MIN_NORMAL,   DoubleConsts.MIN_NORMAL+Double.MIN_VALUE},
            {Double_MAX_VALUEmm,        Double.MAX_VALUE},
            {Double.MAX_VALUE,          infinityD},
            {infinityD,                 infinityD}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("Math.nextUp(double)",
                                 testCases[i][0], Math.nextUp(testCases[i][0]), testCases[i][1]);
            failures+=Tests.test("StrictMath.nextUp(double)",
                                 testCases[i][0], StrictMath.nextUp(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static int testFloatNextDown() {
        int failures=0;
        float testCases [][] = {
            {NaNf,                      NaNf},
            {-infinityF,                -infinityF},
            {-Float.MAX_VALUE,          -infinityF},
            {-Float_MAX_VALUEmm,        -Float.MAX_VALUE},
            {-Float_MAX_SUBNORMAL,      -FloatConsts.MIN_NORMAL},
            {-Float_MAX_SUBNORMALmm,    -Float_MAX_SUBNORMAL},
            {-0.0f,                     -Float.MIN_VALUE},
            {+0.0f,                     -Float.MIN_VALUE},
            {Float.MIN_VALUE,           0.0f},
            {Float.MIN_VALUE*2,         Float.MIN_VALUE},
            {Float_MAX_SUBNORMAL,       Float_MAX_SUBNORMALmm},
            {FloatConsts.MIN_NORMAL,    Float_MAX_SUBNORMAL},
            {FloatConsts.MIN_NORMAL+
             Float.MIN_VALUE,           FloatConsts.MIN_NORMAL},
            {Float.MAX_VALUE,           Float_MAX_VALUEmm},
            {infinityF,                 Float.MAX_VALUE},
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("FpUtils.nextDown(float)",
                                 testCases[i][0], FpUtils.nextDown(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static int testDoubleNextDown() {
        int failures=0;
        double testCases [][] = {
            {NaNd,                      NaNd},
            {-infinityD,                -infinityD},
            {-Double.MAX_VALUE,         -infinityD},
            {-Double_MAX_VALUEmm,       -Double.MAX_VALUE},
            {-Double_MAX_SUBNORMAL,     -DoubleConsts.MIN_NORMAL},
            {-Double_MAX_SUBNORMALmm,   -Double_MAX_SUBNORMAL},
            {-0.0d,                     -Double.MIN_VALUE},
            {+0.0d,                     -Double.MIN_VALUE},
            {Double.MIN_VALUE,          0.0d},
            {Double.MIN_VALUE*2,        Double.MIN_VALUE},
            {Double_MAX_SUBNORMAL,      Double_MAX_SUBNORMALmm},
            {DoubleConsts.MIN_NORMAL,   Double_MAX_SUBNORMAL},
            {DoubleConsts.MIN_NORMAL+
             Double.MIN_VALUE,          DoubleConsts.MIN_NORMAL},
            {Double.MAX_VALUE,          Double_MAX_VALUEmm},
            {infinityD,                 Double.MAX_VALUE},
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("FpUtils.nextDown(double)",
                                 testCases[i][0], FpUtils.nextDown(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static int testFloatBooleanMethods() {
        int failures = 0;
        float testCases [] = {
            NaNf,
            -infinityF,
            infinityF,
            -Float.MAX_VALUE,
            -3.0f,
            -1.0f,
            -FloatConsts.MIN_NORMAL,
            -Float_MAX_SUBNORMALmm,
            -Float_MAX_SUBNORMAL,
            -Float.MIN_VALUE,
            -0.0f,
            +0.0f,
            Float.MIN_VALUE,
            Float_MAX_SUBNORMALmm,
            Float_MAX_SUBNORMAL,
            FloatConsts.MIN_NORMAL,
            1.0f,
            3.0f,
            Float_MAX_VALUEmm,
            Float.MAX_VALUE
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("FpUtils.isNaN(float)", testCases[i],
                                 FpUtils.isNaN(testCases[i]), (i ==0));
            failures+=Tests.test("FpUtils.isFinite(float)", testCases[i],
                                 FpUtils.isFinite(testCases[i]), (i >= 3));
            failures+=Tests.test("FpUtils.isInfinite(float)", testCases[i],
                                 FpUtils.isInfinite(testCases[i]), (i==1 || i==2));
            for(int j = 0; j < testCases.length; j++) {
                failures+=Tests.test("FpUtils.isUnordered(float, float)", testCases[i],testCases[j],
                                     FpUtils.isUnordered(testCases[i],testCases[j]), (i==0 || j==0));
            }
        }
        return failures;
    }
    public static int testDoubleBooleanMethods() {
        int failures = 0;
        boolean result = false;
        double testCases [] = {
            NaNd,
            -infinityD,
            infinityD,
            -Double.MAX_VALUE,
            -3.0d,
            -1.0d,
            -DoubleConsts.MIN_NORMAL,
            -Double_MAX_SUBNORMALmm,
            -Double_MAX_SUBNORMAL,
            -Double.MIN_VALUE,
            -0.0d,
            +0.0d,
            Double.MIN_VALUE,
            Double_MAX_SUBNORMALmm,
            Double_MAX_SUBNORMAL,
            DoubleConsts.MIN_NORMAL,
            1.0d,
            3.0d,
            Double_MAX_VALUEmm,
            Double.MAX_VALUE
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("FpUtils.isNaN(double)", testCases[i],
                                 FpUtils.isNaN(testCases[i]), (i ==0));
            failures+=Tests.test("FpUtils.isFinite(double)", testCases[i],
                                 FpUtils.isFinite(testCases[i]), (i >= 3));
            failures+=Tests.test("FpUtils.isInfinite(double)", testCases[i],
                                 FpUtils.isInfinite(testCases[i]), (i==1 || i==2));
            for(int j = 0; j < testCases.length; j++) {
                failures+=Tests.test("FpUtils.isUnordered(double, double)", testCases[i],testCases[j],
                                     FpUtils.isUnordered(testCases[i],testCases[j]), (i==0 || j==0));
            }
        }
        return failures;
    }
   public static int testFloatCopySign() {
        int failures = 0;
        float testCases [][] = {
            {+0.0f,
             Float.MIN_VALUE,
             Float_MAX_SUBNORMALmm,
             Float_MAX_SUBNORMAL,
             FloatConsts.MIN_NORMAL,
             1.0f,
             3.0f,
             Float_MAX_VALUEmm,
             Float.MAX_VALUE,
             infinityF,
            },
            {-infinityF,
             -Float.MAX_VALUE,
             -3.0f,
             -1.0f,
             -FloatConsts.MIN_NORMAL,
             -Float_MAX_SUBNORMALmm,
             -Float_MAX_SUBNORMAL,
             -Float.MIN_VALUE,
             -0.0f}
        };
        float NaNs[] = {Float.intBitsToFloat(0x7fc00000),       
                        Float.intBitsToFloat(0xFfc00000)};      
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                for(int m = 0; m < testCases[i].length; m++) {
                    for(int n = 0; n < testCases[j].length; n++) {
                        failures+=Tests.test("Math.copySign(float,float)",
                                             testCases[i][m],testCases[j][n],
                                             Math.copySign(testCases[i][m], testCases[j][n]),
                                             (j==0?1.0f:-1.0f)*Math.abs(testCases[i][m]) );
                        failures+=Tests.test("StrictMath.copySign(float,float)",
                                             testCases[i][m],testCases[j][n],
                                             StrictMath.copySign(testCases[i][m], testCases[j][n]),
                                             (j==0?1.0f:-1.0f)*Math.abs(testCases[i][m]) );
                    }
                }
            }
        }
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < NaNs.length; j++) {
                for(int m = 0; m < testCases[i].length; m++) {
                    failures += (Math.abs(Math.copySign(testCases[i][m], NaNs[j])) ==
                                 Math.abs(testCases[i][m])) ? 0:1;
                    failures+=Tests.test("StrictMath.copySign(float,float)",
                                         testCases[i][m], NaNs[j],
                                         StrictMath.copySign(testCases[i][m], NaNs[j]),
                                         Math.abs(testCases[i][m]) );
                }
            }
        }
        return failures;
    }
    public static int testDoubleCopySign() {
        int failures = 0;
        double testCases [][] = {
            {+0.0d,
             Double.MIN_VALUE,
             Double_MAX_SUBNORMALmm,
             Double_MAX_SUBNORMAL,
             DoubleConsts.MIN_NORMAL,
             1.0d,
             3.0d,
             Double_MAX_VALUEmm,
             Double.MAX_VALUE,
             infinityD,
            },
            {-infinityD,
             -Double.MAX_VALUE,
             -3.0d,
             -1.0d,
             -DoubleConsts.MIN_NORMAL,
             -Double_MAX_SUBNORMALmm,
             -Double_MAX_SUBNORMAL,
             -Double.MIN_VALUE,
             -0.0d}
        };
        double NaNs[] = {Double.longBitsToDouble(0x7ff8000000000000L),  
                         Double.longBitsToDouble(0xfff8000000000000L),  
                         Double.longBitsToDouble(0x7FF0000000000001L),
                         Double.longBitsToDouble(0xFFF0000000000001L),
                         Double.longBitsToDouble(0x7FF8555555555555L),
                         Double.longBitsToDouble(0xFFF8555555555555L),
                         Double.longBitsToDouble(0x7FFFFFFFFFFFFFFFL),
                         Double.longBitsToDouble(0xFFFFFFFFFFFFFFFFL),
                         Double.longBitsToDouble(0x7FFDeadBeef00000L),
                         Double.longBitsToDouble(0xFFFDeadBeef00000L),
                         Double.longBitsToDouble(0x7FFCafeBabe00000L),
                         Double.longBitsToDouble(0xFFFCafeBabe00000L)};
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                for(int m = 0; m < testCases[i].length; m++) {
                    for(int n = 0; n < testCases[j].length; n++) {
                        failures+=Tests.test("MathcopySign(double,double)",
                                             testCases[i][m],testCases[j][n],
                                             Math.copySign(testCases[i][m], testCases[j][n]),
                                             (j==0?1.0f:-1.0f)*Math.abs(testCases[i][m]) );
                        failures+=Tests.test("StrictMath.copySign(double,double)",
                                             testCases[i][m],testCases[j][n],
                                             StrictMath.copySign(testCases[i][m], testCases[j][n]),
                                             (j==0?1.0f:-1.0f)*Math.abs(testCases[i][m]) );
                    }
                }
            }
        }
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < NaNs.length; j++) {
                for(int m = 0; m < testCases[i].length; m++) {
                    failures += (Math.abs(Math.copySign(testCases[i][m], NaNs[j])) ==
                                 Math.abs(testCases[i][m])) ? 0:1;
                    failures+=Tests.test("StrictMath.copySign(double,double)",
                                         testCases[i][m], NaNs[j],
                                         StrictMath.copySign(testCases[i][m], NaNs[j]),
                                         Math.abs(testCases[i][m]) );
                }
            }
        }
        return failures;
    }
    static int testScalbCase(float value, int scale_factor, float expected) {
        int failures=0;
        failures+=Tests.test("Math.scalb(float,int)",
                             value, scale_factor,
                             Math.scalb(value, scale_factor), expected);
        failures+=Tests.test("Math.scalb(float,int)",
                             -value, scale_factor,
                             Math.scalb(-value, scale_factor), -expected);
        failures+=Tests.test("StrictMath.scalb(float,int)",
                             value, scale_factor,
                             StrictMath.scalb(value, scale_factor), expected);
        failures+=Tests.test("StrictMath.scalb(float,int)",
                             -value, scale_factor,
                             StrictMath.scalb(-value, scale_factor), -expected);
        return failures;
    }
    public static int testFloatScalb() {
        int failures=0;
        int MAX_SCALE = FloatConsts.MAX_EXPONENT + -FloatConsts.MIN_EXPONENT +
                        FloatConsts.SIGNIFICAND_WIDTH + 1;
        float [] identityTestCases = {NaNf,
                                      -0.0f,
                                      +0.0f,
                                      infinityF,
                                      -infinityF
        };
        float [] subnormalTestCases = {
            Float.MIN_VALUE,
            3.0f*Float.MIN_VALUE,
            Float_MAX_SUBNORMALmm,
            Float_MAX_SUBNORMAL
        };
        float [] someTestCases = {
            Float.MIN_VALUE,
            3.0f*Float.MIN_VALUE,
            Float_MAX_SUBNORMALmm,
            Float_MAX_SUBNORMAL,
            FloatConsts.MIN_NORMAL,
            1.0f,
            2.0f,
            3.0f,
            (float)Math.PI,
            Float_MAX_VALUEmm,
            Float.MAX_VALUE
        };
        int [] oneMultiplyScalingFactors = {
            FloatConsts.MIN_EXPONENT,
            FloatConsts.MIN_EXPONENT+1,
            -3,
            -2,
            -1,
            0,
            1,
            2,
            3,
            FloatConsts.MAX_EXPONENT-1,
            FloatConsts.MAX_EXPONENT
        };
        int [] manyScalingFactors = {
            Integer.MIN_VALUE,
            Integer.MIN_VALUE+1,
            -MAX_SCALE -1,
            -MAX_SCALE,
            -MAX_SCALE+1,
            2*FloatConsts.MIN_EXPONENT-1,       
            2*FloatConsts.MIN_EXPONENT,         
            2*FloatConsts.MIN_EXPONENT+1,       
            FpUtils.ilogb(Float.MIN_VALUE)-1,   
            FpUtils.ilogb(Float.MIN_VALUE),     
            -FloatConsts.MAX_EXPONENT,          
            FloatConsts.MIN_EXPONENT,           
            -2,
            -1,
            0,
            1,
            2,
            FloatConsts.MAX_EXPONENT-1,         
            FloatConsts.MAX_EXPONENT,           
            FloatConsts.MAX_EXPONENT+1,         
            2*FloatConsts.MAX_EXPONENT-1,       
            2*FloatConsts.MAX_EXPONENT,         
            2*FloatConsts.MAX_EXPONENT+1,       
            MAX_SCALE-1,
            MAX_SCALE,
            MAX_SCALE+1,
            Integer.MAX_VALUE-1,
            Integer.MAX_VALUE
        };
        for(int i=0; i < identityTestCases.length; i++) {
            for(int j=0; j < manyScalingFactors.length; j++) {
                failures += testScalbCase(identityTestCases[i],
                                          manyScalingFactors[j],
                                          identityTestCases[i]);
            }
        }
        for(int i=0; i < someTestCases.length; i++) {
            for(int j=0; j < manyScalingFactors.length; j++) {
                int scaleFactor = manyScalingFactors[j];
                if (Math.abs(scaleFactor) >= MAX_SCALE) {
                    float value = someTestCases[i];
                    failures+=testScalbCase(value,
                                            scaleFactor,
                                            FpUtils.copySign( (scaleFactor>0?infinityF:0.0f), value) );
                }
            }
        }
        for(int i=0; i < someTestCases.length; i++) {
            for(int j=0; j < oneMultiplyScalingFactors.length; j++) {
                int scaleFactor = oneMultiplyScalingFactors[j];
                    float value = someTestCases[i];
                    failures+=testScalbCase(value,
                                            scaleFactor,
                                            value*powerOfTwoF(scaleFactor));
            }
        }
        float twoToTheMaxExp = 1.0f; 
        for(int i = 0; i < FloatConsts.MAX_EXPONENT; i++)
            twoToTheMaxExp *=2.0f;
        for(int i=0; i < subnormalTestCases.length; i++) {
            float scale = 1.0f; 
            float value = subnormalTestCases[i];
            for(int j=FloatConsts.MAX_EXPONENT*2; j < MAX_SCALE; j++) { 
                int scaleFactor = j;
                failures+=testScalbCase(value,
                                        scaleFactor,
                                        (FpUtils.ilogb(value) +j > FloatConsts.MAX_EXPONENT ) ?
                                        FpUtils.copySign(infinityF, value) : 
                                        twoToTheMaxExp*(twoToTheMaxExp*(scale*value)) );
                scale*=2.0f;
            }
        }
        float expected = Float_MAX_VALUEmm *0.5f;
        for(int i = -1; i > -MAX_SCALE; i--) {
            failures+=testScalbCase(Float_MAX_VALUEmm, i, expected);
            expected *= 0.5f;
        }
        float value = 0x8.0000bP-5f;
        expected = 0x1.00001p-129f;
        for(int i = 0; i < 129; i++) {
            failures+=testScalbCase(value,
                                    -127-i,
                                    expected);
            value *=2.0f;
        }
        return failures;
    }
    static int testScalbCase(double value, int scale_factor, double expected) {
        int failures=0;
        failures+=Tests.test("Math.scalb(double,int)",
                             value, scale_factor,
                             Math.scalb(value, scale_factor), expected);
        failures+=Tests.test("Math.scalb(double,int)",
                             -value, scale_factor,
                             Math.scalb(-value, scale_factor), -expected);
        failures+=Tests.test("StrictMath.scalb(double,int)",
                             value, scale_factor,
                             StrictMath.scalb(value, scale_factor), expected);
        failures+=Tests.test("StrictMath.scalb(double,int)",
                             -value, scale_factor,
                             StrictMath.scalb(-value, scale_factor), -expected);
        return failures;
    }
    public static int testDoubleScalb() {
        int failures=0;
        int MAX_SCALE = DoubleConsts.MAX_EXPONENT + -DoubleConsts.MIN_EXPONENT +
                        DoubleConsts.SIGNIFICAND_WIDTH + 1;
        double [] identityTestCases = {NaNd,
                                      -0.0,
                                      +0.0,
                                      infinityD,
        };
        double [] subnormalTestCases = {
            Double.MIN_VALUE,
            3.0d*Double.MIN_VALUE,
            Double_MAX_SUBNORMALmm,
            Double_MAX_SUBNORMAL
        };
        double [] someTestCases = {
            Double.MIN_VALUE,
            3.0d*Double.MIN_VALUE,
            Double_MAX_SUBNORMALmm,
            Double_MAX_SUBNORMAL,
            DoubleConsts.MIN_NORMAL,
            1.0d,
            2.0d,
            3.0d,
            Math.PI,
            Double_MAX_VALUEmm,
            Double.MAX_VALUE
        };
        int [] oneMultiplyScalingFactors = {
            DoubleConsts.MIN_EXPONENT,
            DoubleConsts.MIN_EXPONENT+1,
            -3,
            -2,
            -1,
            0,
            1,
            2,
            3,
            DoubleConsts.MAX_EXPONENT-1,
            DoubleConsts.MAX_EXPONENT
        };
        int [] manyScalingFactors = {
            Integer.MIN_VALUE,
            Integer.MIN_VALUE+1,
            -MAX_SCALE -1,
            -MAX_SCALE,
            -MAX_SCALE+1,
            2*DoubleConsts.MIN_EXPONENT-1,      
            2*DoubleConsts.MIN_EXPONENT,        
            2*DoubleConsts.MIN_EXPONENT+1,      
            FpUtils.ilogb(Double.MIN_VALUE)-1,  
            FpUtils.ilogb(Double.MIN_VALUE),    
            -DoubleConsts.MAX_EXPONENT,         
            DoubleConsts.MIN_EXPONENT,          
            -2,
            -1,
            0,
            1,
            2,
            DoubleConsts.MAX_EXPONENT-1,        
            DoubleConsts.MAX_EXPONENT,          
            DoubleConsts.MAX_EXPONENT+1,        
            2*DoubleConsts.MAX_EXPONENT-1,      
            2*DoubleConsts.MAX_EXPONENT,        
            2*DoubleConsts.MAX_EXPONENT+1,      
            MAX_SCALE-1,
            MAX_SCALE,
            MAX_SCALE+1,
            Integer.MAX_VALUE-1,
            Integer.MAX_VALUE
        };
        for(int i=0; i < identityTestCases.length; i++) {
            for(int j=0; j < manyScalingFactors.length; j++) {
                failures += testScalbCase(identityTestCases[i],
                                          manyScalingFactors[j],
                                          identityTestCases[i]);
            }
        }
        for(int i=0; i < someTestCases.length; i++) {
            for(int j=0; j < manyScalingFactors.length; j++) {
                int scaleFactor = manyScalingFactors[j];
                if (Math.abs(scaleFactor) >= MAX_SCALE) {
                    double value = someTestCases[i];
                    failures+=testScalbCase(value,
                                            scaleFactor,
                                            FpUtils.copySign( (scaleFactor>0?infinityD:0.0), value) );
                }
            }
        }
        for(int i=0; i < someTestCases.length; i++) {
            for(int j=0; j < oneMultiplyScalingFactors.length; j++) {
                int scaleFactor = oneMultiplyScalingFactors[j];
                    double value = someTestCases[i];
                    failures+=testScalbCase(value,
                                            scaleFactor,
                                            value*powerOfTwoD(scaleFactor));
            }
        }
        double twoToTheMaxExp = 1.0; 
        for(int i = 0; i < DoubleConsts.MAX_EXPONENT; i++)
            twoToTheMaxExp *=2.0;
        for(int i=0; i < subnormalTestCases.length; i++) {
            double scale = 1.0; 
            double value = subnormalTestCases[i];
            for(int j=DoubleConsts.MAX_EXPONENT*2; j < MAX_SCALE; j++) { 
                int scaleFactor = j;
                failures+=testScalbCase(value,
                                        scaleFactor,
                                        (FpUtils.ilogb(value) +j > DoubleConsts.MAX_EXPONENT ) ?
                                        FpUtils.copySign(infinityD, value) : 
                                        twoToTheMaxExp*(twoToTheMaxExp*(scale*value)) );
                scale*=2.0;
            }
        }
        double expected = Double_MAX_VALUEmm *0.5f;
        for(int i = -1; i > -MAX_SCALE; i--) {
            failures+=testScalbCase(Double_MAX_VALUEmm, i, expected);
            expected *= 0.5;
        }
        double value = 0x1.000000000000bP-1;
        expected     = 0x0.2000000000001P-1022;
        for(int i = 0; i < DoubleConsts.MAX_EXPONENT+2; i++) {
            failures+=testScalbCase(value,
                                    -1024-i,
                                    expected);
            value *=2.0;
        }
        return failures;
    }
    static int testUlpCase(float f, float expected) {
        float minus_f = -f;
        int failures=0;
        failures+=Tests.test("Math.ulp(float)", f,
                             Math.ulp(f), expected);
        failures+=Tests.test("Math.ulp(float)", minus_f,
                             Math.ulp(minus_f), expected);
        failures+=Tests.test("StrictMath.ulp(float)", f,
                             StrictMath.ulp(f), expected);
        failures+=Tests.test("StrictMath.ulp(float)", minus_f,
                             StrictMath.ulp(minus_f), expected);
        return failures;
    }
    static int testUlpCase(double d, double expected) {
        double minus_d = -d;
        int failures=0;
        failures+=Tests.test("Math.ulp(double)", d,
                             Math.ulp(d), expected);
        failures+=Tests.test("Math.ulp(double)", minus_d,
                             Math.ulp(minus_d), expected);
        failures+=Tests.test("StrictMath.ulp(double)", d,
                             StrictMath.ulp(d), expected);
        failures+=Tests.test("StrictMath.ulp(double)", minus_d,
                             StrictMath.ulp(minus_d), expected);
        return failures;
    }
    public static int testFloatUlp() {
        int failures = 0;
        float [] specialValues = {NaNf,
                                  Float.POSITIVE_INFINITY,
                                  +0.0f,
                                  +1.0f,
                                  +2.0f,
                                  +16.0f,
                                  +Float.MIN_VALUE,
                                  +Float_MAX_SUBNORMAL,
                                  +FloatConsts.MIN_NORMAL,
                                  +Float.MAX_VALUE
        };
        float [] specialResults = {NaNf,
                                   Float.POSITIVE_INFINITY,
                                   Float.MIN_VALUE,
                                   powerOfTwoF(-23),
                                   powerOfTwoF(-22),
                                   powerOfTwoF(-19),
                                   Float.MIN_VALUE,
                                   Float.MIN_VALUE,
                                   Float.MIN_VALUE,
                                   powerOfTwoF(104)
        };
        for(int i = 0; i < specialValues.length; i++) {
            failures += testUlpCase(specialValues[i], specialResults[i]);
        }
        for(int i = FloatConsts.MIN_EXPONENT; i <= FloatConsts.MAX_EXPONENT; i++) {
            float expected;
            float po2 = powerOfTwoF(i);
            expected = FpUtils.scalb(1.0f, i - (FloatConsts.SIGNIFICAND_WIDTH-1));
            failures += testUlpCase(po2, expected);
            for(int j = 0; j < 10; j++) {
                int randSignif = rand.nextInt();
                float randFloat;
                randFloat = Float.intBitsToFloat( 
                                                 (Float.floatToIntBits(po2)&
                                                  (~FloatConsts.SIGNIF_BIT_MASK)) |
                                                 (randSignif &
                                                  FloatConsts.SIGNIF_BIT_MASK) );
                failures += testUlpCase(randFloat, expected);
            }
            if (i > FloatConsts.MIN_EXPONENT) {
                float po2minus = FpUtils.nextAfter(po2,
                                                   Float.NEGATIVE_INFINITY);
                failures += testUlpCase(po2minus, expected/2.0f);
            }
        }
        float top=Float.MIN_VALUE;
        for( int i = 1;
            i < FloatConsts.SIGNIFICAND_WIDTH;
            i++, top *= 2.0f) {
            failures += testUlpCase(top, Float.MIN_VALUE);
            if (i >= 3) {
                testUlpCase(FpUtils.nextAfter(top, 0.0f),
                            Float.MIN_VALUE);
                if( i >= 10) {
                    int mask = ~((~0)<<(i-1));
                    float randFloat = Float.intBitsToFloat( 
                                                 Float.floatToIntBits(top) |
                                                 (rand.nextInt() & mask ) ) ;
                    failures += testUlpCase(randFloat, Float.MIN_VALUE);
                }
            }
        }
        return failures;
    }
    public static int testDoubleUlp() {
        int failures = 0;
        double [] specialValues = {NaNd,
                                  Double.POSITIVE_INFINITY,
                                  +0.0d,
                                  +1.0d,
                                  +2.0d,
                                  +16.0d,
                                  +Double.MIN_VALUE,
                                  +Double_MAX_SUBNORMAL,
                                  +DoubleConsts.MIN_NORMAL,
                                  +Double.MAX_VALUE
        };
        double [] specialResults = {NaNf,
                                   Double.POSITIVE_INFINITY,
                                   Double.MIN_VALUE,
                                   powerOfTwoD(-52),
                                   powerOfTwoD(-51),
                                   powerOfTwoD(-48),
                                   Double.MIN_VALUE,
                                   Double.MIN_VALUE,
                                   Double.MIN_VALUE,
                                   powerOfTwoD(971)
        };
        for(int i = 0; i < specialValues.length; i++) {
            failures += testUlpCase(specialValues[i], specialResults[i]);
        }
        for(int i = DoubleConsts.MIN_EXPONENT; i <= DoubleConsts.MAX_EXPONENT; i++) {
            double expected;
            double po2 = powerOfTwoD(i);
            expected = FpUtils.scalb(1.0, i - (DoubleConsts.SIGNIFICAND_WIDTH-1));
            failures += testUlpCase(po2, expected);
            for(int j = 0; j < 10; j++) {
                long randSignif = rand.nextLong();
                double randDouble;
                randDouble = Double.longBitsToDouble( 
                                                 (Double.doubleToLongBits(po2)&
                                                  (~DoubleConsts.SIGNIF_BIT_MASK)) |
                                                 (randSignif &
                                                  DoubleConsts.SIGNIF_BIT_MASK) );
                failures += testUlpCase(randDouble, expected);
            }
            if (i > DoubleConsts.MIN_EXPONENT) {
                double po2minus = FpUtils.nextAfter(po2,
                                                    Double.NEGATIVE_INFINITY);
                failures += testUlpCase(po2minus, expected/2.0f);
            }
        }
        double top=Double.MIN_VALUE;
        for( int i = 1;
            i < DoubleConsts.SIGNIFICAND_WIDTH;
            i++, top *= 2.0f) {
            failures += testUlpCase(top, Double.MIN_VALUE);
            if (i >= 3) {
                testUlpCase(FpUtils.nextAfter(top, 0.0f),
                            Double.MIN_VALUE);
                if( i >= 10) {
                    int mask = ~((~0)<<(i-1));
                    double randDouble = Double.longBitsToDouble( 
                                                 Double.doubleToLongBits(top) |
                                                 (rand.nextLong() & mask ) ) ;
                    failures += testUlpCase(randDouble, Double.MIN_VALUE);
                }
            }
        }
        return failures;
    }
    public static int testFloatSignum() {
        int failures = 0;
        float testCases [][] = {
            {NaNf,                      NaNf},
            {-infinityF,                -1.0f},
            {-Float.MAX_VALUE,          -1.0f},
            {-FloatConsts.MIN_NORMAL,   -1.0f},
            {-1.0f,                     -1.0f},
            {-2.0f,                     -1.0f},
            {-Float_MAX_SUBNORMAL,      -1.0f},
            {-Float.MIN_VALUE,          -1.0f},
            {-0.0f,                     -0.0f},
            {+0.0f,                     +0.0f},
            {Float.MIN_VALUE,            1.0f},
            {Float_MAX_SUBNORMALmm,      1.0f},
            {Float_MAX_SUBNORMAL,        1.0f},
            {FloatConsts.MIN_NORMAL,     1.0f},
            {1.0f,                       1.0f},
            {2.0f,                       1.0f},
            {Float_MAX_VALUEmm,          1.0f},
            {Float.MAX_VALUE,            1.0f},
            {infinityF,                  1.0f}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("Math.signum(float)",
                                 testCases[i][0], Math.signum(testCases[i][0]), testCases[i][1]);
            failures+=Tests.test("StrictMath.signum(float)",
                                 testCases[i][0], StrictMath.signum(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static int testDoubleSignum() {
        int failures = 0;
        double testCases [][] = {
            {NaNd,                      NaNd},
            {-infinityD,                -1.0},
            {-Double.MAX_VALUE,         -1.0},
            {-DoubleConsts.MIN_NORMAL,  -1.0},
            {-1.0,                      -1.0},
            {-2.0,                      -1.0},
            {-Double_MAX_SUBNORMAL,     -1.0},
            {-Double.MIN_VALUE,         -1.0d},
            {-0.0d,                     -0.0d},
            {+0.0d,                     +0.0d},
            {Double.MIN_VALUE,           1.0},
            {Double_MAX_SUBNORMALmm,     1.0},
            {Double_MAX_SUBNORMAL,       1.0},
            {DoubleConsts.MIN_NORMAL,    1.0},
            {1.0,                        1.0},
            {2.0,                        1.0},
            {Double_MAX_VALUEmm,         1.0},
            {Double.MAX_VALUE,           1.0},
            {infinityD,                  1.0}
        };
        for(int i = 0; i < testCases.length; i++) {
            failures+=Tests.test("Math.signum(double)",
                                 testCases[i][0], Math.signum(testCases[i][0]), testCases[i][1]);
            failures+=Tests.test("StrictMath.signum(double)",
                                 testCases[i][0], StrictMath.signum(testCases[i][0]), testCases[i][1]);
        }
        return failures;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += testFloatGetExponent();
        failures += testDoubleGetExponent();
        failures += testFloatNextAfter();
        failures += testDoubleNextAfter();
        failures += testFloatNextUp();
        failures += testDoubleNextUp();
        failures += testFloatNextDown();
        failures += testDoubleNextDown();
        failures += testFloatBooleanMethods();
        failures += testDoubleBooleanMethods();
        failures += testFloatCopySign();
        failures += testDoubleCopySign();
        failures += testFloatScalb();
        failures += testDoubleScalb();
        failures += testFloatUlp();
        failures += testDoubleUlp();
        failures += testFloatSignum();
        failures += testDoubleSignum();
        if (failures > 0) {
            System.err.println("Testing the recommended functions incurred "
                               + failures + " failures.");
            throw new RuntimeException();
        }
    }
}
