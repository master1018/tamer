public class Win32ColorModel24 extends ComponentColorModel {
    public Win32ColorModel24() {
        super(ColorSpace.getInstance(ColorSpace.CS_sRGB),
              new int[] {8, 8, 8}, false, false,
              Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    }
    public WritableRaster createCompatibleWritableRaster (int w, int h) {
        int[] bOffs = {2, 1, 0};
        return Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                              w, h, w*3, 3,
                                              bOffs, null);
    }
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bOffs = {2, 1, 0};
        return new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                                               w, h, 3, w*3, bOffs);
    }
}
