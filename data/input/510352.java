public class Raster {
    protected DataBuffer dataBuffer;
    protected int height;
    protected int minX;
    protected int minY;
    protected int numBands;
    protected int numDataElements;
    protected Raster parent;
    protected SampleModel sampleModel;
    protected int sampleModelTranslateX;
    protected int sampleModelTranslateY;
    protected int width;
    public static WritableRaster createBandedRaster(DataBuffer dataBuffer, int w, int h,
            int scanlineStride, int bankIndices[], int bandOffsets[], Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bankIndices == null || bandOffsets == null) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.277")); 
        }
        if (dataBuffer == null) {
            throw new NullPointerException(Messages.getString("awt.278")); 
        }
        int dataType = dataBuffer.getDataType();
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        BandedSampleModel sampleModel = new BandedSampleModel(dataType, w, h, scanlineStride,
                bankIndices, bandOffsets);
        return new OrdinaryWritableRaster(sampleModel, dataBuffer, location);
    }
    public static WritableRaster createBandedRaster(int dataType, int w, int h, int scanlineStride,
            int bankIndices[], int bandOffsets[], Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bankIndices == null || bandOffsets == null) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.277")); 
        }
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        int maxOffset = bandOffsets[0];
        int maxBank = bankIndices[0];
        for (int i = 0; i < bankIndices.length; i++) {
            if (bandOffsets[i] > maxOffset) {
                maxOffset = bandOffsets[i];
            }
            if (bankIndices[i] > maxBank) {
                maxBank = bankIndices[i];
            }
        }
        int numBanks = maxBank + 1;
        int dataSize = scanlineStride * (h - 1) + w + maxOffset;
        DataBuffer data = null;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                data = new DataBufferByte(dataSize, numBanks);
                break;
            case DataBuffer.TYPE_USHORT:
                data = new DataBufferUShort(dataSize, numBanks);
                break;
            case DataBuffer.TYPE_INT:
                data = new DataBufferInt(dataSize, numBanks);
                break;
        }
        return createBandedRaster(data, w, h, scanlineStride, bankIndices, bandOffsets, location);
    }
    public static WritableRaster createBandedRaster(int dataType, int w, int h, int bands,
            Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bands < 1) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.279")); 
        }
        int bandOffsets[] = new int[bands];
        int bankIndices[] = new int[bands];
        for (int i = 0; i < bands; i++) {
            bandOffsets[i] = 0;
            bankIndices[i] = i;
        }
        return createBandedRaster(dataType, w, h, w, bankIndices, bandOffsets, location);
    }
    public static WritableRaster createInterleavedRaster(DataBuffer dataBuffer, int w, int h,
            int scanlineStride, int pixelStride, int bandOffsets[], Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (dataBuffer == null) {
            throw new NullPointerException(Messages.getString("awt.278")); 
        }
        int dataType = dataBuffer.getDataType();
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        if (dataBuffer.getNumBanks() > 1) {
            throw new RasterFormatException(Messages.getString("awt.27A")); 
        }
        if (bandOffsets == null) {
            throw new NullPointerException(Messages.getString("awt.27B")); 
        }
        PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(dataType, w, h,
                pixelStride, scanlineStride, bandOffsets);
        return new OrdinaryWritableRaster(sampleModel, dataBuffer, location);
    }
    public static WritableRaster createInterleavedRaster(int dataType, int w, int h,
            int scanlineStride, int pixelStride, int bandOffsets[], Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        if (bandOffsets == null) {
            throw new NullPointerException(Messages.getString("awt.27B")); 
        }
        int minOffset = bandOffsets[0];
        for (int i = 1; i < bandOffsets.length; i++) {
            if (bandOffsets[i] < minOffset) {
                minOffset = bandOffsets[i];
            }
        }
        int size = (h - 1) * scanlineStride + w * pixelStride + minOffset;
        DataBuffer data = null;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                data = new DataBufferByte(size);
                break;
            case DataBuffer.TYPE_USHORT:
                data = new DataBufferUShort(size);
                break;
        }
        return createInterleavedRaster(data, w, h, scanlineStride, pixelStride, bandOffsets,
                location);
    }
    public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int bands,
            Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        int bandOffsets[] = new int[bands];
        for (int i = 0; i < bands; i++) {
            bandOffsets[i] = i;
        }
        return createInterleavedRaster(dataType, w, h, w * bands, bands, bandOffsets, location);
    }
    public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h,
            int scanlineStride, int bandMasks[], Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException(Messages.getString("awt.278")); 
        }
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bandMasks == null) {
            throw new RasterFormatException(Messages.getString("awt.27C")); 
        }
        if (dataBuffer.getNumBanks() > 1) {
            throw new RasterFormatException(Messages.getString("awt.27A")); 
        }
        int dataType = dataBuffer.getDataType();
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(dataType, w, h,
                scanlineStride, bandMasks);
        return new OrdinaryWritableRaster(sampleModel, dataBuffer, location);
    }
    public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h,
            int bitsPerPixel, Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (dataBuffer == null) {
            throw new NullPointerException(Messages.getString("awt.278")); 
        }
        if (dataBuffer.getNumBanks() > 1) {
            throw new RasterFormatException(Messages.getString("awt.27A")); 
        }
        int dataType = dataBuffer.getDataType();
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        MultiPixelPackedSampleModel sampleModel = new MultiPixelPackedSampleModel(dataType, w, h,
                bitsPerPixel);
        return new OrdinaryWritableRaster(sampleModel, dataBuffer, location);
    }
    public static WritableRaster createPackedRaster(int dataType, int w, int h, int bands,
            int bitsPerBand, Point location) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bands < 1 || bitsPerBand < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.27D")); 
        }
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        if (bitsPerBand * bands > DataBuffer.getDataTypeSize(dataType)) {
            throw new IllegalArgumentException(Messages.getString("awt.27E")); 
        }
        if (bands > 1) {
            int bandMasks[] = new int[bands];
            int mask = (1 << bitsPerBand) - 1;
            for (int i = 0; i < bands; i++) {
                bandMasks[i] = mask << (bitsPerBand * (bands - 1 - i));
            }
            return createPackedRaster(dataType, w, h, bandMasks, location);
        }
        DataBuffer data = null;
        int size = ((bitsPerBand * w + DataBuffer.getDataTypeSize(dataType) - 1) / DataBuffer
                .getDataTypeSize(dataType))
                * h;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                data = new DataBufferByte(size);
                break;
            case DataBuffer.TYPE_USHORT:
                data = new DataBufferUShort(size);
                break;
            case DataBuffer.TYPE_INT:
                data = new DataBufferInt(size);
                break;
        }
        return createPackedRaster(data, w, h, bitsPerBand, location);
    }
    public static WritableRaster createPackedRaster(int dataType, int w, int h, int bandMasks[],
            Point location) {
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT
                && dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException(Messages.getString("awt.230")); 
        }
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((long)location.x + w > Integer.MAX_VALUE || (long)location.y + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.276")); 
        }
        if (bandMasks == null) {
            throw new NullPointerException(Messages.getString("awt.27C")); 
        }
        DataBuffer data = null;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                data = new DataBufferByte(w * h);
                break;
            case DataBuffer.TYPE_USHORT:
                data = new DataBufferUShort(w * h);
                break;
            case DataBuffer.TYPE_INT:
                data = new DataBufferInt(w * h);
                break;
        }
        return createPackedRaster(data, w, h, w, bandMasks, location);
    }
    public static Raster createRaster(SampleModel sm, DataBuffer db, Point location) {
        if (sm == null || db == null) {
            throw new NullPointerException(Messages.getString("awt.27F")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        return new Raster(sm, db, location);
    }
    public static WritableRaster createWritableRaster(SampleModel sm, DataBuffer db, Point location) {
        if (sm == null || db == null) {
            throw new NullPointerException(Messages.getString("awt.27F")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        return new OrdinaryWritableRaster(sm, db, location);
    }
    public static WritableRaster createWritableRaster(SampleModel sm, Point location) {
        if (sm == null) {
            throw new NullPointerException(Messages.getString("awt.280")); 
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        return createWritableRaster(sm, sm.createDataBuffer(), location);
    }
    protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(),
                sampleModel.getHeight()), origin, null);
    }
    protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion,
            Point sampleModelTranslate, Raster parent) {
        if (sampleModel == null || dataBuffer == null || aRegion == null
                || sampleModelTranslate == null) {
            throw new NullPointerException(Messages.getString("awt.281")); 
        }
        if (aRegion.width <= 0 || aRegion.height <= 0) {
            throw new RasterFormatException(Messages.getString("awt.282")); 
        }
        if ((long)aRegion.x + (long)aRegion.width > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.283")); 
        }
        if ((long)aRegion.y + (long)aRegion.height > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.284")); 
        }
        if (sampleModel instanceof ComponentSampleModel) {
            validateDataBuffer(dataBuffer, aRegion.width, aRegion.height,
                    ((ComponentSampleModel)sampleModel).getScanlineStride());
        } else if (sampleModel instanceof MultiPixelPackedSampleModel) {
            validateDataBuffer(dataBuffer, aRegion.width, aRegion.height,
                    ((MultiPixelPackedSampleModel)sampleModel).getScanlineStride());
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            validateDataBuffer(dataBuffer, aRegion.width, aRegion.height,
                    ((SinglePixelPackedSampleModel)sampleModel).getScanlineStride());
        }
        this.sampleModel = sampleModel;
        this.dataBuffer = dataBuffer;
        this.minX = aRegion.x;
        this.minY = aRegion.y;
        this.width = aRegion.width;
        this.height = aRegion.height;
        this.sampleModelTranslateX = sampleModelTranslate.x;
        this.sampleModelTranslateY = sampleModelTranslate.y;
        this.parent = parent;
        this.numBands = sampleModel.getNumBands();
        this.numDataElements = sampleModel.getNumDataElements();
    }
    protected Raster(SampleModel sampleModel, Point origin) {
        this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y,
                sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }
    public Raster createChild(int parentX, int parentY, int width, int height, int childMinX,
            int childMinY, int bandList[]) {
        if (width <= 0 || height <= 0) {
            throw new RasterFormatException(Messages.getString("awt.285")); 
        }
        if (parentX < this.minX || parentX + width > this.minX + this.width) {
            throw new RasterFormatException(Messages.getString("awt.286")); 
        }
        if (parentY < this.minY || parentY + height > this.minY + this.height) {
            throw new RasterFormatException(Messages.getString("awt.287")); 
        }
        if ((long)parentX + width > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.288")); 
        }
        if ((long)parentY + height > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.289")); 
        }
        if ((long)childMinX + width > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.28A")); 
        }
        if ((long)childMinY + height > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.28B")); 
        }
        SampleModel childModel;
        if (bandList == null) {
            childModel = sampleModel;
        } else {
            childModel = sampleModel.createSubsetSampleModel(bandList);
        }
        int childTranslateX = childMinX - parentX;
        int childTranslateY = childMinY - parentY;
        return new Raster(childModel, dataBuffer,
                new Rectangle(childMinX, childMinY, width, height), new Point(childTranslateX
                        + sampleModelTranslateX, childTranslateY + sampleModelTranslateY), this);
    }
    public WritableRaster createCompatibleWritableRaster() {
        return new OrdinaryWritableRaster(sampleModel, new Point(0, 0));
    }
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.22E")); 
        }
        SampleModel sm = sampleModel.createCompatibleSampleModel(w, h);
        return new OrdinaryWritableRaster(sm, new Point(0, 0));
    }
    public WritableRaster createCompatibleWritableRaster(int x, int y, int w, int h) {
        WritableRaster raster = createCompatibleWritableRaster(w, h);
        return raster.createWritableChild(0, 0, w, h, x, y, null);
    }
    public WritableRaster createCompatibleWritableRaster(Rectangle rect) {
        if (rect == null) {
            throw new NullPointerException(Messages.getString("awt.28C")); 
        }
        return createCompatibleWritableRaster(rect.x, rect.y, rect.width, rect.height);
    }
    public Raster createTranslatedChild(int childMinX, int childMinY) {
        return createChild(minX, minY, width, height, childMinX, childMinY, null);
    }
    public Rectangle getBounds() {
        return new Rectangle(minX, minY, width, height);
    }
    public DataBuffer getDataBuffer() {
        return dataBuffer;
    }
    public Object getDataElements(int x, int y, int w, int h, Object outData) {
        return sampleModel.getDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY, w,
                h, outData, dataBuffer);
    }
    public Object getDataElements(int x, int y, Object outData) {
        return sampleModel.getDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY,
                outData, dataBuffer);
    }
    public final int getHeight() {
        return height;
    }
    public final int getMinX() {
        return minX;
    }
    public final int getMinY() {
        return minY;
    }
    public final int getNumBands() {
        return numBands;
    }
    public final int getNumDataElements() {
        return numDataElements;
    }
    public Raster getParent() {
        return parent;
    }
    public double[] getPixel(int x, int y, double dArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, dArray,
                dataBuffer);
    }
    public float[] getPixel(int x, int y, float fArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, fArray,
                dataBuffer);
    }
    public int[] getPixel(int x, int y, int iArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, iArray,
                dataBuffer);
    }
    public double[] getPixels(int x, int y, int w, int h, double dArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                dArray, dataBuffer);
    }
    public float[] getPixels(int x, int y, int w, int h, float fArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                fArray, dataBuffer);
    }
    public int[] getPixels(int x, int y, int w, int h, int iArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                iArray, dataBuffer);
    }
    public int getSample(int x, int y, int b) {
        return sampleModel.getSample(x - sampleModelTranslateX, y - sampleModelTranslateY, b,
                dataBuffer);
    }
    public double getSampleDouble(int x, int y, int b) {
        return sampleModel.getSampleDouble(x - sampleModelTranslateX, y - sampleModelTranslateY, b,
                dataBuffer);
    }
    public float getSampleFloat(int x, int y, int b) {
        return sampleModel.getSampleFloat(x - sampleModelTranslateX, y - sampleModelTranslateY, b,
                dataBuffer);
    }
    public SampleModel getSampleModel() {
        return sampleModel;
    }
    public final int getSampleModelTranslateX() {
        return sampleModelTranslateX;
    }
    public final int getSampleModelTranslateY() {
        return sampleModelTranslateY;
    }
    public double[] getSamples(int x, int y, int w, int h, int b, double dArray[]) {
        return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                b, dArray, dataBuffer);
    }
    public float[] getSamples(int x, int y, int w, int h, int b, float fArray[]) {
        return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                b, fArray, dataBuffer);
    }
    public int[] getSamples(int x, int y, int w, int h, int b, int iArray[]) {
        return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                b, iArray, dataBuffer);
    }
    public final int getTransferType() {
        return sampleModel.getTransferType();
    }
    public final int getWidth() {
        return width;
    }
    private static void validateDataBuffer(final DataBuffer dataBuffer, final int w, final int h,
            final int scanlineStride) {
        if (dataBuffer.getSize() < (scanlineStride * (h - 1) + w - 1)) {
            throw new RasterFormatException(Messages.getString("awt.298")); 
        }
    }
}
