public class WritableRaster extends Raster {
    protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion,
            Point sampleModelTranslate, WritableRaster parent) {
        super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
    }
    protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.width,
                sampleModel.height), origin, null);
    }
    protected WritableRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y,
                sampleModel.width, sampleModel.height), origin, null);
    }
    public void setDataElements(int x, int y, Object inData) {
        sampleModel.setDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY, inData,
                dataBuffer);
    }
    public void setDataElements(int x, int y, int w, int h, Object inData) {
        sampleModel.setDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h,
                inData, dataBuffer);
    }
    public WritableRaster createWritableChild(int parentX, int parentY, int w, int h,
            int childMinX, int childMinY, int bandList[]) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException(Messages.getString("awt.244")); 
        }
        if (parentX < this.minX || parentX + w > this.minX + this.width) {
            throw new RasterFormatException(Messages.getString("awt.245")); 
        }
        if (parentY < this.minY || parentY + h > this.minY + this.height) {
            throw new RasterFormatException(Messages.getString("awt.246")); 
        }
        if ((long)parentX + w > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.247")); 
        }
        if ((long)parentY + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.248")); 
        }
        if ((long)childMinX + w > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.249")); 
        }
        if ((long)childMinY + h > Integer.MAX_VALUE) {
            throw new RasterFormatException(Messages.getString("awt.24A")); 
        }
        SampleModel childModel;
        if (bandList == null) {
            childModel = sampleModel;
        } else {
            childModel = sampleModel.createSubsetSampleModel(bandList);
        }
        int childTranslateX = childMinX - parentX;
        int childTranslateY = childMinY - parentY;
        return new WritableRaster(childModel, dataBuffer,
                new Rectangle(childMinX, childMinY, w, h), new Point(childTranslateX
                        + sampleModelTranslateX, childTranslateY + sampleModelTranslateY), this);
    }
    public WritableRaster createWritableTranslatedChild(int childMinX, int childMinY) {
        return createWritableChild(minX, minY, width, height, childMinX, childMinY, null);
    }
    public WritableRaster getWritableParent() {
        return (WritableRaster)parent;
    }
    public void setRect(Raster srcRaster) {
        setRect(0, 0, srcRaster);
    }
    public void setRect(int dx, int dy, Raster srcRaster) {
        int w = srcRaster.getWidth();
        int h = srcRaster.getHeight();
        int srcX = srcRaster.getMinX();
        int srcY = srcRaster.getMinY();
        int dstX = srcX + dx;
        int dstY = srcY + dy;
        if (dstX < this.minX) {
            int minOffX = this.minX - dstX;
            w -= minOffX;
            dstX = this.minX;
            srcX += minOffX;
        }
        if (dstY < this.minY) {
            int minOffY = this.minY - dstY;
            h -= minOffY;
            dstY = this.minY;
            srcY += minOffY;
        }
        if (dstX + w > this.minX + this.width) {
            int maxOffX = (dstX + w) - (this.minX + this.width);
            w -= maxOffX;
        }
        if (dstY + h > this.minY + this.height) {
            int maxOffY = (dstY + h) - (this.minY + this.height);
            h -= maxOffY;
        }
        if (w <= 0 || h <= 0) {
            return;
        }
        switch (sampleModel.getDataType()) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                int iPixelsLine[] = null;
                for (int i = 0; i < h; i++) {
                    iPixelsLine = srcRaster.getPixels(srcX, srcY + i, w, 1, iPixelsLine);
                    setPixels(dstX, dstY + i, w, 1, iPixelsLine);
                }
                break;
            case DataBuffer.TYPE_FLOAT:
                float fPixelsLine[] = null;
                for (int i = 0; i < h; i++) {
                    fPixelsLine = srcRaster.getPixels(srcX, srcY + i, w, 1, fPixelsLine);
                    setPixels(dstX, dstY + i, w, 1, fPixelsLine);
                }
                break;
            case DataBuffer.TYPE_DOUBLE:
                double dPixelsLine[] = null;
                for (int i = 0; i < h; i++) {
                    dPixelsLine = srcRaster.getPixels(srcX, srcY + i, w, 1, dPixelsLine);
                    setPixels(dstX, dstY + i, w, 1, dPixelsLine);
                }
                break;
        }
    }
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstX = x + inRaster.getMinX();
        int dstY = y + inRaster.getMinY();
        int w = inRaster.getWidth();
        int h = inRaster.getHeight();
        if (dstX < this.minX || dstX + w > this.minX + this.width || dstY < this.minY
                || dstY + h > this.minY + this.height) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.63")); 
        }
        int srcX = inRaster.getMinX();
        int srcY = inRaster.getMinY();
        Object line = null;
        for (int i = 0; i < h; i++) {
            line = inRaster.getDataElements(srcX, srcY + i, w, 1, line);
            setDataElements(dstX, dstY + i, w, 1, line);
        }
    }
    public void setPixel(int x, int y, int iArray[]) {
        sampleModel.setPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, iArray,
                dataBuffer);
    }
    public void setPixel(int x, int y, float fArray[]) {
        sampleModel.setPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, fArray,
                dataBuffer);
    }
    public void setPixel(int x, int y, double dArray[]) {
        sampleModel.setPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, dArray,
                dataBuffer);
    }
    public void setPixels(int x, int y, int w, int h, int iArray[]) {
        sampleModel.setPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, iArray,
                dataBuffer);
    }
    public void setPixels(int x, int y, int w, int h, float fArray[]) {
        sampleModel.setPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, fArray,
                dataBuffer);
    }
    public void setPixels(int x, int y, int w, int h, double dArray[]) {
        sampleModel.setPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, dArray,
                dataBuffer);
    }
    public void setSamples(int x, int y, int w, int h, int b, int iArray[]) {
        sampleModel.setSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b,
                iArray, dataBuffer);
    }
    public void setSamples(int x, int y, int w, int h, int b, float fArray[]) {
        sampleModel.setSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b,
                fArray, dataBuffer);
    }
    public void setSamples(int x, int y, int w, int h, int b, double dArray[]) {
        sampleModel.setSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b,
                dArray, dataBuffer);
    }
    public void setSample(int x, int y, int b, int s) {
        sampleModel.setSample(x - sampleModelTranslateX, y - sampleModelTranslateY, b, s,
                dataBuffer);
    }
    public void setSample(int x, int y, int b, float s) {
        sampleModel.setSample(x - sampleModelTranslateX, y - sampleModelTranslateY, b, s,
                dataBuffer);
    }
    public void setSample(int x, int y, int b, double s) {
        sampleModel.setSample(x - sampleModelTranslateX, y - sampleModelTranslateY, b, s,
                dataBuffer);
    }
}
