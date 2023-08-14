public class SunCompositeContext implements CompositeContext {
    ColorModel srcCM;
    ColorModel dstCM;
    Composite composite;
    CompositeType comptype;
    public SunCompositeContext(AlphaComposite ac,
                               ColorModel s, ColorModel d)
    {
        if (s == null) {
            throw new NullPointerException("Source color model cannot be null");
        }
        if (d == null) {
            throw new NullPointerException("Destination color model cannot be null");
        }
        srcCM = s;
        dstCM = d;
        this.composite = ac;
        this.comptype = CompositeType.forAlphaComposite(ac);
    }
    public SunCompositeContext(XORComposite xc,
                               ColorModel s, ColorModel d)
    {
        if (s == null) {
            throw new NullPointerException("Source color model cannot be null");
        }
        if (d == null) {
            throw new NullPointerException("Destination color model cannot be null");
        }
        srcCM = s;
        dstCM = d;
        this.composite = xc;
        this.comptype = CompositeType.Xor;
    }
    public void dispose() {
    }
    public void compose(Raster srcArg, Raster dstIn, WritableRaster dstOut) {
        WritableRaster src;
        int w;
        int h;
        if (dstIn != dstOut) {
            dstOut.setDataElements(0, 0, dstIn);
        }
        if (srcArg instanceof WritableRaster) {
            src = (WritableRaster) srcArg;
        } else {
            src = srcArg.createCompatibleWritableRaster();
            src.setDataElements(0, 0, srcArg);
        }
        w = Math.min(src.getWidth(), dstIn.getWidth());
        h = Math.min(src.getHeight(), dstIn.getHeight());
        BufferedImage srcImg = new BufferedImage(srcCM, src,
                                                 srcCM.isAlphaPremultiplied(),
                                                 null);
        BufferedImage dstImg = new BufferedImage(dstCM, dstOut,
                                                 dstCM.isAlphaPremultiplied(),
                                                 null);
        SurfaceData srcData = BufImgSurfaceData.createData(srcImg);
        SurfaceData dstData = BufImgSurfaceData.createData(dstImg);
        Blit blit = Blit.getFromCache(srcData.getSurfaceType(),
                                      comptype,
                                      dstData.getSurfaceType());
        blit.Blit(srcData, dstData, composite, null, 0, 0, 0, 0, w, h);
    }
}
