public class FloatConsts {
    private FloatConsts() {}
    public static final float POSITIVE_INFINITY = java.lang.Float.POSITIVE_INFINITY;
    public static final float NEGATIVE_INFINITY = java.lang.Float.NEGATIVE_INFINITY;
    public static final float NaN = java.lang.Float.NaN;
    public static final float MAX_VALUE = java.lang.Float.MAX_VALUE;
    public static final float MIN_VALUE = java.lang.Float.MIN_VALUE;
    public static final float   MIN_NORMAL      = 1.17549435E-38f;
    public static final int SIGNIFICAND_WIDTH   = 24;
    public static final int     MAX_EXPONENT    = 127;
    public static final int     MIN_EXPONENT    = -126;
    public static final int     MIN_SUB_EXPONENT = MIN_EXPONENT -
                                                   (SIGNIFICAND_WIDTH - 1);
    public static final int     EXP_BIAS        = 127;
    public static final int     SIGN_BIT_MASK   = 0x80000000;
    public static final int     EXP_BIT_MASK    = 0x7F800000;
    public static final int     SIGNIF_BIT_MASK = 0x007FFFFF;
    static {
        assert(((SIGN_BIT_MASK | EXP_BIT_MASK | SIGNIF_BIT_MASK) == ~0) &&
               (((SIGN_BIT_MASK & EXP_BIT_MASK) == 0) &&
                ((SIGN_BIT_MASK & SIGNIF_BIT_MASK) == 0) &&
                ((EXP_BIT_MASK & SIGNIF_BIT_MASK) == 0)));
    }
}
