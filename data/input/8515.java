public class JPEGImageReader extends ImageReader {
    private boolean debug = false;
    private long structPointer = 0;
    private ImageInputStream iis = null;
    private List imagePositions = null;
    private int numImages = 0;
    static {
        java.security.AccessController.doPrivileged(
            new sun.security.action.LoadLibraryAction("jpeg"));
        initReaderIDs(ImageInputStream.class,
                      JPEGQTable.class,
                      JPEGHuffmanTable.class);
    }
    protected static final int WARNING_NO_EOI = 0;
    protected static final int WARNING_NO_JFIF_IN_THUMB = 1;
    protected static final int WARNING_IGNORE_INVALID_ICC = 2;
    private static final int MAX_WARNING = WARNING_IGNORE_INVALID_ICC;
    private int currentImage = -1;
    private int width;
    private int height;
    private int colorSpaceCode;
    private int outColorSpaceCode;
    private int numComponents;
    private ColorSpace iccCS = null;
    private ColorConvertOp convert = null;
    private BufferedImage image = null;
    private WritableRaster raster = null;
    private WritableRaster target = null;
    private DataBufferByte buffer = null;
    private Rectangle destROI = null;
    private int [] destinationBands = null;
    private JPEGMetadata streamMetadata = null;
    private JPEGMetadata imageMetadata = null;
    private int imageMetadataIndex = -1;
    private boolean haveSeeked = false;
    private JPEGQTable [] abbrevQTables = null;
    private JPEGHuffmanTable[] abbrevDCHuffmanTables = null;
    private JPEGHuffmanTable[] abbrevACHuffmanTables = null;
    private int minProgressivePass = 0;
    private int maxProgressivePass = Integer.MAX_VALUE;
    private static final int UNKNOWN = -1;  
    private static final int MIN_ESTIMATED_PASSES = 10; 
    private int knownPassCount = UNKNOWN;
    private int pass = 0;
    private float percentToDate = 0.0F;
    private float previousPassPercentage = 0.0F;
    private int progInterval = 0;
    private boolean tablesOnlyChecked = false;
    private Object disposerReferent = new Object();
    private DisposerRecord disposerRecord;
    private static native void initReaderIDs(Class iisClass,
                                             Class qTableClass,
                                             Class huffClass);
    public JPEGImageReader(ImageReaderSpi originator) {
        super(originator);
        structPointer = initJPEGImageReader();
        disposerRecord = new JPEGReaderDisposerRecord(structPointer);
        Disposer.addRecord(disposerReferent, disposerRecord);
    }
    private native long initJPEGImageReader();
    protected void warningOccurred(int code) {
        if ((code < 0) || (code > MAX_WARNING)){
            throw new InternalError("Invalid warning index");
        }
        processWarningOccurred
            ("com.sun.imageio.plugins.jpeg.JPEGImageReaderResources",
             Integer.toString(code));
    }
    protected void warningWithMessage(String msg) {
        processWarningOccurred(msg);
    }
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata)
    {
        setThreadLock();
        try {
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            this.ignoreMetadata = ignoreMetadata;
            resetInternalState();
            iis = (ImageInputStream) input; 
            setSource(structPointer, iis);
        } finally {
            clearThreadLock();
        }
    }
    private native void setSource(long structPointer,
                                  ImageInputStream source);
    private void checkTablesOnly() throws IOException {
        if (debug) {
            System.out.println("Checking for tables-only image");
        }
        long savePos = iis.getStreamPosition();
        if (debug) {
            System.out.println("saved pos is " + savePos);
            System.out.println("length is " + iis.length());
        }
        boolean tablesOnly = readNativeHeader(true);
        if (tablesOnly) {
            if (debug) {
                System.out.println("tables-only image found");
                long pos = iis.getStreamPosition();
                System.out.println("pos after return from native is " + pos);
            }
            if (ignoreMetadata == false) {
                iis.seek(savePos);
                haveSeeked = true;
                streamMetadata = new JPEGMetadata(true, false,
                                                  iis, this);
                long pos = iis.getStreamPosition();
                if (debug) {
                    System.out.println
                        ("pos after constructing stream metadata is " + pos);
                }
            }
            if (hasNextImage()) {
                imagePositions.add(new Long(iis.getStreamPosition()));
            }
        } else { 
            imagePositions.add(new Long(savePos));
            currentImage = 0;
        }
        if (seekForwardOnly) {
            Long pos = (Long) imagePositions.get(imagePositions.size()-1);
            iis.flushBefore(pos.longValue());
        }
        tablesOnlyChecked = true;
    }
    public int getNumImages(boolean allowSearch) throws IOException {
        setThreadLock();
        try { 
            return getNumImagesOnThread(allowSearch);
        } finally {
            clearThreadLock();
        }
    }
    private int getNumImagesOnThread(boolean allowSearch)
      throws IOException {
        if (numImages != 0) {
            return numImages;
        }
        if (iis == null) {
            throw new IllegalStateException("Input not set");
        }
        if (allowSearch == true) {
            if (seekForwardOnly) {
                throw new IllegalStateException(
                    "seekForwardOnly and allowSearch can't both be true!");
            }
            if (!tablesOnlyChecked) {
                checkTablesOnly();
            }
            iis.mark();
            gotoImage(0);
            JPEGBuffer buffer = new JPEGBuffer(iis);
            buffer.loadBuf(0);
            boolean done = false;
            while (!done) {
                done = buffer.scanForFF(this);
                switch (buffer.buf[buffer.bufPtr] & 0xff) {
                case JPEG.SOI:
                    numImages++;
                case 0: 
                case JPEG.RST0:
                case JPEG.RST1:
                case JPEG.RST2:
                case JPEG.RST3:
                case JPEG.RST4:
                case JPEG.RST5:
                case JPEG.RST6:
                case JPEG.RST7:
                case JPEG.EOI:
                    buffer.bufAvail--;
                    buffer.bufPtr++;
                    break;
                default:
                    buffer.bufAvail--;
                    buffer.bufPtr++;
                    buffer.loadBuf(2);
                    int length = ((buffer.buf[buffer.bufPtr++] & 0xff) << 8) |
                        (buffer.buf[buffer.bufPtr++] & 0xff);
                    buffer.bufAvail -= 2;
                    length -= 2; 
                    buffer.skipData(length);
                }
            }
            iis.reset();
            return numImages;
        }
        return -1;  
    }
    private void gotoImage(int imageIndex) throws IOException {
        if (iis == null) {
            throw new IllegalStateException("Input not set");
        }
        if (imageIndex < minIndex) {
            throw new IndexOutOfBoundsException();
        }
        if (!tablesOnlyChecked) {
            checkTablesOnly();
        }
        if (imageIndex < imagePositions.size()) {
            iis.seek(((Long)(imagePositions.get(imageIndex))).longValue());
        } else {
            Long pos = (Long) imagePositions.get(imagePositions.size()-1);
            iis.seek(pos.longValue());
            skipImage();
            for (int index = imagePositions.size();
                 index <= imageIndex;
                 index++) {
                if (!hasNextImage()) {
                    throw new IndexOutOfBoundsException();
                }
                pos = new Long(iis.getStreamPosition());
                imagePositions.add(pos);
                if (seekForwardOnly) {
                    iis.flushBefore(pos.longValue());
                }
                if (index < imageIndex) {
                    skipImage();
                }  
            }
        }
        if (seekForwardOnly) {
            minIndex = imageIndex;
        }
        haveSeeked = true;  
    }
    private void skipImage() throws IOException {
        if (debug) {
            System.out.println("skipImage called");
        }
        boolean foundFF = false;
        for (int byteval = iis.read();
             byteval != -1;
             byteval = iis.read()) {
            if (foundFF == true) {
                if (byteval == JPEG.EOI) {
                    return;
                }
            }
            foundFF = (byteval == 0xff) ? true : false;
        }
        throw new IndexOutOfBoundsException();
    }
    private boolean hasNextImage() throws IOException {
        if (debug) {
            System.out.print("hasNextImage called; returning ");
        }
        iis.mark();
        boolean foundFF = false;
        for (int byteval = iis.read();
             byteval != -1;
             byteval = iis.read()) {
            if (foundFF == true) {
                if (byteval == JPEG.SOI) {
                    iis.reset();
                    if (debug) {
                        System.out.println("true");
                    }
                    return true;
                }
            }
            foundFF = (byteval == 0xff) ? true : false;
        }
        iis.reset();
        if (debug) {
            System.out.println("false");
        }
        return false;
    }
    private void pushBack(int num) throws IOException {
        if (debug) {
            System.out.println("pushing back " + num + " bytes");
        }
        iis.seek(iis.getStreamPosition()-num);
    }
    private void readHeader(int imageIndex, boolean reset)
        throws IOException {
        gotoImage(imageIndex);
        readNativeHeader(reset); 
        currentImage = imageIndex;
    }
    private boolean readNativeHeader(boolean reset) throws IOException {
        boolean retval = false;
        retval = readImageHeader(structPointer, haveSeeked, reset);
        haveSeeked = false;
        return retval;
    }
    private native boolean readImageHeader(long structPointer,
                                           boolean clearBuffer,
                                           boolean reset)
        throws IOException;
    private void setImageData(int width,
                              int height,
                              int colorSpaceCode,
                              int outColorSpaceCode,
                              int numComponents,
                              byte [] iccData) {
        this.width = width;
        this.height = height;
        this.colorSpaceCode = colorSpaceCode;
        this.outColorSpaceCode = outColorSpaceCode;
        this.numComponents = numComponents;
        if (iccData == null) {
            iccCS = null;
            return;
        }
        ICC_Profile newProfile = null;
        try {
            newProfile = ICC_Profile.getInstance(iccData);
        } catch (IllegalArgumentException e) {
            iccCS = null;
            warningOccurred(WARNING_IGNORE_INVALID_ICC);
            return;
        }
        byte[] newData = newProfile.getData();
        ICC_Profile oldProfile = null;
        if (iccCS instanceof ICC_ColorSpace) {
            oldProfile = ((ICC_ColorSpace)iccCS).getProfile();
        }
        byte[] oldData = null;
        if (oldProfile != null) {
            oldData = oldProfile.getData();
        }
        if (oldData == null ||
            !java.util.Arrays.equals(oldData, newData))
        {
            iccCS = new ICC_ColorSpace(newProfile);
            try {
                float[] colors = iccCS.fromRGB(new float[] {1f, 0f, 0f});
            } catch (CMMException e) {
                iccCS = null;
                warningOccurred(WARNING_IGNORE_INVALID_ICC);
            }
        }
    }
    public int getWidth(int imageIndex) throws IOException {
        setThreadLock();
        try {
            if (currentImage != imageIndex) {
                readHeader(imageIndex, true);
            }
            return width;
        } finally {
            clearThreadLock();
        }
    }
    public int getHeight(int imageIndex) throws IOException {
        setThreadLock();
        try {
            if (currentImage != imageIndex) {
                readHeader(imageIndex, true);
            }
            return height;
        } finally {
            clearThreadLock();
        }
    }
    private ImageTypeProducer getImageType(int code) {
        ImageTypeProducer ret = null;
        if ((code > 0) && (code < JPEG.NUM_JCS_CODES)) {
            ret = ImageTypeProducer.getTypeProducer(code);
        }
        return ret;
    }
    public ImageTypeSpecifier getRawImageType(int imageIndex)
        throws IOException {
        setThreadLock();
        try {
            if (currentImage != imageIndex) {
                readHeader(imageIndex, true);
            }
            return getImageType(colorSpaceCode).getType();
        } finally {
            clearThreadLock();
        }
    }
    public Iterator getImageTypes(int imageIndex)
        throws IOException {
        setThreadLock();
        try {
            return getImageTypesOnThread(imageIndex);
        } finally {
            clearThreadLock();
        }
    }
    private Iterator getImageTypesOnThread(int imageIndex)
        throws IOException {
        if (currentImage != imageIndex) {
            readHeader(imageIndex, true);
        }
        ImageTypeProducer raw = getImageType(colorSpaceCode);
        ArrayList<ImageTypeProducer> list = new ArrayList<ImageTypeProducer>(1);
        switch (colorSpaceCode) {
        case JPEG.JCS_GRAYSCALE:
            list.add(raw);
            list.add(getImageType(JPEG.JCS_RGB));
            break;
        case JPEG.JCS_RGB:
            list.add(raw);
            list.add(getImageType(JPEG.JCS_GRAYSCALE));
            list.add(getImageType(JPEG.JCS_YCC));
            break;
        case JPEG.JCS_RGBA:
            list.add(raw);
            break;
        case JPEG.JCS_YCC:
            if (raw != null) {  
                list.add(raw);
                list.add(getImageType(JPEG.JCS_RGB));
            }
            break;
        case JPEG.JCS_YCCA:
            if (raw != null) {  
                list.add(raw);
            }
            break;
        case JPEG.JCS_YCbCr:
            list.add(getImageType(JPEG.JCS_RGB));
            if (iccCS != null) {
                list.add(new ImageTypeProducer() {
                    protected ImageTypeSpecifier produce() {
                        return ImageTypeSpecifier.createInterleaved
                         (iccCS,
                          JPEG.bOffsRGB,  
                          DataBuffer.TYPE_BYTE,
                          false,
                          false);
                    }
                });
            }
            list.add(getImageType(JPEG.JCS_GRAYSCALE));
            list.add(getImageType(JPEG.JCS_YCC));
            break;
        case JPEG.JCS_YCbCrA:  
            list.add(getImageType(JPEG.JCS_RGBA));
            break;
        }
        return new ImageTypeIterator(list.iterator());
    }
    private void checkColorConversion(BufferedImage image,
                                      ImageReadParam param)
        throws IIOException {
        if (param != null) {
            if ((param.getSourceBands() != null) ||
                (param.getDestinationBands() != null)) {
                return;
            }
        }
        ColorModel cm = image.getColorModel();
        if (cm instanceof IndexColorModel) {
            throw new IIOException("IndexColorModel not supported");
        }
        ColorSpace cs = cm.getColorSpace();
        int csType = cs.getType();
        convert = null;
        switch (outColorSpaceCode) {
        case JPEG.JCS_GRAYSCALE:  
            if  (csType == ColorSpace.TYPE_RGB) { 
                setOutColorSpace(structPointer, JPEG.JCS_RGB);
                outColorSpaceCode = JPEG.JCS_RGB;
                numComponents = 3;
            } else if (csType != ColorSpace.TYPE_GRAY) {
                throw new IIOException("Incompatible color conversion");
            }
            break;
        case JPEG.JCS_RGB:  
            if (csType ==  ColorSpace.TYPE_GRAY) {  
                if (colorSpaceCode == JPEG.JCS_YCbCr) {
                    setOutColorSpace(structPointer, JPEG.JCS_GRAYSCALE);
                    outColorSpaceCode = JPEG.JCS_GRAYSCALE;
                    numComponents = 1;
                }
            } else if ((iccCS != null) &&
                       (cm.getNumComponents() == numComponents) &&
                       (cs != iccCS)) {
                convert = new ColorConvertOp(iccCS, cs, null);
            } else if ((iccCS == null) &&
                       (!cs.isCS_sRGB()) &&
                       (cm.getNumComponents() == numComponents)) {
                convert = new ColorConvertOp(JPEG.JCS.sRGB, cs, null);
            } else if (csType != ColorSpace.TYPE_RGB) {
                throw new IIOException("Incompatible color conversion");
            }
            break;
        case JPEG.JCS_RGBA:
            if ((csType != ColorSpace.TYPE_RGB) ||
                (cm.getNumComponents() != numComponents)) {
                throw new IIOException("Incompatible color conversion");
            }
            break;
        case JPEG.JCS_YCC:
            {
                ColorSpace YCC = JPEG.JCS.getYCC();
                if (YCC == null) { 
                    throw new IIOException("Incompatible color conversion");
                }
                if ((cs != YCC) &&
                    (cm.getNumComponents() == numComponents)) {
                    convert = new ColorConvertOp(YCC, cs, null);
                }
            }
            break;
        case JPEG.JCS_YCCA:
            {
                ColorSpace YCC = JPEG.JCS.getYCC();
                if ((YCC == null) || 
                    (cs != YCC) ||
                    (cm.getNumComponents() != numComponents)) {
                    throw new IIOException("Incompatible color conversion");
                }
            }
            break;
        default:
            throw new IIOException("Incompatible color conversion");
        }
    }
    private native void setOutColorSpace(long structPointer, int id);
    public ImageReadParam getDefaultReadParam() {
        return new JPEGImageReadParam();
    }
    public IIOMetadata getStreamMetadata() throws IOException {
        setThreadLock();
        try {
            if (!tablesOnlyChecked) {
                checkTablesOnly();
            }
            return streamMetadata;
        } finally {
            clearThreadLock();
        }
    }
    public IIOMetadata getImageMetadata(int imageIndex)
        throws IOException {
        setThreadLock();
        try {
            if ((imageMetadataIndex == imageIndex)
                && (imageMetadata != null)) {
                return imageMetadata;
            }
            gotoImage(imageIndex);
            imageMetadata = new JPEGMetadata(false, false, iis, this);
            imageMetadataIndex = imageIndex;
            return imageMetadata;
        } finally {
            clearThreadLock();
        }
    }
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException {
        setThreadLock();
        try {
            try {
                readInternal(imageIndex, param, false);
            } catch (RuntimeException e) {
                resetLibraryState(structPointer);
                throw e;
            } catch (IOException e) {
                resetLibraryState(structPointer);
                throw e;
            }
            BufferedImage ret = image;
            image = null;  
            return ret;
        } finally {
            clearThreadLock();
        }
    }
    private Raster readInternal(int imageIndex,
                                ImageReadParam param,
                                boolean wantRaster) throws IOException {
        readHeader(imageIndex, false);
        WritableRaster imRas = null;
        int numImageBands = 0;
        if (!wantRaster){
            Iterator imageTypes = getImageTypes(imageIndex);
            if (imageTypes.hasNext() == false) {
                throw new IIOException("Unsupported Image Type");
            }
            image = getDestination(param, imageTypes, width, height);
            imRas = image.getRaster();
            numImageBands = image.getSampleModel().getNumBands();
            checkColorConversion(image, param);
            checkReadParamBandSettings(param, numComponents, numImageBands);
        } else {
            setOutColorSpace(structPointer, colorSpaceCode);
            image = null;
        }
        int [] srcBands = JPEG.bandOffsets[numComponents-1];
        int numRasterBands = (wantRaster ? numComponents : numImageBands);
        destinationBands = null;
        Rectangle srcROI = new Rectangle(0, 0, 0, 0);
        destROI = new Rectangle(0, 0, 0, 0);
        computeRegions(param, width, height, image, srcROI, destROI);
        int periodX = 1;
        int periodY = 1;
        minProgressivePass = 0;
        maxProgressivePass = Integer.MAX_VALUE;
        if (param != null) {
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
            int[] sBands = param.getSourceBands();
            if (sBands != null) {
                srcBands = sBands;
                numRasterBands = srcBands.length;
            }
            if (!wantRaster) {  
                destinationBands = param.getDestinationBands();
            }
            minProgressivePass = param.getSourceMinProgressivePass();
            maxProgressivePass = param.getSourceMaxProgressivePass();
            if (param instanceof JPEGImageReadParam) {
                JPEGImageReadParam jparam = (JPEGImageReadParam) param;
                if (jparam.areTablesSet()) {
                    abbrevQTables = jparam.getQTables();
                    abbrevDCHuffmanTables = jparam.getDCHuffmanTables();
                    abbrevACHuffmanTables = jparam.getACHuffmanTables();
                }
            }
        }
        int lineSize = destROI.width*numRasterBands;
        buffer = new DataBufferByte(lineSize);
        int [] bandOffs = JPEG.bandOffsets[numRasterBands-1];
        raster = Raster.createInterleavedRaster(buffer,
                                                destROI.width, 1,
                                                lineSize,
                                                numRasterBands,
                                                bandOffs,
                                                null);
        if (wantRaster) {
            target =  Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                                     destROI.width,
                                                     destROI.height,
                                                     lineSize,
                                                     numRasterBands,
                                                     bandOffs,
                                                     null);
        } else {
            target = imRas;
        }
        int [] bandSizes = target.getSampleModel().getSampleSize();
        boolean callbackUpdates = ((updateListeners != null)
                                   || (progressListeners != null));
        initProgressData();
        if (imageIndex == imageMetadataIndex) { 
            knownPassCount = 0;
            for (Iterator iter = imageMetadata.markerSequence.iterator();
                 iter.hasNext();) {
                if (iter.next() instanceof SOSMarkerSegment) {
                    knownPassCount++;
                }
            }
        }
        progInterval = Math.max((target.getHeight()-1) / 20, 1);
        if (knownPassCount > 0) {
            progInterval *= knownPassCount;
        } else if (maxProgressivePass != Integer.MAX_VALUE) {
            progInterval *= (maxProgressivePass - minProgressivePass + 1);
        }
        if (debug) {
            System.out.println("**** Read Data *****");
            System.out.println("numRasterBands is " + numRasterBands);
            System.out.print("srcBands:");
            for (int i = 0; i<srcBands.length;i++)
                System.out.print(" " + srcBands[i]);
            System.out.println();
            System.out.println("destination bands is " + destinationBands);
            if (destinationBands != null) {
                for (int i = 0; i < destinationBands.length; i++) {
                    System.out.print(" " + destinationBands[i]);
                }
                System.out.println();
            }
            System.out.println("sourceROI is " + srcROI);
            System.out.println("destROI is " + destROI);
            System.out.println("periodX is " + periodX);
            System.out.println("periodY is " + periodY);
            System.out.println("minProgressivePass is " + minProgressivePass);
            System.out.println("maxProgressivePass is " + maxProgressivePass);
            System.out.println("callbackUpdates is " + callbackUpdates);
        }
        processImageStarted(currentImage);
        boolean aborted = false;
        aborted = readImage(structPointer,
                            buffer.getData(),
                            numRasterBands,
                            srcBands,
                            bandSizes,
                            srcROI.x, srcROI.y,
                            srcROI.width, srcROI.height,
                            periodX, periodY,
                            abbrevQTables,
                            abbrevDCHuffmanTables,
                            abbrevACHuffmanTables,
                            minProgressivePass, maxProgressivePass,
                            callbackUpdates);
        if (aborted) {
            processReadAborted();
        } else {
            processImageComplete();
        }
        return target;
    }
    private void acceptPixels(int y, boolean progressive) {
        if (convert != null) {
            convert.filter(raster, raster);
        }
        target.setRect(destROI.x, destROI.y + y, raster);
        processImageUpdate(image,
                           destROI.x, destROI.y+y,
                           raster.getWidth(), 1,
                           1, 1,
                           destinationBands);
        if ((y > 0) && (y%progInterval == 0)) {
            int height = target.getHeight()-1;
            float percentOfPass = ((float)y)/height;
            if (progressive) {
                if (knownPassCount != UNKNOWN) {
                    processImageProgress((pass + percentOfPass)*100.0F
                                         / knownPassCount);
                } else if (maxProgressivePass != Integer.MAX_VALUE) {
                    processImageProgress((pass + percentOfPass)*100.0F
                        / (maxProgressivePass - minProgressivePass + 1));
                } else {
                    int remainingPasses = 
                        Math.max(2, MIN_ESTIMATED_PASSES-pass);
                    int totalPasses = pass + remainingPasses-1;
                    progInterval = Math.max(height/20*totalPasses,
                                            totalPasses);
                    if (y%progInterval == 0) {
                        percentToDate = previousPassPercentage +
                            (1.0F - previousPassPercentage)
                            * (percentOfPass)/remainingPasses;
                        if (debug) {
                            System.out.print("pass= " + pass);
                            System.out.print(", y= " + y);
                            System.out.print(", progInt= " + progInterval);
                            System.out.print(", % of pass: " + percentOfPass);
                            System.out.print(", rem. passes: "
                                             + remainingPasses);
                            System.out.print(", prev%: "
                                             + previousPassPercentage);
                            System.out.print(", %ToDate: " + percentToDate);
                            System.out.print(" ");
                        }
                        processImageProgress(percentToDate*100.0F);
                    }
                }
            } else {
                processImageProgress(percentOfPass * 100.0F);
            }
        }
    }
    private void initProgressData() {
        knownPassCount = UNKNOWN;
        pass = 0;
        percentToDate = 0.0F;
        previousPassPercentage = 0.0F;
        progInterval = 0;
    }
    private void passStarted (int pass) {
        this.pass = pass;
        previousPassPercentage = percentToDate;
        processPassStarted(image,
                           pass,
                           minProgressivePass,
                           maxProgressivePass,
                           0, 0,
                           1,1,
                           destinationBands);
    }
    private void passComplete () {
        processPassComplete(image);
    }
    void thumbnailStarted(int thumbnailIndex) {
        processThumbnailStarted(currentImage, thumbnailIndex);
    }
    void thumbnailProgress(float percentageDone) {
        processThumbnailProgress(percentageDone);
    }
    void thumbnailComplete() {
        processThumbnailComplete();
    }
    private native boolean readImage(long structPointer,
                                     byte [] buffer,
                                     int numRasterBands,
                                     int [] srcBands,
                                     int [] bandSizes,
                                     int sourceXOffset, int sourceYOffset,
                                     int sourceWidth, int sourceHeight,
                                     int periodX, int periodY,
                                     JPEGQTable [] abbrevQTables,
                                     JPEGHuffmanTable [] abbrevDCHuffmanTables,
                                     JPEGHuffmanTable [] abbrevACHuffmanTables,
                                     int minProgressivePass,
                                     int maxProgressivePass,
                                     boolean wantUpdates);
    public void abort() {
        setThreadLock();
        try {
            super.abort();
            abortRead(structPointer);
        } finally {
            clearThreadLock();
        }
    }
    private native void abortRead(long structPointer);
    private native void resetLibraryState(long structPointer);
    public boolean canReadRaster() {
        return true;
    }
    public Raster readRaster(int imageIndex, ImageReadParam param)
        throws IOException {
        setThreadLock();
        Raster retval = null;
        try {
            Point saveDestOffset = null;
            if (param != null) {
                saveDestOffset = param.getDestinationOffset();
                param.setDestinationOffset(new Point(0, 0));
            }
            retval = readInternal(imageIndex, param, true);
            if (saveDestOffset != null) {
                target = target.createWritableTranslatedChild(saveDestOffset.x,
                                                              saveDestOffset.y);
            }
        } catch (RuntimeException e) {
            resetLibraryState(structPointer);
            throw e;
        } catch (IOException e) {
            resetLibraryState(structPointer);
            throw e;
        } finally {
            clearThreadLock();
        }
        return retval;
    }
    public boolean readerSupportsThumbnails() {
        return true;
    }
    public int getNumThumbnails(int imageIndex) throws IOException {
        setThreadLock();
        try {
            getImageMetadata(imageIndex);  
            JFIFMarkerSegment jfif =
                (JFIFMarkerSegment) imageMetadata.findMarkerSegment
                (JFIFMarkerSegment.class, true);
            int retval = 0;
            if (jfif != null) {
                retval = (jfif.thumb == null) ? 0 : 1;
                retval += jfif.extSegments.size();
            }
            return retval;
        } finally {
            clearThreadLock();
        }
    }
    public int getThumbnailWidth(int imageIndex, int thumbnailIndex)
        throws IOException {
        setThreadLock();
        try {
            if ((thumbnailIndex < 0)
                || (thumbnailIndex >= getNumThumbnails(imageIndex))) {
                throw new IndexOutOfBoundsException("No such thumbnail");
            }
            JFIFMarkerSegment jfif =
                (JFIFMarkerSegment) imageMetadata.findMarkerSegment
                (JFIFMarkerSegment.class, true);
            return  jfif.getThumbnailWidth(thumbnailIndex);
        } finally {
            clearThreadLock();
        }
    }
    public int getThumbnailHeight(int imageIndex, int thumbnailIndex)
        throws IOException {
        setThreadLock();
        try {
            if ((thumbnailIndex < 0)
                || (thumbnailIndex >= getNumThumbnails(imageIndex))) {
                throw new IndexOutOfBoundsException("No such thumbnail");
            }
            JFIFMarkerSegment jfif =
                (JFIFMarkerSegment) imageMetadata.findMarkerSegment
                (JFIFMarkerSegment.class, true);
            return  jfif.getThumbnailHeight(thumbnailIndex);
        } finally {
            clearThreadLock();
        }
    }
    public BufferedImage readThumbnail(int imageIndex,
                                       int thumbnailIndex)
        throws IOException {
        setThreadLock();
        try {
            if ((thumbnailIndex < 0)
                || (thumbnailIndex >= getNumThumbnails(imageIndex))) {
                throw new IndexOutOfBoundsException("No such thumbnail");
            }
            JFIFMarkerSegment jfif =
                (JFIFMarkerSegment) imageMetadata.findMarkerSegment
                (JFIFMarkerSegment.class, true);
            return  jfif.getThumbnail(iis, thumbnailIndex, this);
        } finally {
            clearThreadLock();
        }
    }
    private void resetInternalState() {
        resetReader(structPointer);
        numImages = 0;
        imagePositions = new ArrayList();
        currentImage = -1;
        image = null;
        raster = null;
        target = null;
        buffer = null;
        destROI = null;
        destinationBands = null;
        streamMetadata = null;
        imageMetadata = null;
        imageMetadataIndex = -1;
        haveSeeked = false;
        tablesOnlyChecked = false;
        iccCS = null;
        initProgressData();
    }
    public void reset() {
        setThreadLock();
        try {
            super.reset();
        } finally {
            clearThreadLock();
        }
    }
    private native void resetReader(long structPointer);
    public void dispose() {
        setThreadLock();
        try {
            if (structPointer != 0) {
                disposerRecord.dispose();
                structPointer = 0;
            }
        } finally {
            clearThreadLock();
        }
    }
    private static native void disposeReader(long structPointer);
    private static class JPEGReaderDisposerRecord implements DisposerRecord {
        private long pData;
        public JPEGReaderDisposerRecord(long pData) {
            this.pData = pData;
        }
        public synchronized void dispose() {
            if (pData != 0) {
                disposeReader(pData);
                pData = 0;
            }
        }
    }
    private Thread theThread = null;
    private int theLockCount = 0;
    private synchronized void setThreadLock() {
        Thread currThread = Thread.currentThread();
        if (theThread != null) {
            if (theThread != currThread) {
                throw new IllegalStateException("Attempt to use instance of " +
                                                this + " locked on thread " +
                                                theThread + " from thread " +
                                                currThread);
            } else {
                theLockCount ++;
            }
        } else {
            theThread = currThread;
            theLockCount = 1;
        }
    }
    private synchronized void clearThreadLock() {
        Thread currThread = Thread.currentThread();
        if (theThread == null || theThread != currThread) {
            throw new IllegalStateException("Attempt to clear thread lock " +
                                            " form wrong thread." +
                                            " Locked thread: " + theThread +
                                            "; current thread: " + currThread);
        }
        theLockCount --;
        if (theLockCount == 0) {
            theThread = null;
        }
    }
}
class ImageTypeIterator implements Iterator<ImageTypeSpecifier> {
     private Iterator<ImageTypeProducer> producers;
     private ImageTypeSpecifier theNext = null;
     public ImageTypeIterator(Iterator<ImageTypeProducer> producers) {
         this.producers = producers;
     }
     public boolean hasNext() {
         if (theNext != null) {
             return true;
         }
         if (!producers.hasNext()) {
             return false;
         }
         do {
             theNext = producers.next().getType();
         } while (theNext == null && producers.hasNext());
         return (theNext != null);
     }
     public ImageTypeSpecifier next() {
         if (theNext != null || hasNext()) {
             ImageTypeSpecifier t = theNext;
             theNext = null;
             return t;
         } else {
             throw new NoSuchElementException();
         }
     }
     public void remove() {
         producers.remove();
     }
}
class ImageTypeProducer {
    private ImageTypeSpecifier type = null;
    boolean failed = false;
    private int csCode;
    public ImageTypeProducer(int csCode) {
        this.csCode = csCode;
    }
    public ImageTypeProducer() {
        csCode = -1; 
    }
    public synchronized ImageTypeSpecifier getType() {
        if (!failed && type == null) {
            try {
                type = produce();
            } catch (Throwable e) {
                failed = true;
            }
        }
        return type;
    }
    private static final ImageTypeProducer [] defaultTypes =
            new ImageTypeProducer [JPEG.NUM_JCS_CODES];
    public synchronized static ImageTypeProducer getTypeProducer(int csCode) {
        if (csCode < 0 || csCode >= JPEG.NUM_JCS_CODES) {
            return null;
        }
        if (defaultTypes[csCode] == null) {
            defaultTypes[csCode] = new ImageTypeProducer(csCode);
        }
        return defaultTypes[csCode];
    }
    protected ImageTypeSpecifier produce() {
        switch (csCode) {
            case JPEG.JCS_GRAYSCALE:
                return ImageTypeSpecifier.createFromBufferedImageType
                        (BufferedImage.TYPE_BYTE_GRAY);
            case JPEG.JCS_RGB:
                return ImageTypeSpecifier.createInterleaved(JPEG.JCS.sRGB,
                        JPEG.bOffsRGB,
                        DataBuffer.TYPE_BYTE,
                        false,
                        false);
            case JPEG.JCS_RGBA:
                return ImageTypeSpecifier.createPacked(JPEG.JCS.sRGB,
                        0xff000000,
                        0x00ff0000,
                        0x0000ff00,
                        0x000000ff,
                        DataBuffer.TYPE_INT,
                        false);
            case JPEG.JCS_YCC:
                if (JPEG.JCS.getYCC() != null) {
                    return ImageTypeSpecifier.createInterleaved(
                            JPEG.JCS.getYCC(),
                        JPEG.bandOffsets[2],
                        DataBuffer.TYPE_BYTE,
                        false,
                        false);
                } else {
                    return null;
                }
            case JPEG.JCS_YCCA:
                if (JPEG.JCS.getYCC() != null) {
                    return ImageTypeSpecifier.createInterleaved(
                            JPEG.JCS.getYCC(),
                        JPEG.bandOffsets[3],
                        DataBuffer.TYPE_BYTE,
                        true,
                        false);
                } else {
                    return null;
                }
            default:
                return null;
        }
    }
}
