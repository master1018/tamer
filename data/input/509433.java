public class ColorConverter {
    private ColorScaler scaler = new ColorScaler();
    public void loadScalingData(ColorSpace cs) {
        scaler.loadScalingData(cs);
    }
    public void translateColor(ICC_Transform t,
            BufferedImage src, BufferedImage dst) {
      NativeImageFormat srcIF = NativeImageFormat.createNativeImageFormat(src);
      NativeImageFormat dstIF = NativeImageFormat.createNativeImageFormat(dst);
      if (srcIF != null && dstIF != null) {
          t.translateColors(srcIF, dstIF);
          return;
      }
        srcIF = createImageFormat(src);
        dstIF = createImageFormat(dst);
        short srcChanData[] = (short[]) srcIF.getChannelData();
        short dstChanData[] = (short[]) dstIF.getChannelData();
        ColorModel srcCM = src.getColorModel();
        int nColorChannels = srcCM.getNumColorComponents();
        scaler.loadScalingData(srcCM.getColorSpace()); 
        ColorModel dstCM = dst.getColorModel();
        float alpha[] = null;
        boolean saveAlpha = srcCM.hasAlpha() && dstCM.hasAlpha();
        if (saveAlpha) {
            alpha = new float[src.getWidth()*src.getHeight()];
        }
        WritableRaster wr = src.getRaster();
        int srcDataPos = 0, alphaPos = 0;
        float normalizedVal[];
        for (int row=0, nRows = srcIF.getNumRows(); row<nRows; row++) {
            for (int col=0, nCols = srcIF.getNumCols(); col<nCols; col++) {
                normalizedVal = srcCM.getNormalizedComponents(
                    wr.getDataElements(col, row, null),
                    null, 0);
                if (saveAlpha) {
                    alpha[alphaPos++] = normalizedVal[nColorChannels];
                }
                scaler.scale(normalizedVal, srcChanData, srcDataPos);
                srcDataPos += nColorChannels;
            }
        }
        t.translateColors(srcIF, dstIF);
        nColorChannels = dstCM.getNumColorComponents();
        boolean fillAlpha = dstCM.hasAlpha();
        scaler.loadScalingData(dstCM.getColorSpace()); 
        float dstPixel[] = new float[dstCM.getNumComponents()];
        int dstDataPos = 0;
        alphaPos = 0;
        wr = dst.getRaster();
        for (int row=0, nRows = dstIF.getNumRows(); row<nRows; row++) {
            for (int col=0, nCols = dstIF.getNumCols(); col<nCols; col++) {
                scaler.unscale(dstPixel, dstChanData, dstDataPos);
                dstDataPos += nColorChannels;
                if (fillAlpha) {
                    if (saveAlpha) {
                        dstPixel[nColorChannels] = alpha[alphaPos++];
                    } else {
                        dstPixel[nColorChannels] = 1f;
                    }
                }
                wr.setDataElements(col, row,
                        dstCM.getDataElements(dstPixel, 0 , null));
            }
        }
    }
    public float[][] translateColor(ICC_Transform t,
            float buffer[][],
            ColorSpace srcCS,
            ColorSpace dstCS,
            int nPixels) {
        if (srcCS != null) { 
            scaler.loadScalingData(srcCS);
        }
        int nSrcChannels = t.getNumInputChannels();
        short srcShortData[] = new short[nPixels*nSrcChannels];
        for (int i=0, srcDataPos = 0; i<nPixels; i++) {
            scaler.scale(buffer[i], srcShortData, srcDataPos);
            srcDataPos += nSrcChannels;
        }
        short dstShortData[] = this.translateColor(t, srcShortData, null);
        int nDstChannels = t.getNumOutputChannels();
        int bufferSize = buffer[0].length;
        if (bufferSize < nDstChannels + 1) { 
            for (int i=0; i<nPixels; i++) {
                buffer[i] = new float[nDstChannels + 1];
            }
        }
        if (dstCS != null) { 
            scaler.loadScalingData(dstCS);
        }
        for (int i=0, dstDataPos = 0; i<nPixels; i++) {
            scaler.unscale(buffer[i], dstShortData, dstDataPos);
            dstDataPos += nDstChannels;
        }
        return buffer;
    }
   public void translateColor(ICC_Transform t, Raster src, WritableRaster dst) {
        try{
            NativeImageFormat srcFmt = NativeImageFormat.createNativeImageFormat(src);
            NativeImageFormat dstFmt = NativeImageFormat.createNativeImageFormat(dst);
          if (srcFmt != null && dstFmt != null) {
              t.translateColors(srcFmt, dstFmt);
              return;
          }
        } catch (IllegalArgumentException e) {
      }
        scaler.loadScalingData(src, t.getSrc());
        short srcData[] = scaler.scale(src);
        short dstData[] = translateColor(t, srcData, null);
        scaler.loadScalingData(dst, t.getDst());
        scaler.unscale(dstData, dst);
   }
    public short[] translateColor(ICC_Transform t, short src[], short dst[]) {
        NativeImageFormat srcFmt = createImageFormat(t, src, 0, true);
        NativeImageFormat dstFmt = createImageFormat(t, dst, srcFmt.getNumCols(), false);
        t.translateColors(srcFmt, dstFmt);
        return (short[]) dstFmt.getChannelData();
    }
    private NativeImageFormat createImageFormat(BufferedImage bi) {
        int nRows = bi.getHeight();
        int nCols = bi.getWidth();
        int nComps = bi.getColorModel().getNumColorComponents();
        short imgData[] = new short[nRows*nCols*nComps];
        return new NativeImageFormat(
                imgData, nComps, nRows, nCols);
    }
    private NativeImageFormat createImageFormat(
            ICC_Transform t, short arr[], int nCols, boolean in
    ) {
        int nComponents = in ? t.getNumInputChannels() : t.getNumOutputChannels();
        if (arr == null || arr.length < nCols*nComponents) {
            arr = new short[nCols*nComponents];
        }
        if (nCols == 0)
            nCols = arr.length / nComponents;
        return new NativeImageFormat(arr, nComponents, 1, nCols);
    }
}
