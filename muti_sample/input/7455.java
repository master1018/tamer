public class DrawImage implements DrawImagePipe
{
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor)
    {
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if (isSimpleTranslate(sg)) {
            return renderImageCopy(sg, img, bgColor,
                                   x + sg.transX, y + sg.transY,
                                   0, 0, imgw, imgh);
        }
        AffineTransform atfm = sg.transform;
        if ((x | y) != 0) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
        }
        transformImage(sg, img, atfm, sg.interpolationType,
                       0, 0, imgw, imgh, bgColor);
        return true;
    }
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor)
    {
        if (isSimpleTranslate(sg)) {
            return renderImageCopy(sg, img, bgColor,
                                   dx + sg.transX, dy + sg.transY,
                                   sx, sy, w, h);
        }
        scaleImage(sg, img, dx, dy, (dx + w), (dy + h),
                   sx, sy, (sx + w), (sy + h), bgColor);
        return true;
    }
    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y,
                              int width, int height,
                              Color bgColor)
    {
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if ((width > 0) && (height > 0) && isSimpleTranslate(sg)) {
            double dx1 = x + sg.transX;
            double dy1 = y + sg.transY;
            double dx2 = dx1 + width;
            double dy2 = dy1 + height;
            if (renderImageScale(sg, img, bgColor, sg.interpolationType,
                                 0, 0, imgw, imgh,
                                 dx1, dy1, dx2, dy2))
            {
                return true;
            }
        }
        AffineTransform atfm = sg.transform;
        if ((x | y) != 0 || width != imgw || height != imgh) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
            atfm.scale(((double)width)/imgw, ((double)height)/imgh);
        }
        transformImage(sg, img, atfm, sg.interpolationType,
                       0, 0, imgw, imgh, bgColor);
        return true;
    }
    protected void transformImage(SunGraphics2D sg, Image img, int x, int y,
                                  AffineTransform extraAT, int interpType)
    {
        int txtype = extraAT.getType();
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        boolean checkfinalxform;
        if (sg.transformState <= sg.TRANSFORM_ANY_TRANSLATE &&
            (txtype == AffineTransform.TYPE_IDENTITY ||
             txtype == AffineTransform.TYPE_TRANSLATION))
        {
            double tx = extraAT.getTranslateX();
            double ty = extraAT.getTranslateY();
            tx += sg.transform.getTranslateX();
            ty += sg.transform.getTranslateY();
            int itx = (int) Math.floor(tx + 0.5);
            int ity = (int) Math.floor(ty + 0.5);
            if (interpType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR ||
                (closeToInteger(itx, tx) && closeToInteger(ity, ty)))
            {
                renderImageCopy(sg, img, null, x+itx, y+ity, 0, 0, imgw, imgh);
                return;
            }
            checkfinalxform = false;
        } else if (sg.transformState <= sg.TRANSFORM_TRANSLATESCALE &&
                   ((txtype & (AffineTransform.TYPE_FLIP |
                               AffineTransform.TYPE_MASK_ROTATION |
                               AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0))
        {
            double coords[] = new double[] {
                0, 0, imgw, imgh,
            };
            extraAT.transform(coords, 0, coords, 0, 2);
            coords[0] += x;
            coords[1] += y;
            coords[2] += x;
            coords[3] += y;
            sg.transform.transform(coords, 0, coords, 0, 2);
            if (tryCopyOrScale(sg, img, 0, 0, imgw, imgh,
                               null, interpType, coords))
            {
                return;
            }
            checkfinalxform = false;
        } else {
            checkfinalxform = true;
        }
        AffineTransform tx = new AffineTransform(sg.transform);
        tx.translate(x, y);
        tx.concatenate(extraAT);
        if (checkfinalxform) {
            transformImage(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        } else {
            renderImageXform(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        }
    }
    protected void transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform tx, int interpType,
                                  int sx1, int sy1, int sx2, int sy2,
                                  Color bgColor)
    {
        double coords[] = new double[6];
        coords[2] = sx2 - sx1;
        coords[3] = coords[5] = sy2 - sy1;
        tx.transform(coords, 0, coords, 0, 3);
        if (Math.abs(coords[0] - coords[4]) < MAX_TX_ERROR &&
            Math.abs(coords[3] - coords[5]) < MAX_TX_ERROR &&
            tryCopyOrScale(sg, img, sx1, sy1, sx2, sy2,
                           bgColor, interpType, coords))
        {
            return;
        }
        renderImageXform(sg, img, tx, interpType, sx1, sy1, sx2, sy2, bgColor);
    }
    protected boolean tryCopyOrScale(SunGraphics2D sg,
                                     Image img,
                                     int sx1, int sy1,
                                     int sx2, int sy2,
                                     Color bgColor, int interpType,
                                     double coords[])
    {
        double dx = coords[0];
        double dy = coords[1];
        double dw = coords[2] - dx;
        double dh = coords[3] - dy;
        if (closeToInteger(sx2-sx1, dw) && closeToInteger(sy2-sy1, dh)) {
            int idx = (int) Math.floor(dx + 0.5);
            int idy = (int) Math.floor(dy + 0.5);
            if (interpType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR ||
                (closeToInteger(idx, dx) && closeToInteger(idy, dy)))
            {
                renderImageCopy(sg, img, bgColor,
                                idx, idy,
                                sx1, sy1, sx2-sx1, sy2-sy1);
                return true;
            }
        }
        if (dw > 0 && dh > 0) {
            if (renderImageScale(sg, img, bgColor, interpType,
                                 sx1, sy1, sx2, sy2,
                                 coords[0], coords[1], coords[2], coords[3]))
            {
                return true;
            }
        }
        return false;
    }
    BufferedImage makeBufferedImage(Image img, Color bgColor, int type,
                                    int sx1, int sy1, int sx2, int sy2)
    {
        BufferedImage bimg = new BufferedImage(sx2-sx1, sy2-sy1, type);
        Graphics2D g2d = bimg.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        if (bgColor != null) {
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, sx2-sx1, sy2-sy1);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
        g2d.drawImage(img, -sx1, -sy1, null);
        g2d.dispose();
        return bimg;
    }
    protected void renderImageXform(SunGraphics2D sg, Image img,
                                    AffineTransform tx, int interpType,
                                    int sx1, int sy1, int sx2, int sy2,
                                    Color bgColor)
    {
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        SurfaceData srcData = dstData.getSourceSurfaceData(img,
                                                           sg.TRANSFORM_GENERIC,
                                                           sg.imageComp,
                                                           bgColor);
        if (srcData == null) {
            img = getBufferedImage(img);
            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   bgColor);
            if (srcData == null) {
                return;
            }
        }
        if (isBgOperation(srcData, bgColor)) {
            img = makeBufferedImage(img, bgColor, BufferedImage.TYPE_INT_RGB,
                                    sx1, sy1, sx2, sy2);
            sx2 -= sx1;
            sy2 -= sy1;
            sx1 = sy1 = 0;
            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   bgColor);
        }
        SurfaceType srcType = srcData.getSurfaceType();
        TransformHelper helper = TransformHelper.getFromCache(srcType);
        if (helper == null) {
            int type = ((srcData.getTransparency() == Transparency.OPAQUE)
                        ? BufferedImage.TYPE_INT_RGB
                        : BufferedImage.TYPE_INT_ARGB);
            img = makeBufferedImage(img, null, type, sx1, sy1, sx2, sy2);
            sx2 -= sx1;
            sy2 -= sy1;
            sx1 = sy1 = 0;
            srcData = dstData.getSourceSurfaceData(img,
                                                   sg.TRANSFORM_GENERIC,
                                                   sg.imageComp,
                                                   null);
            srcType = srcData.getSurfaceType();
            helper = TransformHelper.getFromCache(srcType);
        }
        AffineTransform itx;
        try {
            itx = tx.createInverse();
        } catch (NoninvertibleTransformException e) {
            return;
        }
        double coords[] = new double[8];
        coords[2] = coords[6] = sx2 - sx1;
        coords[5] = coords[7] = sy2 - sy1;
        tx.transform(coords, 0, coords, 0, 4);
        double ddx1, ddy1, ddx2, ddy2;
        ddx1 = ddx2 = coords[0];
        ddy1 = ddy2 = coords[1];
        for (int i = 2; i < coords.length; i += 2) {
            double d = coords[i];
            if (ddx1 > d) ddx1 = d;
            else if (ddx2 < d) ddx2 = d;
            d = coords[i+1];
            if (ddy1 > d) ddy1 = d;
            else if (ddy2 < d) ddy2 = d;
        }
        int dx1 = (int) Math.floor(ddx1);
        int dy1 = (int) Math.floor(ddy1);
        int dx2 = (int) Math.ceil(ddx2);
        int dy2 = (int) Math.ceil(ddy2);
        SurfaceType dstType = dstData.getSurfaceType();
        MaskBlit maskblit;
        Blit blit;
        if (sg.compositeState <= sg.COMP_ALPHA) {
            maskblit = MaskBlit.getFromCache(SurfaceType.IntArgbPre,
                                             sg.imageComp,
                                             dstType);
            if (maskblit.getNativePrim() != 0) {
                helper.Transform(maskblit, srcData, dstData,
                                 sg.composite, clip,
                                 itx, interpType,
                                 sx1, sy1, sx2, sy2,
                                 dx1, dy1, dx2, dy2,
                                 null, 0, 0);
                return;
            }
            blit = null;
        } else {
            maskblit = null;
            blit = Blit.getFromCache(SurfaceType.IntArgbPre,
                                     sg.imageComp,
                                     dstType);
        }
        BufferedImage tmpimg = new BufferedImage(dx2-dx1, dy2-dy1,
                                                 BufferedImage.TYPE_INT_ARGB);
        SurfaceData tmpData = SurfaceData.getPrimarySurfaceData(tmpimg);
        SurfaceType tmpType = tmpData.getSurfaceType();
        MaskBlit tmpmaskblit =
            MaskBlit.getFromCache(SurfaceType.IntArgbPre,
                                  CompositeType.SrcNoEa,
                                  tmpType);
        int edges[] = new int[(dy2-dy1)*2+2];
        helper.Transform(tmpmaskblit, srcData, tmpData,
                         AlphaComposite.Src, null,
                         itx, interpType,
                         sx1, sy1, sx2, sy2,
                         0, 0, dx2-dx1, dy2-dy1,
                         edges, dx1, dy1);
        int index = 2;
        for (int y = edges[0]; y < edges[1]; y++) {
            int relx1 = edges[index++];
            int relx2 = edges[index++];
            if (relx1 >= relx2) {
                continue;
            }
            if (maskblit != null) {
                maskblit.MaskBlit(tmpData, dstData,
                                  sg.composite, clip,
                                  relx1, y,
                                  dx1+relx1, dy1+y,
                                  relx2 - relx1, 1,
                                  null, 0, 0);
            } else {
                blit.Blit(tmpData, dstData,
                          sg.composite, clip,
                          relx1, y,
                          dx1+relx1, dy1+y,
                          relx2 - relx1, 1);
            }
        }
    }
    protected boolean renderImageCopy(SunGraphics2D sg, Image img,
                                      Color bgColor,
                                      int dx, int dy,
                                      int sx, int sy,
                                      int w, int h)
    {
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        int attempts = 0;
        while (true) {
            SurfaceData srcData =
                dstData.getSourceSurfaceData(img,
                                             sg.TRANSFORM_ISIDENT,
                                             sg.imageComp,
                                             bgColor);
            if (srcData == null) {
                return false;
            }
            try {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                blitSurfaceData(sg, clip,
                                srcData, dstData, srcType, dstType,
                                sx, sy, dx, dy, w, h, bgColor);
                return true;
            } catch (NullPointerException e) {
                if (!(SurfaceData.isNull(dstData) ||
                      SurfaceData.isNull(srcData)))
                {
                    throw e;
                }
                return false;
            } catch (InvalidPipeException e) {
                ++attempts;
                clip = sg.getCompClip();   
                dstData = sg.surfaceData;
                if (SurfaceData.isNull(dstData) ||
                    SurfaceData.isNull(srcData) || (attempts > 1))
                {
                    return false;
                }
            }
        }
    }
    protected boolean renderImageScale(SunGraphics2D sg, Image img,
                                       Color bgColor, int interpType,
                                       int sx1, int sy1,
                                       int sx2, int sy2,
                                       double dx1, double dy1,
                                       double dx2, double dy2)
    {
        if (interpType != AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
            return false;
        }
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        int attempts = 0;
        while (true) {
            SurfaceData srcData =
                dstData.getSourceSurfaceData(img,
                                             sg.TRANSFORM_TRANSLATESCALE,
                                             sg.imageComp,
                                             bgColor);
            if (srcData == null || isBgOperation(srcData, bgColor)) {
                return false;
            }
            try {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                return scaleSurfaceData(sg, clip,
                                        srcData, dstData, srcType, dstType,
                                        sx1, sy1, sx2, sy2,
                                        dx1, dy1, dx2, dy2);
            } catch (NullPointerException e) {
                if (!SurfaceData.isNull(dstData)) {
                    throw e;
                }
                return false;
            } catch (InvalidPipeException e) {
                ++attempts;
                clip = sg.getCompClip();  
                dstData = sg.surfaceData;
                if (SurfaceData.isNull(dstData) ||
                    SurfaceData.isNull(srcData) || (attempts > 1))
                {
                    return false;
                }
            }
        }
    }
    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor)
    {
        int srcW, srcH, dstW, dstH;
        int srcX, srcY, dstX, dstY;
        boolean srcWidthFlip = false;
        boolean srcHeightFlip = false;
        boolean dstWidthFlip = false;
        boolean dstHeightFlip = false;
        if (sx2 > sx1) {
            srcW = sx2 - sx1;
            srcX = sx1;
        } else {
            srcWidthFlip = true;
            srcW = sx1 - sx2;
            srcX = sx2;
        }
        if (sy2 > sy1) {
            srcH = sy2-sy1;
            srcY = sy1;
        } else {
            srcHeightFlip = true;
            srcH = sy1-sy2;
            srcY = sy2;
        }
        if (dx2 > dx1) {
            dstW = dx2 - dx1;
            dstX = dx1;
        } else {
            dstW = dx1 - dx2;
            dstWidthFlip = true;
            dstX = dx2;
        }
        if (dy2 > dy1) {
            dstH = dy2 - dy1;
            dstY = dy1;
        } else {
            dstH = dy1 - dy2;
            dstHeightFlip = true;
            dstY = dy2;
        }
        if (srcW <= 0 || srcH <= 0) {
            return true;
        }
        if ((srcWidthFlip == dstWidthFlip) &&
            (srcHeightFlip == dstHeightFlip) &&
            isSimpleTranslate(sg))
        {
            double ddx1 = dstX + sg.transX;
            double ddy1 = dstY + sg.transY;
            double ddx2 = ddx1 + dstW;
            double ddy2 = ddy1 + dstH;
            if (renderImageScale(sg, img, bgColor, sg.interpolationType,
                                 srcX, srcY, srcX+srcW, srcY+srcH,
                                 ddx1, ddy1, ddx2, ddy2))
            {
                return true;
            }
        }
        AffineTransform atfm = new AffineTransform(sg.transform);
        atfm.translate(dx1, dy1);
        double m00 = (double)(dx2-dx1)/(sx2-sx1);
        double m11 = (double)(dy2-dy1)/(sy2-sy1);
        atfm.scale(m00, m11);
        atfm.translate(srcX-sx1, srcY-sy1);
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        srcW += srcX;
        srcH += srcY;
        if (srcW > imgW) {
            srcW = imgW;
        }
        if (srcH > imgH) {
            srcH = imgH;
        }
        if (srcX < 0) {
            atfm.translate(-srcX, 0);
            srcX = 0;
        }
        if (srcY < 0) {
            atfm.translate(0, -srcY);
            srcY = 0;
        }
        if (srcX >= srcW || srcY >= srcH) {
            return true;
        }
        transformImage(sg, img, atfm, sg.interpolationType,
                       srcX, srcY, srcW, srcH, bgColor);
        return true;
    }
    private static final double MAX_TX_ERROR = .0001;
    public static boolean closeToInteger(int i, double d) {
        return (Math.abs(d-i) < MAX_TX_ERROR);
    }
    public static boolean isSimpleTranslate(SunGraphics2D sg) {
        int ts = sg.transformState;
        if (ts <= sg.TRANSFORM_INT_TRANSLATE) {
            return true;
        }
        if (ts >= sg.TRANSFORM_TRANSLATESCALE) {
            return false;
        }
        if (sg.interpolationType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
            return true;
        }
        return false;
    }
    protected static boolean isBgOperation(SurfaceData srcData, Color bgColor) {
        return ((srcData == null) ||
                ((bgColor != null) &&
                 (srcData.getTransparency() != Transparency.OPAQUE)));
    }
    protected BufferedImage getBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }
        return ((VolatileImage)img).getSnapshot();
    }
    private ColorModel getTransformColorModel(SunGraphics2D sg,
                                              BufferedImage bImg,
                                              AffineTransform tx) {
        ColorModel cm = bImg.getColorModel();
        ColorModel dstCM = cm;
        if (tx.isIdentity()) {
            return dstCM;
        }
        int type = tx.getType();
        boolean needTrans =
            ((type&(tx.TYPE_MASK_ROTATION|tx.TYPE_GENERAL_TRANSFORM)) != 0);
        if (! needTrans && type != tx.TYPE_TRANSLATION && type != tx.TYPE_IDENTITY)
        {
            double[] mtx = new double[4];
            tx.getMatrix(mtx);
            needTrans = (mtx[0] != (int)mtx[0] || mtx[3] != (int)mtx[3]);
        }
        if (sg.renderHint != SunHints.INTVAL_RENDER_QUALITY) {
            if (cm instanceof IndexColorModel) {
                Raster raster = bImg.getRaster();
                IndexColorModel icm = (IndexColorModel) cm;
                if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                    if (raster instanceof sun.awt.image.BytePackedRaster) {
                        dstCM = ColorModel.getRGBdefault();
                    }
                    else {
                        double[] matrix = new double[6];
                        tx.getMatrix(matrix);
                        if (matrix[1] == 0. && matrix[2] ==0.
                            && matrix[4] == 0. && matrix[5] == 0.) {
                        }
                        else {
                            int mapSize = icm.getMapSize();
                            if (mapSize < 256) {
                                int[] cmap = new int[mapSize+1];
                                icm.getRGBs(cmap);
                                cmap[mapSize] = 0x0000;
                                dstCM = new
                                    IndexColorModel(icm.getPixelSize(),
                                                    mapSize+1,
                                                    cmap, 0, true, mapSize,
                                                    DataBuffer.TYPE_BYTE);
                            }
                            else {
                                dstCM = ColorModel.getRGBdefault();
                            }
                        }  
                    }   
                } 
            } 
            else if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                dstCM = ColorModel.getRGBdefault();
            }
        } 
        else {
            if (cm instanceof IndexColorModel ||
                (needTrans && cm.getTransparency() == cm.OPAQUE))
            {
                dstCM = ColorModel.getRGBdefault();
            }
        }
        return dstCM;
    }
    protected void blitSurfaceData(SunGraphics2D sg,
                                   Region clipRegion,
                                   SurfaceData srcData,
                                   SurfaceData dstData,
                                   SurfaceType srcType,
                                   SurfaceType dstType,
                                   int sx, int sy, int dx, int dy,
                                   int w, int h,
                                   Color bgColor)
    {
        if (w <= 0 || h <= 0) {
            return;
        }
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE ||
             (bgColor != null &&
              bgColor.getTransparency() == Transparency.OPAQUE)))
        {
            comp = CompositeType.SrcNoEa;
        }
        if (!isBgOperation(srcData, bgColor)) {
            Blit blit = Blit.getFromCache(srcType, comp, dstType);
            blit.Blit(srcData, dstData, sg.composite, clipRegion,
                      sx, sy, dx, dy, w, h);
        } else {
            BlitBg blit = BlitBg.getFromCache(srcType, comp, dstType);
            blit.BlitBg(srcData, dstData, sg.composite, clipRegion,
                        bgColor.getRGB(), sx, sy, dx, dy, w, h);
        }
    }
    protected boolean scaleSurfaceData(SunGraphics2D sg,
                                       Region clipRegion,
                                       SurfaceData srcData,
                                       SurfaceData dstData,
                                       SurfaceType srcType,
                                       SurfaceType dstType,
                                       int sx1, int sy1,
                                       int sx2, int sy2,
                                       double dx1, double dy1,
                                       double dx2, double dy2)
    {
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE))
        {
            comp = CompositeType.SrcNoEa;
        }
        ScaledBlit blit = ScaledBlit.getFromCache(srcType, comp, dstType);
        if (blit != null) {
            blit.Scale(srcData, dstData, sg.composite, clipRegion,
                       sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            return true;
        }
        return false;
    }
    protected static boolean imageReady(ToolkitImage sunimg,
                                        ImageObserver observer)
    {
        if (sunimg.hasError()) {
            if (observer != null) {
                observer.imageUpdate(sunimg,
                                     ImageObserver.ERROR|ImageObserver.ABORT,
                                     -1, -1, -1, -1);
            }
            return false;
        }
        return true;
    }
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return copyImage(sg, img, x, y, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, bgColor, observer);
        }
    }
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return copyImage(sg, img, dx, dy, sx, sy, w, h, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg,
                                     dx, dy, (dx + w), (dy + h),
                                     sx, sy, (sx + w), (sy + h),
                                     bgColor, observer);
        }
    }
    public boolean scaleImage(SunGraphics2D sg, Image img,
                                int x, int y,
                                int width, int height,
                                Color bgColor,
                                ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return scaleImage(sg, img, x, y, width, height, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, width, height, bgColor,
                                     observer);
        }
    }
    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor,
                              ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return scaleImage(sg, img, dx1, dy1, dx2, dy2,
                              sx1, sy1, sx2, sy2, bgColor);
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, dx1, dy1, dx2, dy2,
                                     sx1, sy1, sx2, sy2, bgColor, observer);
        }
    }
    public boolean transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform atfm,
                                  ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            transformImage(sg, img, 0, 0, atfm, sg.interpolationType);
            return true;
        } else {
            ToolkitImage sunimg = (ToolkitImage)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, atfm, observer);
        }
    }
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
                img = op.filter(img, null);
            }
        }
        copyImage(sg, img, x, y, null);
    }
}
