public class BandCombineOp implements RasterOp {
    static final int offsets3c[] = {
            16, 8, 0
    };
    static final int offsets4ac[] = {
            16, 8, 0, 24
    };
    static final int masks3c[] = {
            0xFF0000, 0xFF00, 0xFF
    };
    static final int masks4ac[] = {
            0xFF0000, 0xFF00, 0xFF, 0xFF000000
    };
    private static final int piOffsets[] = {
            0, 1, 2
    };
    private static final int piInvOffsets[] = {
            2, 1, 0
    };
    private static final int TYPE_BYTE3C = 0;
    private static final int TYPE_BYTE4AC = 1;
    private static final int TYPE_USHORT3C = 2;
    private static final int TYPE_SHORT3C = 3;
    private int mxWidth;
    private int mxHeight;
    private float matrix[][];
    private RenderingHints rHints;
    static {
    }
    public BandCombineOp(float matrix[][], RenderingHints hints) {
        this.mxHeight = matrix.length;
        this.mxWidth = matrix[0].length;
        this.matrix = new float[mxHeight][mxWidth];
        for (int i = 0; i < mxHeight; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, mxWidth);
        }
        this.rHints = hints;
    }
    public final RenderingHints getRenderingHints() {
        return this.rHints;
    }
    public final float[][] getMatrix() {
        float res[][] = new float[mxHeight][mxWidth];
        for (int i = 0; i < mxHeight; i++) {
            System.arraycopy(matrix[i], 0, res[i], 0, mxWidth);
        }
        return res;
    }
    public final Point2D getPoint2D(Point2D srcPoint, Point2D dstPoint) {
        if (dstPoint == null) {
            dstPoint = new Point2D.Float();
        }
        dstPoint.setLocation(srcPoint);
        return dstPoint;
    }
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }
    public WritableRaster createCompatibleDestRaster(Raster src) {
        int numBands = src.getNumBands();
        if (mxWidth != numBands && mxWidth != (numBands + 1) || numBands != mxHeight) {
            throw new IllegalArgumentException(Messages.getString("awt.254", 
                    new Object[] {
                            numBands, mxWidth, mxHeight
                    }));
        }
        return src.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
    }
    public WritableRaster filter(Raster src, WritableRaster dst) {
        int numBands = src.getNumBands();
        if (mxWidth != numBands && mxWidth != (numBands + 1)) {
            throw new IllegalArgumentException(Messages.getString("awt.254", 
                    new Object[] {
                            numBands, mxWidth, mxHeight
                    }));
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        } else if (dst.getNumBands() != mxHeight) {
            throw new IllegalArgumentException(Messages.getString("awt.255", 
                    new Object[] {
                            dst.getNumBands(), mxWidth, mxHeight
                    }));
        }
        if (verySlowFilter(src, dst) != 0) {
            throw new ImagingOpException(Messages.getString("awt.21F")); 
        }
        return dst;
    }
    private static final class SampleModelInfo {
        int channels;
        int channelsOrder[];
        int stride;
    }
    private final SampleModelInfo checkSampleModel(SampleModel sm) {
        SampleModelInfo ret = new SampleModelInfo();
        if (sm instanceof PixelInterleavedSampleModel) {
            if (sm.getDataType() != DataBuffer.TYPE_BYTE) {
                return null;
            }
            ret.channels = sm.getNumBands();
            ret.stride = ((ComponentSampleModel)sm).getScanlineStride();
            ret.channelsOrder = ((ComponentSampleModel)sm).getBandOffsets();
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm1 = (SinglePixelPackedSampleModel)sm;
            ret.channels = sppsm1.getNumBands();
            if (sppsm1.getDataType() != DataBuffer.TYPE_INT) {
                return null;
            }
            for (int i = 0; i < ret.channels; i++) {
                if (sppsm1.getSampleSize(i) != 8) {
                    return null;
                }
            }
            ret.channelsOrder = new int[ret.channels];
            int bitOffsets[] = sppsm1.getBitOffsets();
            for (int i = 0; i < ret.channels; i++) {
                if (bitOffsets[i] % 8 != 0) {
                    return null;
                }
                ret.channelsOrder[i] = bitOffsets[i] / 8;
            }
            ret.channels = 4;
            ret.stride = sppsm1.getScanlineStride() * 4;
        } else {
            return null;
        }
        return ret;
    }
    private final int slowFilter(Raster src, WritableRaster dst) {
        int res = 0;
        SampleModelInfo srcInfo, dstInfo;
        int offsets[] = null;
        srcInfo = checkSampleModel(src.getSampleModel());
        dstInfo = checkSampleModel(dst.getSampleModel());
        if (srcInfo == null || dstInfo == null) {
            return verySlowFilter(src, dst);
        }
        if (src.getParent() != null || dst.getParent() != null) {
            if (src.getSampleModelTranslateX() != 0 || src.getSampleModelTranslateY() != 0
                    || dst.getSampleModelTranslateX() != 0 || dst.getSampleModelTranslateY() != 0) {
                offsets = new int[4];
                offsets[0] = -src.getSampleModelTranslateX() + src.getMinX();
                offsets[1] = -src.getSampleModelTranslateY() + src.getMinY();
                offsets[2] = -dst.getSampleModelTranslateX() + dst.getMinX();
                offsets[3] = -dst.getSampleModelTranslateY() + dst.getMinY();
            }
        }
        int rmxWidth = (srcInfo.channels + 1); 
        float reorderedMatrix[] = new float[rmxWidth * dstInfo.channels];
        for (int j = 0; j < dstInfo.channels; j++) {
            if (j >= dstInfo.channelsOrder.length) {
                continue;
            }
            for (int i = 0; i < srcInfo.channels; i++) {
                if (i >= srcInfo.channelsOrder.length) {
                    break;
                }
                reorderedMatrix[dstInfo.channelsOrder[j] * rmxWidth + srcInfo.channelsOrder[i]] = matrix[j][i];
            }
            if (mxWidth == rmxWidth) {
                reorderedMatrix[(dstInfo.channelsOrder[j] + 1) * rmxWidth - 1] = matrix[j][mxWidth - 1];
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
        simpleCombineBands(srcData, src.getWidth(), src.getHeight(), srcInfo.stride,
                srcInfo.channels, dstData, dstInfo.stride, dstInfo.channels, reorderedMatrix,
                offsets);
        return res;
    }
    private int verySlowFilter(Raster src, WritableRaster dst) {
        int numBands = src.getNumBands();
        int srcMinX = src.getMinX();
        int srcY = src.getMinY();
        int dstMinX = dst.getMinX();
        int dstY = dst.getMinY();
        int dX = src.getWidth();
        int dY = src.getHeight();
        float sample;
        int srcPixels[] = new int[numBands * dX * dY];
        int dstPixels[] = new int[mxHeight * dX * dY];
        srcPixels = src.getPixels(srcMinX, srcY, dX, dY, srcPixels);
        if (numBands == mxWidth) {
            for (int i = 0, j = 0; i < srcPixels.length; i += numBands) {
                for (int dstB = 0; dstB < mxHeight; dstB++) {
                    sample = 0f;
                    for (int srcB = 0; srcB < numBands; srcB++) {
                        sample += matrix[dstB][srcB] * srcPixels[i + srcB];
                    }
                    dstPixels[j++] = (int)sample;
                }
            }
        } else {
            for (int i = 0, j = 0; i < srcPixels.length; i += numBands) {
                for (int dstB = 0; dstB < mxHeight; dstB++) {
                    sample = 0f;
                    for (int srcB = 0; srcB < numBands; srcB++) {
                        sample += matrix[dstB][srcB] * srcPixels[i + srcB];
                    }
                    dstPixels[j++] = (int)(sample + matrix[dstB][numBands]);
                }
            }
        }
        dst.setPixels(dstMinX, dstY, dX, dY, dstPixels);
        return 0;
    }
    @SuppressWarnings("unused")
    private int ippFilter(Raster src, WritableRaster dst) {
        boolean invertChannels;
        boolean inPlace = (src == dst);
        int type;
        int srcStride, dstStride;
        int offsets[] = null;
        int srcBands = src.getNumBands();
        int dstBands = dst.getNumBands();
        if (dstBands != 3
                || (srcBands != 3 && !(srcBands == 4 && matrix[0][3] == 0 && matrix[1][3] == 0 && matrix[2][3] == 0))) {
            return slowFilter(src, dst);
        }
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        if (srcSM instanceof SinglePixelPackedSampleModel
                && dstSM instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm1 = (SinglePixelPackedSampleModel)srcSM;
            SinglePixelPackedSampleModel sppsm2 = (SinglePixelPackedSampleModel)dstSM;
            if (sppsm1.getDataType() != DataBuffer.TYPE_INT
                    || sppsm2.getDataType() != DataBuffer.TYPE_INT) {
                return slowFilter(src, dst);
            }
            if (!Arrays.equals(sppsm2.getBitOffsets(), offsets3c)
                    || !Arrays.equals(sppsm2.getBitMasks(), masks3c)) {
                return slowFilter(src, dst);
            }
            if (srcBands == 3) {
                if (!Arrays.equals(sppsm1.getBitOffsets(), offsets3c)
                        || !Arrays.equals(sppsm1.getBitMasks(), masks3c)) {
                    return slowFilter(src, dst);
                }
            } else if (srcBands == 4) {
                if (!Arrays.equals(sppsm1.getBitOffsets(), offsets4ac)
                        || !Arrays.equals(sppsm1.getBitMasks(), masks4ac)) {
                    return slowFilter(src, dst);
                }
            }
            type = TYPE_BYTE4AC;
            invertChannels = true;
            srcStride = sppsm1.getScanlineStride() * 4;
            dstStride = sppsm2.getScanlineStride() * 4;
        } else if (srcSM instanceof PixelInterleavedSampleModel
                && dstSM instanceof PixelInterleavedSampleModel) {
            if (srcBands != 3) {
                return slowFilter(src, dst);
            }
            int srcDataType = srcSM.getDataType();
            switch (srcDataType) {
                case DataBuffer.TYPE_BYTE:
                    type = TYPE_BYTE3C;
                    break;
                case DataBuffer.TYPE_USHORT:
                    type = TYPE_USHORT3C;
                    break;
                case DataBuffer.TYPE_SHORT:
                    type = TYPE_SHORT3C;
                    break;
                default:
                    return slowFilter(src, dst);
            }
            PixelInterleavedSampleModel pism1 = (PixelInterleavedSampleModel)srcSM;
            PixelInterleavedSampleModel pism2 = (PixelInterleavedSampleModel)dstSM;
            if (srcDataType != pism2.getDataType() || pism1.getPixelStride() != 3
                    || pism2.getPixelStride() != 3
                    || !Arrays.equals(pism1.getBandOffsets(), pism2.getBandOffsets())) {
                return slowFilter(src, dst);
            }
            if (Arrays.equals(pism1.getBandOffsets(), piInvOffsets)) {
                invertChannels = true;
            } else if (Arrays.equals(pism1.getBandOffsets(), piOffsets)) {
                invertChannels = false;
            } else {
                return slowFilter(src, dst);
            }
            int dataTypeSize = DataBuffer.getDataTypeSize(srcDataType) / 8;
            srcStride = pism1.getScanlineStride() * dataTypeSize;
            dstStride = pism2.getScanlineStride() * dataTypeSize;
        } else { 
            return slowFilter(src, dst);
        }
        if (src.getParent() != null || dst.getParent() != null) {
            if (src.getSampleModelTranslateX() != 0 || src.getSampleModelTranslateY() != 0
                    || dst.getSampleModelTranslateX() != 0 || dst.getSampleModelTranslateY() != 0) {
                offsets = new int[4];
                offsets[0] = -src.getSampleModelTranslateX() + src.getMinX();
                offsets[1] = -src.getSampleModelTranslateY() + src.getMinY();
                offsets[2] = -dst.getSampleModelTranslateX() + dst.getMinX();
                offsets[3] = -dst.getSampleModelTranslateY() + dst.getMinY();
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
        float ippMatrix[] = new float[12];
        if (invertChannels) {
            for (int i = 0; i < mxHeight; i++) {
                ippMatrix[i * 4] = matrix[2 - i][2];
                ippMatrix[i * 4 + 1] = matrix[2 - i][1];
                ippMatrix[i * 4 + 2] = matrix[2 - i][0];
                if (mxWidth == 4) {
                    ippMatrix[i * 4 + 3] = matrix[2 - i][3];
                } else if (mxWidth == 5) {
                    ippMatrix[i * 4 + 3] = matrix[2 - i][4];
                }
            }
        } else {
            for (int i = 0; i < mxHeight; i++) {
                ippMatrix[i * 4] = matrix[i][0];
                ippMatrix[i * 4 + 1] = matrix[i][1];
                ippMatrix[i * 4 + 2] = matrix[i][2];
                if (mxWidth == 4) {
                    ippMatrix[i * 4 + 3] = matrix[i][3];
                } else if (mxWidth == 5) {
                    ippMatrix[i * 4 + 3] = matrix[i][4];
                }
            }
        }
        return ippColorTwist(srcData, src.getWidth(), src.getHeight(), srcStride, dstData, dst
                .getWidth(), dst.getHeight(), dstStride, ippMatrix, type, offsets, inPlace);
    }
    private final native int ippColorTwist(Object srcData, int srcWidth, int srcHeight,
            int srcStride, Object dstData, int dstWidth, int dstHeight, int dstStride,
            float ippMatrix[], int type, int offsets[], boolean inPlace);
    private final native int simpleCombineBands(Object srcData, int srcWidth, int srcHeight,
            int srcStride, int srcChannels, Object dstData, int dstStride, int dstChannels,
            float m[], int offsets[]);
}
