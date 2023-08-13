public class FpUtils {
    private FpUtils() {}
    static double twoToTheDoubleScaleUp = powerOfTwoD(512);
    static double twoToTheDoubleScaleDown = powerOfTwoD(-512);
    public static int getExponent(double d){
        return (int)(((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >>
                      (DoubleConsts.SIGNIFICAND_WIDTH - 1)) - DoubleConsts.EXP_BIAS);
    }
    public static int getExponent(float f){
        return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >>
                (FloatConsts.SIGNIFICAND_WIDTH - 1)) - FloatConsts.EXP_BIAS;
    }
    static double powerOfTwoD(int n) {
        assert(n >= DoubleConsts.MIN_EXPONENT && n <= DoubleConsts.MAX_EXPONENT);
        return Double.longBitsToDouble((((long)n + (long)DoubleConsts.EXP_BIAS) <<
                                        (DoubleConsts.SIGNIFICAND_WIDTH-1))
                                       & DoubleConsts.EXP_BIT_MASK);
    }
    static float powerOfTwoF(int n) {
        assert(n >= FloatConsts.MIN_EXPONENT && n <= FloatConsts.MAX_EXPONENT);
        return Float.intBitsToFloat(((n + FloatConsts.EXP_BIAS) <<
                                     (FloatConsts.SIGNIFICAND_WIDTH-1))
                                    & FloatConsts.EXP_BIT_MASK);
    }
    public static double rawCopySign(double magnitude, double sign) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(sign) &
                                        (DoubleConsts.SIGN_BIT_MASK)) |
                                       (Double.doubleToRawLongBits(magnitude) &
                                        (DoubleConsts.EXP_BIT_MASK |
                                         DoubleConsts.SIGNIF_BIT_MASK)));
    }
    public static float rawCopySign(float magnitude, float sign) {
        return Float.intBitsToFloat((Float.floatToRawIntBits(sign) &
                                     (FloatConsts.SIGN_BIT_MASK)) |
                                    (Float.floatToRawIntBits(magnitude) &
                                     (FloatConsts.EXP_BIT_MASK |
                                      FloatConsts.SIGNIF_BIT_MASK)));
    }
    public static boolean isFinite(double d) {
        return Math.abs(d) <= DoubleConsts.MAX_VALUE;
    }
     public static boolean isFinite(float f) {
        return Math.abs(f) <= FloatConsts.MAX_VALUE;
    }
    public static boolean isInfinite(double d) {
        return Double.isInfinite(d);
    }
     public static boolean isInfinite(float f) {
         return Float.isInfinite(f);
    }
    public static boolean isNaN(double d) {
        return Double.isNaN(d);
    }
     public static boolean isNaN(float f) {
        return Float.isNaN(f);
    }
    public static boolean isUnordered(double arg1, double arg2) {
        return isNaN(arg1) || isNaN(arg2);
    }
     public static boolean isUnordered(float arg1, float arg2) {
        return isNaN(arg1) || isNaN(arg2);
    }
    public static int ilogb(double d) {
        int exponent = getExponent(d);
        switch (exponent) {
        case DoubleConsts.MAX_EXPONENT+1:       
            if( isNaN(d) )
                return (1<<30);         
            else 
                return (1<<28);         
        case DoubleConsts.MIN_EXPONENT-1:       
            if(d == 0.0) {
                return -(1<<28);        
            }
            else {
                long transducer = Double.doubleToRawLongBits(d);
                transducer &= DoubleConsts.SIGNIF_BIT_MASK;
                assert(transducer != 0L);
                while (transducer <
                       (1L << (DoubleConsts.SIGNIFICAND_WIDTH - 1))) {
                    transducer *= 2;
                    exponent--;
                }
                exponent++;
                assert( exponent >=
                        DoubleConsts.MIN_EXPONENT - (DoubleConsts.SIGNIFICAND_WIDTH-1) &&
                        exponent < DoubleConsts.MIN_EXPONENT);
                return exponent;
            }
        default:
            assert( exponent >= DoubleConsts.MIN_EXPONENT &&
                    exponent <= DoubleConsts.MAX_EXPONENT);
            return exponent;
        }
    }
     public static int ilogb(float f) {
        int exponent = getExponent(f);
        switch (exponent) {
        case FloatConsts.MAX_EXPONENT+1:        
            if( isNaN(f) )
                return (1<<30);         
            else 
                return (1<<28);         
        case FloatConsts.MIN_EXPONENT-1:        
            if(f == 0.0f) {
                return -(1<<28);        
            }
            else {
                int transducer = Float.floatToRawIntBits(f);
                transducer &= FloatConsts.SIGNIF_BIT_MASK;
                assert(transducer != 0);
                while (transducer <
                       (1 << (FloatConsts.SIGNIFICAND_WIDTH - 1))) {
                    transducer *= 2;
                    exponent--;
                }
                exponent++;
                assert( exponent >=
                        FloatConsts.MIN_EXPONENT - (FloatConsts.SIGNIFICAND_WIDTH-1) &&
                        exponent < FloatConsts.MIN_EXPONENT);
                return exponent;
            }
        default:
            assert( exponent >= FloatConsts.MIN_EXPONENT &&
                    exponent <= FloatConsts.MAX_EXPONENT);
            return exponent;
        }
    }
    public static double scalb(double d, int scale_factor) {
        final int MAX_SCALE = DoubleConsts.MAX_EXPONENT + -DoubleConsts.MIN_EXPONENT +
                              DoubleConsts.SIGNIFICAND_WIDTH + 1;
        int exp_adjust = 0;
        int scale_increment = 0;
        double exp_delta = Double.NaN;
        if(scale_factor < 0) {
            scale_factor = Math.max(scale_factor, -MAX_SCALE);
            scale_increment = -512;
            exp_delta = twoToTheDoubleScaleDown;
        }
        else {
            scale_factor = Math.min(scale_factor, MAX_SCALE);
            scale_increment = 512;
            exp_delta = twoToTheDoubleScaleUp;
        }
        int t = (scale_factor >> 9-1) >>> 32 - 9;
        exp_adjust = ((scale_factor + t) & (512 -1)) - t;
        d *= powerOfTwoD(exp_adjust);
        scale_factor -= exp_adjust;
        while(scale_factor != 0) {
            d *= exp_delta;
            scale_factor -= scale_increment;
        }
        return d;
    }
     public static float scalb(float f, int scale_factor) {
        final int MAX_SCALE = FloatConsts.MAX_EXPONENT + -FloatConsts.MIN_EXPONENT +
                              FloatConsts.SIGNIFICAND_WIDTH + 1;
        scale_factor = Math.max(Math.min(scale_factor, MAX_SCALE), -MAX_SCALE);
        return (float)((double)f*powerOfTwoD(scale_factor));
    }
    public static double nextAfter(double start, double direction) {
        if (isNaN(start) || isNaN(direction)) {
            return start + direction;
        } else if (start == direction) {
            return direction;
        } else {        
            long transducer = Double.doubleToRawLongBits(start + 0.0d);
            if (direction > start) { 
                transducer = transducer + (transducer >= 0L ? 1L:-1L);
            } else  { 
                assert direction < start;
                if (transducer > 0L)
                    --transducer;
                else
                    if (transducer < 0L )
                        ++transducer;
                    else
                        transducer = DoubleConsts.SIGN_BIT_MASK | 1L;
            }
            return Double.longBitsToDouble(transducer);
        }
    }
     public static float nextAfter(float start, double direction) {
        if (isNaN(start) || isNaN(direction)) {
            return start + (float)direction;
        } else if (start == direction) {
            return (float)direction;
        } else {        
            int transducer = Float.floatToRawIntBits(start + 0.0f);
            if (direction > start) {
                transducer = transducer + (transducer >= 0 ? 1:-1);
            } else  { 
                assert direction < start;
                if (transducer > 0)
                    --transducer;
                else
                    if (transducer < 0 )
                        ++transducer;
                    else
                        transducer = FloatConsts.SIGN_BIT_MASK | 1;
            }
            return Float.intBitsToFloat(transducer);
        }
    }
    public static double nextUp(double d) {
        if( isNaN(d) || d == Double.POSITIVE_INFINITY)
            return d;
        else {
            d += 0.0d;
            return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                           ((d >= 0.0d)?+1L:-1L));
        }
    }
     public static float nextUp(float f) {
        if( isNaN(f) || f == FloatConsts.POSITIVE_INFINITY)
            return f;
        else {
            f += 0.0f;
            return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                        ((f >= 0.0f)?+1:-1));
        }
    }
    public static double nextDown(double d) {
        if( isNaN(d) || d == Double.NEGATIVE_INFINITY)
            return d;
        else {
            if (d == 0.0)
                return -Double.MIN_VALUE;
            else
                return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                               ((d > 0.0d)?-1L:+1L));
        }
    }
    public static double nextDown(float f) {
        if( isNaN(f) || f == Float.NEGATIVE_INFINITY)
            return f;
        else {
            if (f == 0.0f)
                return -Float.MIN_VALUE;
            else
                return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                            ((f > 0.0f)?-1:+1));
        }
    }
    public static double copySign(double magnitude, double sign) {
        return rawCopySign(magnitude, (isNaN(sign)?1.0d:sign));
    }
     public static float copySign(float magnitude, float sign) {
        return rawCopySign(magnitude, (isNaN(sign)?1.0f:sign));
    }
    public static double ulp(double d) {
        int exp = getExponent(d);
        switch(exp) {
        case DoubleConsts.MAX_EXPONENT+1:       
            return Math.abs(d);
        case DoubleConsts.MIN_EXPONENT-1:       
            return Double.MIN_VALUE;
        default:
            assert exp <= DoubleConsts.MAX_EXPONENT && exp >= DoubleConsts.MIN_EXPONENT;
            exp = exp - (DoubleConsts.SIGNIFICAND_WIDTH-1);
            if (exp >= DoubleConsts.MIN_EXPONENT) {
                return powerOfTwoD(exp);
            }
            else {
                return Double.longBitsToDouble(1L <<
                (exp - (DoubleConsts.MIN_EXPONENT - (DoubleConsts.SIGNIFICAND_WIDTH-1)) ));
            }
        }
    }
     public static float ulp(float f) {
        int exp = getExponent(f);
        switch(exp) {
        case FloatConsts.MAX_EXPONENT+1:        
            return Math.abs(f);
        case FloatConsts.MIN_EXPONENT-1:        
            return FloatConsts.MIN_VALUE;
        default:
            assert exp <= FloatConsts.MAX_EXPONENT && exp >= FloatConsts.MIN_EXPONENT;
            exp = exp - (FloatConsts.SIGNIFICAND_WIDTH-1);
            if (exp >= FloatConsts.MIN_EXPONENT) {
                return powerOfTwoF(exp);
            }
            else {
                return Float.intBitsToFloat(1 <<
                (exp - (FloatConsts.MIN_EXPONENT - (FloatConsts.SIGNIFICAND_WIDTH-1)) ));
            }
        }
     }
    public static double signum(double d) {
        return (d == 0.0 || isNaN(d))?d:copySign(1.0, d);
    }
    public static float signum(float f) {
        return (f == 0.0f || isNaN(f))?f:copySign(1.0f, f);
    }
}
