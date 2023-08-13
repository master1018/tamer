public class WritableRasterNative extends WritableRaster {
    public static WritableRasterNative createNativeRaster(SampleModel sm,
                                                          DataBuffer db)
    {
        return new WritableRasterNative(sm, db);
    }
    protected WritableRasterNative(SampleModel sm, DataBuffer db) {
        super(sm, db, new Point(0, 0));
    }
    public static WritableRasterNative createNativeRaster(ColorModel cm,
                                                          SurfaceData sd,
                                                          int width,
                                                          int height)
    {
        SampleModel smHw = null;
        int dataType = 0;
        int scanStride = width;
        switch (cm.getPixelSize()) {
        case 8:
        case 12:
            if (cm.getPixelSize() == 8) {
                dataType = DataBuffer.TYPE_BYTE;
            } else {
                dataType = DataBuffer.TYPE_USHORT;
            }
            int[] bandOffsets = new int[1];
            bandOffsets[0] = 0;
            smHw = new PixelInterleavedSampleModel(dataType, width,
                                                   height,
                                                   1, scanStride,
                                                   bandOffsets);
            break;
        case 15:
        case 16:
            dataType = DataBuffer.TYPE_USHORT;
            int[] bitMasks = new int[3];
            DirectColorModel dcm = (DirectColorModel)cm;
            bitMasks[0] = dcm.getRedMask();
            bitMasks[1] = dcm.getGreenMask();
            bitMasks[2] = dcm.getBlueMask();
            smHw = new SinglePixelPackedSampleModel(dataType, width,
                                                    height, scanStride,
                                                    bitMasks);
            break;
        case 24:
        case 32:
            dataType = DataBuffer.TYPE_INT;
            bitMasks = new int[3];
            dcm = (DirectColorModel)cm;
            bitMasks[0] = dcm.getRedMask();
            bitMasks[1] = dcm.getGreenMask();
            bitMasks[2] = dcm.getBlueMask();
            smHw = new SinglePixelPackedSampleModel(dataType, width,
                                                    height, scanStride,
                                                    bitMasks);
            break;
        default:
            throw new InternalError("Unsupported depth " +
                                    cm.getPixelSize());
        }
        DataBuffer dbn = new DataBufferNative(sd, dataType,
                                              width, height);
        return new WritableRasterNative(smHw, dbn);
    }
}
