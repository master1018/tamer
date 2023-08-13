public class NativeImageBlitter implements Blitter {
    final static NativeImageBlitter inst = new NativeImageBlitter();
    public static NativeImageBlitter getInstance(){
        return inst;
    }
    public void blit(int srcX, int srcY, Surface srcSurf, int dstX, int dstY,
            Surface dstSurf, int width, int height, AffineTransform sysxform,
            AffineTransform xform, Composite comp, Color bgcolor,
            MultiRectArea clip) {
        if(!srcSurf.isNativeDrawable()){
            JavaBlitter.inst.blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf, width, height,
                    sysxform, xform, comp, bgcolor, clip);
        }else{
            if(xform == null){
                blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf, width, height,
                        sysxform, comp, bgcolor, clip);
            }else{
                double scaleX = xform.getScaleX();
                double scaleY = xform.getScaleY();
                double scaledX = dstX / scaleX;
                double scaledY = dstY / scaleY;
                AffineTransform at = new AffineTransform();
                at.setToTranslation(scaledX, scaledY);
                xform.concatenate(at);
                sysxform.concatenate(xform);
                blit(srcX, srcY, srcSurf, 0, 0, dstSurf, width, height,
                        sysxform, comp, bgcolor, clip);
            }
        }
    }
    public void blit(int srcX, int srcY, Surface srcSurf, int dstX, int dstY,
            Surface dstSurf, int width, int height, AffineTransform sysxform,
            Composite comp, Color bgcolor, MultiRectArea clip) {
        if(!srcSurf.isNativeDrawable()){
            JavaBlitter.inst.blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf, width, height,
                    sysxform, comp, bgcolor, clip);
        }else{
            int type = sysxform.getType();
            switch(type){
                case AffineTransform.TYPE_TRANSLATION:
                    dstX += sysxform.getTranslateX();
                    dstY += sysxform.getTranslateY();
                case AffineTransform.TYPE_IDENTITY:
                    blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf,
                            width, height, comp, bgcolor, clip);
                    break;
                default:
                    if(srcSurf instanceof ImageSurface){
                        JavaBlitter.inst.blit(srcX, srcY, srcSurf, dstX, dstY, 
                                dstSurf, width, height,
                                sysxform, comp, bgcolor, clip);
                    }else{
                        int w = srcSurf.getWidth();
                        int h = srcSurf.getHeight();
                        BufferedImage tmp = new BufferedImage(w, h, 
                                BufferedImage.TYPE_INT_RGB);
                        Surface tmpSurf = Surface.getImageSurface(tmp);
                        blit(0, 0, srcSurf, 0, 0, tmpSurf,
                                w, h, AlphaComposite.SrcOver, null, null);
                        JavaBlitter.inst.blit(srcX, srcY, tmpSurf, dstX, dstY, 
                                dstSurf, width, height,
                                sysxform, comp, bgcolor, clip);
                    }
            }
        }
    }
    public void blit(int srcX, int srcY, Surface srcSurf, int dstX, int dstY,
            Surface dstSurf, int width, int height, Composite comp,
            Color bgcolor, MultiRectArea clip) {
        if(!srcSurf.isNativeDrawable()){
            JavaBlitter.inst.blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf, width, height,
                    comp, bgcolor, clip);
        }else{
            long dstSurfStruct = dstSurf.getSurfaceDataPtr();
            Object dstData = dstSurf.getData();
            int clipRects[];
            if(clip != null){
                clipRects = clip.rect;
            }else{
                clipRects = new int[]{5, 0, 0, dstSurf.getWidth(),
                        dstSurf.getHeight()};
            }
            if(!(srcSurf instanceof ImageSurface)){
                srcSurf = srcSurf.getImageSurface();
                if(bgcolor != null){
                    bgcolor = null;
                }
            }
            long srcSurfStruct = srcSurf.getSurfaceDataPtr();
            Object srcData = srcSurf.getData();
            if(comp instanceof AlphaComposite){
                AlphaComposite ac = (AlphaComposite) comp;
                int compType = ac.getRule();
                float alpha = ac.getAlpha();
                if(bgcolor != null){
                    bltBG(srcX, srcY, srcSurfStruct, srcData,
                            dstX, dstY, dstSurfStruct, dstData,
                            width, height, bgcolor.getRGB(),
                            compType, alpha, clipRects, srcSurf.invalidated());
                    dstSurf.invalidate();
                    srcSurf.validate();
                }else{
                    blt(srcX, srcY, srcSurfStruct, srcData,
                            dstX, dstY, dstSurfStruct, dstData,
                            width, height, compType, alpha,
                            clipRects, srcSurf.invalidated());
                    dstSurf.invalidate();
                    srcSurf.validate();
                }
            }else if(comp instanceof XORComposite){
                XORComposite xcomp = (XORComposite) comp;
                xor(srcX, srcY, srcSurfStruct, srcData,
                        dstX, dstY, dstSurfStruct, dstData,
                        width, height, xcomp.getXORColor().getRGB(),
                        clipRects, srcSurf.invalidated());
                dstSurf.invalidate();
                srcSurf.validate();
            }else{
                if(srcSurf instanceof ImageSurface){
                    JavaBlitter.inst.blit(srcX, srcY, srcSurf, dstX, dstY, 
                            dstSurf, width, height,
                            comp, bgcolor, clip);
                }else{
                    int w = srcSurf.getWidth();
                    int h = srcSurf.getHeight();
                    BufferedImage tmp = new BufferedImage(w, h, 
                            BufferedImage.TYPE_INT_RGB);
                    Surface tmpSurf = Surface.getImageSurface(tmp);
                    long tmpSurfStruct = tmpSurf.getSurfaceDataPtr();
                    Object tmpData = tmpSurf.getData();
                    int tmpClip[] = new int[]{5, 0, 0, srcSurf.getWidth(),
                            srcSurf.getHeight()};
                    blt(0, 0, srcSurfStruct, srcData, 0, 0,
                            tmpSurfStruct, tmpData, w, h, 
                            AlphaComposite.SRC_OVER,
                            1.0f, tmpClip, srcSurf.invalidated());
                    srcSurf.validate();
                    JavaBlitter.inst.blit(srcX, srcY, tmpSurf, dstX, dstY, 
                            dstSurf, width, height,
                            comp, bgcolor, clip);
                }
            }
        }
    }
    private native void bltBG(int srcX, int srcY, long srsSurfDataPtr,
            Object srcData, int dstX, int dstY, long dstSurfDataPtr,
            Object dstData, int width, int height, int bgcolor,
            int compType, float alpha, int clip[], boolean invalidated);
    private native void blt(int srcX, int srcY, long srsSurfDataPtr,
            Object srcData, int dstX, int dstY, long dstSurfDataPtr,
            Object dstData, int width, int height, int compType,
            float alpha, int clip[], boolean invalidated);
    private native void xor(int srcX, int srcY, long srsSurfDataPtr,
            Object srcData, int dstX, int dstY, long dstSurfDataPtr,
            Object dstData, int width, int height, int xorcolor,
            int clip[], boolean invalidated);
}
