public class BufferedImageGraphics2D extends CommonGraphics2D {
    private BufferedImage bi = null;
    private Rectangle bounds = null;
    public BufferedImageGraphics2D(BufferedImage bi) {
        super();
        this.bi = bi;
        this.bounds = new Rectangle(0, 0, bi.getWidth(), bi.getHeight());
        clip(bounds);
        dstSurf = Surface.getImageSurface(bi);
        if(dstSurf.isNativeDrawable()){
            blitter = NativeImageBlitter.getInstance();
        }else{
            blitter = JavaBlitter.getInstance();
        }
    }
    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }
    @Override
    public Graphics create() {
        BufferedImageGraphics2D res = new BufferedImageGraphics2D(bi);
        copyInternalFields(res);
        return res;
    }
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }
    public ColorModel getColorModel() {
        return bi.getColorModel();
    }
    public WritableRaster getWritableRaster() {
        return bi.getRaster();
    }
}