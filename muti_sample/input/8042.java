public class ByteInterleavedRaster extends ByteComponentRaster {
    boolean inOrder;
    int dbOffset;
    int dbOffsetPacked;
    boolean packed = false;
    int[] bitMasks;
    int[] bitOffsets;
    private int maxX;
    private int maxY;
    public ByteInterleavedRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }
    public ByteInterleavedRaster(SampleModel sampleModel,
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
    private boolean isInterleaved(ComponentSampleModel sm) {
        int numBands = sampleModel.getNumBands();
        if (numBands == 1) {
            return true;
        }
        int[] bankIndices = sm.getBankIndices();
        for (int i = 0; i < numBands; i++) {
            if (bankIndices[i] != 0) {
                return false;
            }
        }
        int[] bandOffsets = sm.getBandOffsets();
        int minOffset = bandOffsets[0];
        int maxOffset = minOffset;
        for (int i = 1; i < numBands; i++) {
            int offset = bandOffsets[i];
            if (offset < minOffset) {
                minOffset = offset;
            }
            if (offset > maxOffset) {
                maxOffset = offset;
            }
        }
        if (maxOffset - minOffset >= sm.getPixelStride()) {
            return false;
        }
        return true;
    }
    public ByteInterleavedRaster(SampleModel sampleModel,
                                  DataBuffer dataBuffer,
                                  Rectangle aRegion,
                                  Point origin,
                                  ByteInterleavedRaster parent) {
        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferByte)) {
            throw new RasterFormatException("ByteInterleavedRasters must have " +
                                            "byte DataBuffers");
        }
        DataBufferByte dbb = (DataBufferByte)dataBuffer;
        this.data = stealData(dbb, 0);
        int xOffset = aRegion.x - origin.x;
        int yOffset = aRegion.y - origin.y;
        if (sampleModel instanceof PixelInterleavedSampleModel ||
            (sampleModel instanceof ComponentSampleModel &&
             isInterleaved((ComponentSampleModel)sampleModel))) {
            ComponentSampleModel csm = (ComponentSampleModel)sampleModel;
            this.scanlineStride = csm.getScanlineStride();
            this.pixelStride = csm.getPixelStride();
            this.dataOffsets = csm.getBandOffsets();
            for (int i = 0; i < getNumDataElements(); i++) {
                dataOffsets[i] += xOffset*pixelStride+yOffset*scanlineStride;
            }
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm =
                    (SinglePixelPackedSampleModel)sampleModel;
            this.packed = true;
            this.bitMasks = sppsm.getBitMasks();
            this.bitOffsets = sppsm.getBitOffsets();
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dbb.getOffset();
            dataOffsets[0] += xOffset*pixelStride+yOffset*scanlineStride;
        } else {
            throw new RasterFormatException("ByteInterleavedRasters must " +
              "have PixelInterleavedSampleModel, SinglePixelPackedSampleModel"+
              " or interleaved ComponentSampleModel.  Sample model is " +
              sampleModel);
        }
        this.bandOffset = this.dataOffsets[0];
        this.dbOffsetPacked = dataBuffer.getOffset() -
            sampleModelTranslateY*scanlineStride -
            sampleModelTranslateX*pixelStride;
        this.dbOffset = dbOffsetPacked -
            (xOffset*pixelStride+yOffset*scanlineStride);
        this.inOrder = false;
        if (numDataElements == pixelStride) {
            inOrder = true;
            for (int i = 1; i < numDataElements; i++) {
                if (dataOffsets[i] - dataOffsets[0] != i) {
                    inOrder = false;
                    break;
                }
            }
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
        return getByteData(x, y, w, h, (byte[])obj);
    }
    public byte[] getByteData(int x, int y, int w, int h,
                              int band, byte[] outData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[w*h];
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
            } else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    System.arraycopy(data, yoff, outData, off, w);
                    off += w;
                }
            }
        } else {
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
            outData = new byte[numDataElements*w*h];
        }
        int yoff = (y-minY)*scanlineStride +
                   (x-minX)*pixelStride;
        int xoff;
        int off = 0;
        int xstart;
        int ystart;
        if (inOrder) {
            yoff += dataOffsets[0];
            int rowBytes = w*pixelStride;
            if (scanlineStride == rowBytes) {
                System.arraycopy(data, yoff, outData, off, rowBytes*h);
            } else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    System.arraycopy(data, yoff, outData, off, rowBytes);
                    off += rowBytes;
                }
            }
        } else if (numDataElements == 1) {
            yoff += dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    outData[off++] = data[xoff];
                }
            }
        } else if (numDataElements == 2) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    outData[off++] = data[xoff];
                    outData[off++] = data[xoff + d1];
                }
            }
        } else if (numDataElements == 3) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            int d2 = dataOffsets[2] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    outData[off++] = data[xoff];
                    outData[off++] = data[xoff + d1];
                    outData[off++] = data[xoff + d2];
                }
            }
        } else if (numDataElements == 4) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            int d2 = dataOffsets[2] - dataOffsets[0];
            int d3 = dataOffsets[3] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    outData[off++] = data[xoff];
                    outData[off++] = data[xoff + d1];
                    outData[off++] = data[xoff + d2];
                    outData[off++] = data[xoff + d3];
                }
            }
        } else {
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    for (int c = 0; c < numDataElements; c++) {
                        outData[off++] = data[dataOffsets[c] + xoff];
                    }
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
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int dstOffX = x + srcOffX;
        int dstOffY = y + srcOffY;
        int width  = inRaster.getWidth();
        int height = inRaster.getHeight();
        if ((dstOffX < this.minX) || (dstOffY < this.minY) ||
            (dstOffX + width > this.maxX) || (dstOffY + height > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        setDataElements(dstOffX, dstOffY, srcOffX, srcOffY,
                        width, height, inRaster);
    }
    private void setDataElements(int dstX, int dstY,
                                 int srcX, int srcY,
                                 int width, int height,
                                 Raster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        Object tdata = null;
        if (inRaster instanceof ByteInterleavedRaster) {
            ByteInterleavedRaster bct = (ByteInterleavedRaster) inRaster;
            byte[] bdata = bct.getDataStorage();
            if (inOrder && bct.inOrder && pixelStride == bct.pixelStride) {
                int toff = bct.getDataOffset(0);
                int tss  = bct.getScanlineStride();
                int tps  = bct.getPixelStride();
                int srcOffset = toff +
                    (srcY - srcOffY) * tss +
                    (srcX - srcOffX) * tps;
                int dstOffset = dataOffsets[0] +
                    (dstY - minY) * scanlineStride +
                    (dstX - minX) * pixelStride;
                int nbytes = width*pixelStride;
                for (int tmpY=0; tmpY < height; tmpY++) {
                    System.arraycopy(bdata, srcOffset,
                                     data, dstOffset, nbytes);
                    srcOffset += tss;
                    dstOffset += scanlineStride;
                }
                markDirty();
                return;
            }
        }
        for (int startY=0; startY < height; startY++) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, tdata);
            setDataElements(dstX, dstY + startY, width, 1, tdata);
        }
    }
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        putByteData(x, y, w, h, (byte[])obj);
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
        if (inOrder) {
            yoff += dataOffsets[0];
            int rowBytes = w*pixelStride;
            if (rowBytes == scanlineStride) {
                System.arraycopy(inData, 0, data, yoff, rowBytes*h);
            } else {
                for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                    System.arraycopy(inData, off, data, yoff, rowBytes);
                    off += rowBytes;
                }
            }
        } else if (numDataElements == 1) {
            yoff += dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    data[xoff] = inData[off++];
                }
            }
        } else if (numDataElements == 2) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    data[xoff] = inData[off++];
                    data[xoff + d1] = inData[off++];
                }
            }
        } else if (numDataElements == 3) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            int d2 = dataOffsets[2] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    data[xoff] = inData[off++];
                    data[xoff + d1] = inData[off++];
                    data[xoff + d2] = inData[off++];
                }
            }
        } else if (numDataElements == 4) {
            yoff += dataOffsets[0];
            int d1 = dataOffsets[1] - dataOffsets[0];
            int d2 = dataOffsets[2] - dataOffsets[0];
            int d3 = dataOffsets[3] - dataOffsets[0];
            for (ystart=0; ystart < h; ystart++, yoff += scanlineStride) {
                xoff = yoff;
                for (xstart=0; xstart < w; xstart++, xoff += pixelStride) {
                    data[xoff] = inData[off++];
                    data[xoff + d1] = inData[off++];
                    data[xoff + d2] = inData[off++];
                    data[xoff + d3] = inData[off++];
                }
            }
        } else {
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
    public int getSample(int x, int y, int b) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (packed) {
            int offset = y*scanlineStride + x + dbOffsetPacked;
            byte sample = data[offset];
            return (sample & bitMasks[b]) >>> bitOffsets[b];
        } else {
            int offset = y*scanlineStride + x*pixelStride + dbOffset;
            return data[offset + dataOffsets[b]] & 0xff;
        }
    }
    public void setSample(int x, int y, int b, int s) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (packed) {
            int offset = y*scanlineStride + x + dbOffsetPacked;
            int bitMask = bitMasks[b];
            byte value = data[offset];
            value &= ~bitMask;
            value |= (s << bitOffsets[b]) & bitMask;
            data[offset] = value;
        } else {
            int offset = y*scanlineStride + x*pixelStride + dbOffset;
            data[offset + dataOffsets[b]] = (byte)s;
        }
        markDirty();
    }
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int[] iArray) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int samples[];
        if (iArray != null) {
            samples = iArray;
        } else {
            samples = new int [w*h];
        }
        int lineOffset = y*scanlineStride + x*pixelStride;
        int dstOffset = 0;
        if (packed) {
            lineOffset += dbOffsetPacked;
            int bitMask = bitMasks[b];
            int bitOffset = bitOffsets[b];
            for (int j = 0; j < h; j++) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; i++) {
                    int value = data[sampleOffset++];
                    samples[dstOffset++] = ((value & bitMask) >>> bitOffset);
                }
                lineOffset += scanlineStride;
            }
        } else {
            lineOffset += dbOffset + dataOffsets[b];
            for (int j = 0; j < h; j++) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; i++) {
                    samples[dstOffset++] = data[sampleOffset] & 0xff;
                    sampleOffset += pixelStride;
                }
                lineOffset += scanlineStride;
            }
        }
        return samples;
    }
    public void setSamples(int x, int y, int w, int h, int b, int iArray[]) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int lineOffset = y*scanlineStride + x*pixelStride;
        int srcOffset = 0;
        if (packed) {
            lineOffset += dbOffsetPacked;
            int bitMask = bitMasks[b];
            for (int j = 0; j < h; j++) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; i++) {
                    byte value = data[sampleOffset];
                    value &= ~bitMask;
                    int sample = iArray[srcOffset++];
                    value |= (sample << bitOffsets[b]) & bitMask;
                    data[sampleOffset++] = value;
                }
                lineOffset += scanlineStride;
            }
        } else {
            lineOffset += dbOffset + dataOffsets[b];
            for (int i = 0; i < h; i++) {
                int sampleOffset = lineOffset;
                for (int j = 0; j < w; j++) {
                    data[sampleOffset] = (byte)iArray[srcOffset++];
                    sampleOffset += pixelStride;
                }
                lineOffset += scanlineStride;
            }
        }
        markDirty();
    }
    public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray != null) {
            pixels = iArray;
        } else {
            pixels = new int[w*h*numBands];
        }
        int lineOffset = y*scanlineStride + x*pixelStride;
        int dstOffset = 0;
        if (packed) {
            lineOffset += dbOffsetPacked;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int value = data[lineOffset + i];
                    for (int k = 0; k < numBands; k++) {
                        pixels[dstOffset++] =
                            (value & bitMasks[k]) >>> bitOffsets[k];
                    }
                }
                lineOffset += scanlineStride;
            }
        } else {
            lineOffset += dbOffset;
            int d0 = dataOffsets[0];
            if (numBands == 1) {
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        pixels[dstOffset++] = data[pixelOffset] & 0xff;
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 2) {
                int d1 = dataOffsets[1] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        pixels[dstOffset++] = data[pixelOffset] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d1] & 0xff;
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 3) {
                int d1 = dataOffsets[1] - d0;
                int d2 = dataOffsets[2] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        pixels[dstOffset++] = data[pixelOffset] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d1] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d2] & 0xff;
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 4) {
                int d1 = dataOffsets[1] - d0;
                int d2 = dataOffsets[2] - d0;
                int d3 = dataOffsets[3] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        pixels[dstOffset++] = data[pixelOffset] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d1] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d2] & 0xff;
                        pixels[dstOffset++] = data[pixelOffset + d3] & 0xff;
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else {
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset;
                    for (int i = 0; i < w; i++) {
                        for (int k = 0; k < numBands; k++) {
                            pixels[dstOffset++] =
                                data[pixelOffset + dataOffsets[k]] & 0xff;
                        }
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            }
        }
        return pixels;
    }
    public void setPixels(int x, int y, int w, int h, int[] iArray) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int lineOffset = y*scanlineStride + x*pixelStride;
        int srcOffset = 0;
        if (packed) {
            lineOffset += dbOffsetPacked;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int value = 0;
                    for (int k = 0; k < numBands; k++) {
                        int srcValue = iArray[srcOffset++];
                        value |= ((srcValue << bitOffsets[k])
                                  & bitMasks[k]);
                    }
                    data[lineOffset + i] = (byte)value;
                }
                lineOffset += scanlineStride;
            }
        } else {
            lineOffset += dbOffset;
            int d0 = dataOffsets[0];
            if (numBands == 1) {
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        data[pixelOffset] = (byte)iArray[srcOffset++];
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 2) {
                int d1 = dataOffsets[1] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        data[pixelOffset] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 3) {
                int d1 = dataOffsets[1] - d0;
                int d2 = dataOffsets[2] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        data[pixelOffset] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d2] = (byte)iArray[srcOffset++];
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else if (numBands == 4) {
                int d1 = dataOffsets[1] - d0;
                int d2 = dataOffsets[2] - d0;
                int d3 = dataOffsets[3] - d0;
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; i++) {
                        data[pixelOffset] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d2] = (byte)iArray[srcOffset++];
                        data[pixelOffset + d3] = (byte)iArray[srcOffset++];
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            } else {
                for (int j = 0; j < h; j++) {
                    int pixelOffset = lineOffset;
                    for (int i = 0; i < w; i++) {
                        for (int k = 0; k < numBands; k++) {
                            data[pixelOffset + dataOffsets[k]] =
                                (byte)iArray[srcOffset++];
                        }
                        pixelOffset += pixelStride;
                    }
                    lineOffset += scanlineStride;
                }
            }
        }
        markDirty();
    }
    public void setRect(int dx, int dy, Raster srcRaster) {
        if (!(srcRaster instanceof ByteInterleavedRaster)) {
            super.setRect(dx, dy, srcRaster);
            return;
        }
        int width  = srcRaster.getWidth();
        int height = srcRaster.getHeight();
        int srcOffX = srcRaster.getMinX();
        int srcOffY = srcRaster.getMinY();
        int dstOffX = dx+srcOffX;
        int dstOffY = dy+srcOffY;
        if (dstOffX < this.minX) {
            int skipX = minX - dstOffX;
            width -= skipX;
            srcOffX += skipX;
            dstOffX = this.minX;
        }
        if (dstOffY < this.minY) {
            int skipY = this.minY - dstOffY;
            height -= skipY;
            srcOffY += skipY;
            dstOffY = this.minY;
        }
        if (dstOffX+width > this.maxX) {
            width = this.maxX - dstOffX;
        }
        if (dstOffY+height > this.maxY) {
            height = this.maxY - dstOffY;
        }
        setDataElements(dstOffX, dstOffY,
                        srcOffX, srcOffY,
                        width, height, srcRaster);
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
        return new ByteInterleavedRaster(sm,
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
        return new ByteInterleavedRaster(sm, new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width,height);
    }
    private void verify (boolean strictCheck) {
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
        return new String ("ByteInterleavedRaster: width = "+width+" height = "
                           + height
                           +" #numDataElements "+numDataElements
                           +" dataOff[0] = "+dataOffsets[0]);
    }
}
