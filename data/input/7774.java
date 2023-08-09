public class WCustomCursor extends CustomCursor {
    public WCustomCursor(Image cursor, Point hotSpot, String name)
            throws IndexOutOfBoundsException {
        super(cursor, hotSpot, name);
    }
    protected void createNativeCursor(Image im, int[] pixels, int w, int h,
                                      int xHotSpot, int yHotSpot) {
        BufferedImage bimage = new BufferedImage(w, h,
                               BufferedImage.TYPE_INT_RGB);
        Graphics g = bimage.getGraphics();
        try {
            if (im instanceof ToolkitImage) {
                ImageRepresentation ir = ((ToolkitImage)im).getImageRep();
                ir.reconstruct(ImageObserver.ALLBITS);
            }
            g.drawImage(im, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        Raster  raster = bimage.getRaster();
        DataBuffer buffer = raster.getDataBuffer();
        int data[] = ((DataBufferInt)buffer).getData();
        byte[] andMask = new byte[w * h / 8];
        int npixels = pixels.length;
        for (int i = 0; i < npixels; i++) {
            int ibyte = i / 8;
            int omask = 1 << (7 - (i % 8));
            if ((pixels[i] & 0xff000000) == 0) {
                andMask[ibyte] |= omask;
            }
        }
        {
            int     ficW = raster.getWidth();
            if( raster instanceof IntegerComponentRaster ) {
                ficW = ((IntegerComponentRaster)raster).getScanlineStride();
            }
            createCursorIndirect(
                ((DataBufferInt)bimage.getRaster().getDataBuffer()).getData(),
                andMask, ficW, raster.getWidth(), raster.getHeight(),
                xHotSpot, yHotSpot);
        }
    }
    private native void createCursorIndirect(int[] rData, byte[] andMask,
                                             int nScanStride, int width,
                                             int height, int xHotSpot,
                                             int yHotSpot);
    static native int getCursorWidth();
    static native int getCursorHeight();
}
