public class XRPMBlitLoops {
    static WeakReference<SunVolatileImage> argbTmpPM = new WeakReference<SunVolatileImage>(null);
    static WeakReference<SunVolatileImage> rgbTmpPM = new WeakReference<SunVolatileImage>(null);
    public XRPMBlitLoops() {
    }
    public static void register() {
        GraphicsPrimitive[] primitives = { new XRPMBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntRgbX11),
                new XRPMBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntArgbPreX11),
                new XRPMBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntRgbX11),
                new XRPMBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntArgbPreX11),
                new XRPMScaledBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntRgbX11),
                new XRPMScaledBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntArgbPreX11),
                new XRPMScaledBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntRgbX11),
                new XRPMScaledBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntArgbPreX11),
                new XRPMTransformedBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntRgbX11),
                new XRPMTransformedBlit(XRSurfaceData.IntRgbX11, XRSurfaceData.IntArgbPreX11),
                new XRPMTransformedBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntRgbX11),
                new XRPMTransformedBlit(XRSurfaceData.IntArgbPreX11, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.IntArgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.IntRgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.IntBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntRgbX11),
                new XrSwToPMBlit(SurfaceType.IntArgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.IntRgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.IntBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.IntArgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.IntRgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.IntBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntRgbX11),
                new XrSwToPMScaledBlit(SurfaceType.IntArgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.IntRgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.IntBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMScaledBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntArgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntRgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntRgbX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntArgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntRgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.IntBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.ThreeByteBgr, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.Ushort565Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.Ushort555Rgb, XRSurfaceData.IntArgbPreX11),
                new XrSwToPMTransformedBlit(SurfaceType.ByteIndexed, XRSurfaceData.IntArgbPreX11), };
        GraphicsPrimitiveMgr.register(primitives);
    }
    protected static XRSurfaceData cacheToTmpSurface(SurfaceData src, XRSurfaceData dst, int w, int h, int sx, int sy) {
        SunVolatileImage vImg;
        SurfaceType vImgSurfaceType;
        if (src.getTransparency() == Transparency.OPAQUE) {
            vImg = rgbTmpPM.get();
            vImgSurfaceType = SurfaceType.IntRgb;
        } else {
            vImg = argbTmpPM.get();
            vImgSurfaceType = SurfaceType.IntArgbPre;
        }
        if (vImg == null || vImg.getWidth() < w || vImg.getHeight() < h) {
            if (vImg != null) {
                vImg.flush();
            }
            vImg = (SunVolatileImage) dst.getGraphicsConfig().createCompatibleVolatileImage(w, h, src.getTransparency());
            vImg.setAccelerationPriority(1.0f);
            if (src.getTransparency() == SurfaceData.OPAQUE) {
                rgbTmpPM = new WeakReference<SunVolatileImage>(vImg);
            } else {
                argbTmpPM = new WeakReference<SunVolatileImage>(vImg);
            }
        }
        Blit swToSurfaceBlit = Blit.getFromCache(src.getSurfaceType(), CompositeType.SrcNoEa, vImgSurfaceType);
        XRSurfaceData vImgSurface = (XRSurfaceData) vImg.getDestSurface();
        swToSurfaceBlit.Blit(src, vImgSurface, AlphaComposite.Src, null,
                             sx, sy, 0, 0, w, h);
        return vImgSurface;
    }
}
class XRPMBlit extends Blit {
    public XRPMBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
    }
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        try {
            SunToolkit.awtLock();
            XRSurfaceData x11sdDst = (XRSurfaceData) dst;
            x11sdDst.validateAsDestination(null, clip);
            XRSurfaceData x11sdSrc = (XRSurfaceData) src;
            x11sdSrc.validateAsSource(null, XRUtils.RepeatNone, XRUtils.FAST);
            x11sdDst.maskBuffer.validateCompositeState(comp, null, null, null);
            x11sdDst.maskBuffer.compositeBlit(x11sdSrc, x11sdDst, sx, sy, dx, dy, w, h);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
}
class XRPMScaledBlit extends ScaledBlit {
    public XRPMScaledBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
    }
    public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1,
            double dx2, double dy2) {
        try {
            SunToolkit.awtLock();
            XRSurfaceData x11sdDst = (XRSurfaceData) dst;
            x11sdDst.validateAsDestination(null, clip);
            XRSurfaceData x11sdSrc = (XRSurfaceData) src;
            x11sdDst.maskBuffer.validateCompositeState(comp, null, null, null);
            double xScale = (dx2 - dx1) / (sx2 - sx1);
            double yScale = (dy2 - dy1) / (sy2 - sy1);
            sx1 *= xScale;
            sx2 *= xScale;
            sy1 *= yScale;
            sy2 *= yScale;
            AffineTransform xForm = AffineTransform.getScaleInstance(1 / xScale, 1 / yScale);
            x11sdSrc.validateAsSource(xForm, XRUtils.RepeatNone, XRUtils.FAST); 
            x11sdDst.maskBuffer.compositeBlit(x11sdSrc, x11sdDst, (int) sx1, (int) sy1, (int) dx1, (int) dy1, (int) (dx2 - dx1), (int) (dy2 - dy1));
        } finally {
            SunToolkit.awtUnlock();
        }
    }
}
class XRPMTransformedBlit extends TransformBlit {
    public XRPMTransformedBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
    }
    public Rectangle getCompositeBounds(AffineTransform tr, int dstx, int dsty, int width, int height) {
        double[] compBounds = new double[8];
        compBounds[0] = dstx;
        compBounds[1] = dsty;
        compBounds[2] = dstx + width;
        compBounds[3] = dsty;
        compBounds[4] = dstx + width;
        compBounds[5] = dsty + height;
        compBounds[6] = dstx;
        compBounds[7] = dsty + height;
        tr.transform(compBounds, 0, compBounds, 0, 4);
        double minX = Math.min(compBounds[0], Math.min(compBounds[2], Math.min(compBounds[4], compBounds[6])));
        double minY = Math.min(compBounds[1], Math.min(compBounds[3], Math.min(compBounds[5], compBounds[7])));
        double maxX = Math.max(compBounds[0], Math.max(compBounds[2], Math.max(compBounds[4], compBounds[6])));
        double maxY = Math.max(compBounds[1], Math.max(compBounds[3], Math.max(compBounds[5], compBounds[7])));
        minX = Math.floor(minX);
        minY = Math.floor(minY);
        maxX = Math.ceil(maxX);
        maxY = Math.ceil(maxY);
        return new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
    }
    public void Transform(SurfaceData src, SurfaceData dst, Composite comp, Region clip, AffineTransform xform, int hint, int srcx, int srcy,
            int dstx, int dsty, int width, int height) {
        try {
            SunToolkit.awtLock();
            int filter = XRUtils.ATransOpToXRQuality(hint);
            XRSurfaceData x11sdDst = (XRSurfaceData) dst;
            x11sdDst.validateAsDestination(null, clip);
            XRSurfaceData x11sdSrc = (XRSurfaceData) src;
            x11sdDst.maskBuffer.validateCompositeState(comp, null, null, null);
            Rectangle bounds = getCompositeBounds(xform, dstx, dsty, width, height);
            AffineTransform trx = AffineTransform.getTranslateInstance((-bounds.x), (-bounds.y));
            trx.concatenate(xform);
            AffineTransform maskTX = (AffineTransform) trx.clone();
            trx.translate(-srcx, -srcy);
            try {
                trx.invert();
            } catch (NoninvertibleTransformException ex) {
                trx.setToIdentity();
                System.err.println("Reseted to identity!");
            }
            boolean omitMask = isMaskOmittable(trx, comp, filter);
            if (!omitMask) {
                XRMaskImage mask = x11sdSrc.maskBuffer.getMaskImage();
                x11sdSrc.validateAsSource(trx, XRUtils.RepeatPad, filter);
                int maskPicture = mask.prepareBlitMask(x11sdDst, maskTX, width, height);
                x11sdDst.maskBuffer.con.renderComposite(XRCompositeManager.getInstance(x11sdSrc).getCompRule(), x11sdSrc.picture, maskPicture, x11sdDst.picture,
                        0, 0, 0, 0, bounds.x, bounds.y, bounds.width, bounds.height);
            } else {
                int repeat = filter == XRUtils.FAST ? XRUtils.RepeatNone : XRUtils.RepeatPad;
                x11sdSrc.validateAsSource(trx, repeat, filter);
                x11sdDst.maskBuffer.compositeBlit(x11sdSrc, x11sdDst, 0, 0, bounds.x, bounds.y, bounds.width, bounds.height);
            }
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    protected static boolean isMaskOmittable(AffineTransform trx, Composite comp, int filter) {
        return (filter == XRUtils.FAST || trx.getTranslateX() == (int) trx.getTranslateX() 
                && trx.getTranslateY() == (int) trx.getTranslateY() && (trx.getShearX() == 0 && trx.getShearY() == 0 
                || trx.getShearX() == -trx.getShearY())) && ((AlphaComposite) comp).getAlpha() == 1.0f; 
    }
}
class XrSwToPMBlit extends Blit {
    Blit pmToSurfaceBlit;
    XrSwToPMBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
        pmToSurfaceBlit = new XRPMBlit(dstType, dstType);
    }
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        if (CompositeType.SrcOverNoEa.equals(comp) && (src.getTransparency() == Transparency.OPAQUE)) {
            Blit opaqueSwToSurfaceBlit = Blit.getFromCache(src.getSurfaceType(), CompositeType.SrcNoEa, dst.getSurfaceType());
            opaqueSwToSurfaceBlit.Blit(src, dst, comp, clip, sx, sy, dx, dy, w, h);
        } else {
            try {
                SunToolkit.awtLock();
                XRSurfaceData vImgSurface = XRPMBlitLoops.cacheToTmpSurface(src, (XRSurfaceData) dst, w, h, sx, sy);
                pmToSurfaceBlit.Blit(vImgSurface, dst, comp, clip, 0, 0, dx, dy, w, h);
            } finally {
                SunToolkit.awtUnlock();
            }
        }
    }
}
class XrSwToPMScaledBlit extends ScaledBlit {
    ScaledBlit pmToSurfaceBlit;
    XrSwToPMScaledBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
        pmToSurfaceBlit = new XRPMScaledBlit(dstType, dstType);
    }
    public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1,
            double dx2, double dy2) {
        {
            int w = sx2 - sx1;
            int h = sy2 - sy1;
            try {
                SunToolkit.awtLock();
                XRSurfaceData vImgSurface = XRPMBlitLoops.cacheToTmpSurface(src, (XRSurfaceData) dst, w, h, sx1, sy1);
                pmToSurfaceBlit.Scale(vImgSurface, dst, comp, clip, 0, 0, w, h, dx1, dy1, dx2, dy2);
            } finally {
                SunToolkit.awtUnlock();
            }
        }
    }
}
class XrSwToPMTransformedBlit extends TransformBlit {
    TransformBlit pmToSurfaceBlit;
    XrSwToPMTransformedBlit(SurfaceType srcType, SurfaceType dstType) {
        super(srcType, CompositeType.AnyAlpha, dstType);
        pmToSurfaceBlit = new XRPMTransformedBlit(dstType, dstType);
    }
    public void Transform(SurfaceData src, SurfaceData dst, Composite comp, Region clip, AffineTransform xform, int hint, int sx, int sy, int dstx,
            int dsty, int w, int h) {
        try {
            SunToolkit.awtLock();
            XRSurfaceData vImgSurface = XRPMBlitLoops.cacheToTmpSurface(src, (XRSurfaceData) dst, w, h, sx, sy);
            pmToSurfaceBlit.Transform(vImgSurface, dst, comp, clip, xform, hint, 0, 0, dstx, dsty, w, h);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
}
