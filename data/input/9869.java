public class XRUtils {
    public static final int None = 0;
    public static final byte PictOpClear = 0;
    public static final byte PictOpSrc = 1;
    public static final byte PictOpDst = 2;
    public static final byte PictOpOver = 3;
    public static final byte PictOpOverReverse = 4;
    public static final byte PictOpIn = 5;
    public static final byte PictOpInReverse = 6;
    public static final byte PictOpOut = 7;
    public static final byte PictOpOutReverse = 8;
    public static final byte PictOpAtop = 9;
    public static final byte PictOpAtopReverse = 10;
    public static final byte PictOpXor = 11;
    public static final byte PictOpAdd = 12;
    public static final byte PictOpSaturate = 13;
    public static final int RepeatNone = 0;
    public static final int RepeatNormal = 1;
    public static final int RepeatPad = 2;
    public static final int RepeatReflect = 3;
    public static final int FAST = 0;
    public static final int GOOD = 1;
    public static final int BEST = 2;
    public static final byte[] FAST_NAME = "fast".getBytes();
    public static final byte[] GOOD_NAME = "good".getBytes();
    public static final byte[] BEST_NAME = "best".getBytes();
    public static final int PictStandardARGB32 = 0;
    public static final int PictStandardRGB24 = 1;
    public static final int PictStandardA8 = 2;
    public static final int PictStandardA4 = 3;
    public static final int PictStandardA1 = 4;
    public static int ATransOpToXRQuality(int affineTranformOp) {
        switch (affineTranformOp) {
        case AffineTransformOp.TYPE_NEAREST_NEIGHBOR:
            return FAST;
        case AffineTransformOp.TYPE_BILINEAR:
            return GOOD;
        case AffineTransformOp.TYPE_BICUBIC:
            return BEST;
        }
        return -1;
    }
    public static byte[] ATransOpToXRQualityName(int affineTranformOp) {
        switch (affineTranformOp) {
        case AffineTransformOp.TYPE_NEAREST_NEIGHBOR:
            return FAST_NAME;
        case AffineTransformOp.TYPE_BILINEAR:
            return GOOD_NAME;
        case AffineTransformOp.TYPE_BICUBIC:
            return BEST_NAME;
        }
        return null;
    }
    public static byte[] getFilterName(int filterType) {
        switch (filterType) {
        case FAST:
            return FAST_NAME;
        case GOOD:
            return GOOD_NAME;
        case BEST:
            return BEST_NAME;
        }
        return null;
    }
    public static int getPictureFormatForTransparency(int transparency) {
        switch (transparency) {
        case Transparency.OPAQUE:
            return PictStandardRGB24;
        case Transparency.BITMASK:
        case Transparency.TRANSLUCENT:
            return PictStandardARGB32;
        }
        return -1;
    }
    public static SurfaceType getXRSurfaceTypeForTransparency(int transparency) {
        if (transparency == Transparency.OPAQUE) {
            return SurfaceType.IntRgb;
        }else {
            return SurfaceType.IntArgbPre;
        }
    }
    public static int getRepeatForCycleMethod(CycleMethod cycleMethod) {
        if (cycleMethod.equals(CycleMethod.NO_CYCLE)) {
            return RepeatPad;
        } else if (cycleMethod.equals(CycleMethod.REFLECT)) {
            return RepeatReflect;
        } else if (cycleMethod.equals(CycleMethod.REPEAT)) {
            return RepeatNormal;
        }
        return RepeatNone;
    }
    public static int XDoubleToFixed(double dbl) {
        return (int) (dbl * 65536);
    }
    public static double XFixedToDouble(int fixed) {
        return ((double) fixed) / 65536;
    }
    public static int[] convertFloatsToFixed(float[] values) {
        int[] fixed = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            fixed[i] = XDoubleToFixed(values[i]);
        }
        return fixed;
    }
    public static long intToULong(int signed) {
        if (signed < 0) {
            return ((long) signed) + (((long) Integer.MAX_VALUE) -
                    ((long) Integer.MIN_VALUE) + 1);
        }
        return signed;
    }
    public static byte j2dAlphaCompToXR(int j2dRule) {
        switch (j2dRule) {
        case CLEAR:
            return PictOpClear;
        case SRC:
            return PictOpSrc;
        case DST:
            return PictOpDst;
        case SRC_OVER:
            return PictOpOver;
        case DST_OVER:
            return PictOpOverReverse;
        case SRC_IN:
            return PictOpIn;
        case DST_IN:
            return PictOpInReverse;
        case SRC_OUT:
            return PictOpOut;
        case DST_OUT:
            return PictOpOutReverse;
        case SRC_ATOP:
            return PictOpAtop;
        case DST_ATOP:
            return PictOpAtopReverse;
        case XOR:
            return PictOpXor;
        }
        throw new InternalError("No XRender equivalent available for requested java2d composition rule: "+j2dRule);
    }
    public static short clampToShort(int x) {
        return (short) (x > Short.MAX_VALUE
                           ? Short.MAX_VALUE
                           : (x < Short.MIN_VALUE ? Short.MIN_VALUE : x));
    }
    public static short clampToUShort(int x) {
        return (short) (x > 65535 ? 65535 : (x < 0) ? 0 : x);
    }
}
