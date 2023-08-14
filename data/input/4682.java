public class GIFImageWriter extends ImageWriter {
    private static final boolean DEBUG = false; 
    static final String STANDARD_METADATA_NAME =
    IIOMetadataFormatImpl.standardMetadataFormatName;
    static final String STREAM_METADATA_NAME =
    GIFWritableStreamMetadata.NATIVE_FORMAT_NAME;
    static final String IMAGE_METADATA_NAME =
    GIFWritableImageMetadata.NATIVE_FORMAT_NAME;
    private ImageOutputStream stream = null;
    private boolean isWritingSequence = false;
    private boolean wroteSequenceHeader = false;
    private GIFWritableStreamMetadata theStreamMetadata = null;
    private int imageIndex = 0;
    private static int getNumBits(int value) throws IOException {
        int numBits;
        switch(value) {
        case 2:
            numBits = 1;
            break;
        case 4:
            numBits = 2;
            break;
        case 8:
            numBits = 3;
            break;
        case 16:
            numBits = 4;
            break;
        case 32:
            numBits = 5;
            break;
        case 64:
            numBits = 6;
            break;
        case 128:
            numBits = 7;
            break;
        case 256:
            numBits = 8;
            break;
        default:
            throw new IOException("Bad palette length: "+value+"!");
        }
        return numBits;
    }
    private static void computeRegions(Rectangle sourceBounds,
                                       Dimension destSize,
                                       ImageWriteParam p) {
        ImageWriteParam param;
        int periodX = 1;
        int periodY = 1;
        if (p != null) {
            int[] sourceBands = p.getSourceBands();
            if (sourceBands != null &&
                (sourceBands.length != 1 ||
                 sourceBands[0] != 0)) {
                throw new IllegalArgumentException("Cannot sub-band image!");
            }
            Rectangle sourceRegion = p.getSourceRegion();
            if (sourceRegion != null) {
                sourceRegion = sourceRegion.intersection(sourceBounds);
                sourceBounds.setBounds(sourceRegion);
            }
            int gridX = p.getSubsamplingXOffset();
            int gridY = p.getSubsamplingYOffset();
            sourceBounds.x += gridX;
            sourceBounds.y += gridY;
            sourceBounds.width -= gridX;
            sourceBounds.height -= gridY;
            periodX = p.getSourceXSubsampling();
            periodY = p.getSourceYSubsampling();
        }
        destSize.setSize((sourceBounds.width + periodX - 1)/periodX,
                         (sourceBounds.height + periodY - 1)/periodY);
        if (destSize.width <= 0 || destSize.height <= 0) {
            throw new IllegalArgumentException("Empty source region!");
        }
    }
    private static byte[] createColorTable(ColorModel colorModel,
                                           SampleModel sampleModel)
    {
        byte[] colorTable;
        if (colorModel instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)colorModel;
            int mapSize = icm.getMapSize();
            int ctSize = getGifPaletteSize(mapSize);
            byte[] reds = new byte[ctSize];
            byte[] greens = new byte[ctSize];
            byte[] blues = new byte[ctSize];
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(blues);
            for (int i = mapSize; i < ctSize; i++) {
                reds[i] = reds[0];
                greens[i] = greens[0];
                blues[i] = blues[0];
            }
            colorTable = new byte[3*ctSize];
            int idx = 0;
            for (int i = 0; i < ctSize; i++) {
                colorTable[idx++] = reds[i];
                colorTable[idx++] = greens[i];
                colorTable[idx++] = blues[i];
            }
        } else if (sampleModel.getNumBands() == 1) {
            int numBits = sampleModel.getSampleSize()[0];
            if (numBits > 8) {
                numBits = 8;
            }
            int colorTableLength = 3*(1 << numBits);
            colorTable = new byte[colorTableLength];
            for (int i = 0; i < colorTableLength; i++) {
                colorTable[i] = (byte)(i/3);
            }
        } else {
            colorTable = null;
        }
        return colorTable;
    }
    private static int getGifPaletteSize(int x) {
        if (x <= 2) {
            return 2;
        }
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >> 16);
        return x + 1;
    }
    public GIFImageWriter(GIFImageWriterSpi originatingProvider) {
        super(originatingProvider);
        if (DEBUG) {
            System.err.println("GIF Writer is created");
        }
    }
    public boolean canWriteSequence() {
        return true;
    }
    private void convertMetadata(String metadataFormatName,
                                 IIOMetadata inData,
                                 IIOMetadata outData) {
        String formatName = null;
        String nativeFormatName = inData.getNativeMetadataFormatName();
        if (nativeFormatName != null &&
            nativeFormatName.equals(metadataFormatName)) {
            formatName = metadataFormatName;
        } else {
            String[] extraFormatNames = inData.getExtraMetadataFormatNames();
            if (extraFormatNames != null) {
                for (int i = 0; i < extraFormatNames.length; i++) {
                    if (extraFormatNames[i].equals(metadataFormatName)) {
                        formatName = metadataFormatName;
                        break;
                    }
                }
            }
        }
        if (formatName == null &&
            inData.isStandardMetadataFormatSupported()) {
            formatName = STANDARD_METADATA_NAME;
        }
        if (formatName != null) {
            try {
                Node root = inData.getAsTree(formatName);
                outData.mergeTree(formatName, root);
            } catch(IIOInvalidTreeException e) {
            }
        }
    }
    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
                                             ImageWriteParam param) {
        if (inData == null) {
            throw new IllegalArgumentException("inData == null!");
        }
        IIOMetadata sm = getDefaultStreamMetadata(param);
        convertMetadata(STREAM_METADATA_NAME, inData, sm);
        return sm;
    }
    public IIOMetadata convertImageMetadata(IIOMetadata inData,
                                            ImageTypeSpecifier imageType,
                                            ImageWriteParam param) {
        if (inData == null) {
            throw new IllegalArgumentException("inData == null!");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("imageType == null!");
        }
        GIFWritableImageMetadata im =
            (GIFWritableImageMetadata)getDefaultImageMetadata(imageType,
                                                              param);
        boolean isProgressive = im.interlaceFlag;
        convertMetadata(IMAGE_METADATA_NAME, inData, im);
        if (param != null && param.canWriteProgressive() &&
            param.getProgressiveMode() != param.MODE_COPY_FROM_METADATA) {
            im.interlaceFlag = isProgressive;
        }
        return im;
    }
    public void endWriteSequence() throws IOException {
        if (stream == null) {
            throw new IllegalStateException("output == null!");
        }
        if (!isWritingSequence) {
            throw new IllegalStateException("prepareWriteSequence() was not invoked!");
        }
        writeTrailer();
        resetLocal();
    }
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
                                               ImageWriteParam param) {
        GIFWritableImageMetadata imageMetadata =
            new GIFWritableImageMetadata();
        SampleModel sampleModel = imageType.getSampleModel();
        Rectangle sourceBounds = new Rectangle(sampleModel.getWidth(),
                                               sampleModel.getHeight());
        Dimension destSize = new Dimension();
        computeRegions(sourceBounds, destSize, param);
        imageMetadata.imageWidth = destSize.width;
        imageMetadata.imageHeight = destSize.height;
        if (param != null && param.canWriteProgressive() &&
            param.getProgressiveMode() == ImageWriteParam.MODE_DISABLED) {
            imageMetadata.interlaceFlag = false;
        } else {
            imageMetadata.interlaceFlag = true;
        }
        ColorModel colorModel = imageType.getColorModel();
        imageMetadata.localColorTable =
            createColorTable(colorModel, sampleModel);
        if (colorModel instanceof IndexColorModel) {
            int transparentIndex =
                ((IndexColorModel)colorModel).getTransparentPixel();
            if (transparentIndex != -1) {
                imageMetadata.transparentColorFlag = true;
                imageMetadata.transparentColorIndex = transparentIndex;
            }
        }
        return imageMetadata;
    }
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        GIFWritableStreamMetadata streamMetadata =
            new GIFWritableStreamMetadata();
        streamMetadata.version = "89a";
        return streamMetadata;
    }
    public ImageWriteParam getDefaultWriteParam() {
        return new GIFImageWriteParam(getLocale());
    }
    public void prepareWriteSequence(IIOMetadata streamMetadata)
      throws IOException {
        if (stream == null) {
            throw new IllegalStateException("Output is not set.");
        }
        resetLocal();
        if (streamMetadata == null) {
            this.theStreamMetadata =
                (GIFWritableStreamMetadata)getDefaultStreamMetadata(null);
        } else {
            this.theStreamMetadata = new GIFWritableStreamMetadata();
            convertMetadata(STREAM_METADATA_NAME, streamMetadata,
                            theStreamMetadata);
        }
        this.isWritingSequence = true;
    }
    public void reset() {
        super.reset();
        resetLocal();
    }
    private void resetLocal() {
        this.isWritingSequence = false;
        this.wroteSequenceHeader = false;
        this.theStreamMetadata = null;
        this.imageIndex = 0;
    }
    public void setOutput(Object output) {
        super.setOutput(output);
        if (output != null) {
            if (!(output instanceof ImageOutputStream)) {
                throw new
                    IllegalArgumentException("output is not an ImageOutputStream");
            }
            this.stream = (ImageOutputStream)output;
            this.stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else {
            this.stream = null;
        }
    }
    public void write(IIOMetadata sm,
                      IIOImage iioimage,
                      ImageWriteParam p) throws IOException {
        if (stream == null) {
            throw new IllegalStateException("output == null!");
        }
        if (iioimage == null) {
            throw new IllegalArgumentException("iioimage == null!");
        }
        if (iioimage.hasRaster()) {
            throw new UnsupportedOperationException("canWriteRasters() == false!");
        }
        resetLocal();
        GIFWritableStreamMetadata streamMetadata;
        if (sm == null) {
            streamMetadata =
                (GIFWritableStreamMetadata)getDefaultStreamMetadata(p);
        } else {
            streamMetadata =
                (GIFWritableStreamMetadata)convertStreamMetadata(sm, p);
        }
        write(true, true, streamMetadata, iioimage, p);
    }
    public void writeToSequence(IIOImage image, ImageWriteParam param)
      throws IOException {
        if (stream == null) {
            throw new IllegalStateException("output == null!");
        }
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        if (image.hasRaster()) {
            throw new UnsupportedOperationException("canWriteRasters() == false!");
        }
        if (!isWritingSequence) {
            throw new IllegalStateException("prepareWriteSequence() was not invoked!");
        }
        write(!wroteSequenceHeader, false, theStreamMetadata,
              image, param);
        if (!wroteSequenceHeader) {
            wroteSequenceHeader = true;
        }
        this.imageIndex++;
    }
    private boolean needToCreateIndex(RenderedImage image) {
        SampleModel sampleModel = image.getSampleModel();
        ColorModel colorModel = image.getColorModel();
        return sampleModel.getNumBands() != 1 ||
            sampleModel.getSampleSize()[0] > 8 ||
            colorModel.getComponentSize()[0] > 8;
    }
    private void write(boolean writeHeader,
                       boolean writeTrailer,
                       IIOMetadata sm,
                       IIOImage iioimage,
                       ImageWriteParam p) throws IOException {
        clearAbortRequest();
        RenderedImage image = iioimage.getRenderedImage();
        if (needToCreateIndex(image)) {
            image = PaletteBuilder.createIndexedImage(image);
            iioimage.setRenderedImage(image);
        }
        ColorModel colorModel = image.getColorModel();
        SampleModel sampleModel = image.getSampleModel();
        Rectangle sourceBounds = new Rectangle(image.getMinX(),
                                               image.getMinY(),
                                               image.getWidth(),
                                               image.getHeight());
        Dimension destSize = new Dimension();
        computeRegions(sourceBounds, destSize, p);
        GIFWritableImageMetadata imageMetadata = null;
        if (iioimage.getMetadata() != null) {
            imageMetadata = new GIFWritableImageMetadata();
            convertMetadata(IMAGE_METADATA_NAME, iioimage.getMetadata(),
                            imageMetadata);
            if (imageMetadata.localColorTable == null) {
                imageMetadata.localColorTable =
                    createColorTable(colorModel, sampleModel);
                if (colorModel instanceof IndexColorModel) {
                    IndexColorModel icm =
                        (IndexColorModel)colorModel;
                    int index = icm.getTransparentPixel();
                    imageMetadata.transparentColorFlag = (index != -1);
                    if (imageMetadata.transparentColorFlag) {
                        imageMetadata.transparentColorIndex = index;
                    }
                }
            }
        }
        byte[] globalColorTable = null;
        if (writeHeader) {
            if (sm == null) {
                throw new IllegalArgumentException("Cannot write null header!");
            }
            GIFWritableStreamMetadata streamMetadata =
                (GIFWritableStreamMetadata)sm;
            if (streamMetadata.version == null) {
                streamMetadata.version = "89a";
            }
            if (streamMetadata.logicalScreenWidth ==
                GIFMetadata.UNDEFINED_INTEGER_VALUE)
            {
                streamMetadata.logicalScreenWidth = destSize.width;
            }
            if (streamMetadata.logicalScreenHeight ==
                GIFMetadata.UNDEFINED_INTEGER_VALUE)
            {
                streamMetadata.logicalScreenHeight = destSize.height;
            }
            if (streamMetadata.colorResolution ==
                GIFMetadata.UNDEFINED_INTEGER_VALUE)
            {
                streamMetadata.colorResolution = colorModel != null ?
                    colorModel.getComponentSize()[0] :
                    sampleModel.getSampleSize()[0];
            }
            if (streamMetadata.globalColorTable == null) {
                if (isWritingSequence && imageMetadata != null &&
                    imageMetadata.localColorTable != null) {
                    streamMetadata.globalColorTable =
                        imageMetadata.localColorTable;
                } else if (imageMetadata == null ||
                           imageMetadata.localColorTable == null) {
                    streamMetadata.globalColorTable =
                        createColorTable(colorModel, sampleModel);
                }
            }
            globalColorTable = streamMetadata.globalColorTable;
            int bitsPerPixel;
            if (globalColorTable != null) {
                bitsPerPixel = getNumBits(globalColorTable.length/3);
            } else if (imageMetadata != null &&
                       imageMetadata.localColorTable != null) {
                bitsPerPixel =
                    getNumBits(imageMetadata.localColorTable.length/3);
            } else {
                bitsPerPixel = sampleModel.getSampleSize(0);
            }
            writeHeader(streamMetadata, bitsPerPixel);
        } else if (isWritingSequence) {
            globalColorTable = theStreamMetadata.globalColorTable;
        } else {
            throw new IllegalArgumentException("Must write header for single image!");
        }
        writeImage(iioimage.getRenderedImage(), imageMetadata, p,
                   globalColorTable, sourceBounds, destSize);
        if (writeTrailer) {
            writeTrailer();
        }
    }
    private void writeImage(RenderedImage image,
                            GIFWritableImageMetadata imageMetadata,
                            ImageWriteParam param, byte[] globalColorTable,
                            Rectangle sourceBounds, Dimension destSize)
      throws IOException {
        ColorModel colorModel = image.getColorModel();
        SampleModel sampleModel = image.getSampleModel();
        boolean writeGraphicsControlExtension;
        if (imageMetadata == null) {
            imageMetadata = (GIFWritableImageMetadata)getDefaultImageMetadata(
                new ImageTypeSpecifier(image), param);
            writeGraphicsControlExtension = imageMetadata.transparentColorFlag;
        } else {
            NodeList list = null;
            try {
                IIOMetadataNode root = (IIOMetadataNode)
                    imageMetadata.getAsTree(IMAGE_METADATA_NAME);
                list = root.getElementsByTagName("GraphicControlExtension");
            } catch(IllegalArgumentException iae) {
            }
            writeGraphicsControlExtension =
                list != null && list.getLength() > 0;
            if (param != null && param.canWriteProgressive()) {
                if (param.getProgressiveMode() ==
                    ImageWriteParam.MODE_DISABLED) {
                    imageMetadata.interlaceFlag = false;
                } else if (param.getProgressiveMode() ==
                           ImageWriteParam.MODE_DEFAULT) {
                    imageMetadata.interlaceFlag = true;
                }
            }
        }
        if (Arrays.equals(globalColorTable, imageMetadata.localColorTable)) {
            imageMetadata.localColorTable = null;
        }
        imageMetadata.imageWidth = destSize.width;
        imageMetadata.imageHeight = destSize.height;
        if (writeGraphicsControlExtension) {
            writeGraphicControlExtension(imageMetadata);
        }
        writePlainTextExtension(imageMetadata);
        writeApplicationExtension(imageMetadata);
        writeCommentExtension(imageMetadata);
        int bitsPerPixel =
            getNumBits(imageMetadata.localColorTable == null ?
                       (globalColorTable == null ?
                        sampleModel.getSampleSize(0) :
                        globalColorTable.length/3) :
                       imageMetadata.localColorTable.length/3);
        writeImageDescriptor(imageMetadata, bitsPerPixel);
        writeRasterData(image, sourceBounds, destSize,
                        param, imageMetadata.interlaceFlag);
    }
    private void writeRows(RenderedImage image, LZWCompressor compressor,
                           int sx, int sdx, int sy, int sdy, int sw,
                           int dy, int ddy, int dw, int dh,
                           int numRowsWritten, int progressReportRowPeriod)
      throws IOException {
        if (DEBUG) System.out.println("Writing unoptimized");
        int[] sbuf = new int[sw];
        byte[] dbuf = new byte[dw];
        Raster raster =
            image.getNumXTiles() == 1 && image.getNumYTiles() == 1 ?
            image.getTile(0, 0) : image.getData();
        for (int y = dy; y < dh; y += ddy) {
            if (numRowsWritten % progressReportRowPeriod == 0) {
                if (abortRequested()) {
                    processWriteAborted();
                    return;
                }
                processImageProgress((numRowsWritten*100.0F)/dh);
            }
            raster.getSamples(sx, sy, sw, 1, 0, sbuf);
            for (int i = 0, j = 0; i < dw; i++, j += sdx) {
                dbuf[i] = (byte)sbuf[j];
            }
            compressor.compress(dbuf, 0, dw);
            numRowsWritten++;
            sy += sdy;
        }
    }
    private void writeRowsOpt(byte[] data, int offset, int lineStride,
                              LZWCompressor compressor,
                              int dy, int ddy, int dw, int dh,
                              int numRowsWritten, int progressReportRowPeriod)
      throws IOException {
        if (DEBUG) System.out.println("Writing optimized");
        offset += dy*lineStride;
        lineStride *= ddy;
        for (int y = dy; y < dh; y += ddy) {
            if (numRowsWritten % progressReportRowPeriod == 0) {
                if (abortRequested()) {
                    processWriteAborted();
                    return;
                }
                processImageProgress((numRowsWritten*100.0F)/dh);
            }
            compressor.compress(data, offset, dw);
            numRowsWritten++;
            offset += lineStride;
        }
    }
    private void writeRasterData(RenderedImage image,
                                 Rectangle sourceBounds,
                                 Dimension destSize,
                                 ImageWriteParam param,
                                 boolean interlaceFlag) throws IOException {
        int sourceXOffset = sourceBounds.x;
        int sourceYOffset = sourceBounds.y;
        int sourceWidth = sourceBounds.width;
        int sourceHeight = sourceBounds.height;
        int destWidth = destSize.width;
        int destHeight = destSize.height;
        int periodX;
        int periodY;
        if (param == null) {
            periodX = 1;
            periodY = 1;
        } else {
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
        }
        SampleModel sampleModel = image.getSampleModel();
        int bitsPerPixel = sampleModel.getSampleSize()[0];
        int initCodeSize = bitsPerPixel;
        if (initCodeSize == 1) {
            initCodeSize++;
        }
        stream.write(initCodeSize);
        LZWCompressor compressor =
            new LZWCompressor(stream, initCodeSize, false);
        boolean isOptimizedCase =
            periodX == 1 && periodY == 1 &&
            image.getNumXTiles() == 1 && image.getNumYTiles() == 1 &&
            sampleModel instanceof ComponentSampleModel &&
            image.getTile(0, 0) instanceof ByteComponentRaster &&
            image.getTile(0, 0).getDataBuffer() instanceof DataBufferByte;
        int numRowsWritten = 0;
        int progressReportRowPeriod = Math.max(destHeight/20, 1);
        processImageStarted(imageIndex);
        if (interlaceFlag) {
            if (DEBUG) System.out.println("Writing interlaced");
            if (isOptimizedCase) {
                ByteComponentRaster tile =
                    (ByteComponentRaster)image.getTile(0, 0);
                byte[] data = ((DataBufferByte)tile.getDataBuffer()).getData();
                ComponentSampleModel csm =
                    (ComponentSampleModel)tile.getSampleModel();
                int offset = csm.getOffset(sourceXOffset, sourceYOffset, 0);
                offset += tile.getDataOffset(0);
                int lineStride = csm.getScanlineStride();
                writeRowsOpt(data, offset, lineStride, compressor,
                             0, 8, destWidth, destHeight,
                             numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += destHeight/8;
                writeRowsOpt(data, offset, lineStride, compressor,
                             4, 8, destWidth, destHeight,
                             numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += (destHeight - 4)/8;
                writeRowsOpt(data, offset, lineStride, compressor,
                             2, 4, destWidth, destHeight,
                             numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += (destHeight - 2)/4;
                writeRowsOpt(data, offset, lineStride, compressor,
                             1, 2, destWidth, destHeight,
                             numRowsWritten, progressReportRowPeriod);
            } else {
                writeRows(image, compressor,
                          sourceXOffset, periodX,
                          sourceYOffset, 8*periodY,
                          sourceWidth,
                          0, 8, destWidth, destHeight,
                          numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += destHeight/8;
                writeRows(image, compressor, sourceXOffset, periodX,
                          sourceYOffset + 4*periodY, 8*periodY,
                          sourceWidth,
                          4, 8, destWidth, destHeight,
                          numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += (destHeight - 4)/8;
                writeRows(image, compressor, sourceXOffset, periodX,
                          sourceYOffset + 2*periodY, 4*periodY,
                          sourceWidth,
                          2, 4, destWidth, destHeight,
                          numRowsWritten, progressReportRowPeriod);
                if (abortRequested()) {
                    return;
                }
                numRowsWritten += (destHeight - 2)/4;
                writeRows(image, compressor, sourceXOffset, periodX,
                          sourceYOffset + periodY, 2*periodY,
                          sourceWidth,
                          1, 2, destWidth, destHeight,
                          numRowsWritten, progressReportRowPeriod);
            }
        } else {
            if (DEBUG) System.out.println("Writing non-interlaced");
            if (isOptimizedCase) {
                Raster tile = image.getTile(0, 0);
                byte[] data = ((DataBufferByte)tile.getDataBuffer()).getData();
                ComponentSampleModel csm =
                    (ComponentSampleModel)tile.getSampleModel();
                int offset = csm.getOffset(sourceXOffset, sourceYOffset, 0);
                int lineStride = csm.getScanlineStride();
                writeRowsOpt(data, offset, lineStride, compressor,
                             0, 1, destWidth, destHeight,
                             numRowsWritten, progressReportRowPeriod);
            } else {
                writeRows(image, compressor,
                          sourceXOffset, periodX,
                          sourceYOffset, periodY,
                          sourceWidth,
                          0, 1, destWidth, destHeight,
                          numRowsWritten, progressReportRowPeriod);
            }
        }
        if (abortRequested()) {
            return;
        }
        processImageProgress(100.0F);
        compressor.flush();
        stream.write(0x00);
        processImageComplete();
    }
    private void writeHeader(String version,
                             int logicalScreenWidth,
                             int logicalScreenHeight,
                             int colorResolution,
                             int pixelAspectRatio,
                             int backgroundColorIndex,
                             boolean sortFlag,
                             int bitsPerPixel,
                             byte[] globalColorTable) throws IOException {
        try {
            stream.writeBytes("GIF"+version);
            stream.writeShort((short)logicalScreenWidth);
            stream.writeShort((short)logicalScreenHeight);
            int packedFields = globalColorTable != null ? 0x80 : 0x00;
            packedFields |= ((colorResolution - 1) & 0x7) << 4;
            if (sortFlag) {
                packedFields |= 0x8;
            }
            packedFields |= (bitsPerPixel - 1);
            stream.write(packedFields);
            stream.write(backgroundColorIndex);
            stream.write(pixelAspectRatio);
            if (globalColorTable != null) {
                stream.write(globalColorTable);
            }
        } catch (IOException e) {
            throw new IIOException("I/O error writing header!", e);
        }
    }
    private void writeHeader(IIOMetadata streamMetadata, int bitsPerPixel)
      throws IOException {
        GIFWritableStreamMetadata sm;
        if (streamMetadata instanceof GIFWritableStreamMetadata) {
            sm = (GIFWritableStreamMetadata)streamMetadata;
        } else {
            sm = new GIFWritableStreamMetadata();
            Node root =
                streamMetadata.getAsTree(STREAM_METADATA_NAME);
            sm.setFromTree(STREAM_METADATA_NAME, root);
        }
        writeHeader(sm.version,
                    sm.logicalScreenWidth,
                    sm.logicalScreenHeight,
                    sm.colorResolution,
                    sm.pixelAspectRatio,
                    sm.backgroundColorIndex,
                    sm.sortFlag,
                    bitsPerPixel,
                    sm.globalColorTable);
    }
    private void writeGraphicControlExtension(int disposalMethod,
                                              boolean userInputFlag,
                                              boolean transparentColorFlag,
                                              int delayTime,
                                              int transparentColorIndex)
      throws IOException {
        try {
            stream.write(0x21);
            stream.write(0xf9);
            stream.write(4);
            int packedFields = (disposalMethod & 0x3) << 2;
            if (userInputFlag) {
                packedFields |= 0x2;
            }
            if (transparentColorFlag) {
                packedFields |= 0x1;
            }
            stream.write(packedFields);
            stream.writeShort((short)delayTime);
            stream.write(transparentColorIndex);
            stream.write(0x00);
        } catch (IOException e) {
            throw new IIOException("I/O error writing Graphic Control Extension!", e);
        }
    }
    private void writeGraphicControlExtension(GIFWritableImageMetadata im)
      throws IOException {
        writeGraphicControlExtension(im.disposalMethod,
                                     im.userInputFlag,
                                     im.transparentColorFlag,
                                     im.delayTime,
                                     im.transparentColorIndex);
    }
    private void writeBlocks(byte[] data) throws IOException {
        if (data != null && data.length > 0) {
            int offset = 0;
            while (offset < data.length) {
                int len = Math.min(data.length - offset, 255);
                stream.write(len);
                stream.write(data, offset, len);
                offset += len;
            }
        }
    }
    private void writePlainTextExtension(GIFWritableImageMetadata im)
      throws IOException {
        if (im.hasPlainTextExtension) {
            try {
                stream.write(0x21);
                stream.write(0x1);
                stream.write(12);
                stream.writeShort(im.textGridLeft);
                stream.writeShort(im.textGridTop);
                stream.writeShort(im.textGridWidth);
                stream.writeShort(im.textGridHeight);
                stream.write(im.characterCellWidth);
                stream.write(im.characterCellHeight);
                stream.write(im.textForegroundColor);
                stream.write(im.textBackgroundColor);
                writeBlocks(im.text);
                stream.write(0x00);
            } catch (IOException e) {
                throw new IIOException("I/O error writing Plain Text Extension!", e);
            }
        }
    }
    private void writeApplicationExtension(GIFWritableImageMetadata im)
      throws IOException {
        if (im.applicationIDs != null) {
            Iterator iterIDs = im.applicationIDs.iterator();
            Iterator iterCodes = im.authenticationCodes.iterator();
            Iterator iterData = im.applicationData.iterator();
            while (iterIDs.hasNext()) {
                try {
                    stream.write(0x21);
                    stream.write(0xff);
                    stream.write(11);
                    stream.write((byte[])iterIDs.next(), 0, 8);
                    stream.write((byte[])iterCodes.next(), 0, 3);
                    writeBlocks((byte[])iterData.next());
                    stream.write(0x00);
                } catch (IOException e) {
                    throw new IIOException("I/O error writing Application Extension!", e);
                }
            }
        }
    }
    private void writeCommentExtension(GIFWritableImageMetadata im)
      throws IOException {
        if (im.comments != null) {
            try {
                Iterator iter = im.comments.iterator();
                while (iter.hasNext()) {
                    stream.write(0x21);
                    stream.write(0xfe);
                    writeBlocks((byte[])iter.next());
                    stream.write(0x00);
                }
            } catch (IOException e) {
                throw new IIOException("I/O error writing Comment Extension!", e);
            }
        }
    }
    private void writeImageDescriptor(int imageLeftPosition,
                                      int imageTopPosition,
                                      int imageWidth,
                                      int imageHeight,
                                      boolean interlaceFlag,
                                      boolean sortFlag,
                                      int bitsPerPixel,
                                      byte[] localColorTable)
      throws IOException {
        try {
            stream.write(0x2c);
            stream.writeShort((short)imageLeftPosition);
            stream.writeShort((short)imageTopPosition);
            stream.writeShort((short)imageWidth);
            stream.writeShort((short)imageHeight);
            int packedFields = localColorTable != null ? 0x80 : 0x00;
            if (interlaceFlag) {
                packedFields |= 0x40;
            }
            if (sortFlag) {
                packedFields |= 0x8;
            }
            packedFields |= (bitsPerPixel - 1);
            stream.write(packedFields);
            if (localColorTable != null) {
                stream.write(localColorTable);
            }
        } catch (IOException e) {
            throw new IIOException("I/O error writing Image Descriptor!", e);
        }
    }
    private void writeImageDescriptor(GIFWritableImageMetadata imageMetadata,
                                      int bitsPerPixel)
      throws IOException {
        writeImageDescriptor(imageMetadata.imageLeftPosition,
                             imageMetadata.imageTopPosition,
                             imageMetadata.imageWidth,
                             imageMetadata.imageHeight,
                             imageMetadata.interlaceFlag,
                             imageMetadata.sortFlag,
                             bitsPerPixel,
                             imageMetadata.localColorTable);
    }
    private void writeTrailer() throws IOException {
        stream.write(0x3b);
    }
}
class GIFImageWriteParam extends ImageWriteParam {
    GIFImageWriteParam(Locale locale) {
        super(locale);
        this.canWriteCompressed = true;
        this.canWriteProgressive = true;
        this.compressionTypes = new String[] {"LZW", "lzw"};
        this.compressionType = compressionTypes[0];
    }
    public void setCompressionMode(int mode) {
        if (mode == MODE_DISABLED) {
            throw new UnsupportedOperationException("MODE_DISABLED is not supported.");
        }
        super.setCompressionMode(mode);
    }
}
