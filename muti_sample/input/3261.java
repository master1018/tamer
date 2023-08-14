public class BufferedBufImgOps {
    public static void enableBufImgOp(RenderQueue rq, SurfaceData srcData,
                                      BufferedImage srcImg,
                                      BufferedImageOp biop)
    {
        if (biop instanceof ConvolveOp) {
            enableConvolveOp(rq, srcData, (ConvolveOp)biop);
        } else if (biop instanceof RescaleOp) {
            enableRescaleOp(rq, srcData, srcImg, (RescaleOp)biop);
        } else if (biop instanceof LookupOp) {
            enableLookupOp(rq, srcData, srcImg, (LookupOp)biop);
        } else {
            throw new InternalError("Unknown BufferedImageOp");
        }
    }
    public static void disableBufImgOp(RenderQueue rq, BufferedImageOp biop) {
        if (biop instanceof ConvolveOp) {
            disableConvolveOp(rq);
        } else if (biop instanceof RescaleOp) {
            disableRescaleOp(rq);
        } else if (biop instanceof LookupOp) {
            disableLookupOp(rq);
        } else {
            throw new InternalError("Unknown BufferedImageOp");
        }
    }
    public static boolean isConvolveOpValid(ConvolveOp cop) {
        Kernel kernel = cop.getKernel();
        int kw = kernel.getWidth();
        int kh = kernel.getHeight();
        if (!(kw == 3 && kh == 3) && !(kw == 5 && kh == 5)) {
            return false;
        }
        return true;
    }
    private static void enableConvolveOp(RenderQueue rq,
                                         SurfaceData srcData,
                                         ConvolveOp cop)
    {
        boolean edgeZero =
            cop.getEdgeCondition() == ConvolveOp.EDGE_ZERO_FILL;
        Kernel kernel = cop.getKernel();
        int kernelWidth = kernel.getWidth();
        int kernelHeight = kernel.getHeight();
        int kernelSize = kernelWidth * kernelHeight;
        int sizeofFloat = 4;
        int totalBytesRequired = 4 + 8 + 12 + (kernelSize * sizeofFloat);
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(totalBytesRequired, 4);
        buf.putInt(ENABLE_CONVOLVE_OP);
        buf.putLong(srcData.getNativeOps());
        buf.putInt(edgeZero ? 1 : 0);
        buf.putInt(kernelWidth);
        buf.putInt(kernelHeight);
        buf.put(kernel.getKernelData(null));
    }
    private static void disableConvolveOp(RenderQueue rq) {
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacity(4);
        buf.putInt(DISABLE_CONVOLVE_OP);
    }
    public static boolean isRescaleOpValid(RescaleOp rop,
                                           BufferedImage srcImg)
    {
        int numFactors = rop.getNumFactors();
        ColorModel srcCM = srcImg.getColorModel();
        if (srcCM instanceof IndexColorModel) {
            throw new
                IllegalArgumentException("Rescaling cannot be "+
                                         "performed on an indexed image");
        }
        if (numFactors != 1 &&
            numFactors != srcCM.getNumColorComponents() &&
            numFactors != srcCM.getNumComponents())
        {
            throw new IllegalArgumentException("Number of scaling constants "+
                                               "does not equal the number of"+
                                               " of color or color/alpha "+
                                               " components");
        }
        int csType = srcCM.getColorSpace().getType();
        if (csType != ColorSpace.TYPE_RGB &&
            csType != ColorSpace.TYPE_GRAY)
        {
            return false;
        }
        if (numFactors == 2 || numFactors > 4) {
            return false;
        }
        return true;
    }
    private static void enableRescaleOp(RenderQueue rq,
                                        SurfaceData srcData,
                                        BufferedImage srcImg,
                                        RescaleOp rop)
    {
        ColorModel srcCM = srcImg.getColorModel();
        boolean nonPremult =
            srcCM.hasAlpha() &&
            srcCM.isAlphaPremultiplied();
        int numFactors = rop.getNumFactors();
        float[] origScaleFactors = rop.getScaleFactors(null);
        float[] origOffsets = rop.getOffsets(null);
        float[] normScaleFactors;
        float[] normOffsets;
        if (numFactors == 1) {
            normScaleFactors = new float[4];
            normOffsets      = new float[4];
            for (int i = 0; i < 3; i++) {
                normScaleFactors[i] = origScaleFactors[0];
                normOffsets[i]      = origOffsets[0];
            }
            normScaleFactors[3] = 1.0f;
            normOffsets[3]      = 0.0f;
        } else if (numFactors == 3) {
            normScaleFactors = new float[4];
            normOffsets      = new float[4];
            for (int i = 0; i < 3; i++) {
                normScaleFactors[i] = origScaleFactors[i];
                normOffsets[i]      = origOffsets[i];
            }
            normScaleFactors[3] = 1.0f;
            normOffsets[3]      = 0.0f;
        } else { 
            normScaleFactors = origScaleFactors;
            normOffsets      = origOffsets;
        }
        if (srcCM.getNumComponents() == 1) {
            int nBits = srcCM.getComponentSize(0);
            int maxValue = (1 << nBits) - 1;
            for (int i = 0; i < 3; i++) {
                normOffsets[i] /= maxValue;
            }
        } else {
            for (int i = 0; i < srcCM.getNumComponents(); i++) {
                int nBits = srcCM.getComponentSize(i);
                int maxValue = (1 << nBits) - 1;
                normOffsets[i] /= maxValue;
            }
        }
        int sizeofFloat = 4;
        int totalBytesRequired = 4 + 8 + 4 + (4 * sizeofFloat * 2);
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(totalBytesRequired, 4);
        buf.putInt(ENABLE_RESCALE_OP);
        buf.putLong(srcData.getNativeOps());
        buf.putInt(nonPremult ? 1 : 0);
        buf.put(normScaleFactors);
        buf.put(normOffsets);
    }
    private static void disableRescaleOp(RenderQueue rq) {
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacity(4);
        buf.putInt(DISABLE_RESCALE_OP);
    }
    public static boolean isLookupOpValid(LookupOp lop,
                                          BufferedImage srcImg)
    {
        LookupTable table = lop.getTable();
        int numComps = table.getNumComponents();
        ColorModel srcCM = srcImg.getColorModel();
        if (srcCM instanceof IndexColorModel) {
            throw new
                IllegalArgumentException("LookupOp cannot be "+
                                         "performed on an indexed image");
        }
        if (numComps != 1 &&
            numComps != srcCM.getNumComponents() &&
            numComps != srcCM.getNumColorComponents())
        {
            throw new IllegalArgumentException("Number of arrays in the "+
                                               " lookup table ("+
                                               numComps+
                                               ") is not compatible with"+
                                               " the src image: "+srcImg);
        }
        int csType = srcCM.getColorSpace().getType();
        if (csType != ColorSpace.TYPE_RGB &&
            csType != ColorSpace.TYPE_GRAY)
        {
            return false;
        }
        if (numComps == 2 || numComps > 4) {
            return false;
        }
        if (table instanceof ByteLookupTable) {
            byte[][] data = ((ByteLookupTable)table).getTable();
            for (int i = 1; i < data.length; i++) {
                if (data[i].length > 256 ||
                    data[i].length != data[i-1].length)
                {
                    return false;
                }
            }
        } else if (table instanceof ShortLookupTable) {
            short[][] data = ((ShortLookupTable)table).getTable();
            for (int i = 1; i < data.length; i++) {
                if (data[i].length > 256 ||
                    data[i].length != data[i-1].length)
                {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    private static void enableLookupOp(RenderQueue rq,
                                       SurfaceData srcData,
                                       BufferedImage srcImg,
                                       LookupOp lop)
    {
        boolean nonPremult =
            srcImg.getColorModel().hasAlpha() &&
            srcImg.isAlphaPremultiplied();
        LookupTable table = lop.getTable();
        int numBands = table.getNumComponents();
        int offset = table.getOffset();
        int bandLength;
        int bytesPerElem;
        boolean shortData;
        if (table instanceof ShortLookupTable) {
            short[][] data = ((ShortLookupTable)table).getTable();
            bandLength = data[0].length;
            bytesPerElem = 2;
            shortData = true;
        } else { 
            byte[][] data = ((ByteLookupTable)table).getTable();
            bandLength = data[0].length;
            bytesPerElem = 1;
            shortData = false;
        }
        int totalLutBytes = numBands * bandLength * bytesPerElem;
        int paddedLutBytes = (totalLutBytes + 3) & (~3);
        int padding = paddedLutBytes - totalLutBytes;
        int totalBytesRequired = 4 + 8 + 20 + paddedLutBytes;
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(totalBytesRequired, 4);
        buf.putInt(ENABLE_LOOKUP_OP);
        buf.putLong(srcData.getNativeOps());
        buf.putInt(nonPremult ? 1 : 0);
        buf.putInt(shortData ? 1 : 0);
        buf.putInt(numBands);
        buf.putInt(bandLength);
        buf.putInt(offset);
        if (shortData) {
            short[][] data = ((ShortLookupTable)table).getTable();
            for (int i = 0; i < numBands; i++) {
                buf.put(data[i]);
            }
        } else {
            byte[][] data = ((ByteLookupTable)table).getTable();
            for (int i = 0; i < numBands; i++) {
                buf.put(data[i]);
            }
        }
        if (padding != 0) {
            buf.position(buf.position() + padding);
        }
    }
    private static void disableLookupOp(RenderQueue rq) {
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacity(4);
        buf.putInt(DISABLE_LOOKUP_OP);
    }
}
