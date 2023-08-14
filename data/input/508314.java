public class ColorConvertOp implements BufferedImageOp, RasterOp {
    RenderingHints renderingHints;
    Object conversionSequence[] = new ICC_Profile[0]; 
    private ICC_Profile midProfiles[];
    private final ColorConverter cc = new ColorConverter();
    private final ICC_TransfomCreator tCreator = new ICC_TransfomCreator();
    private boolean isICC = true;
    private class ICC_TransfomCreator {
        private ICC_Transform transform;
        private int maxComponents;
        public ICC_Transform getTransform(ICC_Profile src, ICC_Profile dst, ICC_Profile convSeq[]) {
            if (transform != null && src == transform.getSrc() && dst == transform.getDst()) {
                return transform;
            }
            int length = convSeq.length;
            int srcFlg = 0, dstFlg = 0;
            if (length == 0 || src != convSeq[0]) {
                if (src != null) {
                    srcFlg = 1; 
                }
            }
            if (length == 0 || dst != convSeq[length - 1]) {
                if (dst != null) {
                    dstFlg = 1; 
                }
            }
            ICC_Profile profiles[];
            int nProfiles = length + srcFlg + dstFlg;
            if (nProfiles == length) {
                profiles = convSeq;
            } else {
                profiles = new ICC_Profile[nProfiles];
                int pos = 0;
                if (srcFlg != 0) {
                    profiles[pos++] = src;
                }
                for (int i = 0; i < length; i++) {
                    profiles[pos++] = convSeq[i];
                }
                if (dstFlg != 0) {
                    profiles[pos++] = dst;
                }
            }
            return transform = new ICC_Transform(profiles);
        }
        public Object[] getSequence(Object src, Object dst) {
            ArrayList<Object> profiles = new ArrayList<Object>(10);
            ArrayList<Object> sequence = new ArrayList<Object>(10);
            ICC_Profile xyzProfile = ICC_Profile.getInstance(ColorSpace.CS_CIEXYZ);
            Object conversionFirst = null, conversionLast = null;
            int conversionLength = conversionSequence.length;
            if (conversionLength > 0) {
                conversionFirst = conversionSequence[0];
                conversionLast = conversionSequence[conversionLength - 1];
            }
            boolean iccSequenceStarted = false;
            if (src != conversionFirst && src != null) {
                if (src instanceof ICC_Profile) {
                    profiles.add(src);
                    iccSequenceStarted = true;
                } else {
                    profiles.add(xyzProfile);
                    sequence.add(src); 
                }
            } else {
                profiles.add(xyzProfile);
            }
            for (int i = 0; i < conversionLength; i++) {
                if (conversionSequence[i] instanceof ICC_Profile) {
                    profiles.add(conversionSequence[i]);
                    iccSequenceStarted = true;
                } else if (iccSequenceStarted) {
                    profiles.add(xyzProfile);
                    Object prev = profiles.get(0);
                    for (int k = 1; k < profiles.size(); k++) {
                        if (prev == profiles.get(k)) {
                            k--;
                            profiles.remove(k);
                        }
                        prev = profiles.get(k);
                    }
                    if (profiles.size() > 1) {
                        sequence.add(new ICC_Transform(profiles.toArray(new ICC_Profile[0])));
                        sequence.add(conversionSequence[i]);
                    }
                    profiles.clear();
                    profiles.add(xyzProfile);
                    iccSequenceStarted = false; 
                } else { 
                    sequence.add(conversionSequence[i]);
                }
            }
            if (dst != conversionLast && dst != null) { 
                if (dst instanceof ICC_Profile) {
                    profiles.add(dst);
                    iccSequenceStarted = true;
                } else if (iccSequenceStarted) {
                    profiles.add(xyzProfile);
                } else {
                    sequence.add(dst); 
                }
            }
            if (iccSequenceStarted) { 
                sequence.add(new ICC_Transform(profiles.toArray(new ICC_Profile[0])));
                if (dst != null && !(dst instanceof ICC_Profile)) {
                    sequence.add(dst); 
                }
            }
            maxComponents = 0;
            Object o;
            for (int i = 0, size = sequence.size(); i < size; i++) {
                o = sequence.get(i);
                if (o instanceof ICC_Transform) {
                    ICC_Transform t = (ICC_Transform)o;
                    maxComponents = (maxComponents > t.getNumInputChannels() + 1) ? maxComponents
                            : t.getNumInputChannels() + 1;
                    maxComponents = (maxComponents > t.getNumOutputChannels() + 1) ? maxComponents
                            : t.getNumOutputChannels() + 1;
                } else {
                    ColorSpace cs = (ColorSpace)o;
                    maxComponents = (maxComponents > cs.getNumComponents() + 1) ? maxComponents
                            : cs.getNumComponents() + 1;
                }
            }
            return sequence.toArray();
        }
    }
    public ColorConvertOp(ColorSpace srcCS, ColorSpace dstCS, RenderingHints hints) {
        if (srcCS == null || dstCS == null) {
            throw new NullPointerException(Messages.getString("awt.25B")); 
        }
        renderingHints = hints;
        boolean srcICC = srcCS instanceof ICC_ColorSpace;
        boolean dstICC = dstCS instanceof ICC_ColorSpace;
        if (srcICC && dstICC) {
            conversionSequence = new ICC_Profile[2];
        } else {
            conversionSequence = new Object[2];
            isICC = false;
        }
        if (srcICC) {
            conversionSequence[0] = ((ICC_ColorSpace)srcCS).getProfile();
        } else {
            conversionSequence[0] = srcCS;
        }
        if (dstICC) {
            conversionSequence[1] = ((ICC_ColorSpace)dstCS).getProfile();
        } else {
            conversionSequence[1] = dstCS;
        }
    }
    public ColorConvertOp(ICC_Profile profiles[], RenderingHints hints) {
        if (profiles == null) {
            throw new NullPointerException(Messages.getString("awt.25C")); 
        }
        renderingHints = hints;
        midProfiles = profiles;
        conversionSequence = new ICC_Profile[midProfiles.length];
        for (int i = 0, length = midProfiles.length; i < length; i++) {
            conversionSequence[i] = midProfiles[i];
        }
    }
    public ColorConvertOp(ColorSpace cs, RenderingHints hints) {
        if (cs == null) {
            throw new NullPointerException(Messages.getString("awt.25B")); 
        }
        renderingHints = hints;
        if (cs instanceof ICC_ColorSpace) {
            conversionSequence = new ICC_Profile[1];
            conversionSequence[0] = ((ICC_ColorSpace)cs).getProfile();
        } else {
            conversionSequence = new Object[1];
            conversionSequence[0] = cs;
            isICC = false;
        }
    }
    public ColorConvertOp(RenderingHints hints) {
        renderingHints = hints;
    }
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (conversionSequence.length < 2) {
            throw new IllegalArgumentException(Messages.getString("awt.25D")); 
        }
        ICC_Profile srcPf = null, dstPf = null; 
        int nSrcColorComps, nDstColorComps;
        Object first = conversionSequence[0];
        Object last = conversionSequence[conversionSequence.length - 1];
        if (isICC) {
            srcPf = (ICC_Profile)first;
            dstPf = (ICC_Profile)last;
            nSrcColorComps = srcPf.getNumComponents();
            nDstColorComps = dstPf.getNumComponents();
        } else {
            if (first instanceof ICC_Profile) {
                srcPf = (ICC_Profile)first;
                nSrcColorComps = srcPf.getNumComponents();
            } else {
                nSrcColorComps = ((ColorSpace)first).getNumComponents();
            }
            if (last instanceof ICC_Profile) {
                dstPf = (ICC_Profile)last;
                nDstColorComps = dstPf.getNumComponents();
            } else {
                nDstColorComps = ((ColorSpace)last).getNumComponents();
            }
        }
        if (src.getNumBands() != nSrcColorComps) {
            throw new IllegalArgumentException(Messages.getString("awt.25E")); 
        }
        if (dst != null) { 
            if (dst.getNumBands() != nDstColorComps) {
                throw new IllegalArgumentException(Messages.getString("awt.25F")); 
            }
            if (src.getWidth() != dst.getWidth() || src.getHeight() != dst.getHeight()) {
                throw new IllegalArgumentException(Messages.getString("awt.260")); 
            }
        } else {
            dst = createCompatibleDestRaster(src);
        }
        if (isICC) {
            ICC_Transform t = tCreator
                    .getTransform(srcPf, dstPf, (ICC_Profile[])conversionSequence);
            cc.translateColor(t, src, dst);
        } else {
            Object[] sequence = tCreator.getSequence(null, null);
            ColorScaler scaler = new ColorScaler();
            scaler.loadScalingData(src, null);
            float tmpData[][] = scaler.scaleNormalize(src);
            ColorSpace srcCS = (srcPf == null) ? (ColorSpace)first : new ICC_ColorSpace(srcPf);
            ColorSpace dstCS = (dstPf == null) ? (ColorSpace)last : new ICC_ColorSpace(dstPf);
            applySequence(sequence, tmpData, srcCS, dstCS);
            scaler.loadScalingData(dst, null);
            scaler.unscaleNormalized(dst, tmpData);
        }
        return dst;
    }
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        if (destCM != null) {
            return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(),
                    src.getHeight()), destCM.isAlphaPremultiplied(), null);
        }
        int nSpaces = conversionSequence.length;
        if (nSpaces < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.261")); 
        }
        Object destination = conversionSequence[nSpaces - 1];
        ColorSpace dstCS = (destination instanceof ColorSpace) ? (ColorSpace)destination
                : new ICC_ColorSpace((ICC_Profile)destination);
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM = new ComponentColorModel(dstCS, srcCM.hasAlpha(), srcCM
                .isAlphaPremultiplied(), srcCM.getTransparency(), srcCM.getTransferType());
        return new BufferedImage(dstCM, destCM.createCompatibleWritableRaster(src.getWidth(), src
                .getHeight()), destCM.isAlphaPremultiplied(), null);
    }
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null && conversionSequence.length < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.262")); 
        }
        ColorModel srcCM = src.getColorModel();
        if (srcCM instanceof IndexColorModel) {
            src = ((IndexColorModel)srcCM).convertToIntDiscrete(src.getRaster(), false);
        }
        ColorSpace srcCS = srcCM.getColorSpace();
        BufferedImage res;
        boolean isDstIndex = false;
        if (dst != null) {
            if (src.getWidth() != dst.getWidth() || src.getHeight() != dst.getHeight()) {
                throw new IllegalArgumentException(Messages.getString("awt.263")); 
            }
            if (dst.getColorModel() instanceof IndexColorModel) {
                isDstIndex = true;
                res = createCompatibleDestImage(src, null);
            } else {
                res = dst;
            }
        } else {
            res = createCompatibleDestImage(src, null);
        }
        ColorModel dstCM = res.getColorModel();
        ColorSpace dstCS = dstCM.getColorSpace();
        ICC_Profile srcPf = null, dstPf = null;
        if (srcCS instanceof ICC_ColorSpace) {
            srcPf = ((ICC_ColorSpace)srcCS).getProfile();
        }
        if (dstCS instanceof ICC_ColorSpace) {
            dstPf = ((ICC_ColorSpace)dstCS).getProfile();
        }
        boolean isFullICC = isICC && srcPf != null && dstPf != null;
        if (isFullICC) {
            ICC_Transform t = tCreator
                    .getTransform(srcPf, dstPf, (ICC_Profile[])conversionSequence);
            cc.translateColor(t, src, res);
        } else { 
            Object sequence[] = tCreator.getSequence(srcPf == null ? (Object)srcCS : srcPf,
                    dstPf == null ? (Object)dstCS : dstPf);
            int srcW = src.getWidth();
            int srcH = src.getHeight();
            int numPixels = srcW * srcH;
            float tmpData[][] = new float[numPixels][tCreator.maxComponents];
            for (int row = 0, dataPos = 0; row < srcW; row++) {
                for (int col = 0; col < srcH; col++) {
                    tmpData[dataPos] = srcCM.getNormalizedComponents(src.getRaster()
                            .getDataElements(row, col, null), tmpData[dataPos], 0);
                    dataPos++;
                }
            }
            float alpha[] = null;
            int alphaIdx = srcCM.numComponents - 1;
            if (srcCM.hasAlpha() && dstCM.hasAlpha()) {
                alpha = new float[numPixels];
                for (int i = 0; i < numPixels; i++) {
                    alpha[i] = tmpData[i][alphaIdx];
                }
            }
            applySequence(sequence, tmpData, srcCS, dstCS);
            if (dstCM.hasAlpha()) {
                alphaIdx = dstCM.numComponents - 1;
                if (alpha != null) {
                    for (int i = 0; i < numPixels; i++) {
                        tmpData[i][alphaIdx] = alpha[i];
                    }
                } else {
                    for (int i = 0; i < numPixels; i++) {
                        tmpData[i][alphaIdx] = 1f;
                    }
                }
            }
            for (int row = 0, dataPos = 0; row < srcW; row++) {
                for (int col = 0; col < srcH; col++) {
                    res.getRaster().setDataElements(row, col,
                            dstCM.getDataElements(tmpData[dataPos++], 0, null));
                }
            }
        }
        if (isDstIndex) { 
            Graphics2D g2d = dst.createGraphics();
            g2d.drawImage(res, 0, 0, null);
            g2d.dispose();
            return dst;
        }
        return res;
    }
    private void applySequence(Object sequence[], float tmpData[][], ColorSpace srcCS,
            ColorSpace dstCS) {
        ColorSpace xyzCS = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        int numPixels = tmpData.length;
        if (sequence[0] instanceof ICC_Transform) { 
            ICC_Transform t = (ICC_Transform)sequence[0];
            cc.translateColor(t, tmpData, srcCS, xyzCS, numPixels);
        } else { 
            for (int k = 0; k < numPixels; k++) {
                tmpData[k] = srcCS.toCIEXYZ(tmpData[k]);
            }
            cc.loadScalingData(xyzCS); 
        }
        for (Object element : sequence) {
            if (element instanceof ICC_Transform) {
                ICC_Transform t = (ICC_Transform)element;
                cc.translateColor(t, tmpData, null, null, numPixels);
            } else {
                ColorSpace cs = (ColorSpace)element;
                for (int k = 0; k < numPixels; k++) {
                    tmpData[k] = cs.fromCIEXYZ(tmpData[k]);
                    tmpData[k] = cs.toCIEXYZ(tmpData[k]);
                }
            }
        }
        if (sequence[sequence.length - 1] instanceof ICC_Transform) { 
            ICC_Transform t = (ICC_Transform)sequence[sequence.length - 1];
            cc.translateColor(t, tmpData, xyzCS, dstCS, numPixels);
        } else { 
            for (int k = 0; k < numPixels; k++) {
                tmpData[k] = dstCS.fromCIEXYZ(tmpData[k]);
            }
        }
    }
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt != null) {
            dstPt.setLocation(srcPt);
            return dstPt;
        }
        return new Point2D.Float((float)srcPt.getX(), (float)srcPt.getY());
    }
    public WritableRaster createCompatibleDestRaster(Raster src) {
        int nComps = 0;
        int nSpaces = conversionSequence.length;
        if (nSpaces < 2) {
            throw new IllegalArgumentException(Messages.getString("awt.261")); 
        }
        Object lastCS = conversionSequence[nSpaces - 1];
        if (lastCS instanceof ColorSpace) {
            nComps = ((ColorSpace)lastCS).getNumComponents();
        } else {
            nComps = ((ICC_Profile)lastCS).getNumComponents();
        }
        int dstDataType = src.getDataBuffer().getDataType();
        if (dstDataType != DataBuffer.TYPE_BYTE && dstDataType != DataBuffer.TYPE_SHORT) {
            dstDataType = DataBuffer.TYPE_SHORT;
        }
        return Raster.createInterleavedRaster(dstDataType, src.getWidth(), src.getHeight(), nComps,
                new Point(src.getMinX(), src.getMinY()));
    }
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return src.getRaster().getBounds();
    }
    public final ICC_Profile[] getICC_Profiles() {
        if (midProfiles != null) {
            return midProfiles;
        }
        return null;
    }
    public final RenderingHints getRenderingHints() {
        return renderingHints;
    }
}
