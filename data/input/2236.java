public abstract class SampleModel
{
    protected int width;
    protected int height;
    protected int numBands;
    protected int dataType;
    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }
    public SampleModel(int dataType, int w, int h, int numBands)
    {
        long size = (long)w * h;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width ("+w+") and height ("+
                                               h+") must be > 0");
        }
        if (size >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width="+w+
                                               " height="+h+") are too large");
        }
        if (dataType < DataBuffer.TYPE_BYTE ||
            (dataType > DataBuffer.TYPE_DOUBLE &&
             dataType != DataBuffer.TYPE_UNDEFINED))
        {
            throw new IllegalArgumentException("Unsupported dataType: "+
                                               dataType);
        }
        if (numBands <= 0) {
            throw new IllegalArgumentException("Number of bands must be > 0");
        }
        this.dataType = dataType;
        this.width = w;
        this.height = h;
        this.numBands = numBands;
    }
    final public int getWidth() {
         return width;
    }
    final public int getHeight() {
         return height;
    }
    final public int getNumBands() {
         return numBands;
    }
    public abstract int getNumDataElements();
    final public int getDataType() {
        return dataType;
    }
    public int getTransferType() {
        return dataType;
    }
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
        int pixels[];
        if (iArray != null)
            pixels = iArray;
        else
            pixels = new int[numBands];
        for (int i=0; i<numBands; i++) {
            pixels[i] = getSample(x, y, i, data);
        }
        return pixels;
    }
    public abstract Object getDataElements(int x, int y,
                                           Object obj, DataBuffer data);
    public Object getDataElements(int x, int y, int w, int h,
                                  Object obj, DataBuffer data) {
        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int cnt = 0;
        Object o = null;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        switch(type) {
        case DataBuffer.TYPE_BYTE:
            byte[] btemp;
            byte[] bdata;
            if (obj == null)
                bdata = new byte[numDataElems*w*h];
            else
                bdata = (byte[])obj;
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    btemp = (byte[])o;
                    for (int k=0; k<numDataElems; k++) {
                        bdata[cnt++] = btemp[k];
                    }
                }
            }
            obj = (Object)bdata;
            break;
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:
            short[] sdata;
            short[] stemp;
            if (obj == null)
                sdata = new short[numDataElems*w*h];
            else
                sdata = (short[])obj;
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    stemp = (short[])o;
                    for (int k=0; k<numDataElems; k++) {
                        sdata[cnt++] = stemp[k];
                    }
                }
            }
            obj = (Object)sdata;
            break;
        case DataBuffer.TYPE_INT:
            int[] idata;
            int[] itemp;
            if (obj == null)
                idata = new int[numDataElems*w*h];
            else
                idata = (int[])obj;
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    itemp = (int[])o;
                    for (int k=0; k<numDataElems; k++) {
                        idata[cnt++] = itemp[k];
                    }
                }
            }
            obj = (Object)idata;
            break;
        case DataBuffer.TYPE_FLOAT:
            float[] fdata;
            float[] ftemp;
            if (obj == null)
                fdata = new float[numDataElems*w*h];
            else
                fdata = (float[])obj;
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    ftemp = (float[])o;
                    for (int k=0; k<numDataElems; k++) {
                        fdata[cnt++] = ftemp[k];
                    }
                }
            }
            obj = (Object)fdata;
            break;
        case DataBuffer.TYPE_DOUBLE:
            double[] ddata;
            double[] dtemp;
            if (obj == null)
                ddata = new double[numDataElems*w*h];
            else
                ddata = (double[])obj;
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    dtemp = (double[])o;
                    for (int k=0; k<numDataElems; k++) {
                        ddata[cnt++] = dtemp[k];
                    }
                }
            }
            obj = (Object)ddata;
            break;
        }
        return obj;
    }
    public abstract void setDataElements(int x, int y,
                                         Object obj, DataBuffer data);
    public void setDataElements(int x, int y, int w, int h,
                                Object obj, DataBuffer data) {
        int cnt = 0;
        Object o = null;
        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        switch(type) {
        case DataBuffer.TYPE_BYTE:
            byte[] barray = (byte[])obj;
            byte[] btemp = new byte[numDataElems];
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        btemp[k] = barray[cnt++];
                    }
                    setDataElements(j, i, btemp, data);
                }
            }
            break;
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:
            short[] sarray = (short[])obj;
            short[] stemp = new short[numDataElems];
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        stemp[k] = sarray[cnt++];
                    }
                    setDataElements(j, i, stemp, data);
                }
            }
            break;
        case DataBuffer.TYPE_INT:
            int[] iArray = (int[])obj;
            int[] itemp = new int[numDataElems];
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        itemp[k] = iArray[cnt++];
                    }
                    setDataElements(j, i, itemp, data);
                }
            }
            break;
        case DataBuffer.TYPE_FLOAT:
            float[] fArray = (float[])obj;
            float[] ftemp = new float[numDataElems];
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        ftemp[k] = fArray[cnt++];
                    }
                    setDataElements(j, i, ftemp, data);
                }
            }
            break;
        case DataBuffer.TYPE_DOUBLE:
            double[] dArray = (double[])obj;
            double[] dtemp = new double[numDataElems];
            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        dtemp[k] = dArray[cnt++];
                    }
                    setDataElements(j, i, dtemp, data);
                }
            }
            break;
        }
    }
    public float[] getPixel(int x, int y, float fArray[],
                            DataBuffer data) {
        float pixels[];
        if (fArray != null)
            pixels = fArray;
        else
            pixels = new float[numBands];
        for (int i=0; i<numBands; i++)
            pixels[i] = getSampleFloat(x, y, i, data);
        return pixels;
    }
    public double[] getPixel(int x, int y, double dArray[],
                             DataBuffer data) {
        double pixels[];
        if(dArray != null)
            pixels = dArray;
        else
            pixels = new double[numBands];
        for (int i=0; i<numBands; i++)
            pixels[i] = getSampleDouble(x, y, i, data);
        return pixels;
    }
    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {
        int pixels[];
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        if (iArray != null)
            pixels = iArray;
        else
            pixels = new int[numBands * w * h];
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for(int k=0; k<numBands; k++) {
                    pixels[Offset++] = getSample(j, i, k, data);
                }
            }
        }
        return pixels;
    }
    public float[] getPixels(int x, int y, int w, int h,
                             float fArray[], DataBuffer data) {
        float pixels[];
        int Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        if (fArray != null)
            pixels = fArray;
        else
            pixels = new float[numBands * w * h];
        for (int i=y; i<y1; i++) {
            for(int j=x; j<x1; j++) {
                for(int k=0; k<numBands; k++) {
                    pixels[Offset++] = getSampleFloat(j, i, k, data);
                }
            }
        }
        return pixels;
    }
    public double[] getPixels(int x, int y, int w, int h,
                              double dArray[], DataBuffer data) {
        double pixels[];
        int    Offset = 0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        if (dArray != null)
            pixels = dArray;
        else
            pixels = new double[numBands * w * h];
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    pixels[Offset++] = getSampleDouble(j, i, k, data);
                }
            }
        }
        return pixels;
    }
    public abstract int getSample(int x, int y, int b, DataBuffer data);
    public float getSampleFloat(int x, int y, int b, DataBuffer data) {
        float sample;
        sample = (float) getSample(x, y, b, data);
        return sample;
    }
    public double getSampleDouble(int x, int y, int b, DataBuffer data) {
        double sample;
        sample = (double) getSample(x, y, b, data);
        return sample;
    }
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int iArray[], DataBuffer data) {
        int pixels[];
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        if (iArray != null)
            pixels = iArray;
        else
            pixels = new int[w * h];
        for(int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                pixels[Offset++] = getSample(j, i, b, data);
            }
        }
        return pixels;
    }
    public float[] getSamples(int x, int y, int w, int h,
                              int b, float fArray[],
                              DataBuffer data) {
        float pixels[];
        int   Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates");
        }
        if (fArray != null)
            pixels = fArray;
        else
            pixels = new float[w * h];
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                pixels[Offset++] = getSampleFloat(j, i, b, data);
            }
        }
        return pixels;
    }
    public double[] getSamples(int x, int y, int w, int h,
                               int b, double dArray[],
                               DataBuffer data) {
        double pixels[];
        int    Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates");
        }
        if (dArray != null)
            pixels = dArray;
        else
            pixels = new double[w * h];
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                pixels[Offset++] = getSampleDouble(j, i, b, data);
            }
        }
        return pixels;
    }
    public void setPixel(int x, int y, int iArray[], DataBuffer data) {
        for (int i=0; i<numBands; i++)
            setSample(x, y, i, iArray[i], data);
    }
    public void setPixel(int x, int y, float fArray[], DataBuffer data) {
        for (int i=0; i<numBands; i++)
            setSample(x, y, i, fArray[i], data);
    }
    public void setPixel(int x, int y, double dArray[], DataBuffer data) {
        for (int i=0; i<numBands; i++)
            setSample(x, y, i, dArray[i], data);
    }
    public void setPixels(int x, int y, int w, int h,
                          int iArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    setSample(j, i, k, iArray[Offset++], data);
                }
            }
        }
    }
    public void setPixels(int x, int y, int w, int h,
                          float fArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for(int k=0; k<numBands; k++) {
                    setSample(j, i, k, fArray[Offset++], data);
                }
            }
        }
    }
    public void setPixels(int x, int y, int w, int h,
                          double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    setSample(j, i, k, dArray[Offset++], data);
                }
            }
        }
    }
    public abstract void setSample(int x, int y, int b,
                                   int s,
                                   DataBuffer data);
    public void setSample(int x, int y, int b,
                          float s ,
                          DataBuffer data) {
        int sample = (int)s;
        setSample(x, y, b, sample, data);
    }
    public void setSample(int x, int y, int b,
                          double s,
                          DataBuffer data) {
        int sample = (int)s;
        setSample(x, y, b, sample, data);
    }
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, iArray[Offset++], data);
            }
        }
    }
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, fArray[Offset++], data);
            }
        }
    }
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }
        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, dArray[Offset++], data);
            }
        }
    }
    public abstract SampleModel createCompatibleSampleModel(int w, int h);
    public abstract SampleModel createSubsetSampleModel(int bands[]);
    public abstract DataBuffer createDataBuffer();
    public abstract int[] getSampleSize();
    public abstract int getSampleSize(int band);
}
