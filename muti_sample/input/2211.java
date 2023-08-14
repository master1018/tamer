public final class CustomComponent {
    public static void register() {
        Class owner = CustomComponent.class;
        GraphicsPrimitive[] primitives = {
            new GraphicsPrimitiveProxy(owner, "OpaqueCopyAnyToArgb",
                                       Blit.methodSignature,
                                       Blit.primTypeID,
                                       SurfaceType.Any,
                                       CompositeType.SrcNoEa,
                                       SurfaceType.IntArgb),
            new GraphicsPrimitiveProxy(owner, "OpaqueCopyArgbToAny",
                                       Blit.methodSignature,
                                       Blit.primTypeID,
                                       SurfaceType.IntArgb,
                                       CompositeType.SrcNoEa,
                                       SurfaceType.Any),
            new GraphicsPrimitiveProxy(owner, "XorCopyArgbToAny",
                                       Blit.methodSignature,
                                       Blit.primTypeID,
                                       SurfaceType.IntArgb,
                                       CompositeType.Xor,
                                       SurfaceType.Any),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    public static Region getRegionOfInterest(SurfaceData src, SurfaceData dst,
                                             Region clip,
                                             int srcx, int srcy,
                                             int dstx, int dsty,
                                             int w, int h)
    {
        Region ret = Region.getInstanceXYWH(dstx, dsty, w, h);
        ret = ret.getIntersection(dst.getBounds());
        Rectangle r = src.getBounds();
        r.translate(dstx - srcx, dsty - srcy);
        ret = ret.getIntersection(r);
        if (clip != null) {
            ret = ret.getIntersection(clip);
        }
        return ret;
    }
}
class OpaqueCopyAnyToArgb extends Blit {
    OpaqueCopyAnyToArgb() {
        super(SurfaceType.Any,
              CompositeType.SrcNoEa,
              SurfaceType.IntArgb);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int srcx, int srcy, int dstx, int dsty, int w, int h)
    {
        Raster srcRast = src.getRaster(srcx, srcy, w, h);
        ColorModel srcCM = src.getColorModel();
        Raster dstRast = dst.getRaster(dstx, dsty, w, h);
        IntegerComponentRaster icr = (IntegerComponentRaster) dstRast;
        int[] dstPix = icr.getDataStorage();
        Region roi = CustomComponent.getRegionOfInterest(src, dst, clip,
                                                         srcx, srcy,
                                                         dstx, dsty, w, h);
        SpanIterator si = roi.getSpanIterator();
        Object srcPix = null;
        int dstScan = icr.getScanlineStride();
        srcx -= dstx;
        srcy -= dsty;
        int span[] = new int[4];
        while (si.nextSpan(span)) {
            int rowoff = icr.getDataOffset(0) + span[1] * dstScan + span[0];
            for (int y = span[1]; y < span[3]; y++) {
                int off = rowoff;
                for (int x = span[0]; x < span[2]; x++) {
                    srcPix = srcRast.getDataElements(x+srcx, y+srcy, srcPix);
                    dstPix[off++] = srcCM.getRGB(srcPix);
                }
                rowoff += dstScan;
            }
        }
        icr.markDirty();
    }
}
class OpaqueCopyArgbToAny extends Blit {
    OpaqueCopyArgbToAny() {
        super(SurfaceType.IntArgb,
              CompositeType.SrcNoEa,
              SurfaceType.Any);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int srcx, int srcy, int dstx, int dsty, int w, int h)
    {
        Raster srcRast = src.getRaster(srcx, srcy, w, h);
        IntegerComponentRaster icr = (IntegerComponentRaster) srcRast;
        int[] srcPix = icr.getDataStorage();
        WritableRaster dstRast =
            (WritableRaster) dst.getRaster(dstx, dsty, w, h);
        ColorModel dstCM = dst.getColorModel();
        Region roi = CustomComponent.getRegionOfInterest(src, dst, clip,
                                                         srcx, srcy,
                                                         dstx, dsty, w, h);
        SpanIterator si = roi.getSpanIterator();
        Object dstPix = null;
        int srcScan = icr.getScanlineStride();
        srcx -= dstx;
        srcy -= dsty;
        int span[] = new int[4];
        while (si.nextSpan(span)) {
            int rowoff = (icr.getDataOffset(0) +
                          (srcy + span[1]) * srcScan +
                          (srcx + span[0]));
            for (int y = span[1]; y < span[3]; y++) {
                int off = rowoff;
                for (int x = span[0]; x < span[2]; x++) {
                    dstPix = dstCM.getDataElements(srcPix[off++], dstPix);
                    dstRast.setDataElements(x, y, dstPix);
                }
                rowoff += srcScan;
            }
        }
    }
}
class XorCopyArgbToAny extends Blit {
    XorCopyArgbToAny() {
        super(SurfaceType.IntArgb,
              CompositeType.Xor,
              SurfaceType.Any);
    }
    public void Blit(SurfaceData src, SurfaceData dst,
                     Composite comp, Region clip,
                     int srcx, int srcy, int dstx, int dsty, int w, int h)
    {
        Raster srcRast = src.getRaster(srcx, srcy, w, h);
        IntegerComponentRaster icr = (IntegerComponentRaster) srcRast;
        int[] srcPix = icr.getDataStorage();
        WritableRaster dstRast =
            (WritableRaster) dst.getRaster(dstx, dsty, w, h);
        ColorModel dstCM = dst.getColorModel();
        Region roi = CustomComponent.getRegionOfInterest(src, dst, clip,
                                                         srcx, srcy,
                                                         dstx, dsty, w, h);
        SpanIterator si = roi.getSpanIterator();
        int xorrgb = ((XORComposite)comp).getXorColor().getRGB();
        Object xorPixel = dstCM.getDataElements(xorrgb, null);
        Object srcPixel = null;
        Object dstPixel = null;
        int srcScan = icr.getScanlineStride();
        srcx -= dstx;
        srcy -= dsty;
        int span[] = new int[4];
        while (si.nextSpan(span)) {
            int rowoff = (icr.getDataOffset(0) +
                          (srcy + span[1]) * srcScan +
                          (srcx + span[0]));
            for (int y = span[1]; y < span[3]; y++) {
                int off = rowoff;
                for (int x = span[0]; x < span[2]; x++) {
                    srcPixel = dstCM.getDataElements(srcPix[off++], srcPixel);
                    dstPixel = dstRast.getDataElements(x, y, dstPixel);
                    switch (dstCM.getTransferType()) {
                    case DataBuffer.TYPE_BYTE:
                        byte[] bytesrcarr = (byte[]) srcPixel;
                        byte[] bytedstarr = (byte[]) dstPixel;
                        byte[] bytexorarr = (byte[]) xorPixel;
                        for (int i = 0; i < bytedstarr.length; i++) {
                            bytedstarr[i] ^= bytesrcarr[i] ^ bytexorarr[i];
                        }
                        break;
                    case DataBuffer.TYPE_SHORT:
                    case DataBuffer.TYPE_USHORT:
                        short[] shortsrcarr = (short[]) srcPixel;
                        short[] shortdstarr = (short[]) dstPixel;
                        short[] shortxorarr = (short[]) xorPixel;
                        for (int i = 0; i < shortdstarr.length; i++) {
                            shortdstarr[i] ^= shortsrcarr[i] ^ shortxorarr[i];
                        }
                        break;
                    case DataBuffer.TYPE_INT:
                        int[] intsrcarr = (int[]) srcPixel;
                        int[] intdstarr = (int[]) dstPixel;
                        int[] intxorarr = (int[]) xorPixel;
                        for (int i = 0; i < intdstarr.length; i++) {
                            intdstarr[i] ^= intsrcarr[i] ^ intxorarr[i];
                        }
                        break;
                    case DataBuffer.TYPE_FLOAT:
                        float[] floatsrcarr = (float[]) srcPixel;
                        float[] floatdstarr = (float[]) dstPixel;
                        float[] floatxorarr = (float[]) xorPixel;
                        for (int i = 0; i < floatdstarr.length; i++) {
                            int v = (Float.floatToIntBits(floatdstarr[i]) ^
                                     Float.floatToIntBits(floatsrcarr[i]) ^
                                     Float.floatToIntBits(floatxorarr[i]));
                            floatdstarr[i] = Float.intBitsToFloat(v);
                        }
                        break;
                    case DataBuffer.TYPE_DOUBLE:
                        double[] doublesrcarr = (double[]) srcPixel;
                        double[] doubledstarr = (double[]) dstPixel;
                        double[] doublexorarr = (double[]) xorPixel;
                        for (int i = 0; i < doubledstarr.length; i++) {
                            long v = (Double.doubleToLongBits(doubledstarr[i]) ^
                                      Double.doubleToLongBits(doublesrcarr[i]) ^
                                      Double.doubleToLongBits(doublexorarr[i]));
                            doubledstarr[i] = Double.longBitsToDouble(v);
                        }
                        break;
                    default:
                        throw new InternalError("Unsupported XOR pixel type");
                    }
                    dstRast.setDataElements(x, y, dstPixel);
                }
                rowoff += srcScan;
            }
        }
    }
}
