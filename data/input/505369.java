class NativeImageFormat {
    private static final int PT_ANY = 0;    
    private static final int PT_GRAY     = 3;
    private static final int PT_RGB      = 4;
    private static final int INT_RGB_LCMS_FMT =
        colorspaceSh(PT_RGB)|
        extraSh(1)|
        channelsSh(3)|
        bytesSh(1)|
        doswapSh(1)|
        swapfirstSh(1);
    private static final int INT_ARGB_LCMS_FMT = INT_RGB_LCMS_FMT;
    private static final int INT_BGR_LCMS_FMT =
        colorspaceSh(PT_RGB)|
        extraSh(1)|
        channelsSh(3)|
        bytesSh(1);
    private static final int THREE_BYTE_BGR_LCMS_FMT =
        colorspaceSh(PT_RGB)|
        channelsSh(3)|
        bytesSh(1)|
        doswapSh(1);
    private static final int FOUR_BYTE_ABGR_LCMS_FMT =
        colorspaceSh(PT_RGB)|
        extraSh(1)|
        channelsSh(3)|
        bytesSh(1)|
        doswapSh(1);
    private static final int BYTE_GRAY_LCMS_FMT =
        colorspaceSh(PT_GRAY)|
        channelsSh(1)|
        bytesSh(1);
    private static final int USHORT_GRAY_LCMS_FMT =
        colorspaceSh(PT_GRAY)|
        channelsSh(1)|
        bytesSh(2);
    private int cmmFormat = 0;
    private int rows = 0;
    private int cols = 0;
    private int scanlineStride = -1;
    private Object imageData;
    private int dataOffset;
    private int alphaOffset = -1;
    private static native void initIDs();
    static {
        NativeCMM.loadCMM();
        initIDs();
    }
    private static int colorspaceSh(int s) {
        return (s << 16);
    }
    private static int swapfirstSh(int s) {
        return (s << 14);
    }
    private static int flavorSh(int s) {
        return (s << 13);
    }
    private static int planarSh(int s) {
        return (s << 12);
    }
    private static int endianSh(int s) {
        return (s << 11);
    }
    private static int doswapSh(int s) {
        return (s << 10);
    }
    private static int extraSh(int s) {
        return (s << 7);
    }
    private static int channelsSh(int s) {
        return (s << 3);
    }
    private static int bytesSh(int s) {
        return s;
    }
    Object getChannelData() {
        return imageData;
    }
    int getNumCols() {
        return cols;
    }
    int getNumRows() {
        return rows;
    }
    public NativeImageFormat() {
    }
    public NativeImageFormat(Object imgData, int nChannels, int nRows, int nCols) {
        if (imgData instanceof short[]) {
            cmmFormat |= bytesSh(2);
        }
        else if (imgData instanceof byte[]) {
            cmmFormat |= bytesSh(1);
        }
        else
            throw new IllegalArgumentException(Messages.getString("awt.47")); 
        cmmFormat |= channelsSh(nChannels);
        rows = nRows;
        cols = nCols;
        imageData = imgData;
        dataOffset = 0;
    }
    public static NativeImageFormat createNativeImageFormat(BufferedImage bi) {
        NativeImageFormat fmt = new NativeImageFormat();
        switch (bi.getType()) {
            case BufferedImage.TYPE_INT_RGB: {
                fmt.cmmFormat = INT_RGB_LCMS_FMT;
                break;
            }
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE: {
                fmt.cmmFormat = INT_ARGB_LCMS_FMT;
                fmt.alphaOffset = 3;
                break;
            }
            case BufferedImage.TYPE_INT_BGR: {
                fmt.cmmFormat = INT_BGR_LCMS_FMT;
                break;
            }
            case BufferedImage.TYPE_3BYTE_BGR: {
                fmt.cmmFormat = THREE_BYTE_BGR_LCMS_FMT;
                break;
            }
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR: {
                fmt.cmmFormat = FOUR_BYTE_ABGR_LCMS_FMT;
                fmt.alphaOffset = 0;
                break;
            }
            case BufferedImage.TYPE_BYTE_GRAY: {
                fmt.cmmFormat = BYTE_GRAY_LCMS_FMT;
                break;
            }
            case BufferedImage.TYPE_USHORT_GRAY: {
                fmt.cmmFormat = USHORT_GRAY_LCMS_FMT;
                break;
            }
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_BYTE_INDEXED: {
                return null;
            }
            default:
                break; 
        }
        if (fmt.cmmFormat == 0) {
            ColorModel cm = bi.getColorModel();
            SampleModel sm = bi.getSampleModel();
            if (sm instanceof ComponentSampleModel) {
                ComponentSampleModel csm = (ComponentSampleModel) sm;
                fmt.cmmFormat = getFormatFromComponentModel(csm, cm.hasAlpha());
                fmt.scanlineStride = calculateScanlineStrideCSM(csm, bi.getRaster());
            } else if (sm instanceof SinglePixelPackedSampleModel) {
                SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel) sm;
                fmt.cmmFormat = getFormatFromSPPSampleModel(sppsm, cm.hasAlpha());
                fmt.scanlineStride = calculateScanlineStrideSPPSM(sppsm, bi.getRaster());
            }
            if (cm.hasAlpha())
                fmt.alphaOffset = calculateAlphaOffset(sm, bi.getRaster());
        }
        if (fmt.cmmFormat == 0)
            return null;
        if (!fmt.setImageData(bi.getRaster().getDataBuffer())) {
            return null;
        }
        fmt.rows = bi.getHeight();
        fmt.cols = bi.getWidth();
        fmt.dataOffset = bi.getRaster().getDataBuffer().getOffset();
        return fmt;
    }
    public static NativeImageFormat createNativeImageFormat(Raster r) {
        NativeImageFormat fmt = new NativeImageFormat();
        SampleModel sm = r.getSampleModel();
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel) sm;
            fmt.cmmFormat = getFormatFromComponentModel(csm, false);
            fmt.scanlineStride = calculateScanlineStrideCSM(csm, r);
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel) sm;
            fmt.cmmFormat = getFormatFromSPPSampleModel(sppsm, false);
            fmt.scanlineStride = calculateScanlineStrideSPPSM(sppsm, r);
        }
        if (fmt.cmmFormat == 0)
            return null;
        fmt.cols = r.getWidth();
        fmt.rows = r.getHeight();
        fmt.dataOffset = r.getDataBuffer().getOffset();
        if (!fmt.setImageData(r.getDataBuffer()))
            return null;
        return fmt;
    }
    private static int getFormatFromComponentModel(ComponentSampleModel sm, boolean hasAlpha) {
        int bankIndex = sm.getBankIndices()[0];
        for (int i=1; i < sm.getNumBands(); i++) {
            if (sm.getBankIndices()[i] != bankIndex) {
                return 0;
            }
        }
        int channels = hasAlpha ? sm.getNumBands()-1 : sm.getNumBands();
        int extra = hasAlpha ? 1 : 0;
        int bytes = 1;
        switch (sm.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                bytes = 1; break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                bytes = 2; break;
            case DataBuffer.TYPE_INT:
                bytes = 4; break;
            case DataBuffer.TYPE_DOUBLE:
                bytes = 0; break;
            default:
                return 0; 
        }
        int doSwap = 0;
        int swapFirst = 0;
        boolean knownFormat = false;
        int i;
        for (i=0; i < sm.getNumBands(); i++) {
            if (sm.getBandOffsets()[i] != i) break;
        }
        if (i == sm.getNumBands()) { 
            doSwap = 0;
            swapFirst = 0;
            knownFormat = true;
        }
        if (!knownFormat) {
            for (i=0; i < sm.getNumBands()-1; i++) {
                if (sm.getBandOffsets()[i] != i+1) break;
            }
            if (sm.getBandOffsets()[i] == 0) i++;
            if (i == sm.getNumBands()) { 
                doSwap = 0;
                swapFirst = 1;
                knownFormat = true;
            }
        }
        if (!knownFormat) {
            for (i=0; i < sm.getNumBands()-1; i++) {
                if (sm.getBandOffsets()[i] != sm.getNumBands() - 2 - i) break;
            }
            if (sm.getBandOffsets()[i] == sm.getNumBands()-1) i++;
            if (i == sm.getNumBands()) { 
                doSwap = 1;
                swapFirst = 1;
                knownFormat = true;
            }
        }
        if (!knownFormat) {
            for (i=0; i < sm.getNumBands(); i++) {
                if (sm.getBandOffsets()[i] != sm.getNumBands() - 1 - i) break;
            }
            if (i == sm.getNumBands()) { 
                doSwap = 1;
                swapFirst = 0;
                knownFormat = true;
            }
        }
        if (!knownFormat)
            return 0;
        return
            channelsSh(channels) |
            bytesSh(bytes) |
            extraSh(extra) |
            doswapSh(doSwap) |
            swapfirstSh(swapFirst);
    }
    private static int getFormatFromSPPSampleModel(SinglePixelPackedSampleModel sm,
            boolean hasAlpha) {
        int mask = sm.getBitMasks()[0] >>> sm.getBitOffsets()[0];
        if (!(mask == 0xFF || mask == 0xFFFF || mask == 0xFFFFFFFF))
            return 0;
        for (int i = 1; i < sm.getNumBands(); i++) {
            if ((sm.getBitMasks()[i] >>> sm.getBitOffsets()[i]) != mask)
                return 0;
        }
        int pixelSize = 0;
        if (sm.getDataType() == DataBuffer.TYPE_USHORT)
            pixelSize = 2;
        else if (sm.getDataType() == DataBuffer.TYPE_INT)
            pixelSize = 4;
        else
            return 0;
        int bytes = 0;
        switch (mask) {
            case 0xFF:
                bytes = 1;
                break;
            case 0xFFFF:
                bytes = 2;
                break;
            case 0xFFFFFFFF:
                bytes = 4;
                break;
            default: return 0;
        }
        int channels = hasAlpha ? sm.getNumBands()-1 : sm.getNumBands();
        int extra = hasAlpha ? 1 : 0;
        extra +=  pixelSize/bytes - sm.getNumBands(); 
        ArrayList<Integer> offsetsLst = new ArrayList<Integer>();
        for (int k=0; k < sm.getNumBands(); k++) {
            offsetsLst.add(new Integer(sm.getBitOffsets()[k]/(bytes*8)));
        }
        for (int i=0; i<pixelSize/bytes; i++) {
            if (offsetsLst.indexOf(new Integer(i)) < 0)
                offsetsLst.add(new Integer(i));
        }
        int offsets[] = new int[pixelSize/bytes];
        for (int i=0; i<offsetsLst.size(); i++) {
            offsets[i] = offsetsLst.get(i).intValue();
        }
        int doSwap = 0;
        int swapFirst = 0;
        boolean knownFormat = false;
        int i;
        for (i=0; i < pixelSize; i++) {
            if (offsets[i] != i) break;
        }
        if (i == pixelSize) { 
            doSwap = 0;
            swapFirst = 0;
            knownFormat = true;
        }
        if (!knownFormat) {
            for (i=0; i < pixelSize-1; i++) {
                if (offsets[i] != i+1) break;
            }
            if (offsets[i] == 0) i++;
            if (i == pixelSize) { 
                doSwap = 0;
                swapFirst = 1;
                knownFormat = true;
            }
        }
        if (!knownFormat) {
            for (i=0; i < pixelSize-1; i++) {
                if (offsets[i] != pixelSize - 2 - i) break;
            }
            if (offsets[i] == pixelSize-1) i++;
            if (i == pixelSize) { 
                doSwap = 1;
                swapFirst = 1;
                knownFormat = true;
            }
        }
        if (!knownFormat) {
            for (i=0; i < pixelSize; i++) {
                if (offsets[i] != pixelSize - 1 - i) break;
            }
            if (i == pixelSize) { 
                doSwap = 1;
                swapFirst = 0;
                knownFormat = true;
            }
        }
        if (!knownFormat)
            return 0;
        return
            channelsSh(channels) |
            bytesSh(bytes) |
            extraSh(extra) |
            doswapSh(doSwap) |
            swapfirstSh(swapFirst);
    }
    private boolean setImageData(DataBuffer db) {
        AwtImageBackdoorAccessor dbAccess = AwtImageBackdoorAccessor.getInstance();
        try {
            imageData = dbAccess.getData(db);
        } catch (IllegalArgumentException e) {
            return false; 
        }
        return true;
    }
    private static int calculateScanlineStrideCSM(ComponentSampleModel csm, Raster r) {
        if (csm.getScanlineStride() != csm.getPixelStride()*csm.getWidth()) {
            int dataTypeSize = DataBuffer.getDataTypeSize(r.getDataBuffer().getDataType()) / 8;
            return csm.getScanlineStride()*dataTypeSize;
        }
        return -1;
    }
    private static int calculateScanlineStrideSPPSM(SinglePixelPackedSampleModel sppsm, Raster r) {
        if (sppsm.getScanlineStride() != sppsm.getWidth()) {
            int dataTypeSize = DataBuffer.getDataTypeSize(r.getDataBuffer().getDataType()) / 8;
            return sppsm.getScanlineStride()*dataTypeSize;
        }
        return -1;
    }
    private static int calculateAlphaOffset(SampleModel sm, Raster r) {
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel) sm;
            int dataTypeSize =
                DataBuffer.getDataTypeSize(r.getDataBuffer().getDataType()) / 8;
            return
                csm.getBandOffsets()[csm.getBandOffsets().length - 1] * dataTypeSize;
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel) sm;
            return sppsm.getBitOffsets()[sppsm.getBitOffsets().length - 1] / 8;
        } else {
            return -1; 
        }
    }
}
