public class BitmapMask {
    protected final BitmapIndexed1BPP mask;
    public BitmapMask(final BitmapDescriptor pDescriptor) {
        mask = new BitmapIndexed1BPP(pDescriptor);
    }
    void read(final StreamDecoder pDec) throws IOException {
        mask.readBitmap(pDec);
    }
    public int getPaletteIndex(final int pXPos, final int pYPos) {
        return mask.getPaletteIndex(pXPos, pYPos);
    }
    void setDescriptor(final BitmapDescriptor pDescriptor) {
        mask.setDescriptor(pDescriptor);
    }
    public boolean isOpaque(final int pXPos, final int pYPos) {
        return mask.getPaletteIndex(pXPos, pYPos) == 0;
    }
}
