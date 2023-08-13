public final class XORComposite implements Composite {
    Color xorColor;
    int xorPixel;
    int alphaMask;
    public XORComposite(Color xorColor, SurfaceData sd) {
        this.xorColor = xorColor;
        SurfaceType sType = sd.getSurfaceType();
        this.xorPixel = sd.pixelFor(xorColor.getRGB());
        this.alphaMask = sType.getAlphaMask();
    }
    public Color getXorColor() {
        return xorColor;
    }
    public int getXorPixel() {
        return xorPixel;
    }
    public int getAlphaMask() {
        return alphaMask;
    }
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new SunCompositeContext(this, srcColorModel, dstColorModel);
    }
}
