class DropShadowEffect extends ShadowEffect {
    @Override
    EffectType getEffectType() {
        return EffectType.UNDER;
    }
    @Override
    BufferedImage applyEffect(BufferedImage src, BufferedImage dst, int w, int h) {
        if (src == null || src.getType() != BufferedImage.TYPE_INT_ARGB){
            throw new IllegalArgumentException("Effect only works with " +
                    "source images of type BufferedImage.TYPE_INT_ARGB.");
        }
        if (dst != null && dst.getType() != BufferedImage.TYPE_INT_ARGB){
            throw new IllegalArgumentException("Effect only works with " +
                    "destination images of type BufferedImage.TYPE_INT_ARGB.");
        }
        double trangleAngle = Math.toRadians(angle - 90);
        int offsetX = (int) (Math.sin(trangleAngle) * distance);
        int offsetY = (int) (Math.cos(trangleAngle) * distance);
        int tmpOffX = offsetX + size;
        int tmpOffY = offsetX + size;
        int tmpW = w + offsetX + size + size;
        int tmpH = h + offsetX + size;
        int[] lineBuf = getArrayCache().getTmpIntArray(w);
        byte[] tmpBuf1 = getArrayCache().getTmpByteArray1(tmpW * tmpH);
        Arrays.fill(tmpBuf1, (byte) 0x00);
        byte[] tmpBuf2 = getArrayCache().getTmpByteArray2(tmpW * tmpH);
        Raster srcRaster = src.getRaster();
        for (int y = 0; y < h; y++) {
            int dy = (y + tmpOffY);
            int offset = dy * tmpW;
            srcRaster.getDataElements(0, y, w, 1, lineBuf);
            for (int x = 0; x < w; x++) {
                int dx = x + tmpOffX;
                tmpBuf1[offset + dx] = (byte) ((lineBuf[x] & 0xFF000000) >>> 24);
            }
        }
        float[] kernel = EffectUtils.createGaussianKernel(size);
        EffectUtils.blur(tmpBuf1, tmpBuf2, tmpW, tmpH, kernel, size); 
        EffectUtils.blur(tmpBuf2, tmpBuf1, tmpH, tmpW, kernel, size);
        float spread = Math.min(1 / (1 - (0.01f * this.spread)), 255);
        for (int i = 0; i < tmpBuf1.length; i++) {
            int val = (int) (((int) tmpBuf1[i] & 0xFF) * spread);
            tmpBuf1[i] = (val > 255) ? (byte) 0xFF : (byte) val;
        }
        if (dst == null) dst = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        WritableRaster shadowRaster = dst.getRaster();
        int red = color.getRed(), green = color.getGreen(), blue = color.getBlue();
        for (int y = 0; y < h; y++) {
            int srcY = y + tmpOffY;
            int shadowOffset = (srcY - offsetY) * tmpW;
            for (int x = 0; x < w; x++) {
                int srcX = x + tmpOffX;
                lineBuf[x] = tmpBuf1[shadowOffset + (srcX - offsetX)] << 24 | red << 16 | green << 8 | blue;
            }
            shadowRaster.setDataElements(0, y, w, 1, lineBuf);
        }
        return dst;
    }
}
