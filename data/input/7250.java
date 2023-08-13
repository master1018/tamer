public class XRGlyphCacheEntry {
    long glyphInfoPtr;
    int lastUsed;
    boolean pinned;
    int xOff;
    int yOff;
    int glyphSet;
    public XRGlyphCacheEntry(long glyphInfoPtr, GlyphList gl) {
        this.glyphInfoPtr = glyphInfoPtr;
        xOff = (int) Math.round(getXAdvance());
        yOff = (int) Math.round(getYAdvance());
    }
    public int getXOff() {
        return xOff;
    }
    public int getYOff() {
        return yOff;
    }
    public void setGlyphSet(int glyphSet) {
        this.glyphSet = glyphSet;
    }
    public int getGlyphSet() {
        return glyphSet;
    }
    public static int getGlyphID(long glyphInfoPtr) {
        return (int) StrikeCache.unsafe.getInt(glyphInfoPtr + StrikeCache.cacheCellOffset);
    }
    public static void setGlyphID(long glyphInfoPtr, int id) {
        StrikeCache.unsafe.putInt(glyphInfoPtr + StrikeCache.cacheCellOffset, id);
    }
    public int getGlyphID() {
        return getGlyphID(glyphInfoPtr);
    }
    public void setGlyphID(int id) {
        setGlyphID(glyphInfoPtr, id);
    }
    public float getXAdvance() {
        return StrikeCache.unsafe.getFloat(glyphInfoPtr + StrikeCache.xAdvanceOffset);
    }
    public float getYAdvance() {
        return StrikeCache.unsafe.getFloat(glyphInfoPtr + StrikeCache.yAdvanceOffset);
    }
    public int getSourceRowBytes() {
        return StrikeCache.unsafe.getShort(glyphInfoPtr + StrikeCache.rowBytesOffset);
    }
    public int getWidth() {
        return StrikeCache.unsafe.getShort(glyphInfoPtr + StrikeCache.widthOffset);
    }
    public int getHeight() {
        return StrikeCache.unsafe.getShort(glyphInfoPtr + StrikeCache.heightOffset);
    }
    public void writePixelData(ByteArrayOutputStream os, boolean uploadAsLCD) {
        long pixelDataAddress;
        if (StrikeCache.nativeAddressSize == 4) {
            pixelDataAddress = 0xffffffff & StrikeCache.unsafe.getInt(glyphInfoPtr + StrikeCache.pixelDataOffset);
        } else {
            pixelDataAddress = StrikeCache.unsafe.getLong(glyphInfoPtr + StrikeCache.pixelDataOffset);
        }
        if (pixelDataAddress == 0L) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        int rowBytes = getSourceRowBytes();
        int paddedWidth = getPaddedWidth(uploadAsLCD);
        if (!uploadAsLCD) {
            for (int line = 0; line < height; line++) {
                for(int x = 0; x < paddedWidth; x++) {
                    if(x < width) {
                        os.write(StrikeCache.unsafe.getByte(pixelDataAddress + (line * rowBytes + x)));
                    }else {
                         os.write(0);
                    }
                }
            }
        } else {
            for (int line = 0; line < height; line++) {
                int rowStart = line * rowBytes;
                int rowBytesWidth = width * 3;
                int srcpix = 0;
                while (srcpix < rowBytesWidth) {
                    os.write(StrikeCache.unsafe.getByte
                          (pixelDataAddress + (rowStart + srcpix + 2)));
                    os.write(StrikeCache.unsafe.getByte
                          (pixelDataAddress + (rowStart + srcpix + 1)));
                    os.write(StrikeCache.unsafe.getByte
                          (pixelDataAddress + (rowStart + srcpix + 0)));
                    os.write(255);
                    srcpix += 3;
                }
            }
        }
    }
    public float getTopLeftXOffset() {
        return StrikeCache.unsafe.getFloat(glyphInfoPtr + StrikeCache.topLeftXOffset);
    }
    public float getTopLeftYOffset() {
        return StrikeCache.unsafe.getFloat(glyphInfoPtr + StrikeCache.topLeftYOffset);
    }
    public long getGlyphInfoPtr() {
        return glyphInfoPtr;
    }
    public boolean isGrayscale(boolean listContainsLCDGlyphs) {
        return getSourceRowBytes() == getWidth() && !(getWidth() == 0 && getHeight() == 0 && listContainsLCDGlyphs);
    }
    public int getPaddedWidth(boolean listContainsLCDGlyphs) {
        int width = getWidth();
        return isGrayscale(listContainsLCDGlyphs) ? (int) Math.ceil(width / 4.0) * 4 : width;
    }
    public int getDestinationRowBytes(boolean listContainsLCDGlyphs) {
        boolean grayscale = isGrayscale(listContainsLCDGlyphs);
        return grayscale ? getPaddedWidth(grayscale) : getWidth() * 4;
    }
    public int getGlyphDataLenth(boolean listContainsLCDGlyphs) {
        return getDestinationRowBytes(listContainsLCDGlyphs) * getHeight();
    }
    public void setPinned() {
        pinned = true;
    }
    public void setUnpinned() {
        pinned = false;
    }
    public int getLastUsed() {
        return lastUsed;
    }
    public void setLastUsed(int lastUsed) {
        this.lastUsed = lastUsed;
    }
    public int getPixelCnt() {
        return getWidth() * getHeight();
    }
    public boolean isPinned() {
        return pinned;
    }
}
