public class ImagingLib {
    static boolean useLib = true;
    static boolean verbose = false;
    private static final int NUM_NATIVE_OPS = 3;
    private static final int LOOKUP_OP   = 0;
    private static final int AFFINE_OP   = 1;
    private static final int CONVOLVE_OP = 2;
    private static Class[] nativeOpClass = new Class[NUM_NATIVE_OPS];
    private static native boolean init();
    static public native int transformBI(BufferedImage src, BufferedImage dst,
                                         double[] matrix, int interpType);
    static public native int transformRaster(Raster src, Raster dst,
                                             double[] matrix,
                                             int interpType);
    static public native int convolveBI(BufferedImage src, BufferedImage dst,
                                        Kernel kernel, int edgeHint);
    static public native int convolveRaster(Raster src, Raster dst,
                                            Kernel kernel, int edgeHint);
    static public native int lookupByteBI(BufferedImage src, BufferedImage dst,
                                        byte[][] table);
    static public native int lookupByteRaster(Raster src, Raster dst,
                                              byte[][] table);
    static {
        PrivilegedAction<Boolean> doMlibInitialization =
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    String arch = System.getProperty("os.arch");
                    if (arch == null || !arch.startsWith("sparc")) {
                        try {
                            System.loadLibrary("mlib_image");
                        } catch (UnsatisfiedLinkError e) {
                            return Boolean.FALSE;
                        }
                    }
                    boolean success =  init();
                    return Boolean.valueOf(success);
                }
            };
        useLib = AccessController.doPrivileged(doMlibInitialization);
        try {
            nativeOpClass[LOOKUP_OP] =
                Class.forName("java.awt.image.LookupOp");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find class: "+e);
        }
        try {
            nativeOpClass[AFFINE_OP] =
                Class.forName("java.awt.image.AffineTransformOp");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find class: "+e);
        }
        try {
            nativeOpClass[CONVOLVE_OP] =
                Class.forName("java.awt.image.ConvolveOp");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find class: "+e);
        }
    }
    private static int getNativeOpIndex(Class opClass) {
        int opIndex = -1;
        for (int i=0; i<NUM_NATIVE_OPS; i++) {
            if (opClass == nativeOpClass[i]) {
                opIndex = i;
                break;
            }
        }
        return opIndex;
    }
    public static WritableRaster filter(RasterOp op, Raster src,
                                        WritableRaster dst) {
        if (useLib == false) {
            return null;
        }
        if (dst == null) {
            dst = op.createCompatibleDestRaster(src);
        }
        WritableRaster retRaster = null;
        switch (getNativeOpIndex(op.getClass())) {
          case LOOKUP_OP:
            LookupTable table = ((LookupOp)op).getTable();
            if (table.getOffset() != 0) {
                return null;
            }
            if (table instanceof ByteLookupTable) {
                ByteLookupTable bt = (ByteLookupTable) table;
                if (lookupByteRaster(src, dst, bt.getTable()) > 0) {
                    retRaster = dst;
                }
            }
            break;
          case AFFINE_OP:
            AffineTransformOp bOp = (AffineTransformOp) op;
            double[] matrix = new double[6];
            bOp.getTransform().getMatrix(matrix);
            if (transformRaster(src, dst, matrix,
                                bOp.getInterpolationType()) > 0) {
                retRaster =  dst;
            }
            break;
          case CONVOLVE_OP:
            ConvolveOp cOp = (ConvolveOp) op;
            if (convolveRaster(src, dst,
                               cOp.getKernel(), cOp.getEdgeCondition()) > 0) {
                retRaster = dst;
            }
            break;
          default:
            break;
        }
        if (retRaster != null) {
            SunWritableRaster.markDirty(retRaster);
        }
        return retRaster;
    }
    public static BufferedImage filter(BufferedImageOp op, BufferedImage src,
                                       BufferedImage dst)
    {
        if (verbose) {
            System.out.println("in filter and op is "+op
                               + "bufimage is "+src+" and "+dst);
        }
        if (useLib == false) {
            return null;
        }
        if (dst == null) {
            dst = op.createCompatibleDestImage(src, null);
        }
        BufferedImage retBI = null;
        switch (getNativeOpIndex(op.getClass())) {
          case LOOKUP_OP:
            LookupTable table = ((LookupOp)op).getTable();
            if (table.getOffset() != 0) {
                return null;
            }
            if (table instanceof ByteLookupTable) {
                ByteLookupTable bt = (ByteLookupTable) table;
                if (lookupByteBI(src, dst, bt.getTable()) > 0) {
                    retBI = dst;
                }
            }
            break;
          case AFFINE_OP:
            AffineTransformOp bOp = (AffineTransformOp) op;
            double[] matrix = new double[6];
            AffineTransform xform = bOp.getTransform();
            bOp.getTransform().getMatrix(matrix);
            if (transformBI(src, dst, matrix,
                            bOp.getInterpolationType())>0) {
                retBI = dst;
            }
            break;
          case CONVOLVE_OP:
            ConvolveOp cOp = (ConvolveOp) op;
            if (convolveBI(src, dst, cOp.getKernel(),
                           cOp.getEdgeCondition()) > 0) {
                retBI = dst;
            }
            break;
          default:
            break;
        }
        if (retBI != null) {
            SunWritableRaster.markDirty(retBI);
        }
        return retBI;
    }
}
