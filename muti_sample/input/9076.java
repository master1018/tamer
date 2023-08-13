public class ByteBandedRaster extends SunWritableRaster {
    int[]         dataOffsets;
    int           scanlineStride;
    byte[][]      data;
    private int maxX;
    private int maxY;
    public ByteBandedRaster(SampleModel sampleModel,
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
    public ByteBandedRaster(SampleModel sampleModel,
                               DataBuffer dataBuffer,
                               Point origin) {
        this(sampleModel, dataBuffer,
             new Rectangle(origin.x , origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin, null);
    }
    public ByteBandedRaster(SampleModel sampleModel,
                            DataBuffer dataBuffer,
                            Rectangle aRegion,
                            Point origin,
                            ByteBandedRaster parent) {
        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferByte)) {
           throw new RasterFormatException("ByteBandedRaster must have" +
                "byte DataBuffers");
        }
        DataBufferByte dbb = (DataBufferByte)dataBuffer;
        if (sampleModel instanceof BandedSampleModel) {
            BandedSampleModel bsm = (BandedSampleModel)sampleModel;
            this.scanlineStride = bsm.getScanlineStride();
            int bankIndices[] = bsm.getBankIndices();
            int bandOffsets[] = bsm.getBandOffsets();
            int dOffsets[] = dbb.getOffsets();
            dataOffsets = new int[bankIndices.length];
            data = new byte[bankIndices.length][];
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            for (int i = 0; i < bankIndices.length; i++) {
               data[i] = stealData(dbb, bankIndices[i]);
               dataOffsets[i] = dOffsets[bankIndices[i]] +
                   xOffset + yOffset*scanlineStride + bandOffsets[i];
            }
        } else {
            throw new RasterFormatException("ByteBandedRasters must have"+
                "BandedSampleModels");
        }
        verify(false);
    }
    public int[] getDataOffsets() {
        return (int[])dataOffsets.clone();
    }
    public int getDataOffset(int band) {
        return dataOffsets[band];
    }
    public int getScanlineStride() {
        return scanlineStride;
    }
    public int getPixelStride() {
        return 1;
    }
    public byte[][] getDataStorage() {
        return data;
    }
    public byte[] getDataStorage(int band) {
        return data[band];
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
        int off = (y-minY)*scanlineStride + (x-minX);
        for (int band = 0; band < numDataElements; band++) {
            outData[band] = data[band][dataOffsets[band] + off];
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
            outData = new byte[numDataElements*w*h];
        } else {
            outData = (byte[])obj;
        }
        int yoff = (y-minY)*scanlineStride + (x-minX);
        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            byte[] bank = data[c];
            int dataOffset = dataOffsets[c];
            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    outData[off] = bank[xoff++];
                    off += numDataElements;
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
        int yoff = (y-minY)*scanlineStride + (x-minX) + dataOffsets[band];
        if (scanlineStride == w) {
            System.arraycopy(data[band], yoff, outData, 0, w*h);
        } else {
            int off = 0;
            for (int ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                System.arraycopy(data[band], yoff, outData, off, w);
                off += w;
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
        int yoff = (y-minY)*scanlineStride + (x-minX);
        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            byte[] bank = data[c];
            int dataOffset = dataOffsets[c];
            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    outData[off] = bank[xoff++];
                    off += numDataElements;
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
        int off = (y-minY)*scanlineStride + (x-minX);
        for (int i = 0; i < numDataElements; i++) {
            data[i][dataOffsets[i] + off] = inData[i];
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
        int yoff = (y-minY)*scanlineStride + (x-minX);
        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            byte[] bank = data[c];
            int dataOffset = dataOffsets[c];
            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    bank[xoff++] = inData[off];
                    off += numDataElements;
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
        int yoff = (y-minY)*scanlineStride + (x-minX) + dataOffsets[band];
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (scanlineStride == w) {
            System.arraycopy(inData, 0, data[band], yoff, w*h);
        } else {
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                System.arraycopy(inData, off, data[band], yoff, w);
                off += w;
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
        int yoff = (y-minY)*scanlineStride + (x-minX);
        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            byte[] bank = data[c];
            int dataOffset = dataOffsets[c];
            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    bank[xoff++] = inData[off];
                    off += numDataElements;
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
        if ((x+width < x) || (x+width > this.width + this.minX)) {
            throw new RasterFormatException("(x + width) is outside raster") ;
        }
        if ((y+height < y) || (y+height > this.height + this.minY)) {
            throw new RasterFormatException("(y + height) is outside raster");
        }
        SampleModel sm;
        if (bandList != null)
            sm = sampleModel.createSubsetSampleModel(bandList);
        else
            sm = sampleModel;
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new ByteBandedRaster(sm,
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
        return new ByteBandedRaster(sm, new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width, height);
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
            size = (height-1)*scanlineStride + (width-1) + dataOffsets[i];
            if (size > maxSize) {
                maxSize = size;
            }
        }
        if (data.length == 1) {
            if (data[0].length < maxSize*numDataElements) {
                throw new RasterFormatException("Data array too small "+
                                                "(it is "+data[0].length+
                                                " and should be "+
                                                (maxSize*numDataElements)+
                                                " )");
            }
        }
        else {
            for (int i=0; i < numDataElements; i++) {
                if (data[i].length < maxSize) {
                    throw new RasterFormatException("Data array too small "+
                                                    "(it is "+data[i].length+
                                                    " and should be "+
                                                    maxSize+" )");
                }
            }
        }
    }
    public String toString() {
        return new String ("ByteBandedRaster: width = "+width+" height = "
                           + height
                           +" #bands "+numDataElements
                           +" minX = "+minX+" minY = "+minY);
    }
}
