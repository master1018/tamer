class D3DBufImgOps extends BufferedBufImgOps {
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
        if (!(dstData instanceof D3DSurfaceData) ||
            (sg.interpolationType == AffineTransformOp.TYPE_BICUBIC) ||
            (sg.compositeState > SunGraphics2D.COMP_ALPHA))
        {
            return false;
        }
        SurfaceData srcData =
            dstData.getSourceSurfaceData(img, sg.TRANSFORM_ISIDENT,
                                         CompositeType.SrcOver, null);
        if (!(srcData instanceof D3DSurfaceData)) {
            srcData =
                dstData.getSourceSurfaceData(img, sg.TRANSFORM_ISIDENT,
                                             CompositeType.SrcOver, null);
            if (!(srcData instanceof D3DSurfaceData)) {
                return false;
            }
        }
        D3DSurfaceData d3dSrc = (D3DSurfaceData)srcData;
        D3DGraphicsDevice gd =
            (D3DGraphicsDevice)d3dSrc.getDeviceConfiguration().getDevice();
        if (d3dSrc.getType() != D3DSurfaceData.TEXTURE ||
            !gd.isCapPresent(CAPS_LCD_SHADER))
        {
            return false;
        }
        int sw = img.getWidth();
        int sh = img.getHeight();
        D3DBlitLoops.IsoBlit(srcData, dstData,
                             img, biop,
                             sg.composite, sg.getCompClip(),
                             sg.transform, sg.interpolationType,
                             0, 0, sw, sh,
                             x, y, x+sw, y+sh,
                             true);
        return true;
    }
}
