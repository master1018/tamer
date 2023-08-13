public class ByteComponentRaster extends SunWritableRaster {
    protected int bandOffset;
    protected int[]         dataOffsets;
    protected int           scanlineStride;
    protected int           pixelStride;
    protected byte[]        data;
    int type;
    private int maxX;
    private int maxY;
    static private native void initIDs();
    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }
    public ByteComponentRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }
    public ByteComponentRaster(SampleModel sampleModel,
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
    public ByteComponentRaster(SampleModel sampleModel,
                                  DataBuffer dataBuffer,
                                  Rectangle aRegion,
                                  Point origin,
                                  ByteComponentRaster parent) {
        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferByte)) {
            throw new RasterFormatException("ByteComponentRasters must have " +
                                            "byte DataBuffers");
        }
        DataBufferByte dbb = (DataBufferByte)dataBuffer;
        this.data = stealData(dbb, 0);
        if (dbb.getNumBanks() != 1) {
            throw new
                RasterFormatException("DataBuffer for ByteComponentRasters"+
                                      " must only have 1 bank.");
        }
        int dbOffset = dbb.getOffset();
        if (sampleModel instanceof ComponentSampleModel) {
            ComponentSampleModel ism = (ComponentSampleModel)sampleModel;
            this.type = IntegerComponentRaster.TYPE_BYTE_SAMPLES;
            this.scanlineStride = ism.getScanlineStride();
            this.pixelStride = ism.getPixelStride();
            this.dataOffsets = ism.getBandOffsets();
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            for (int i = 0; i < getNumDataElements(); i++) {
                dataOffsets[i] += dbOffset +
                    xOffset*pixelStride+yOffset*scanlineStride;
            }
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm =
                    (SinglePixelPackedSampleModel)sampleModel;
            this.type = IntegerComponentRaster.TYPE_BYTE_PACKED_SAMPLES;
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride    = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dbOffset;
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            dataOffsets[0] += xOffset*pixelStride+yOffset*scanlineStride;
        } else {
            throw new RasterFormatException("IntegerComponentRasters must " +
                "have ComponentSampleModel or SinglePixelPackedSampleModel");
        }
        this.bandOffset = this.dataOffsets[0];
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
    public byte[] getDataStorage() {
        return data;
    }
    public Object getDataElements(int x, int y, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        byte outData[];
        if (obj == null) {
            outData = new byte[numDataElements];
        } else {
            outData = (byte[])obj;
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
        byte outData[];
        if (obj == null) {
            outData = new byte[w*h*numDataElements];
        } else {
            outData = (byte[])obj;
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
    public byte[] getByteData(int x, int y, int w, int h,
                              int band, byte[] outData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[scanlineStride*h];
        }
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride + dataOffsets[band];
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (pixelStride == 1) {
            if (scanlineStride == w) {
                System.arraycopy(data, yoff, outData, 0, w*h);
            }
            else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    System.arraycopy(data, yoff, outData, off, w);
                    off += w;
                }
            }
        }
        else {
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    outData[off++] = data[xoff];
                }
            }
        }
        return outData;
    }
    public byte[] getByteData(int x, int y, int w, int h, byte[] outData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[numDataElements*scanlineStride*h];
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
        byte inData[] = (byte[])obj;
        int off = (y-minY)*scanlineStride +
                  (x-minX)*pixelStride;
        for (int i = 0; i < numDataElements; i++) {
            data[dataOffsets[i] + off] = inData[i];
        }
        markDirty();
    }
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = inRaster.getMinX() + x;
        int dstOffY = inRaster.getMinY() + y;
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
        Object tdata = null;
        if (inRaster instanceof ByteComponentRaster) {
            ByteComponentRaster bct = (ByteComponentRaster) inRaster;
            byte[] bdata = bct.getDataStorage();
            if (numDataElements == 1) {
                int toff = bct.getDataOffset(0);
                int tss  = bct.getScanlineStride();
                int srcOffset = toff;
                int dstOffset = dataOffsets[0]+(dstY-minY)*scanlineStride+
                                               (dstX-minX);
                if (pixelStride == bct.getPixelStride()) {
                    width *= pixelStride;
                    for (int tmpY=0; tmpY < height; tmpY++) {
                        System.arraycopy(bdata, srcOffset,
                                         data, dstOffset, width);
                        srcOffset += tss;
                        dstOffset += scanlineStride;
                    }
                    markDirty();
                    return;
                }
            }
        }
        for (int startY=0; startY < height; startY++) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, tdata);
            setDataElements(dstX, dstY+startY, width, 1, tdata);
        }
    }
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        byte inData[] = (byte[])obj;
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride;
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (numDataElements == 1) {
            int srcOffset = 0;
            int dstOffset = yoff + dataOffsets[0];
            for (ystart=0; ystart < h; ystart++) {
                xoff = yoff;
                System.arraycopy(inData, srcOffset,
                                 data, dstOffset, w);
                srcOffset += w;
                dstOffset += scanlineStride;
            }
            markDirty();
            return;
        }
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
    public void putByteData(int x, int y, int w, int h,
                            int band, byte[] inData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride + dataOffsets[band];
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (pixelStride == 1) {
            if (scanlineStride == w) {
                System.arraycopy(inData, 0, data, yoff, w*h);
            }
            else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    System.arraycopy(inData, off, data, yoff, w);
                    off += w;
                }
            }
        }
        else {
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    data[xoff] = inData[off++];
                }
            }
        }
        markDirty();
    }
    public void putByteData(int x, int y, int w, int h, byte[] inData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride;
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (numDataElements == 1) {
            yoff += dataOffsets[0];
            if (pixelStride == 1) {
                if (scanlineStride == w) {
                    System.arraycopy(inData, 0, data, yoff, w*h);
                }
                else {
                    for (ystart=0; ystart < h; ystart++) {
                        System.arraycopy(inData, off, data, yoff, w);
                        off += w;
                        yoff += scanlineStride;
                    }
                }
            }
            else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    xoff = yoff;
                    for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                        data[xoff] = inData[off++];
                    }
                }
            }
        }
        else {
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    for (int c = 0; c < numDataElements; c++) {
                        data[dataOffsets[c] + xoff] = inData[off++];
                    }
                }
            }
        }
        markDirty();
    }
    public Raster createChild(int x, int y,
                              int width, int height,
                              int x0, int y0, int[] bandList) {
        WritableRaster newRaster = createWritableChild(x, y,
                                                       width, height,
                                                       x0, y0,
                                                       bandList);
        return (Raster) newRaster;
    }
    public WritableRaster createWritableChild(int x, int y,
                                              int width, int height,
                                              int x0, int y0,
                                              int[] bandList) {
        if (x < this.minX) {
            throw new RasterFormatException("x lies outside the raster");
        }
        if (y < this.minY) {
            throw new RasterFormatException("y lies outside the raster");
        }
        if ((x+width < x) || (x+width > this.minX + this.width)) {
            throw new RasterFormatException("(x + width) is outside of Raster");
        }
        if ((y+height < y) || (y+height > this.minY + this.height)) {
            throw new RasterFormatException("(y + height) is outside of Raster");
        }
        SampleModel sm;
        if (bandList != null)
            sm = sampleModel.createSubsetSampleModel(bandList);
        else
            sm = sampleModel;
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new ByteComponentRaster(sm,
                                       dataBuffer,
                                       new Rectangle(x0, y0, width, height),
                                       new Point(sampleModelTranslateX+deltaX,
                                                 sampleModelTranslateY+deltaY),
                                       this);
    }
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <=0) {
            throw new RasterFormatException("negative "+
                                          ((w <= 0) ? "width" : "height"));
        }
        SampleModel sm = sampleModel.createCompatibleSampleModel(w, h);
        return new ByteComponentRaster(sm , new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width,height);
    }
    private void verify (boolean strictCheck) {
        for (int i=0; i < dataOffsets.length; i++) {
            if (dataOffsets[i] < 0) {
                throw new RasterFormatException("Data offsets for band "+i+
                                                "("+dataOffsets[i]+
                                                ") must be >= 0");
            }
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
                                          maxSize+" )");
        }
    }
    public String toString() {
        return new String ("ByteComponentRaster: width = "+width+" height = "
                           + height
                           +" #numDataElements "+numDataElements
                           +" dataOff[0] = "+dataOffsets[0]);
    }
}
