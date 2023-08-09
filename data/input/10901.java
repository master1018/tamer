public class IntegerComponentRaster extends SunWritableRaster {
    static final int TYPE_CUSTOM                = 0;
    static final int TYPE_BYTE_SAMPLES          = 1;
    static final int TYPE_USHORT_SAMPLES        = 2;
    static final int TYPE_INT_SAMPLES           = 3;
    static final int TYPE_BYTE_BANDED_SAMPLES   = 4;
    static final int TYPE_USHORT_BANDED_SAMPLES = 5;
    static final int TYPE_INT_BANDED_SAMPLES    = 6;
    static final int TYPE_BYTE_PACKED_SAMPLES   = 7;
    static final int TYPE_USHORT_PACKED_SAMPLES = 8;
    static final int TYPE_INT_PACKED_SAMPLES    = 9;
    static final int TYPE_INT_8BIT_SAMPLES      = 10;
    static final int TYPE_BYTE_BINARY_SAMPLES   = 11;
    protected int bandOffset;
    protected int[]         dataOffsets;
    protected int           scanlineStride;
    protected int           pixelStride;
    protected int[]         data;
    protected int           numDataElems;
    int type;
    private int maxX;
    private int maxY;
    static private native void initIDs();
    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }
    public IntegerComponentRaster(SampleModel sampleModel,
                                     Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }
    public IntegerComponentRaster(SampleModel sampleModel,
                                     DataBuffer dataBuffer,
                                     Point origin) {
        this(sampleModel,
             dataBuffer,
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }
    public IntegerComponentRaster(SampleModel sampleModel,
                                     DataBuffer dataBuffer,
                                     Rectangle aRegion,
                                     Point origin,
                                     IntegerComponentRaster parent){
        super(sampleModel,dataBuffer,aRegion,origin,parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferInt)) {
           throw new RasterFormatException("IntegerComponentRasters must have" +
                "integer DataBuffers");
        }
        DataBufferInt dbi = (DataBufferInt)dataBuffer;
        if (dbi.getNumBanks() != 1) {
            throw new
                RasterFormatException("DataBuffer for IntegerComponentRasters"+
                                      " must only have 1 bank.");
        }
        this.data = stealData(dbi, 0);
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm =
                    (SinglePixelPackedSampleModel)sampleModel;
            int[] boffsets = sppsm.getBitOffsets();
            boolean notByteBoundary = false;
            for (int i=1; i < boffsets.length; i++) {
                if ((boffsets[i]%8) != 0) {
                    notByteBoundary = true;
                }
            }
            this.type = (notByteBoundary
                         ? IntegerComponentRaster.TYPE_INT_PACKED_SAMPLES
                         : IntegerComponentRaster.TYPE_INT_8BIT_SAMPLES);
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride    = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dbi.getOffset();
            this.bandOffset = this.dataOffsets[0];
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            dataOffsets[0] += xOffset+yOffset*scanlineStride;
            this.numDataElems = sppsm.getNumDataElements();
        } else {
            throw new RasterFormatException("IntegerComponentRasters must have"+
                                            " SinglePixelPackedSampleModel");
        }
        verify(false);
    }
    public int[] getDataOffsets() {
        return (int[]) dataOffsets.clone();
    }
    public int getDataOffset(int band) {
        return dataOffsets[band];
    }
    public int getScanlineStride() {
        return scanlineStride;
    }
    public int getPixelStride() {
        return pixelStride;
    }
    public int[] getDataStorage() {
        return data;
    }
    public Object getDataElements(int x, int y, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int outData[];
        if (obj == null) {
            outData = new int[numDataElements];
        } else {
            outData = (int[])obj;
        }
        int off = (y-minY)*scanlineStride +
                  (x-minX)*pixelStride;
        for (int band = 0; band < numDataElements; band++) {
            outData[band] = data[dataOffsets[band] + off];
        }
        return outData;
    }
    public Object getDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int outData[];
        if (obj instanceof int[]) {
            outData = (int[])obj;
        } else {
            outData = new int[numDataElements*w*h];
        }
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride;
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
            xoff = yoff;
            for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                for (int c = 0; c < numDataElements; c++) {
                    outData[off++] = data[dataOffsets[c] + xoff];
                }
            }
        }
        return outData;
    }
    public void setDataElements(int x, int y, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int inData[] = (int[])obj;
        int off = (y-minY)*scanlineStride +
                  (x-minX)*pixelStride;
        for (int i = 0; i < numDataElements; i++) {
            data[dataOffsets[i] + off] = inData[i];
        }
        markDirty();
    }
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = x + inRaster.getMinX();
        int dstOffY = y + inRaster.getMinY();
        int width  = inRaster.getWidth();
        int height = inRaster.getHeight();
        if ((dstOffX < this.minX) || (dstOffY < this.minY) ||
            (dstOffX + width > this.maxX) || (dstOffY + height > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        setDataElements(dstOffX, dstOffY, width, height, inRaster);
    }
    private void setDataElements(int dstX, int dstY,
                                 int width, int height,
                                 Raster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int tdata[] = null;
        if (inRaster instanceof IntegerComponentRaster &&
            (pixelStride == 1) && (numDataElements == 1)) {
            IntegerComponentRaster ict = (IntegerComponentRaster) inRaster;
            if (ict.getNumDataElements() != 1) {
                throw new ArrayIndexOutOfBoundsException("Number of bands"+
                                                         " does not match");
            }
            tdata    = ict.getDataStorage();
            int tss  = ict.getScanlineStride();
            int toff = ict.getDataOffset(0);
            int srcOffset = toff;
            int dstOffset = dataOffsets[0]+(dstY-minY)*scanlineStride+
                                           (dstX-minX);
            if (ict.getPixelStride() == pixelStride) {
                width *= pixelStride;
                for (int startY=0; startY < height; startY++) {
                    System.arraycopy(tdata, srcOffset, data, dstOffset, width);
                    srcOffset += tss;
                    dstOffset += scanlineStride;
                }
                markDirty();
                return;
            }
        }
        Object odata = null;
        for (int startY=0; startY < height; startY++) {
            odata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, odata);
            setDataElements(dstX, dstY+startY,
                            width, 1, odata);
        }
    }
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int inData[] = (int[])obj;
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride;
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
            xoff = yoff;
            for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                for (int c = 0; c < numDataElements; c++) {
                    data[dataOffsets[c] + xoff] = inData[off++];
                }
            }
        }
        markDirty();
    }
    public WritableRaster createWritableChild (int x, int y,
                                               int width, int height,
                                               int x0, int y0,
                                               int bandList[]) {
        if (x < this.minX) {
            throw new RasterFormatException("x lies outside raster");
        }
        if (y < this.minY) {
            throw new RasterFormatException("y lies outside raster");
        }
        if ((x+width < x) || (x+width > this.minX + this.width)) {
            throw new RasterFormatException("(x + width) is outside raster");
        }
        if ((y+height < y) || (y+height > this.minY + this.height)) {
            throw new RasterFormatException("(y + height) is outside raster");
        }
        SampleModel sm;
        if (bandList != null)
            sm = sampleModel.createSubsetSampleModel(bandList);
        else
            sm = sampleModel;
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new IntegerComponentRaster(sm,
                                          dataBuffer,
                                          new Rectangle(x0,y0,width,height),
                                          new Point(sampleModelTranslateX+deltaX,
                                                    sampleModelTranslateY+deltaY),
                                          this);
    }
    public Raster createChild (int x, int y,
                               int width, int height,
                               int x0, int y0,
                               int bandList[]) {
        return createWritableChild(x, y, width, height, x0, y0, bandList);
    }
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <=0) {
            throw new RasterFormatException("negative "+
                                          ((w <= 0) ? "width" : "height"));
        }
        SampleModel sm = sampleModel.createCompatibleSampleModel(w,h);
        return new IntegerComponentRaster(sm, new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width,height);
    }
    private void verify (boolean strictCheck) {
        if (dataOffsets[0] < 0) {
            throw new RasterFormatException("Data offset ("+dataOffsets[0]+
                                            ") must be >= 0");
        }
        int maxSize = 0;
        int size;
        for (int i=0; i < numDataElements; i++) {
            size = (height-1)*scanlineStride + (width-1)*pixelStride +
                dataOffsets[i];
            if (size > maxSize) {
                maxSize = size;
            }
        }
        if (data.length < maxSize) {
            throw new RasterFormatException("Data array too small (should be "+
                                          maxSize
                                          +" but is "+data.length+" )");
        }
    }
    public String toString() {
        return new String ("IntegerComponentRaster: width = "+width
                           +" height = " + height
                           +" #Bands = " + numBands
                           +" #DataElements "+numDataElements
                           +" xOff = "+sampleModelTranslateX
                           +" yOff = "+sampleModelTranslateY
                           +" dataOffset[0] "+dataOffsets[0]);
    }
}
