public class NullSurfaceData extends SurfaceData {
    public static final SurfaceData theInstance = new NullSurfaceData();
    private NullSurfaceData() {
        super(State.IMMUTABLE, SurfaceType.Any, ColorModel.getRGBdefault());
    }
    public void invalidate() {
    }
    public SurfaceData getReplacement() {
        return this;
    }
    private final static NullPipe nullpipe = new NullPipe();
    public void validatePipe(SunGraphics2D sg2d) {
        sg2d.drawpipe = nullpipe;
        sg2d.fillpipe = nullpipe;
        sg2d.shapepipe = nullpipe;
        sg2d.textpipe = nullpipe;
        sg2d.imagepipe = nullpipe;
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InvalidPipeException("should be NOP");
    }
    public boolean useTightBBoxes() {
        return false;
    }
    public int pixelFor(int rgb) {
        return rgb;
    }
    public int rgbFor(int pixel) {
        return pixel;
    }
    public Rectangle getBounds() {
        return new Rectangle();
    }
    protected void checkCustomComposite() {
        return;
    }
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        return true;
    }
    public Object getDestination() {
        return null;
    }
}
