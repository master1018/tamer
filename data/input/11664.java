public abstract class X11SurfaceDataProxy extends SurfaceDataProxy
    implements Transparency
{
    public static SurfaceDataProxy createProxy(SurfaceData srcData,
                                               X11GraphicsConfig dstConfig)
    {
        if (srcData instanceof X11SurfaceData) {
            return UNCACHED;
        }
        ColorModel cm = srcData.getColorModel();
        int transparency = cm.getTransparency();
        if (transparency == Transparency.OPAQUE) {
            return new Opaque(dstConfig);
        } else if (transparency == Transparency.BITMASK) {
            if ((cm instanceof IndexColorModel) && cm.getPixelSize() == 8) {
                return new Bitmask(dstConfig);
            }
            if (cm instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) cm;
                int colormask = (dcm.getRedMask() |
                                 dcm.getGreenMask() |
                                 dcm.getBlueMask());
                int alphamask = dcm.getAlphaMask();
                if ((colormask & 0xff000000) == 0 &&
                    (alphamask & 0xff000000) != 0)
                {
                    return new Bitmask(dstConfig);
                }
            }
        }
        return UNCACHED;
    }
    X11GraphicsConfig x11gc;
    public X11SurfaceDataProxy(X11GraphicsConfig x11gc) {
        this.x11gc = x11gc;
    }
    @Override
    public SurfaceData validateSurfaceData(SurfaceData srcData,
                                           SurfaceData cachedData,
                                           int w, int h)
    {
        if (cachedData == null) {
            cachedData = X11SurfaceData.createData(x11gc, w, h,
                                                   x11gc.getColorModel(),
                                                   null, 0, getTransparency());
        }
        return cachedData;
    }
    public static class Opaque extends X11SurfaceDataProxy {
        public Opaque(X11GraphicsConfig x11gc) {
            super(x11gc);
        }
        public int getTransparency() {
            return Transparency.OPAQUE;
        }
        @Override
        public boolean isSupportedOperation(SurfaceData srcData,
                                            int txtype,
                                            CompositeType comp,
                                            Color bgColor)
        {
            return (txtype < SunGraphics2D.TRANSFORM_TRANSLATESCALE &&
                    (CompositeType.SrcOverNoEa.equals(comp) ||
                     CompositeType.SrcNoEa.equals(comp)));
        }
    }
    public static class Bitmask extends X11SurfaceDataProxy {
        public Bitmask(X11GraphicsConfig x11gc) {
            super(x11gc);
        }
        public int getTransparency() {
            return Transparency.BITMASK;
        }
        @Override
        public boolean isSupportedOperation(SurfaceData srcData,
                                            int txtype,
                                            CompositeType comp,
                                            Color bgColor)
        {
            if (txtype >= SunGraphics2D.TRANSFORM_TRANSLATESCALE) {
                return false;
            }
            if (bgColor != null &&
                bgColor.getTransparency() != Transparency.OPAQUE)
            {
                return false;
            }
            if (CompositeType.SrcOverNoEa.equals(comp) ||
                (CompositeType.SrcNoEa.equals(comp) &&
                 bgColor != null))
            {
                return true;
            }
            return false;
        }
    }
}
