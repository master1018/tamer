public class OGLDrawImage extends DrawImage {
    @Override
    protected void renderImageXform(SunGraphics2D sg, Image img,
                                    AffineTransform tx, int interpType,
                                    int sx1, int sy1, int sx2, int sy2,
                                    Color bgColor)
    {
        if (interpType != AffineTransformOp.TYPE_BICUBIC) {
            SurfaceData dstData = sg.surfaceData;
            SurfaceData srcData =
                dstData.getSourceSurfaceData(img,
                                             sg.TRANSFORM_GENERIC,
                                             sg.imageComp,
                                             bgColor);
            if (srcData != null &&
                !isBgOperation(srcData, bgColor) &&
                (srcData.getSurfaceType() == OGLSurfaceData.OpenGLTexture ||
                 srcData.getSurfaceType() == OGLSurfaceData.OpenGLSurfaceRTT ||
                 interpType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR))
            {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                TransformBlit blit = TransformBlit.getFromCache(srcType,
                                                                sg.imageComp,
                                                                dstType);
                if (blit != null) {
                    blit.Transform(srcData, dstData,
                                   sg.composite, sg.getCompClip(),
                                   tx, interpType,
                                   sx1, sy1, 0, 0, sx2-sx1, sy2-sy1);
                    return;
                }
            }
        }
        super.renderImageXform(sg, img, tx, interpType,
                               sx1, sy1, sx2, sy2, bgColor);
    }
    @Override
    public void transformImage(SunGraphics2D sg, BufferedImage img,
                               BufferedImageOp op, int x, int y)
    {
        if (op != null) {
            if (op instanceof AffineTransformOp) {
                AffineTransformOp atop = (AffineTransformOp) op;
                transformImage(sg, img, x, y,
                               atop.getTransform(),
                               atop.getInterpolationType());
                return;
            } else {
                if (OGLBufImgOps.renderImageWithOp(sg, img, op, x, y)) {
                    return;
                }
            }
            img = op.filter(img, null);
        }
        copyImage(sg, img, x, y, null);
    }
}
