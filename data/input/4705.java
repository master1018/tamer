public class JPEGImageWriter extends ImageWriter {
    private boolean debug = false;
    private long structPointer = 0;
    private ImageOutputStream ios = null;
    private Raster srcRas = null;
    private WritableRaster raster = null;
    private boolean indexed = false;
    private IndexColorModel indexCM = null;
    private boolean convertTosRGB = false;  
    private WritableRaster converted = null;
    private boolean isAlphaPremultiplied = false;
    private ColorModel srcCM = null;
    private List thumbnails = null;
    private ICC_Profile iccProfile = null;
    private int sourceXOffset = 0;
    private int sourceYOffset = 0;
    private int sourceWidth = 0;
    private int [] srcBands = null;
    private int sourceHeight = 0;
    private int currentImage = 0;
    private ColorConvertOp convertOp = null;
    private JPEGQTable [] streamQTables = null;
    private JPEGHuffmanTable[] streamDCHuffmanTables = null;
    private JPEGHuffmanTable[] streamACHuffmanTables = null;
    private boolean ignoreJFIF = false;  
    private boolean forceJFIF = false;  
    private boolean ignoreAdobe = false;  
    private int newAdobeTransform = JPEG.ADOBE_IMPOSSIBLE;  
    private boolean writeDefaultJFIF = false;
    private boolean writeAdobe = false;
    private JPEGMetadata metadata = null;
    private boolean sequencePrepared = false;
    private int numScans = 0;
    private Object disposerReferent = new Object();
    private DisposerRecord disposerRecord;
    protected static final int WARNING_DEST_IGNORED = 0;
    protected static final int WARNING_STREAM_METADATA_IGNORED = 1;
    protected static final int WARNING_DEST_METADATA_COMP_MISMATCH = 2;
    protected static final int WARNING_DEST_METADATA_JFIF_MISMATCH = 3;
    protected static final int WARNING_DEST_METADATA_ADOBE_MISMATCH = 4;
    protected static final int WARNING_IMAGE_METADATA_JFIF_MISMATCH = 5;
    protected static final int WARNING_IMAGE_METADATA_ADOBE_MISMATCH = 6;
    protected static final int WARNING_METADATA_NOT_JPEG_FOR_RASTER = 7;
    protected static final int WARNING_NO_BANDS_ON_INDEXED = 8;
    protected static final int WARNING_ILLEGAL_THUMBNAIL = 9;
    protected static final int WARNING_IGNORING_THUMBS = 10;
    protected static final int WARNING_FORCING_JFIF = 11;
    protected static final int WARNING_THUMB_CLIPPED = 12;
    protected static final int WARNING_METADATA_ADJUSTED_FOR_THUMB = 13;
    protected static final int WARNING_NO_RGB_THUMB_AS_INDEXED = 14;
    protected static final int WARNING_NO_GRAY_THUMB_AS_INDEXED = 15;
    private static final int MAX_WARNING = WARNING_NO_GRAY_THUMB_AS_INDEXED;
    static {
        java.security.AccessController.doPrivileged(
            new sun.security.action.LoadLibraryAction("jpeg"));
        initWriterIDs(ImageOutputStream.class,
                      JPEGQTable.class,
                      JPEGHuffmanTable.class);
    }
    public JPEGImageWriter(ImageWriterSpi originator) {
        super(originator);
        structPointer = initJPEGImageWriter();
        disposerRecord = new JPEGWriterDisposerRecord(structPointer);
        Disposer.addRecord(disposerReferent, disposerRecord);
    }
    public void setOutput(Object output) {
        setThreadLock();
        try {
            super.setOutput(output); 
            resetInternalState();
            ios = (ImageOutputStream) output; 
            setDest(structPointer, ios);
        } finally {
            clearThreadLock();
        }
    }
    public ImageWriteParam getDefaultWriteParam() {
        return new JPEGImageWriteParam(null);
    }
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        setThreadLock();
        try {
            return new JPEGMetadata(param, this);
        } finally {
            clearThreadLock();
        }
    }
    public IIOMetadata
        getDefaultImageMetadata(ImageTypeSpecifier imageType,
                                ImageWriteParam param) {
        setThreadLock();
        try {
            return new JPEGMetadata(imageType, param, this);
        } finally {
            clearThreadLock();
        }
    }
    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                             ImageWriteParam param) {
        if (inData instanceof JPEGMetadata) {
            JPEGMetadata jpegData = (JPEGMetadata) inData;
            if (jpegData.isStream) {
                return inData;
            }
        }
        return null;
    }
    public IIOMetadata
        convertImageMetadata(IIOMetadata inData,
                             ImageTypeSpecifier imageType,
                             ImageWriteParam param) {
        setThreadLock();
        try {
            return convertImageMetadataOnThread(inData, imageType, param);
        } finally {
            clearThreadLock();
        }
    }
    private IIOMetadata
        convertImageMetadataOnThread(IIOMetadata inData,
                                     ImageTypeSpecifier imageType,
                                     ImageWriteParam param) {
        if (inData instanceof JPEGMetadata) {
            JPEGMetadata jpegData = (JPEGMetadata) inData;
            if (!jpegData.isStream) {
                return inData;
            } else {
                return null;
            }
        }
        if (inData.isStandardMetadataFormatSupported()) {
            String formatName =
                IIOMetadataFormatImpl.standardMetadataFormatName;
            Node tree = inData.getAsTree(formatName);
            if (tree != null) {
                JPEGMetadata jpegData = new JPEGMetadata(imageType,
                                                         param,
                                                         this);
                try {
                    jpegData.setFromTree(formatName, tree);
                } catch (IIOInvalidTreeException e) {
                    return null;
                }
                return jpegData;
            }
        }
        return null;
    }
    public int getNumThumbnailsSupported(ImageTypeSpecifier imageType,
                                         ImageWriteParam param,
                                         IIOMetadata streamMetadata,
                                         IIOMetadata imageMetadata) {
        if (jfifOK(imageType, param, streamMetadata, imageMetadata)) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }
    static final Dimension [] preferredThumbSizes = {new Dimension(1, 1),
                                                     new Dimension(255, 255)};
    public Dimension[] getPreferredThumbnailSizes(ImageTypeSpecifier imageType,
                                                  ImageWriteParam param,
                                                  IIOMetadata streamMetadata,
                                                  IIOMetadata imageMetadata) {
        if (jfifOK(imageType, param, streamMetadata, imageMetadata)) {
            return (Dimension [])preferredThumbSizes.clone();
        }
        return null;
    }
    private boolean jfifOK(ImageTypeSpecifier imageType,
                           ImageWriteParam param,
                           IIOMetadata streamMetadata,
                           IIOMetadata imageMetadata) {
        if ((imageType != null) &&
            (!JPEG.isJFIFcompliant(imageType, true))) {
            return false;
        }
        if (imageMetadata != null) {
            JPEGMetadata metadata = null;
            if (imageMetadata instanceof JPEGMetadata) {
                metadata = (JPEGMetadata) imageMetadata;
            } else {
                metadata = (JPEGMetadata)convertImageMetadata(imageMetadata,
                                                              imageType,
                                                              param);
            }
            if (metadata.findMarkerSegment
                (JFIFMarkerSegment.class, true) == null){
                return false;
            }
        }
        return true;
    }
    public boolean canWriteRasters() {
        return true;
    }
    public void write(IIOMetadata streamMetadata,
                      IIOImage image,
                      ImageWriteParam param) throws IOException {
        setThreadLock();
        try {
            writeOnThread(streamMetadata, image, param);
        } finally {
            clearThreadLock();
        }
    }
    private void writeOnThread(IIOMetadata streamMetadata,
                      IIOImage image,
                      ImageWriteParam param) throws IOException {
        if (ios == null) {
            throw new IllegalStateException("Output has not been set!");
        }
        if (image == null) {
            throw new IllegalArgumentException("image is null!");
        }
        if (streamMetadata != null) {
            warningOccurred(WARNING_STREAM_METADATA_IGNORED);
        }
        boolean rasterOnly = image.hasRaster();
        RenderedImage rimage = null;
        if (rasterOnly) {
            srcRas = image.getRaster();
        } else {
            rimage = image.getRenderedImage();
            if (rimage instanceof BufferedImage) {
                srcRas = ((BufferedImage)rimage).getRaster();
            } else if (rimage.getNumXTiles() == 1 &&
                       rimage.getNumYTiles() == 1)
            {
                srcRas = rimage.getTile(rimage.getMinTileX(),
                                        rimage.getMinTileY());
                if (srcRas.getWidth() != rimage.getWidth() ||
                    srcRas.getHeight() != rimage.getHeight())
                {
                    srcRas = srcRas.createChild(srcRas.getMinX(),
                                                srcRas.getMinY(),
                                                rimage.getWidth(),
                                                rimage.getHeight(),
                                                srcRas.getMinX(),
                                                srcRas.getMinY(),
                                                null);
                }
            } else {
                srcRas = rimage.getData();
            }
        }
        int numSrcBands = srcRas.getNumBands();
        indexed = false;
        indexCM = null;
        ColorModel cm = null;
        ColorSpace cs = null;
        isAlphaPremultiplied = false;
        srcCM = null;
        if (!rasterOnly) {
            cm = rimage.getColorModel();
            if (cm != null) {
                cs = cm.getColorSpace();
                if (cm instanceof IndexColorModel) {
                    indexed = true;
                    indexCM = (IndexColorModel) cm;
                    numSrcBands = cm.getNumComponents();
                }
                if (cm.isAlphaPremultiplied()) {
                    isAlphaPremultiplied = true;
                    srcCM = cm;
                }
            }
        }
        srcBands = JPEG.bandOffsets[numSrcBands-1];
        int numBandsUsed = numSrcBands;
        if (param != null) {
            int[] sBands = param.getSourceBands();
            if (sBands != null) {
                if (indexed) {
                    warningOccurred(WARNING_NO_BANDS_ON_INDEXED);
                } else {
                    srcBands = sBands;
                    numBandsUsed = srcBands.length;
                    if (numBandsUsed > numSrcBands) {
                        throw new IIOException
                        ("ImageWriteParam specifies too many source bands");
                    }
                }
            }
        }
        boolean usingBandSubset = (numBandsUsed != numSrcBands);
        boolean fullImage = ((!rasterOnly) && (!usingBandSubset));
        int [] bandSizes = null;
        if (!indexed) {
            bandSizes = srcRas.getSampleModel().getSampleSize();
            if (usingBandSubset) {
                int [] temp = new int [numBandsUsed];
                for (int i = 0; i < numBandsUsed; i++) {
                    temp[i] = bandSizes[srcBands[i]];
                }
                bandSizes = temp;
            }
        } else {
            int [] tempSize = srcRas.getSampleModel().getSampleSize();
            bandSizes = new int [numSrcBands];
            for (int i = 0; i < numSrcBands; i++) {
                bandSizes[i] = tempSize[0];  
            }
        }
        for (int i = 0; i < bandSizes.length; i++) {
            if (bandSizes[i] > 8) {
                throw new IIOException("Sample size must be <= 8");
            }
            if (indexed) {
                bandSizes[i] = 8;
            }
        }
        if (debug) {
            System.out.println("numSrcBands is " + numSrcBands);
            System.out.println("numBandsUsed is " + numBandsUsed);
            System.out.println("usingBandSubset is " + usingBandSubset);
            System.out.println("fullImage is " + fullImage);
            System.out.print("Band sizes:");
            for (int i = 0; i< bandSizes.length; i++) {
                System.out.print(" " + bandSizes[i]);
            }
            System.out.println();
        }
        ImageTypeSpecifier destType = null;
        if (param != null) {
            destType = param.getDestinationType();
            if ((fullImage) && (destType != null)) {
                warningOccurred(WARNING_DEST_IGNORED);
                destType = null;
            }
        }
        sourceXOffset = srcRas.getMinX();
        sourceYOffset = srcRas.getMinY();
        int imageWidth = srcRas.getWidth();
        int imageHeight = srcRas.getHeight();
        sourceWidth = imageWidth;
        sourceHeight = imageHeight;
        int periodX = 1;
        int periodY = 1;
        int gridX = 0;
        int gridY = 0;
        JPEGQTable [] qTables = null;
        JPEGHuffmanTable[] DCHuffmanTables = null;
        JPEGHuffmanTable[] ACHuffmanTables = null;
        boolean optimizeHuffman = false;
        JPEGImageWriteParam jparam = null;
        int progressiveMode = ImageWriteParam.MODE_DISABLED;
        if (param != null) {
            Rectangle sourceRegion = param.getSourceRegion();
            if (sourceRegion != null) {
                Rectangle imageBounds = new Rectangle(sourceXOffset,
                                                      sourceYOffset,
                                                      sourceWidth,
                                                      sourceHeight);
                sourceRegion = sourceRegion.intersection(imageBounds);
                sourceXOffset = sourceRegion.x;
                sourceYOffset = sourceRegion.y;
                sourceWidth = sourceRegion.width;
                sourceHeight = sourceRegion.height;
            }
            if (sourceWidth + sourceXOffset > imageWidth) {
                sourceWidth = imageWidth - sourceXOffset;
            }
            if (sourceHeight + sourceYOffset > imageHeight) {
                sourceHeight = imageHeight - sourceYOffset;
            }
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
            gridX = param.getSubsamplingXOffset();
            gridY = param.getSubsamplingYOffset();
            switch(param.getCompressionMode()) {
            case ImageWriteParam.MODE_DISABLED:
                throw new IIOException("JPEG compression cannot be disabled");
            case ImageWriteParam.MODE_EXPLICIT:
                float quality = param.getCompressionQuality();
                quality = JPEG.convertToLinearQuality(quality);
                qTables = new JPEGQTable[2];
                qTables[0] = JPEGQTable.K1Luminance.getScaledInstance
                    (quality, true);
                qTables[1] = JPEGQTable.K2Chrominance.getScaledInstance
                    (quality, true);
                break;
            case ImageWriteParam.MODE_DEFAULT:
                qTables = new JPEGQTable[2];
                qTables[0] = JPEGQTable.K1Div2Luminance;
                qTables[1] = JPEGQTable.K2Div2Chrominance;
                break;
            }
            progressiveMode = param.getProgressiveMode();
            if (param instanceof JPEGImageWriteParam) {
                jparam = (JPEGImageWriteParam)param;
                optimizeHuffman = jparam.getOptimizeHuffmanTables();
            }
        }
        IIOMetadata mdata = image.getMetadata();
        if (mdata != null) {
            if (mdata instanceof JPEGMetadata) {
                metadata = (JPEGMetadata) mdata;
                if (debug) {
                    System.out.println
                        ("We have metadata, and it's JPEG metadata");
                }
            } else {
                if (!rasterOnly) {
                    ImageTypeSpecifier type = destType;
                    if (type == null) {
                        type = new ImageTypeSpecifier(rimage);
                    }
                    metadata = (JPEGMetadata) convertImageMetadata(mdata,
                                                                   type,
                                                                   param);
                } else {
                    warningOccurred(WARNING_METADATA_NOT_JPEG_FOR_RASTER);
                }
            }
        }
        ignoreJFIF = false;  
        ignoreAdobe = false;  
        newAdobeTransform = JPEG.ADOBE_IMPOSSIBLE;  
        writeDefaultJFIF = false;
        writeAdobe = false;
        int inCsType = JPEG.JCS_UNKNOWN;
        int outCsType = JPEG.JCS_UNKNOWN;
        JFIFMarkerSegment jfif = null;
        AdobeMarkerSegment adobe = null;
        SOFMarkerSegment sof = null;
        if (metadata != null) {
            jfif = (JFIFMarkerSegment) metadata.findMarkerSegment
                (JFIFMarkerSegment.class, true);
            adobe = (AdobeMarkerSegment) metadata.findMarkerSegment
                (AdobeMarkerSegment.class, true);
            sof = (SOFMarkerSegment) metadata.findMarkerSegment
                (SOFMarkerSegment.class, true);
        }
        iccProfile = null;  
        convertTosRGB = false;  
        converted = null;
        if (destType != null) {
            if (numBandsUsed != destType.getNumBands()) {
                throw new IIOException
                    ("Number of source bands != number of destination bands");
            }
            cs = destType.getColorModel().getColorSpace();
            if (metadata != null) {
                checkSOFBands(sof, numBandsUsed);
                checkJFIF(jfif, destType, false);
                if ((jfif != null) && (ignoreJFIF == false)) {
                    if (JPEG.isNonStandardICC(cs)) {
                        iccProfile = ((ICC_ColorSpace) cs).getProfile();
                    }
                }
                checkAdobe(adobe, destType, false);
            } else { 
                if (JPEG.isJFIFcompliant(destType, false)) {
                    writeDefaultJFIF = true;
                    if (JPEG.isNonStandardICC(cs)) {
                        iccProfile = ((ICC_ColorSpace) cs).getProfile();
                    }
                } else {
                    int transform = JPEG.transformForType(destType, false);
                    if (transform != JPEG.ADOBE_IMPOSSIBLE) {
                        writeAdobe = true;
                        newAdobeTransform = transform;
                    }
                }
                metadata = new JPEGMetadata(destType, null, this);
            }
            inCsType = getSrcCSType(destType);
            outCsType = getDefaultDestCSType(destType);
        } else { 
            if (metadata == null) {
                if (fullImage) {  
                    metadata = new JPEGMetadata(new ImageTypeSpecifier(rimage),
                                                param, this);
                    if (metadata.findMarkerSegment
                        (JFIFMarkerSegment.class, true) != null) {
                        cs = rimage.getColorModel().getColorSpace();
                        if (JPEG.isNonStandardICC(cs)) {
                            iccProfile = ((ICC_ColorSpace) cs).getProfile();
                        }
                    }
                    inCsType = getSrcCSType(rimage);
                    outCsType = getDefaultDestCSType(rimage);
                }
            } else { 
                checkSOFBands(sof, numBandsUsed);
                if (fullImage) {  
                    ImageTypeSpecifier inputType =
                        new ImageTypeSpecifier(rimage);
                    inCsType = getSrcCSType(rimage);
                    if (cm != null) {
                        boolean alpha = cm.hasAlpha();
                        switch (cs.getType()) {
                        case ColorSpace.TYPE_GRAY:
                            if (!alpha) {
                                outCsType = JPEG.JCS_GRAYSCALE;
                            } else {
                                if (jfif != null) {
                                    ignoreJFIF = true;
                                    warningOccurred
                                    (WARNING_IMAGE_METADATA_JFIF_MISMATCH);
                                }
                            }
                            if ((adobe != null)
                                && (adobe.transform != JPEG.ADOBE_UNKNOWN)) {
                                newAdobeTransform = JPEG.ADOBE_UNKNOWN;
                                warningOccurred
                                (WARNING_IMAGE_METADATA_ADOBE_MISMATCH);
                            }
                            break;
                        case ColorSpace.TYPE_RGB:
                            if (!alpha) {
                                if (jfif != null) {
                                    outCsType = JPEG.JCS_YCbCr;
                                    if (JPEG.isNonStandardICC(cs)
                                        || ((cs instanceof ICC_ColorSpace)
                                            && (jfif.iccSegment != null))) {
                                        iccProfile =
                                            ((ICC_ColorSpace) cs).getProfile();
                                    }
                                } else if (adobe != null) {
                                    switch (adobe.transform) {
                                    case JPEG.ADOBE_UNKNOWN:
                                        outCsType = JPEG.JCS_RGB;
                                        break;
                                    case JPEG.ADOBE_YCC:
                                        outCsType = JPEG.JCS_YCbCr;
                                        break;
                                    default:
                                        warningOccurred
                                        (WARNING_IMAGE_METADATA_ADOBE_MISMATCH);
                                        newAdobeTransform = JPEG.ADOBE_UNKNOWN;
                                        outCsType = JPEG.JCS_RGB;
                                        break;
                                    }
                                } else {
                                    int outCS = sof.getIDencodedCSType();
                                    if (outCS != JPEG.JCS_UNKNOWN) {
                                        outCsType = outCS;
                                    } else {
                                        boolean subsampled =
                                        isSubsampled(sof.componentSpecs);
                                        if (subsampled) {
                                            outCsType = JPEG.JCS_YCbCr;
                                        } else {
                                            outCsType = JPEG.JCS_RGB;
                                        }
                                    }
                                }
                            } else { 
                                if (jfif != null) {
                                    ignoreJFIF = true;
                                    warningOccurred
                                    (WARNING_IMAGE_METADATA_JFIF_MISMATCH);
                                }
                                if (adobe != null) {
                                    if (adobe.transform
                                        != JPEG.ADOBE_UNKNOWN) {
                                        newAdobeTransform = JPEG.ADOBE_UNKNOWN;
                                        warningOccurred
                                        (WARNING_IMAGE_METADATA_ADOBE_MISMATCH);
                                    }
                                    outCsType = JPEG.JCS_RGBA;
                                } else {
                                    int outCS = sof.getIDencodedCSType();
                                    if (outCS != JPEG.JCS_UNKNOWN) {
                                        outCsType = outCS;
                                    } else {
                                        boolean subsampled =
                                        isSubsampled(sof.componentSpecs);
                                        outCsType = subsampled ?
                                            JPEG.JCS_YCbCrA : JPEG.JCS_RGBA;
                                    }
                                }
                            }
                            break;
                        case ColorSpace.TYPE_3CLR:
                            if (cs == JPEG.JCS.getYCC()) {
                                if (!alpha) {
                                    if (jfif != null) {
                                        convertTosRGB = true;
                                        convertOp =
                                        new ColorConvertOp(cs,
                                                           JPEG.JCS.sRGB,
                                                           null);
                                        outCsType = JPEG.JCS_YCbCr;
                                    } else if (adobe != null) {
                                        if (adobe.transform
                                            != JPEG.ADOBE_YCC) {
                                            newAdobeTransform = JPEG.ADOBE_YCC;
                                            warningOccurred
                                            (WARNING_IMAGE_METADATA_ADOBE_MISMATCH);
                                        }
                                        outCsType = JPEG.JCS_YCC;
                                    } else {
                                        outCsType = JPEG.JCS_YCC;
                                    }
                                } else { 
                                    if (jfif != null) {
                                        ignoreJFIF = true;
                                        warningOccurred
                                        (WARNING_IMAGE_METADATA_JFIF_MISMATCH);
                                    } else if (adobe != null) {
                                        if (adobe.transform
                                            != JPEG.ADOBE_UNKNOWN) {
                                            newAdobeTransform
                                            = JPEG.ADOBE_UNKNOWN;
                                            warningOccurred
                                            (WARNING_IMAGE_METADATA_ADOBE_MISMATCH);
                                        }
                                    }
                                    outCsType = JPEG.JCS_YCCA;
                                }
                            }
                        }
                    }
                } 
            }
        }
        boolean metadataProgressive = false;
        int [] scans = null;
        if (metadata != null) {
            if (sof == null) {
                sof = (SOFMarkerSegment) metadata.findMarkerSegment
                    (SOFMarkerSegment.class, true);
            }
            if ((sof != null) && (sof.tag == JPEG.SOF2)) {
                metadataProgressive = true;
                if (progressiveMode == ImageWriteParam.MODE_COPY_FROM_METADATA) {
                    scans = collectScans(metadata, sof);  
                } else {
                    numScans = 0;
                }
            }
            if (jfif == null) {
                jfif = (JFIFMarkerSegment) metadata.findMarkerSegment
                    (JFIFMarkerSegment.class, true);
            }
        }
        thumbnails = image.getThumbnails();
        int numThumbs = image.getNumThumbnails();
        forceJFIF = false;
        if (!writeDefaultJFIF) {
            if (metadata == null) {
                thumbnails = null;
                if (numThumbs != 0) {
                    warningOccurred(WARNING_IGNORING_THUMBS);
                }
            } else {
                if (fullImage == false) {
                    if (jfif == null) {
                        thumbnails = null;  
                        if (numThumbs != 0) {
                            warningOccurred(WARNING_IGNORING_THUMBS);
                        }
                    }
                } else {  
                    if (jfif == null) {  
                        if ((outCsType == JPEG.JCS_GRAYSCALE)
                            || (outCsType == JPEG.JCS_YCbCr)) {
                            if (numThumbs != 0) {
                                forceJFIF = true;
                                warningOccurred(WARNING_FORCING_JFIF);
                            }
                        } else {  
                            thumbnails = null;
                            if (numThumbs != 0) {
                                warningOccurred(WARNING_IGNORING_THUMBS);
                            }
                        }
                    }
                }
            }
        }
        boolean haveMetadata =
            ((metadata != null) || writeDefaultJFIF || writeAdobe);
        boolean writeDQT = true;
        boolean writeDHT = true;
        DQTMarkerSegment dqt = null;
        DHTMarkerSegment dht = null;
        int restartInterval = 0;
        if (metadata != null) {
            dqt = (DQTMarkerSegment) metadata.findMarkerSegment
                (DQTMarkerSegment.class, true);
            dht = (DHTMarkerSegment) metadata.findMarkerSegment
                (DHTMarkerSegment.class, true);
            DRIMarkerSegment dri =
                (DRIMarkerSegment) metadata.findMarkerSegment
                (DRIMarkerSegment.class, true);
            if (dri != null) {
                restartInterval = dri.restartInterval;
            }
            if (dqt == null) {
                writeDQT = false;
            }
            if (dht == null) {
                writeDHT = false;  
            }
        }
        if (qTables == null) { 
            if (dqt != null) {
                qTables = collectQTablesFromMetadata(metadata);
            } else if (streamQTables != null) {
                qTables = streamQTables;
            } else if ((jparam != null) && (jparam.areTablesSet())) {
                qTables = jparam.getQTables();
            } else {
                qTables = JPEG.getDefaultQTables();
            }
        }
        if (optimizeHuffman == false) {
            if ((dht != null) && (metadataProgressive == false)) {
                DCHuffmanTables = collectHTablesFromMetadata(metadata, true);
                ACHuffmanTables = collectHTablesFromMetadata(metadata, false);
            } else if (streamDCHuffmanTables != null) {
                DCHuffmanTables = streamDCHuffmanTables;
                ACHuffmanTables = streamACHuffmanTables;
            } else if ((jparam != null) && (jparam.areTablesSet())) {
                DCHuffmanTables = jparam.getDCHuffmanTables();
                ACHuffmanTables = jparam.getACHuffmanTables();
            } else {
                DCHuffmanTables = JPEG.getDefaultHuffmanTables(true);
                ACHuffmanTables = JPEG.getDefaultHuffmanTables(false);
            }
        }
        int [] componentIds = new int[numBandsUsed];
        int [] HsamplingFactors = new int[numBandsUsed];
        int [] VsamplingFactors = new int[numBandsUsed];
        int [] QtableSelectors = new int[numBandsUsed];
        for (int i = 0; i < numBandsUsed; i++) {
            componentIds[i] = i+1; 
            HsamplingFactors[i] = 1;
            VsamplingFactors[i] = 1;
            QtableSelectors[i] = 0;
        }
        if (sof != null) {
            for (int i = 0; i < numBandsUsed; i++) {
                if (forceJFIF == false) {  
                    componentIds[i] = sof.componentSpecs[i].componentId;
                }
                HsamplingFactors[i] = sof.componentSpecs[i].HsamplingFactor;
                VsamplingFactors[i] = sof.componentSpecs[i].VsamplingFactor;
                QtableSelectors[i] = sof.componentSpecs[i].QtableSelector;
            }
        }
        sourceXOffset += gridX;
        sourceWidth -= gridX;
        sourceYOffset += gridY;
        sourceHeight -= gridY;
        int destWidth = (sourceWidth + periodX - 1)/periodX;
        int destHeight = (sourceHeight + periodY - 1)/periodY;
        int lineSize = sourceWidth*numBandsUsed;
        DataBufferByte buffer = new DataBufferByte(lineSize);
        int [] bandOffs = JPEG.bandOffsets[numBandsUsed-1];
        raster = Raster.createInterleavedRaster(buffer,
                                                sourceWidth, 1,
                                                lineSize,
                                                numBandsUsed,
                                                bandOffs,
                                                null);
        processImageStarted(currentImage);
        boolean aborted = false;
        if (debug) {
            System.out.println("inCsType: " + inCsType);
            System.out.println("outCsType: " + outCsType);
        }
        aborted = writeImage(structPointer,
                             buffer.getData(),
                             inCsType, outCsType,
                             numBandsUsed,
                             bandSizes,
                             sourceWidth,
                             destWidth, destHeight,
                             periodX, periodY,
                             qTables,
                             writeDQT,
                             DCHuffmanTables,
                             ACHuffmanTables,
                             writeDHT,
                             optimizeHuffman,
                             (progressiveMode
                              != ImageWriteParam.MODE_DISABLED),
                             numScans,
                             scans,
                             componentIds,
                             HsamplingFactors,
                             VsamplingFactors,
                             QtableSelectors,
                             haveMetadata,
                             restartInterval);
        if (aborted) {
            processWriteAborted();
        } else {
            processImageComplete();
        }
        ios.flush();
        currentImage++;  
    }
    public void prepareWriteSequence(IIOMetadata streamMetadata)
        throws IOException {
        setThreadLock();
        try {
            prepareWriteSequenceOnThread(streamMetadata);
        } finally {
            clearThreadLock();
        }
    }
    private void prepareWriteSequenceOnThread(IIOMetadata streamMetadata)
        throws IOException {
        if (ios == null) {
            throw new IllegalStateException("Output has not been set!");
        }
        if (streamMetadata != null) {
            if (streamMetadata instanceof JPEGMetadata) {
                JPEGMetadata jmeta = (JPEGMetadata) streamMetadata;
                if (jmeta.isStream == false) {
                    throw new IllegalArgumentException
                        ("Invalid stream metadata object.");
                }
                if (currentImage != 0) {
                    throw new IIOException
                        ("JPEG Stream metadata must precede all images");
                }
                if (sequencePrepared == true) {
                    throw new IIOException("Stream metadata already written!");
                }
                streamQTables = collectQTablesFromMetadata(jmeta);
                if (debug) {
                    System.out.println("after collecting from stream metadata, "
                                       + "streamQTables.length is "
                                       + streamQTables.length);
                }
                if (streamQTables == null) {
                    streamQTables = JPEG.getDefaultQTables();
                }
                streamDCHuffmanTables =
                    collectHTablesFromMetadata(jmeta, true);
                if (streamDCHuffmanTables == null) {
                    streamDCHuffmanTables = JPEG.getDefaultHuffmanTables(true);
                }
                streamACHuffmanTables =
                    collectHTablesFromMetadata(jmeta, false);
                if (streamACHuffmanTables == null) {
                    streamACHuffmanTables = JPEG.getDefaultHuffmanTables(false);
                }
                writeTables(structPointer,
                            streamQTables,
                            streamDCHuffmanTables,
                            streamACHuffmanTables);
            } else {
                throw new IIOException("Stream metadata must be JPEG metadata");
            }
        }
        sequencePrepared = true;
    }
    public void writeToSequence(IIOImage image, ImageWriteParam param)
        throws IOException {
        setThreadLock();
        try {
            if (sequencePrepared == false) {
                throw new IllegalStateException("sequencePrepared not called!");
            }
            write(null, image, param);
        } finally {
            clearThreadLock();
        }
    }
    public void endWriteSequence() throws IOException {
        setThreadLock();
        try {
            if (sequencePrepared == false) {
                throw new IllegalStateException("sequencePrepared not called!");
            }
            sequencePrepared = false;
        } finally {
            clearThreadLock();
        }
    }
    public synchronized void abort() {
        setThreadLock();
        try {
            super.abort();
            abortWrite(structPointer);
        } finally {
            clearThreadLock();
        }
    }
    private void resetInternalState() {
        resetWriter(structPointer);
        srcRas = null;
        raster = null;
        convertTosRGB = false;
        currentImage = 0;
        numScans = 0;
        metadata = null;
    }
    public void reset() {
        setThreadLock();
        try {
            super.reset();
        } finally {
            clearThreadLock();
        }
    }
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
    void warningOccurred(int code) {
        if ((code < 0) || (code > MAX_WARNING)){
            throw new InternalError("Invalid warning index");
        }
        processWarningOccurred
            (currentImage,
             "com.sun.imageio.plugins.jpeg.JPEGImageWriterResources",
             Integer.toString(code));
    }
    void warningWithMessage(String msg) {
        processWarningOccurred(currentImage, msg);
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
    private void checkSOFBands(SOFMarkerSegment sof, int numBandsUsed)
        throws IIOException {
        if (sof != null) {
            if (sof.componentSpecs.length != numBandsUsed) {
                throw new IIOException
                    ("Metadata components != number of destination bands");
            }
        }
    }
    private void checkJFIF(JFIFMarkerSegment jfif,
                           ImageTypeSpecifier type,
                           boolean input) {
        if (jfif != null) {
            if (!JPEG.isJFIFcompliant(type, input)) {
                ignoreJFIF = true;  
                warningOccurred(input
                                ? WARNING_IMAGE_METADATA_JFIF_MISMATCH
                                : WARNING_DEST_METADATA_JFIF_MISMATCH);
            }
        }
    }
    private void checkAdobe(AdobeMarkerSegment adobe,
                           ImageTypeSpecifier type,
                           boolean input) {
        if (adobe != null) {
            int rightTransform = JPEG.transformForType(type, input);
            if (adobe.transform != rightTransform) {
                warningOccurred(input
                                ? WARNING_IMAGE_METADATA_ADOBE_MISMATCH
                                : WARNING_DEST_METADATA_ADOBE_MISMATCH);
                if (rightTransform == JPEG.ADOBE_IMPOSSIBLE) {
                    ignoreAdobe = true;
                } else {
                    newAdobeTransform = rightTransform;
                }
            }
        }
    }
    private int [] collectScans(JPEGMetadata metadata,
                                SOFMarkerSegment sof) {
        List segments = new ArrayList();
        int SCAN_SIZE = 9;
        int MAX_COMPS_PER_SCAN = 4;
        for (Iterator iter = metadata.markerSequence.iterator();
             iter.hasNext();) {
            MarkerSegment seg = (MarkerSegment) iter.next();
            if (seg instanceof SOSMarkerSegment) {
                segments.add(seg);
            }
        }
        int [] retval = null;
        numScans = 0;
        if (!segments.isEmpty()) {
            numScans = segments.size();
            retval = new int [numScans*SCAN_SIZE];
            int index = 0;
            for (int i = 0; i < numScans; i++) {
                SOSMarkerSegment sos = (SOSMarkerSegment) segments.get(i);
                retval[index++] = sos.componentSpecs.length; 
                for (int j = 0; j < MAX_COMPS_PER_SCAN; j++) {
                    if (j < sos.componentSpecs.length) {
                        int compSel = sos.componentSpecs[j].componentSelector;
                        for (int k = 0; k < sof.componentSpecs.length; k++) {
                            if (compSel == sof.componentSpecs[k].componentId) {
                                retval[index++] = k;
                                break; 
                            }
                        }
                    } else {
                        retval[index++] = 0;
                    }
                }
                retval[index++] = sos.startSpectralSelection;
                retval[index++] = sos.endSpectralSelection;
                retval[index++] = sos.approxHigh;
                retval[index++] = sos.approxLow;
            }
        }
        return retval;
    }
    private JPEGQTable [] collectQTablesFromMetadata
        (JPEGMetadata metadata) {
        ArrayList tables = new ArrayList();
        Iterator iter = metadata.markerSequence.iterator();
        while (iter.hasNext()) {
            MarkerSegment seg = (MarkerSegment) iter.next();
            if (seg instanceof DQTMarkerSegment) {
                DQTMarkerSegment dqt =
                    (DQTMarkerSegment) seg;
                tables.addAll(dqt.tables);
            }
        }
        JPEGQTable [] retval = null;
        if (tables.size() != 0) {
            retval = new JPEGQTable[tables.size()];
            for (int i = 0; i < retval.length; i++) {
                retval[i] =
                    new JPEGQTable(((DQTMarkerSegment.Qtable)tables.get(i)).data);
            }
        }
        return retval;
    }
    private JPEGHuffmanTable[] collectHTablesFromMetadata
        (JPEGMetadata metadata, boolean wantDC) throws IIOException {
        ArrayList tables = new ArrayList();
        Iterator iter = metadata.markerSequence.iterator();
        while (iter.hasNext()) {
            MarkerSegment seg = (MarkerSegment) iter.next();
            if (seg instanceof DHTMarkerSegment) {
                DHTMarkerSegment dht =
                    (DHTMarkerSegment) seg;
                for (int i = 0; i < dht.tables.size(); i++) {
                    DHTMarkerSegment.Htable htable =
                        (DHTMarkerSegment.Htable) dht.tables.get(i);
                    if (htable.tableClass == (wantDC ? 0 : 1)) {
                        tables.add(htable);
                    }
                }
            }
        }
        JPEGHuffmanTable [] retval = null;
        if (tables.size() != 0) {
            DHTMarkerSegment.Htable [] htables =
                new DHTMarkerSegment.Htable[tables.size()];
            tables.toArray(htables);
            retval = new JPEGHuffmanTable[tables.size()];
            for (int i = 0; i < retval.length; i++) {
                retval[i] = null;
                for (int j = 0; j < tables.size(); j++) {
                    if (htables[j].tableID == i) {
                        if (retval[i] != null) {
                            throw new IIOException("Metadata has duplicate Htables!");
                        }
                        retval[i] = new JPEGHuffmanTable(htables[j].numCodes,
                                                         htables[j].values);
                    }
                }
            }
        }
        return retval;
    }
    private int getSrcCSType(ImageTypeSpecifier type) {
         return getSrcCSType(type.getColorModel());
    }
    private int getSrcCSType(RenderedImage rimage) {
        return getSrcCSType(rimage.getColorModel());
    }
    private int getSrcCSType(ColorModel cm) {
        int retval = JPEG.JCS_UNKNOWN;
        if (cm != null) {
            boolean alpha = cm.hasAlpha();
            ColorSpace cs = cm.getColorSpace();
            switch (cs.getType()) {
            case ColorSpace.TYPE_GRAY:
                retval = JPEG.JCS_GRAYSCALE;
                break;
            case ColorSpace.TYPE_RGB:
                if (alpha) {
                    retval = JPEG.JCS_RGBA;
                } else {
                    retval = JPEG.JCS_RGB;
                }
                break;
            case ColorSpace.TYPE_YCbCr:
                if (alpha) {
                    retval = JPEG.JCS_YCbCrA;
                } else {
                    retval = JPEG.JCS_YCbCr;
                }
                break;
            case ColorSpace.TYPE_3CLR:
                if (cs == JPEG.JCS.getYCC()) {
                    if (alpha) {
                        retval = JPEG.JCS_YCCA;
                    } else {
                        retval = JPEG.JCS_YCC;
                    }
                }
            case ColorSpace.TYPE_CMYK:
                retval = JPEG.JCS_CMYK;
                break;
            }
        }
        return retval;
    }
    private int getDestCSType(ImageTypeSpecifier destType) {
        ColorModel cm = destType.getColorModel();
        boolean alpha = cm.hasAlpha();
        ColorSpace cs = cm.getColorSpace();
        int retval = JPEG.JCS_UNKNOWN;
        switch (cs.getType()) {
        case ColorSpace.TYPE_GRAY:
                retval = JPEG.JCS_GRAYSCALE;
                break;
            case ColorSpace.TYPE_RGB:
                if (alpha) {
                    retval = JPEG.JCS_RGBA;
                } else {
                    retval = JPEG.JCS_RGB;
                }
                break;
            case ColorSpace.TYPE_YCbCr:
                if (alpha) {
                    retval = JPEG.JCS_YCbCrA;
                } else {
                    retval = JPEG.JCS_YCbCr;
                }
                break;
            case ColorSpace.TYPE_3CLR:
                if (cs == JPEG.JCS.getYCC()) {
                    if (alpha) {
                        retval = JPEG.JCS_YCCA;
                    } else {
                        retval = JPEG.JCS_YCC;
                    }
                }
            case ColorSpace.TYPE_CMYK:
                retval = JPEG.JCS_CMYK;
                break;
            }
        return retval;
        }
    private int getDefaultDestCSType(ImageTypeSpecifier type) {
        return getDefaultDestCSType(type.getColorModel());
    }
    private int getDefaultDestCSType(RenderedImage rimage) {
        return getDefaultDestCSType(rimage.getColorModel());
    }
    private int getDefaultDestCSType(ColorModel cm) {
        int retval = JPEG.JCS_UNKNOWN;
        if (cm != null) {
            boolean alpha = cm.hasAlpha();
            ColorSpace cs = cm.getColorSpace();
            switch (cs.getType()) {
            case ColorSpace.TYPE_GRAY:
                retval = JPEG.JCS_GRAYSCALE;
                break;
            case ColorSpace.TYPE_RGB:
                if (alpha) {
                    retval = JPEG.JCS_YCbCrA;
                } else {
                    retval = JPEG.JCS_YCbCr;
                }
                break;
            case ColorSpace.TYPE_YCbCr:
                if (alpha) {
                    retval = JPEG.JCS_YCbCrA;
                } else {
                    retval = JPEG.JCS_YCbCr;
                }
                break;
            case ColorSpace.TYPE_3CLR:
                if (cs == JPEG.JCS.getYCC()) {
                    if (alpha) {
                        retval = JPEG.JCS_YCCA;
                    } else {
                        retval = JPEG.JCS_YCC;
                    }
                }
            case ColorSpace.TYPE_CMYK:
                retval = JPEG.JCS_YCCK;
                break;
            }
        }
        return retval;
    }
    private boolean isSubsampled(SOFMarkerSegment.ComponentSpec [] specs) {
        int hsamp0 = specs[0].HsamplingFactor;
        int vsamp0 = specs[0].VsamplingFactor;
        for (int i = 1; i < specs.length; i++) {
            if ((specs[i].HsamplingFactor != hsamp0) ||
                (specs[i].HsamplingFactor != hsamp0))
                return true;
        }
        return false;
    }
    private static native void initWriterIDs(Class iosClass,
                                             Class qTableClass,
                                             Class huffClass);
    private native long initJPEGImageWriter();
    private native void setDest(long structPointer,
                                ImageOutputStream ios);
    private native boolean writeImage(long structPointer,
                                      byte [] data,
                                      int inCsType, int outCsType,
                                      int numBands,
                                      int [] bandSizes,
                                      int srcWidth,
                                      int destWidth, int destHeight,
                                      int stepX, int stepY,
                                      JPEGQTable [] qtables,
                                      boolean writeDQT,
                                      JPEGHuffmanTable[] DCHuffmanTables,
                                      JPEGHuffmanTable[] ACHuffmanTables,
                                      boolean writeDHT,
                                      boolean optimizeHuffman,
                                      boolean progressive,
                                      int numScans,
                                      int [] scans,
                                      int [] componentIds,
                                      int [] HsamplingFactors,
                                      int [] VsamplingFactors,
                                      int [] QtableSelectors,
                                      boolean haveMetadata,
                                      int restartInterval);
    private void writeMetadata() throws IOException {
        if (metadata == null) {
            if (writeDefaultJFIF) {
                JFIFMarkerSegment.writeDefaultJFIF(ios,
                                                   thumbnails,
                                                   iccProfile,
                                                   this);
            }
            if (writeAdobe) {
                AdobeMarkerSegment.writeAdobeSegment(ios, newAdobeTransform);
            }
        } else {
            metadata.writeToStream(ios,
                                   ignoreJFIF,
                                   forceJFIF,
                                   thumbnails,
                                   iccProfile,
                                   ignoreAdobe,
                                   newAdobeTransform,
                                   this);
        }
    }
    private native void writeTables(long structPointer,
                                    JPEGQTable [] qtables,
                                    JPEGHuffmanTable[] DCHuffmanTables,
                                    JPEGHuffmanTable[] ACHuffmanTables);
    private void grabPixels(int y) {
        Raster sourceLine = null;
        if (indexed) {
            sourceLine = srcRas.createChild(sourceXOffset,
                                            sourceYOffset+y,
                                            sourceWidth, 1,
                                            0, 0,
                                            new int [] {0});
            boolean forceARGB =
                (indexCM.getTransparency() != Transparency.OPAQUE);
            BufferedImage temp = indexCM.convertToIntDiscrete(sourceLine,
                                                              forceARGB);
            sourceLine = temp.getRaster();
        } else {
            sourceLine = srcRas.createChild(sourceXOffset,
                                            sourceYOffset+y,
                                            sourceWidth, 1,
                                            0, 0,
                                            srcBands);
        }
        if (convertTosRGB) {
            if (debug) {
                System.out.println("Converting to sRGB");
            }
            converted = convertOp.filter(sourceLine, converted);
            sourceLine = converted;
        }
        if (isAlphaPremultiplied) {
            WritableRaster wr = sourceLine.createCompatibleWritableRaster();
            int[] data = null;
            data = sourceLine.getPixels(sourceLine.getMinX(), sourceLine.getMinY(),
                                        sourceLine.getWidth(), sourceLine.getHeight(),
                                        data);
            wr.setPixels(sourceLine.getMinX(), sourceLine.getMinY(),
                         sourceLine.getWidth(), sourceLine.getHeight(),
                         data);
            srcCM.coerceData(wr, false);
            sourceLine = wr.createChild(wr.getMinX(), wr.getMinY(),
                                        wr.getWidth(), wr.getHeight(),
                                        0, 0,
                                        srcBands);
        }
        raster.setRect(sourceLine);
        if ((y > 7) && (y%8 == 0)) {  
            processImageProgress((float) y / (float) sourceHeight * 100.0F);
        }
    }
    private native void abortWrite(long structPointer);
    private native void resetWriter(long structPointer);
    private static native void disposeWriter(long structPointer);
    private static class JPEGWriterDisposerRecord implements DisposerRecord {
        private long pData;
        public JPEGWriterDisposerRecord(long pData) {
            this.pData = pData;
        }
        public synchronized void dispose() {
            if (pData != 0) {
                disposeWriter(pData);
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
            throw new IllegalStateException("Attempt to clear thread lock form wrong thread. " +
                                            "Locked thread: " + theThread +
                                            "; current thread: " + currThread);
        }
        theLockCount --;
        if (theLockCount == 0) {
            theThread = null;
        }
    }
}
