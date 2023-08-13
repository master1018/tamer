public class IntegerInterleavedRaster extends IntegerComponentRaster {
    private int maxX;
    private int maxY;
    public IntegerInterleavedRaster(SampleModel sampleModel,
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
    public IntegerInterleavedRaster(SampleModel sampleModel,
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
    public IntegerInterleavedRaster(SampleModel sampleModel,
                                     DataBuffer dataBuffer,
                                     Rectangle aRegion,
                                     Point origin,
                                     IntegerInterleavedRaster parent){
        super(sampleModel,dataBuffer,aRegion,origin,parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferInt)) {
           throw new RasterFormatException("IntegerInterleavedRasters must have" +
                "integer DataBuffers");
        }
        DataBufferInt dbi = (DataBufferInt)dataBuffer;
        this.data = stealData(dbi, 0);
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm =
                    (SinglePixelPackedSampleModel)sampleModel;
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
            throw new RasterFormatException("IntegerInterleavedRasters must have"+
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
            outData = new int[1];
        } else {
            outData = (int[])obj;
        }
        int off = (y-minY)*scanlineStride + (x-minX) + dataOffsets[0];
        outData[0] = data[off];
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
            outData = new int[w*h];
        }
        int yoff = (y-minY)*scanlineStride + (x-minX) + dataOffsets[0];
        int off = 0;
        for (int ystart = 0; ystart < h; ystart++) {
            System.arraycopy(data, yoff, outData, off, w);
            off += w;
            yoff += scanlineStride;
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
        int off = (y-minY)*scanlineStride + (x-minX) + dataOffsets[0];
        data[off] = inData[0];
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
        if (inRaster instanceof IntegerInterleavedRaster) {
            IntegerInterleavedRaster ict = (IntegerInterleavedRaster) inRaster;
            tdata    = ict.getDataStorage();
            int tss  = ict.getScanlineStride();
            int toff = ict.getDataOffset(0);
            int srcOffset = toff;
            int dstOffset = dataOffsets[0]+(dstY-minY)*scanlineStride+
                                           (dstX-minX);
            for (int startY=0; startY < height; startY++) {
                System.arraycopy(tdata, srcOffset, data, dstOffset, width);
                srcOffset += tss;
                dstOffset += scanlineStride;
            }
            markDirty();
            return;
        }
        Object odata = null;
        for (int startY=0; startY < height; startY++) {
            odata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, odata);
            setDataElements(dstX, dstY+startY, width, 1, odata);
        }
    }
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int inData[] = (int[])obj;
        int yoff = (y-minY)*scanlineStride + (x-minX) + dataOffsets[0];
        int off = 0;
        for (int ystart = 0; ystart < h; ystart++) {
            System.arraycopy(inData, off, data, yoff, w);
            off += w;
            yoff += scanlineStride;
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
        return new IntegerInterleavedRaster(sm,
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
        return new IntegerInterleavedRaster(sm, new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width,height);
    }
    private void verify (boolean strictCheck) {
        int maxSize = 0;
        int size;
        size = (height-1)*scanlineStride + (width-1) + dataOffsets[0];
        if (size > maxSize) {
            maxSize = size;
        }
        if (data.length < maxSize) {
            throw new RasterFormatException("Data array too small (should be "+
                                          maxSize
                                          +" but is "+data.length+" )");
        }
    }
    public String toString() {
        return new String ("IntegerInterleavedRaster: width = "+width
                           +" height = " + height
                           +" #Bands = " + numBands
                           +" xOff = "+sampleModelTranslateX
                           +" yOff = "+sampleModelTranslateY
                           +" dataOffset[0] "+dataOffsets[0]);
    }
}
