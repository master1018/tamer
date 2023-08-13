public class LCMSTransform implements ColorTransform {
    long ID;
    private int inFormatter;
    private int outFormatter;
    ICC_Profile[] profiles;
    long [] profileIDs;
    int renderType;
    int transformType;
    private int numInComponents = -1;
    private int numOutComponents = -1;
    private Object disposerReferent = new Object();
    static {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
    }
    public LCMSTransform(ICC_Profile profile, int renderType,
                         int transformType)
    {
        profiles = new ICC_Profile[1];
        profiles[0] = profile;
        profileIDs = new long[1];
        profileIDs[0] = LCMS.getProfileID(profile);
        this.renderType = (renderType == ColorTransform.Any)?
                              ICC_Profile.icPerceptual : renderType;
        this.transformType = transformType;
        numInComponents = profiles[0].getNumComponents();
        numOutComponents = profiles[profiles.length - 1].getNumComponents();
    }
    public LCMSTransform (ColorTransform[] transforms) {
        int size = 0;
        for (int i=0; i < transforms.length; i++) {
            size+=((LCMSTransform)transforms[i]).profiles.length;
        }
        profiles = new ICC_Profile[size];
        profileIDs = new long[size];
        int j = 0;
        for (int i=0; i < transforms.length; i++) {
            LCMSTransform curTrans = (LCMSTransform)transforms[i];
            System.arraycopy(curTrans.profiles, 0, profiles, j,
                             curTrans.profiles.length);
            System.arraycopy(curTrans.profileIDs, 0, profileIDs, j,
                             curTrans.profileIDs.length);
            j += curTrans.profiles.length;
        }
        renderType = ((LCMSTransform)transforms[0]).renderType;
        numInComponents = profiles[0].getNumComponents();
        numOutComponents = profiles[profiles.length - 1].getNumComponents();
    }
    public int getNumInComponents() {
        return numInComponents;
    }
    public int getNumOutComponents() {
        return numOutComponents;
    }
    private synchronized void doTransform(LCMSImageLayout in,
                                          LCMSImageLayout out) {
        if (ID == 0L ||
            inFormatter != in.pixelType ||
            outFormatter != out.pixelType) {
            if (ID != 0L) {
                disposerReferent = new Object();
            }
            inFormatter = in.pixelType;
            outFormatter = out.pixelType;
            ID = LCMS.createNativeTransform(profileIDs, renderType,
                                            inFormatter, outFormatter,
                                            disposerReferent);
        }
        LCMS.colorConvert(this, in, out);
    }
    public void colorConvert(BufferedImage src, BufferedImage dst) {
        if (LCMSImageLayout.isSupported(src) &&
            LCMSImageLayout.isSupported(dst))
        {
            doTransform(new LCMSImageLayout(src), new LCMSImageLayout(dst));
            return;
        }
        LCMSImageLayout srcIL, dstIL;
        Raster srcRas = src.getRaster();
        WritableRaster dstRas = dst.getRaster();
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM = dst.getColorModel();
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        int precision = 8;
        float maxNum = 255.0f;
        for (int i = 0; i < srcNumComp; i++) {
            if (srcCM.getComponentSize(i) > 8) {
                 precision = 16;
                 maxNum = 65535.0f;
             }
        }
        for (int i = 0; i < dstNumComp; i++) {
            if (dstCM.getComponentSize(i) > 8) {
                 precision = 16;
                 maxNum = 65535.0f;
             }
        }
        float[] srcMinVal = new float[srcNumComp];
        float[] srcInvDiffMinMax = new float[srcNumComp];
        ColorSpace cs = srcCM.getColorSpace();
        for (int i = 0; i < srcNumComp; i++) {
            srcMinVal[i] = cs.getMinValue(i);
            srcInvDiffMinMax[i] = maxNum / (cs.getMaxValue(i) - srcMinVal[i]);
        }
        cs = dstCM.getColorSpace();
        float[] dstMinVal = new float[dstNumComp];
        float[] dstDiffMinMax = new float[dstNumComp];
        for (int i = 0; i < dstNumComp; i++) {
            dstMinVal[i] = cs.getMinValue(i);
            dstDiffMinMax[i] = (cs.getMaxValue(i) - dstMinVal[i]) / maxNum;
        }
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        float[] dstColor;
        if (dstHasAlpha) {
            dstColor = new float[dstNumComp + 1];
        } else {
            dstColor = new float[dstNumComp];
        }
        if (precision == 8) {
            byte[] srcLine = new byte[w * srcNumComp];
            byte[] dstLine = new byte[w * dstNumComp];
            Object pixel;
            float[] color;
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            int idx;
            srcIL = new LCMSImageLayout(
                srcLine, srcLine.length/getNumInComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
                LCMSImageLayout.BYTES_SH(1), getNumInComponents());
            dstIL = new LCMSImageLayout(
                dstLine, dstLine.length/getNumOutComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
                LCMSImageLayout.BYTES_SH(1), getNumOutComponents());
            for (int y = 0; y < h; y++) {
                pixel = null;
                color = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    for (int i = 0; i < srcNumComp; i++) {
                        srcLine[idx++] = (byte)
                            ((color[i] - srcMinVal[i]) * srcInvDiffMinMax[i] +
                             0.5f);
                    }
                    if (needSrcAlpha) {
                        alpha[x] = color[srcNumComp];
                    }
                }
                doTransform(srcIL, dstIL);
                pixel = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    for (int i = 0; i < dstNumComp; i++) {
                        dstColor[i] = ((float) (dstLine[idx++] & 0xff)) *
                                      dstDiffMinMax[i] + dstMinVal[i];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        } else {
            short[] srcLine = new short[w * srcNumComp];
            short[] dstLine = new short[w * dstNumComp];
            Object pixel;
            float[] color;
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            int idx;
            srcIL = new LCMSImageLayout(
                srcLine, srcLine.length/getNumInComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
                LCMSImageLayout.BYTES_SH(2), getNumInComponents()*2);
            dstIL = new LCMSImageLayout(
                dstLine, dstLine.length/getNumOutComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
                LCMSImageLayout.BYTES_SH(2), getNumOutComponents()*2);
            for (int y = 0; y < h; y++) {
                pixel = null;
                color = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    for (int i = 0; i < srcNumComp; i++) {
                        srcLine[idx++] = (short)
                            ((color[i] - srcMinVal[i]) * srcInvDiffMinMax[i] +
                             0.5f);
                    }
                    if (needSrcAlpha) {
                        alpha[x] = color[srcNumComp];
                    }
                }
                doTransform(srcIL, dstIL);
                pixel = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    for (int i = 0; i < dstNumComp; i++) {
                        dstColor[i] = ((float) (dstLine[idx++] & 0xffff)) *
                                      dstDiffMinMax[i] + dstMinVal[i];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        }
    }
    public void colorConvert(Raster src, WritableRaster dst,
                             float[] srcMinVal, float[]srcMaxVal,
                             float[] dstMinVal, float[]dstMaxVal) {
        LCMSImageLayout srcIL, dstIL;
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        boolean srcIsFloat, dstIsFloat;
        if ((srcTransferType == DataBuffer.TYPE_FLOAT) ||
            (srcTransferType == DataBuffer.TYPE_DOUBLE)) {
            srcIsFloat = true;
        } else {
            srcIsFloat = false;
        }
        if ((dstTransferType == DataBuffer.TYPE_FLOAT) ||
            (dstTransferType == DataBuffer.TYPE_DOUBLE)) {
            dstIsFloat = true;
        } else {
            dstIsFloat = false;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        float[] srcScaleFactor = new float[srcNumBands];
        float[] dstScaleFactor = new float[dstNumBands];
        float[] srcUseMinVal = new float[srcNumBands];
        float[] dstUseMinVal = new float[dstNumBands];
        for (int i = 0; i < srcNumBands; i++) {
            if (srcIsFloat) {
                srcScaleFactor[i] =  65535.0f / (srcMaxVal[i] - srcMinVal[i]);
                srcUseMinVal[i] = srcMinVal[i];
            } else {
                if (srcTransferType == DataBuffer.TYPE_SHORT) {
                    srcScaleFactor[i] = 65535.0f / 32767.0f;
                } else {
                    srcScaleFactor[i] = 65535.0f /
                        ((float) ((1 << srcSM.getSampleSize(i)) - 1));
                }
                srcUseMinVal[i] = 0.0f;
            }
        }
        for (int i = 0; i < dstNumBands; i++) {
            if (dstIsFloat) {
                dstScaleFactor[i] = (dstMaxVal[i] - dstMinVal[i]) / 65535.0f;
                dstUseMinVal[i] = dstMinVal[i];
            } else {
                if (dstTransferType == DataBuffer.TYPE_SHORT) {
                    dstScaleFactor[i] = 32767.0f / 65535.0f;
                } else {
                    dstScaleFactor[i] =
                        ((float) ((1 << dstSM.getSampleSize(i)) - 1)) /
                        65535.0f;
                }
                dstUseMinVal[i] = 0.0f;
            }
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        int xs, xd;
        float sample;
        short[] srcLine = new short[w * srcNumBands];
        short[] dstLine = new short[w * dstNumBands];
        int idx;
        srcIL = new LCMSImageLayout(
            srcLine, srcLine.length/getNumInComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
            LCMSImageLayout.BYTES_SH(2), getNumInComponents()*2);
        dstIL = new LCMSImageLayout(
            dstLine, dstLine.length/getNumOutComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
            LCMSImageLayout.BYTES_SH(2), getNumOutComponents()*2);
        for (int y = 0; y < h; y++, ys++, yd++) {
            xs = src.getMinX();
            idx = 0;
            for (int x = 0; x < w; x++, xs++) {
                for (int i = 0; i < srcNumBands; i++) {
                    sample = src.getSampleFloat(xs, ys, i);
                    srcLine[idx++] = (short)
                        ((sample - srcUseMinVal[i]) * srcScaleFactor[i] + 0.5f);
                }
            }
            doTransform(srcIL, dstIL);
            xd = dst.getMinX();
            idx = 0;
            for (int x = 0; x < w; x++, xd++) {
                for (int i = 0; i < dstNumBands; i++) {
                    sample = ((dstLine[idx++] & 0xffff) * dstScaleFactor[i]) +
                             dstUseMinVal[i];
                    dst.setSample(xd, yd, i, sample);
                }
            }
        }
    }
    public void colorConvert(Raster src, WritableRaster dst) {
        LCMSImageLayout srcIL, dstIL;
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        int precision = 8;
        float maxNum = 255.0f;
        for (int i = 0; i < srcNumBands; i++) {
            if (srcSM.getSampleSize(i) > 8) {
                 precision = 16;
                 maxNum = 65535.0f;
             }
        }
        for (int i = 0; i < dstNumBands; i++) {
            if (dstSM.getSampleSize(i) > 8) {
                 precision = 16;
                 maxNum = 65535.0f;
             }
        }
        float[] srcScaleFactor = new float[srcNumBands];
        float[] dstScaleFactor = new float[dstNumBands];
        for (int i = 0; i < srcNumBands; i++) {
            if (srcTransferType == DataBuffer.TYPE_SHORT) {
                srcScaleFactor[i] = maxNum / 32767.0f;
            } else {
                srcScaleFactor[i] = maxNum /
                    ((float) ((1 << srcSM.getSampleSize(i)) - 1));
            }
        }
        for (int i = 0; i < dstNumBands; i++) {
            if (dstTransferType == DataBuffer.TYPE_SHORT) {
                dstScaleFactor[i] = 32767.0f / maxNum;
            } else {
                dstScaleFactor[i] =
                    ((float) ((1 << dstSM.getSampleSize(i)) - 1)) / maxNum;
            }
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        int xs, xd;
        int sample;
        if (precision == 8) {
            byte[] srcLine = new byte[w * srcNumBands];
            byte[] dstLine = new byte[w * dstNumBands];
            int idx;
            srcIL = new LCMSImageLayout(
                srcLine, srcLine.length/getNumInComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
                LCMSImageLayout.BYTES_SH(1), getNumInComponents());
            dstIL = new LCMSImageLayout(
                dstLine, dstLine.length/getNumOutComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
                LCMSImageLayout.BYTES_SH(1), getNumOutComponents());
            for (int y = 0; y < h; y++, ys++, yd++) {
                xs = src.getMinX();
                idx = 0;
                for (int x = 0; x < w; x++, xs++) {
                    for (int i = 0; i < srcNumBands; i++) {
                        sample = src.getSample(xs, ys, i);
                        srcLine[idx++] = (byte)
                            ((sample * srcScaleFactor[i]) + 0.5f);
                    }
                }
                doTransform(srcIL, dstIL);
                xd = dst.getMinX();
                idx = 0;
                for (int x = 0; x < w; x++, xd++) {
                    for (int i = 0; i < dstNumBands; i++) {
                        sample = (int) (((dstLine[idx++] & 0xff) *
                                         dstScaleFactor[i]) + 0.5f);
                        dst.setSample(xd, yd, i, sample);
                    }
                }
            }
        } else {
            short[] srcLine = new short[w * srcNumBands];
            short[] dstLine = new short[w * dstNumBands];
            int idx;
            srcIL = new LCMSImageLayout(
                srcLine, srcLine.length/getNumInComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
                LCMSImageLayout.BYTES_SH(2), getNumInComponents()*2);
            dstIL = new LCMSImageLayout(
                dstLine, dstLine.length/getNumOutComponents(),
                LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
                LCMSImageLayout.BYTES_SH(2), getNumOutComponents()*2);
            for (int y = 0; y < h; y++, ys++, yd++) {
                xs = src.getMinX();
                idx = 0;
                for (int x = 0; x < w; x++, xs++) {
                    for (int i = 0; i < srcNumBands; i++) {
                        sample = src.getSample(xs, ys, i);
                        srcLine[idx++] = (short)
                            ((sample * srcScaleFactor[i]) + 0.5f);
                    }
                }
                doTransform(srcIL, dstIL);
                xd = dst.getMinX();
                idx = 0;
                for (int x = 0; x < w; x++, xd++) {
                    for (int i = 0; i < dstNumBands; i++) {
                        sample = (int) (((dstLine[idx++] & 0xffff) *
                                         dstScaleFactor[i]) + 0.5f);
                        dst.setSample(xd, yd, i, sample);
                    }
                }
            }
        }
    }
    public short[] colorConvert(short[] src, short[] dst) {
        if (dst == null) {
            dst = new short [(src.length/getNumInComponents())*getNumOutComponents()];
        }
        LCMSImageLayout srcIL = new LCMSImageLayout(
            src, src.length/getNumInComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
            LCMSImageLayout.BYTES_SH(2), getNumInComponents()*2);
        LCMSImageLayout dstIL = new LCMSImageLayout(
            dst, dst.length/getNumOutComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
            LCMSImageLayout.BYTES_SH(2), getNumOutComponents()*2);
        doTransform(srcIL, dstIL);
        return dst;
    }
    public byte[] colorConvert(byte[] src, byte[] dst) {
        if (dst == null) {
            dst = new byte [(src.length/getNumInComponents())*getNumOutComponents()];
        }
        LCMSImageLayout srcIL = new LCMSImageLayout(
            src, src.length/getNumInComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumInComponents()) |
            LCMSImageLayout.BYTES_SH(1), getNumInComponents());
        LCMSImageLayout dstIL = new LCMSImageLayout(
            dst, dst.length/getNumOutComponents(),
            LCMSImageLayout.CHANNELS_SH(getNumOutComponents()) |
            LCMSImageLayout.BYTES_SH(1), getNumOutComponents());
        doTransform(srcIL, dstIL);
        return dst;
    }
}
