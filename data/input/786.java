public class DoubleConsts {
    private DoubleConsts() {}
    public static final double POSITIVE_INFINITY = java.lang.Double.POSITIVE_INFINITY;
    public static final double NEGATIVE_INFINITY = java.lang.Double.NEGATIVE_INFINITY;
    public static final double NaN = java.lang.Double.NaN;
    public static final double MAX_VALUE = java.lang.Double.MAX_VALUE;
    public static final double MIN_VALUE = java.lang.Double.MIN_VALUE;
    public static final double  MIN_NORMAL      = 2.2250738585072014E-308;
    public static final int SIGNIFICAND_WIDTH   = 53;
    public static final int     MAX_EXPONENT    = 1023;
    public static final int     MIN_EXPONENT    = -1022;
    public static final int     MIN_SUB_EXPONENT = MIN_EXPONENT -
                                                   (SIGNIFICAND_WIDTH - 1);
    public static final int     EXP_BIAS        = 1023;
    public static final long    SIGN_BIT_MASK   = 0x8000000000000000L;
    public static final long    EXP_BIT_MASK    = 0x7FF0000000000000L;
    public static final long    SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
    static {
        assert(((SIGN_BIT_MASK | EXP_BIT_MASK | SIGNIF_BIT_MASK) == ~0L) &&
               (((SIGN_BIT_MASK & EXP_BIT_MASK) == 0L) &&
                ((SIGN_BIT_MASK & SIGNIF_BIT_MASK) == 0L) &&
                ((EXP_BIT_MASK & SIGNIF_BIT_MASK) == 0L)));
    }
}
