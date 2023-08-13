public class ICompositeContext implements CompositeContext {
    Composite composite;
    ColorModel srcCM, dstCM;
    ImageSurface srcSurf, dstSurf;
    public ICompositeContext(Composite comp, ColorModel src, ColorModel dst){
        composite = comp;
        srcCM = src;
        dstCM = dst;
    }
    public void dispose() {
        srcSurf.dispose();
        dstSurf.dispose();
    }
    public void compose(Raster srcIn, Raster dstIn, WritableRaster dstOut) {
        if(!srcCM.isCompatibleRaster(srcIn)) {
            throw new IllegalArgumentException(Messages.getString("awt.48")); 
        }
        if(!dstCM.isCompatibleRaster(dstIn)) {
            throw new IllegalArgumentException(Messages.getString("awt.49")); 
        }
        if(dstIn != dstOut){
            if(!dstCM.isCompatibleRaster(dstOut)) {
                throw new IllegalArgumentException(Messages.getString("awt.4A")); 
            }
            dstOut.setDataElements(0, 0, dstIn);
        }
        WritableRaster src;
        if(srcIn instanceof WritableRaster){
            src = (WritableRaster) srcIn;
        }else{
            src = srcIn.createCompatibleWritableRaster();
            src.setDataElements(0, 0, srcIn);
        }
        srcSurf = new ImageSurface(srcCM, src);
        dstSurf = new ImageSurface(dstCM, dstOut);
        int w = Math.min(srcIn.getWidth(), dstOut.getWidth());
        int h = Math.min(srcIn.getHeight(), dstOut.getHeight());
        NativeImageBlitter.getInstance().blit(0, 0, srcSurf, 0, 0, dstSurf,
                w, h, composite, null, null);
    }
}
