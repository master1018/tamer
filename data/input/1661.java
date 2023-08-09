class OGLBufImgOps extends BufferedBufImgOps {
    static boolean renderImageWithOp(SunGraphics2D sg, BufferedImage img,
                                     BufferedImageOp biop, int x, int y)
    {
        if (biop instanceof ConvolveOp) {
            if (!isConvolveOpValid((ConvolveOp)biop)) {
                return false;
            }
        } else if (biop instanceof RescaleOp) {
            if (!isRescaleOpValid((RescaleOp)biop, img)) {
                return false;
            }
        } else if (biop instanceof LookupOp) {
            if (!isLookupOpValid((LookupOp)biop, img)) {
                return false;
            }
        } else {
            return false;
        }
        SurfaceData dstData = sg.surfaceData;
        if (!(dstData instanceof OGLSurfaceData) ||
            (sg.interpolationType == AffineTransformOp.TYPE_BICUBIC) ||
            (sg.compositeState > SunGraphics2D.COMP_ALPHA))
        {
            return false;
        }
        SurfaceData srcData =
            dstData.getSourceSurfaceData(img, sg.TRANSFORM_ISIDENT,
                                         CompositeType.SrcOver, null);
        if (!(srcData instanceof OGLSurfaceData)) {
            srcData =
                dstData.getSourceSurfaceData(img, sg.TRANSFORM_ISIDENT,
                                             CompositeType.SrcOver, null);
            if (!(srcData instanceof OGLSurfaceData)) {
                return false;
            }
        }
        OGLSurfaceData oglSrc = (OGLSurfaceData)srcData;
        OGLGraphicsConfig gc = oglSrc.getOGLGraphicsConfig();
        if (oglSrc.getType() != OGLSurfaceData.TEXTURE ||
            !gc.isCapPresent(CAPS_EXT_BIOP_SHADER))
        {
            return false;
        }
        int sw = img.getWidth();
        int sh = img.getHeight();
        OGLBlitLoops.IsoBlit(srcData, dstData,
                             img, biop,
                             sg.composite, sg.getCompClip(),
                             sg.transform, sg.interpolationType,
                             0, 0, sw, sh,
                             x, y, x+sw, y+sh,
                             true);
        return true;
    }
}
