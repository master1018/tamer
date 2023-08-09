public class ConvolveOp implements BufferedImageOp, RasterOp {
    public static final int EDGE_ZERO_FILL = 0;
    public static final int EDGE_NO_OP = 1;
    private Kernel kernel;
    private int edgeCond;
    private RenderingHints rhs = null;
    static {
    }
    public ConvolveOp(Kernel kernel, int edgeCondition, RenderingHints hints) {
        this.kernel = kernel;
        this.edgeCond = edgeCondition;
        this.rhs = hints;
    }
    public ConvolveOp(Kernel kernel) {
        this.kernel = kernel;
        this.edgeCond = EDGE_ZERO_FILL;
    }
    public final Kernel getKernel() {
        return (Kernel)kernel.clone();
    }
    public final RenderingHints getRenderingHints() {
        return rhs;
    }
    public int getEdgeCondition() {
        return edgeCond;
    }
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return getBounds2D(src.getRaster());
    }
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt);
        return dstPt;
    }
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        if (dstCM instanceof IndexColorModel) {
            dstCM = ColorModel.getRGBdefault();
        }
        WritableRaster r = dstCM.isCompatibleSampleModel(src.getSampleModel()) ? src.getRaster()
                .createCompatibleWritableRaster(src.getWidth(), src.getHeight()) : dstCM
                .createCompatibleWritableRaster(src.getWidth(), src.getHeight());
        return new BufferedImage(dstCM, r, dstCM.isAlphaPremultiplied(), null);
    }
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (src == null) { 
            throw new NullPointerException(Messages.getString("awt.256")); 
        }
        if (src == dst) {
            throw new IllegalArgumentException(Messages.getString("awt.257")); 
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        } else if (src.getNumBands() != dst.getNumBands()) {
            throw new IllegalArgumentException(Messages.getString(
                    "awt.258", src.getNumBands(), dst.getNumBands())); 
        }
        if (slowFilter(src, dst) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        return dst;
    }
    private int slowFilter(Raster src, WritableRaster dst) {
        try {
            SampleModel sm = src.getSampleModel();
            int numBands = src.getNumBands();
            int srcHeight = src.getHeight();
            int srcWidth = src.getWidth();
            int xOrigin = kernel.getXOrigin();
            int yOrigin = kernel.getYOrigin();
            int kWidth = kernel.getWidth();
            int kHeight = kernel.getHeight();
            float[] data = kernel.getKernelData(null);
            int srcMinX = src.getMinX();
            int srcMinY = src.getMinY();
            int dstMinX = dst.getMinX();
            int dstMinY = dst.getMinY();
            int srcConvMaxX = srcWidth - (kWidth - xOrigin - 1);
            int srcConvMaxY = srcHeight - (kHeight - yOrigin - 1);
            int[] maxValues = new int[numBands];
            int[] masks = new int[numBands];
            int[] sampleSizes = sm.getSampleSize();
            for (int i = 0; i < numBands; i++) {
                maxValues[i] = (1 << sampleSizes[i]) - 1;
                masks[i] = ~(maxValues[i]);
            }
            float[] pixels = null;
            pixels = src.getPixels(srcMinX, srcMinY, srcWidth, srcHeight, pixels);
            float[] newPixels = new float[pixels.length];
            int rowLength = srcWidth * numBands;
            if (this.edgeCond == ConvolveOp.EDGE_NO_OP) {
                int start = 0;
                int length = yOrigin * rowLength;
                System.arraycopy(pixels, start, newPixels, start, length);
                start = (srcHeight - (kHeight - yOrigin - 1)) * rowLength;
                length = (kHeight - yOrigin - 1) * rowLength;
                System.arraycopy(pixels, start, newPixels, start, length);
                length = xOrigin * numBands;
                int length1 = (kWidth - xOrigin - 1) * numBands;
                start = yOrigin * rowLength;
                int start1 = (yOrigin + 1) * rowLength - length1;
                for (int i = yOrigin; i < (srcHeight - (kHeight - yOrigin - 1)); i++) {
                    System.arraycopy(pixels, start, newPixels, start, length);
                    System.arraycopy(pixels, start1, newPixels, start1, length1);
                    start += rowLength;
                    start1 += rowLength;
                }
            }
            for (int i = yOrigin; i < srcConvMaxY; i++) {
                for (int j = xOrigin; j < srcConvMaxX; j++) {
                    int kernelIdx = data.length - 1;
                    int pixelIndex = i * rowLength + j * numBands;
                    for (int hIdx = 0, rasterHIdx = i - yOrigin; hIdx < kHeight; hIdx++, rasterHIdx++) {
                        for (int wIdx = 0, rasterWIdx = j - xOrigin; wIdx < kWidth; wIdx++, rasterWIdx++) {
                            int curIndex = rasterHIdx * rowLength + rasterWIdx * numBands;
                            for (int idx = 0; idx < numBands; idx++) {
                                newPixels[pixelIndex + idx] += data[kernelIdx]
                                        * pixels[curIndex + idx];
                            }
                            kernelIdx--;
                        }
                    }
                    for (int idx = 0; idx < numBands; idx++) {
                        if (((int)newPixels[pixelIndex + idx] & masks[idx]) != 0) {
                            if (newPixels[pixelIndex + idx] < 0) {
                                newPixels[pixelIndex + idx] = 0;
                            } else {
                                newPixels[pixelIndex + idx] = maxValues[idx];
                            }
                        }
                    }
                }
            }
            dst.setPixels(dstMinX, dstMinY, srcWidth, srcHeight, newPixels);
        } catch (Exception e) { 
            return 1;
        }
        return 0;
    }
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == null) {
            throw new NullPointerException(Messages.getString("awt.259")); 
        }
        if (src == dst) {
            throw new IllegalArgumentException(Messages.getString("awt.25A")); 
        }
        ColorModel srcCM = src.getColorModel();
        BufferedImage finalDst = null;
        if (srcCM instanceof IndexColorModel) {
            src = ((IndexColorModel)srcCM).convertToIntDiscrete(src.getRaster(), true);
            srcCM = src.getColorModel();
        }
        if (dst == null) {
            dst = createCompatibleDestImage(src, srcCM);
        } else {
            if (!srcCM.equals(dst.getColorModel())) {
                if (!((src.getType() == BufferedImage.TYPE_INT_RGB || src.getType() == BufferedImage.TYPE_INT_ARGB) && (dst
                        .getType() == BufferedImage.TYPE_INT_RGB || dst.getType() == BufferedImage.TYPE_INT_ARGB))) {
                    finalDst = dst;
                    dst = createCompatibleDestImage(src, srcCM);
                }
            }
        }
        if (slowFilter(src.getRaster(), dst.getRaster()) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        if (finalDst != null) {
            Graphics2D g = finalDst.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(dst, 0, 0, null);
        } else {
            finalDst = dst;
        }
        return finalDst;
    }
    @SuppressWarnings("unused")
    private int ippFilter(Raster src, WritableRaster dst, int imageType) {
        int srcStride, dstStride;
        boolean skipChannel = false;
        int channels;
        int offsets[] = null;
        switch (imageType) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                skipChannel = true;
                break;
            }
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: {
                channels = 4;
                srcStride = src.getWidth() * 4;
                dstStride = dst.getWidth() * 4;
                break;
            }
            case BufferedImage.TYPE_BYTE_GRAY: {
                channels = 1;
                srcStride = src.getWidth();
                dstStride = dst.getWidth();
                break;
            }
            case BufferedImage.TYPE_3BYTE_BGR: {
                channels = 3;
                srcStride = src.getWidth() * 3;
                dstStride = dst.getWidth() * 3;
                break;
            }
            case BufferedImage.TYPE_USHORT_GRAY: 
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_BYTE_BINARY: {
                return slowFilter(src, dst);
            }
            default: {
                SampleModel srcSM = src.getSampleModel();
                SampleModel dstSM = dst.getSampleModel();
                if (srcSM instanceof PixelInterleavedSampleModel
                        && dstSM instanceof PixelInterleavedSampleModel) {
                    if (srcSM.getDataType() != DataBuffer.TYPE_BYTE
                            || dstSM.getDataType() != DataBuffer.TYPE_BYTE) {
                        return slowFilter(src, dst);
                    }
                    channels = srcSM.getNumBands(); 
                    if (!(channels == 1 || channels == 3 || channels == 4)) {
                        return slowFilter(src, dst);
                    }
                    srcStride = ((ComponentSampleModel)srcSM).getScanlineStride();
                    dstStride = ((ComponentSampleModel)dstSM).getScanlineStride();
                } else if (srcSM instanceof SinglePixelPackedSampleModel
                        && dstSM instanceof SinglePixelPackedSampleModel) {
                    SinglePixelPackedSampleModel sppsm1 = (SinglePixelPackedSampleModel)srcSM;
                    SinglePixelPackedSampleModel sppsm2 = (SinglePixelPackedSampleModel)dstSM;
                    channels = sppsm1.getNumBands();
                    if (sppsm1.getDataType() != DataBuffer.TYPE_INT
                            || sppsm2.getDataType() != DataBuffer.TYPE_INT
                            || !(channels == 3 || channels == 4)) {
                        return slowFilter(src, dst);
                    }
                    if (!Arrays.equals(sppsm1.getBitOffsets(), sppsm2.getBitOffsets())
                            || !Arrays.equals(sppsm1.getBitMasks(), sppsm2.getBitMasks())) {
                        return slowFilter(src, dst);
                    }
                    for (int i = 0; i < channels; i++) {
                        if (sppsm1.getSampleSize(i) != 8) {
                            return slowFilter(src, dst);
                        }
                    }
                    if (channels == 3) { 
                        channels = 4;
                    }
                    srcStride = sppsm1.getScanlineStride() * 4;
                    dstStride = sppsm2.getScanlineStride() * 4;
                } else {
                    return slowFilter(src, dst);
                }
                if (src.getParent() != null || dst.getParent() != null) {
                    if (src.getSampleModelTranslateX() != 0 || src.getSampleModelTranslateY() != 0
                            || dst.getSampleModelTranslateX() != 0
                            || dst.getSampleModelTranslateY() != 0) {
                        offsets = new int[4];
                        offsets[0] = -src.getSampleModelTranslateX() + src.getMinX();
                        offsets[1] = -src.getSampleModelTranslateY() + src.getMinY();
                        offsets[2] = -dst.getSampleModelTranslateX() + dst.getMinX();
                        offsets[3] = -dst.getSampleModelTranslateY() + dst.getMinY();
                    }
                }
            }
        }
        Object srcData, dstData;
        AwtImageBackdoorAccessor dbAccess = AwtImageBackdoorAccessor.getInstance();
        try {
            srcData = dbAccess.getData(src.getDataBuffer());
            dstData = dbAccess.getData(dst.getDataBuffer());
        } catch (IllegalArgumentException e) {
            return -1; 
        }
        return ippFilter32f(kernel.data, kernel.getWidth(), kernel.getHeight(),
                kernel.getXOrigin(), kernel.getYOrigin(), edgeCond, srcData, src.getWidth(), src
                        .getHeight(), srcStride, dstData, dst.getWidth(), dst.getHeight(),
                dstStride, channels, skipChannel, offsets);
    }
    private native int ippFilter32f(float kernel[], int kWidth, int kHeight, int anchorX,
            int anchorY, int borderType, Object src, int srcWidth, int srcHeight, int srcStride,
            Object dst, int dstWidth, int dstHeight, int dstStride, int channels,
            boolean skipChannel, int offsets[]);
}
