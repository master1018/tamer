public class BytePackedRaster extends SunWritableRaster {
    int           dataBitOffset;
    int           scanlineStride;
    int           pixelBitStride;
    int           bitMask;
    byte[]        data;
    int shiftOffset;
    int type;
    private int maxX;
    private int maxY;
    static private native void initIDs();
    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }
    public BytePackedRaster(SampleModel sampleModel,
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
    public BytePackedRaster(SampleModel sampleModel,
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
    public BytePackedRaster(SampleModel sampleModel,
                            DataBuffer dataBuffer,
                            Rectangle aRegion,
                            Point origin,
                            BytePackedRaster parent){
        super(sampleModel,dataBuffer,aRegion,origin, parent);
        this.maxX = minX + width;
        this.maxY = minY + height;
        if (!(dataBuffer instanceof DataBufferByte)) {
           throw new RasterFormatException("BytePackedRasters must have" +
                "byte DataBuffers");
        }
        DataBufferByte dbb = (DataBufferByte)dataBuffer;
        this.data = stealData(dbb, 0);
        if (dbb.getNumBanks() != 1) {
            throw new
                RasterFormatException("DataBuffer for BytePackedRasters"+
                                      " must only have 1 bank.");
        }
        int dbOffset = dbb.getOffset();
        if (sampleModel instanceof MultiPixelPackedSampleModel) {
            MultiPixelPackedSampleModel mppsm =
                (MultiPixelPackedSampleModel)sampleModel;
            this.type = IntegerComponentRaster.TYPE_BYTE_BINARY_SAMPLES;
            pixelBitStride = mppsm.getPixelBitStride();
            if (pixelBitStride != 1 &&
                pixelBitStride != 2 &&
                pixelBitStride != 4) {
                throw new RasterFormatException
                  ("BytePackedRasters must have a bit depth of 1, 2, or 4");
            }
            scanlineStride = mppsm.getScanlineStride();
            dataBitOffset = mppsm.getDataBitOffset() + dbOffset*8;
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            dataBitOffset += xOffset*pixelBitStride + yOffset*scanlineStride*8;
            bitMask = (1 << pixelBitStride) -1;
            shiftOffset = 8 - pixelBitStride;
        } else {
            throw new RasterFormatException("BytePackedRasters must have"+
                "MultiPixelPackedSampleModel");
        }
        verify(false);
    }
    public int getDataBitOffset() {
        return dataBitOffset;
    }
    public int getScanlineStride() {
        return scanlineStride;
    }
    public int getPixelBitStride() {
        return pixelBitStride;
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
        int bitnum = dataBitOffset + (x-minX) * pixelBitStride;
        int element = data[(y-minY) * scanlineStride + (bitnum >> 3)] & 0xff;
        int shift = shiftOffset - (bitnum & 7);
        outData[0] = (byte)((element >> shift) & bitMask);
        return outData;
    }
    public Object getDataElements(int x, int y, int w, int h,
                                  Object outData) {
        return getByteData(x, y, w, h, (byte[])outData);
    }
    public Object getPixelData(int x, int y, int w, int h, Object obj) {
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
        int pixbits = pixelBitStride;
        int scanbit = dataBitOffset + (x-minX) * pixbits;
        int index = (y-minY) * scanlineStride;
        int outindex = 0;
        byte data[] = this.data;
        for (int j = 0; j < h; j++) {
            int bitnum = scanbit;
            for (int i = 0; i < w; i++) {
                int shift = shiftOffset - (bitnum & 7);
                outData[outindex++] =
                    (byte)(bitMask & (data[index + (bitnum >> 3)] >> shift));
                bitnum += pixbits;
            }
            index += scanlineStride;
        }
        return outData;
    }
    public byte[] getByteData(int x, int y, int w, int h,
                              int band, byte[] outData) {
        return getByteData(x, y, w, h, outData);
    }
    public byte[] getByteData(int x, int y, int w, int h, byte[] outData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[w * h];
        }
        int pixbits = pixelBitStride;
        int scanbit = dataBitOffset + (x-minX) * pixbits;
        int index = (y-minY) * scanlineStride;
        int outindex = 0;
        byte data[] = this.data;
        for (int j = 0; j < h; j++) {
            int bitnum = scanbit;
            int element;
            int i = 0;
            while ((i < w) && ((bitnum & 7) != 0)) {
                int shift = shiftOffset - (bitnum & 7);
                outData[outindex++] =
                    (byte)(bitMask & (data[index + (bitnum >> 3)] >> shift));
                bitnum += pixbits;
                i++;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
            case 1:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 7) & 1);
                    outData[outindex++] = (byte)((element >> 6) & 1);
                    outData[outindex++] = (byte)((element >> 5) & 1);
                    outData[outindex++] = (byte)((element >> 4) & 1);
                    outData[outindex++] = (byte)((element >> 3) & 1);
                    outData[outindex++] = (byte)((element >> 2) & 1);
                    outData[outindex++] = (byte)((element >> 1) & 1);
                    outData[outindex++] = (byte)(element & 1);
                    bitnum += 8;
                }
                break;
            case 2:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 6) & 3);
                    outData[outindex++] = (byte)((element >> 4) & 3);
                    outData[outindex++] = (byte)((element >> 2) & 3);
                    outData[outindex++] = (byte)(element & 3);
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 6) & 3);
                    outData[outindex++] = (byte)((element >> 4) & 3);
                    outData[outindex++] = (byte)((element >> 2) & 3);
                    outData[outindex++] = (byte)(element & 3);
                    bitnum += 16;
                }
                break;
            case 4:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 4) & 0xf);
                    outData[outindex++] = (byte)(element & 0xf);
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 4) & 0xf);
                    outData[outindex++] = (byte)(element & 0xf);
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 4) & 0xf);
                    outData[outindex++] = (byte)(element & 0xf);
                    element = data[inIndex++];
                    outData[outindex++] = (byte)((element >> 4) & 0xf);
                    outData[outindex++] = (byte)(element & 0xf);
                    bitnum += 32;
                }
                break;
            }
            for (; i < w; i++) {
                int shift = shiftOffset - (bitnum & 7);
                outData[outindex++] =
                    (byte) (bitMask & (data[index + (bitnum >> 3)] >> shift));
                bitnum += pixbits;
            }
            index += scanlineStride;
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
        int bitnum = dataBitOffset + (x-minX) * pixelBitStride;
        int index = (y-minY) * scanlineStride + (bitnum >> 3);
        int shift = shiftOffset - (bitnum & 7);
        byte element = data[index];
        element &= ~(bitMask << shift);
        element |= (inData[0] & bitMask) << shift;
        data[index] = element;
        markDirty();
    }
    public void setDataElements(int x, int y, Raster inRaster) {
        if (!(inRaster instanceof BytePackedRaster) ||
            ((BytePackedRaster)inRaster).pixelBitStride != pixelBitStride) {
            super.setDataElements(x, y, inRaster);
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int dstOffX = srcOffX + x;
        int dstOffY = srcOffY + y;
        int width = inRaster.getWidth();
        int height = inRaster.getHeight();
        if ((dstOffX < this.minX) || (dstOffY < this.minY) ||
            (dstOffX + width > this.maxX) || (dstOffY + height > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        setDataElements(dstOffX, dstOffY,
                        srcOffX, srcOffY,
                        width, height,
                        (BytePackedRaster)inRaster);
    }
    private void setDataElements(int dstX, int dstY,
                                 int srcX, int srcY,
                                 int width, int height,
                                 BytePackedRaster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        byte[] inData = inRaster.data;
        byte[] outData = this.data;
        int inscan = inRaster.scanlineStride;
        int outscan = this.scanlineStride;
        int inbit = inRaster.dataBitOffset +
                      8 * (srcY - inRaster.minY) * inscan +
                      (srcX - inRaster.minX) * inRaster.pixelBitStride;
        int outbit = (this.dataBitOffset +
                      8 * (dstY - minY) * outscan +
                      (dstX - minX) * this.pixelBitStride);
        int copybits = width * pixelBitStride;
        if ((inbit & 7) == (outbit & 7)) {
            int bitpos = outbit & 7;
            if (bitpos != 0) {
                int bits = 8 - bitpos;
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int mask = 0xff >> bitpos;
                if (copybits < bits) {
                    mask &= 0xff << (bits - copybits);
                    bits = copybits;
                }
                for (int j = 0; j < height; j++) {
                    int element = outData[outbyte];
                    element &= ~mask;
                    element |= (inData[inbyte] & mask);
                    outData[outbyte] = (byte) element;
                    inbyte += inscan;
                    outbyte += outscan;
                }
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits >= 8) {
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int copybytes = copybits >> 3;
                if (copybytes == inscan && inscan == outscan) {
                    System.arraycopy(inData, inbyte,
                                     outData, outbyte,
                                     inscan * height);
                } else {
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(inData, inbyte,
                                         outData, outbyte,
                                         copybytes);
                        inbyte += inscan;
                        outbyte += outscan;
                    }
                }
                int bits = copybytes*8;
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits > 0) {
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int mask = (0xff00 >> copybits) & 0xff;
                for (int j = 0; j < height; j++) {
                    int element = outData[outbyte];
                    element &= ~mask;
                    element |= (inData[inbyte] & mask);
                    outData[outbyte] = (byte) element;
                    inbyte += inscan;
                    outbyte += outscan;
                }
            }
        } else {
            int bitpos = outbit & 7;
            if (bitpos != 0 || copybits < 8) {
                int bits = 8 - bitpos;
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int lshift = inbit & 7;
                int rshift = 8 - lshift;
                int mask = 0xff >> bitpos;
                if (copybits < bits) {
                    mask &= 0xff << (bits - copybits);
                    bits = copybits;
                }
                int lastByte = inData.length - 1;
                for (int j = 0; j < height; j++) {
                    byte inData0 = inData[inbyte];
                    byte inData1 = (byte)0;
                    if (inbyte < lastByte) {
                        inData1 = inData[inbyte + 1];
                    }
                    int element = outData[outbyte];
                    element &= ~mask;
                    element |= (((inData0 << lshift) |
                                 ((inData1 & 0xff) >> rshift))
                                >> bitpos) & mask;
                    outData[outbyte] = (byte)element;
                    inbyte += inscan;
                    outbyte += outscan;
                }
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits >= 8) {
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int copybytes = copybits >> 3;
                int lshift = inbit & 7;
                int rshift = 8 - lshift;
                for (int j = 0; j < height; j++) {
                    int ibyte = inbyte + j*inscan;
                    int obyte = outbyte + j*outscan;
                    int inData0 = inData[ibyte];
                    for (int i = 0; i < copybytes; i++) {
                        int inData1 = inData[ibyte + 1];
                        int val = (inData0 << lshift) |
                            ((inData1 & 0xff) >> rshift);
                        outData[obyte] = (byte)val;
                        inData0 = inData1;
                        ++ibyte;
                        ++obyte;
                    }
                }
                int bits = copybytes*8;
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits > 0) {
                int inbyte = inbit >> 3;
                int outbyte = outbit >> 3;
                int mask = (0xff00 >> copybits) & 0xff;
                int lshift = inbit & 7;
                int rshift = 8 - lshift;
                int lastByte = inData.length - 1;
                for (int j = 0; j < height; j++) {
                    byte inData0 = inData[inbyte];
                    byte inData1 = (byte)0;
                    if (inbyte < lastByte) {
                        inData1 = inData[inbyte + 1];
                    }
                    int element = outData[outbyte];
                    element &= ~mask;
                    element |= ((inData0 << lshift) |
                                ((inData1 & 0xff) >> rshift)) & mask;
                    outData[outbyte] = (byte)element;
                    inbyte += inscan;
                    outbyte += outscan;
                }
            }
        }
        markDirty();
    }
    public void setRect(int dx, int dy, Raster srcRaster) {
        if (!(srcRaster instanceof BytePackedRaster) ||
            ((BytePackedRaster)srcRaster).pixelBitStride != pixelBitStride) {
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
            int skipX = this.minX - dstOffX;
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
                        width, height,
                        (BytePackedRaster)srcRaster);
    }
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        putByteData(x, y, w, h, (byte[])obj);
    }
    public void putByteData(int x, int y, int w, int h,
                            int band, byte[] inData) {
        putByteData(x, y, w, h, inData);
    }
    public void putByteData(int x, int y, int w, int h, byte[] inData) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (w == 0 || h == 0) {
            return;
        }
        int pixbits = pixelBitStride;
        int scanbit = dataBitOffset + (x - minX) * pixbits;
        int index = (y - minY) * scanlineStride;
        int outindex = 0;
        byte data[] = this.data;
        for (int j = 0; j < h; j++) {
            int bitnum = scanbit;
            int element;
            int i = 0;
            while ((i < w) && ((bitnum & 7) != 0)) {
                int shift = shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(bitMask << shift);
                element |= (inData[outindex++] & bitMask) << shift;
                data[index + (bitnum >> 3)] = (byte)element;
                bitnum += pixbits;
                i++;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
            case 1:
                for (; i < w - 7; i += 8) {
                    element = (inData[outindex++] & 1) << 7;
                    element |= (inData[outindex++] & 1) << 6;
                    element |= (inData[outindex++] & 1) << 5;
                    element |= (inData[outindex++] & 1) << 4;
                    element |= (inData[outindex++] & 1) << 3;
                    element |= (inData[outindex++] & 1) << 2;
                    element |= (inData[outindex++] & 1) << 1;
                    element |= (inData[outindex++] & 1);
                    data[inIndex++] = (byte)element;
                    bitnum += 8;
                }
                break;
            case 2:
                for (; i < w - 7; i += 8) {
                    element = (inData[outindex++] & 3) << 6;
                    element |= (inData[outindex++] & 3) << 4;
                    element |= (inData[outindex++] & 3) << 2;
                    element |= (inData[outindex++] & 3);
                    data[inIndex++] = (byte)element;
                    element = (inData[outindex++] & 3) << 6;
                    element |= (inData[outindex++] & 3) << 4;
                    element |= (inData[outindex++] & 3) << 2;
                    element |= (inData[outindex++] & 3);
                    data[inIndex++] = (byte)element;
                    bitnum += 16;
                }
                break;
            case 4:
                for (; i < w - 7; i += 8) {
                    element = (inData[outindex++] & 0xf) << 4;
                    element |= (inData[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (inData[outindex++] & 0xf) << 4;
                    element |= (inData[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (inData[outindex++] & 0xf) << 4;
                    element |= (inData[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (inData[outindex++] & 0xf) << 4;
                    element |= (inData[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    bitnum += 32;
                }
                break;
            }
            for (; i < w; i++) {
                int shift = shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(bitMask << shift);
                element |= (inData[outindex++] & bitMask) << shift;
                data[index + (bitnum >> 3)] = (byte)element;
                bitnum += pixbits;
            }
            index += scanlineStride;
        }
        markDirty();
    }
    public int[] getPixels(int x, int y, int w, int h, int iArray[]) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        if (iArray == null) {
            iArray = new int[w * h];
        }
        int pixbits = pixelBitStride;
        int scanbit = dataBitOffset + (x-minX) * pixbits;
        int index = (y-minY) * scanlineStride;
        int outindex = 0;
        byte data[] = this.data;
        for (int j = 0; j < h; j++) {
            int bitnum = scanbit;
            int element;
            int i = 0;
            while ((i < w) && ((bitnum & 7) != 0)) {
                int shift = shiftOffset - (bitnum & 7);
                iArray[outindex++] =
                    bitMask & (data[index + (bitnum >> 3)] >> shift);
                bitnum += pixbits;
                i++;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
            case 1:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 7) & 1;
                    iArray[outindex++] = (element >> 6) & 1;
                    iArray[outindex++] = (element >> 5) & 1;
                    iArray[outindex++] = (element >> 4) & 1;
                    iArray[outindex++] = (element >> 3) & 1;
                    iArray[outindex++] = (element >> 2) & 1;
                    iArray[outindex++] = (element >> 1) & 1;
                    iArray[outindex++] = element & 1;
                    bitnum += 8;
                }
                break;
            case 2:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 6) & 3;
                    iArray[outindex++] = (element >> 4) & 3;
                    iArray[outindex++] = (element >> 2) & 3;
                    iArray[outindex++] = element & 3;
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 6) & 3;
                    iArray[outindex++] = (element >> 4) & 3;
                    iArray[outindex++] = (element >> 2) & 3;
                    iArray[outindex++] = element & 3;
                    bitnum += 16;
                }
                break;
            case 4:
                for (; i < w - 7; i += 8) {
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 4) & 0xf;
                    iArray[outindex++] = element & 0xf;
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 4) & 0xf;
                    iArray[outindex++] = element & 0xf;
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 4) & 0xf;
                    iArray[outindex++] = element & 0xf;
                    element = data[inIndex++];
                    iArray[outindex++] = (element >> 4) & 0xf;
                    iArray[outindex++] = element & 0xf;
                    bitnum += 32;
                }
                break;
            }
            for (; i < w; i++) {
                int shift = shiftOffset - (bitnum & 7);
                iArray[outindex++] =
                    bitMask & (data[index + (bitnum >> 3)] >> shift);
                bitnum += pixbits;
            }
            index += scanlineStride;
        }
        return iArray;
    }
    public void setPixels(int x, int y, int w, int h, int iArray[]) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixbits = pixelBitStride;
        int scanbit = dataBitOffset + (x - minX) * pixbits;
        int index = (y - minY) * scanlineStride;
        int outindex = 0;
        byte data[] = this.data;
        for (int j = 0; j < h; j++) {
            int bitnum = scanbit;
            int element;
            int i = 0;
            while ((i < w) && ((bitnum & 7) != 0)) {
                int shift = shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(bitMask << shift);
                element |= (iArray[outindex++] & bitMask) << shift;
                data[index + (bitnum >> 3)] = (byte)element;
                bitnum += pixbits;
                i++;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
            case 1:
                for (; i < w - 7; i += 8) {
                    element = (iArray[outindex++] & 1) << 7;
                    element |= (iArray[outindex++] & 1) << 6;
                    element |= (iArray[outindex++] & 1) << 5;
                    element |= (iArray[outindex++] & 1) << 4;
                    element |= (iArray[outindex++] & 1) << 3;
                    element |= (iArray[outindex++] & 1) << 2;
                    element |= (iArray[outindex++] & 1) << 1;
                    element |= (iArray[outindex++] & 1);
                    data[inIndex++] = (byte)element;
                    bitnum += 8;
                }
                break;
            case 2:
                for (; i < w - 7; i += 8) {
                    element = (iArray[outindex++] & 3) << 6;
                    element |= (iArray[outindex++] & 3) << 4;
                    element |= (iArray[outindex++] & 3) << 2;
                    element |= (iArray[outindex++] & 3);
                    data[inIndex++] = (byte)element;
                    element = (iArray[outindex++] & 3) << 6;
                    element |= (iArray[outindex++] & 3) << 4;
                    element |= (iArray[outindex++] & 3) << 2;
                    element |= (iArray[outindex++] & 3);
                    data[inIndex++] = (byte)element;
                    bitnum += 16;
                }
                break;
            case 4:
                for (; i < w - 7; i += 8) {
                    element = (iArray[outindex++] & 0xf) << 4;
                    element |= (iArray[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (iArray[outindex++] & 0xf) << 4;
                    element |= (iArray[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (iArray[outindex++] & 0xf) << 4;
                    element |= (iArray[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    element = (iArray[outindex++] & 0xf) << 4;
                    element |= (iArray[outindex++] & 0xf);
                    data[inIndex++] = (byte)element;
                    bitnum += 32;
                }
                break;
            }
            for (; i < w; i++) {
                int shift = shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(bitMask << shift);
                element |= (iArray[outindex++] & bitMask) << shift;
                data[index + (bitnum >> 3)] = (byte)element;
                bitnum += pixbits;
            }
            index += scanlineStride;
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
        if (bandList != null) {
            sm = sampleModel.createSubsetSampleModel(bandList);
        }
        else {
            sm = sampleModel;
        }
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new BytePackedRaster(sm,
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
        SampleModel sm = sampleModel.createCompatibleSampleModel(w,h);
        return new BytePackedRaster(sm, new Point(0,0));
    }
    public WritableRaster createCompatibleWritableRaster () {
        return createCompatibleWritableRaster(width,height);
    }
    private void verify (boolean strictCheck) {
        if (dataBitOffset < 0) {
            throw new RasterFormatException("Data offsets must be >= 0");
        }
        int lastbit = (dataBitOffset
                       + (height-1) * scanlineStride * 8
                       + (width-1) * pixelBitStride
                       + pixelBitStride - 1);
        if (lastbit / 8 >= data.length) {
            throw new RasterFormatException("raster dimensions overflow " +
                                            "array bounds");
        }
        if (strictCheck) {
            if (height > 1) {
                lastbit = width * pixelBitStride - 1;
                if (lastbit / 8 >= scanlineStride) {
                    throw new RasterFormatException("data for adjacent" +
                                                    " scanlines overlaps");
                }
            }
        }
    }
    public String toString() {
        return new String ("BytePackedRaster: width = "+width+" height = "+height
                           +" #channels "+numBands
                           +" xOff = "+sampleModelTranslateX
                           +" yOff = "+sampleModelTranslateY);
    }
}
