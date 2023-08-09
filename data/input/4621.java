public class XRDrawImage extends DrawImage {
    @Override
    protected void renderImageXform(SunGraphics2D sg, Image img,
            AffineTransform tx, int interpType, int sx1, int sy1, int sx2,
            int sy2, Color bgColor) {
        SurfaceData dstData = sg.surfaceData;
        SurfaceData srcData = dstData.getSourceSurfaceData(img,
                SunGraphics2D.TRANSFORM_GENERIC, sg.imageComp, bgColor);
        if (srcData != null && !isBgOperation(srcData, bgColor))  { 
            SurfaceType srcType = srcData.getSurfaceType();
            SurfaceType dstType = dstData.getSurfaceType();
            TransformBlit blit = TransformBlit.getFromCache(srcType,
                    sg.imageComp, dstType);
            if (blit != null) {
                blit.Transform(srcData, dstData, sg.composite,
                        sg.getCompClip(), tx, interpType, sx1, sy1, 0, 0, sx2
                                - sx1, sy2 - sy1);
                return;
            }
        }
        super.renderImageXform(sg, img, tx, interpType, sx1, sy1, sx2, sy2,
                bgColor);
    }
}
