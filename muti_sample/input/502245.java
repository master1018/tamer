public class GifDecoder extends ImageDecoder {
    private static native void initIDs();
    static {
        System.loadLibrary("gl"); 
        initIDs();
    }
    private static final int baseHints =
            ImageConsumer.SINGLEPASS | ImageConsumer.COMPLETESCANLINES |
            ImageConsumer.SINGLEFRAME;
    private static final int interlacedHints =
            baseHints | ImageConsumer.RANDOMPIXELORDER;
    static final int IMPOSSIBLE_VALUE = 0x0FFFFFFF;
    private static final int BUFFER_SIZE = 1024;
    private byte buffer[] = new byte[BUFFER_SIZE];
    GifDataStream gifDataStream = new GifDataStream();
    GifGraphicBlock currBlock;
    private long hNativeDecoder; 
    private int bytesConsumed;
    private boolean consumersPrepared;
    private Hashtable<String, String> properties = new Hashtable<String, String>();
    private boolean forceRGB;
    private byte screenBuffer[];
    private int screenRGBBuffer[];
    public GifDecoder(DecodingImageSource src, InputStream is) {
        super(src, is);
    }
    private static native int[] toRGB(byte imageData[], byte colormap[], int transparentColor);
    private static native void releaseNativeDecoder(long hDecoder);
    private native int decode(
            byte input[],
            int bytesInBuffer,
            long hDecoder,
            GifDataStream dataStream,
            GifGraphicBlock currBlock
            );
    private int[] getScreenRGBBuffer() {
        if (screenRGBBuffer == null) {
            if (screenBuffer != null) {
                int transparentColor =
                        gifDataStream.logicalScreen.globalColorTable.cm.getTransparentPixel();
                transparentColor = transparentColor > 0 ? transparentColor : IMPOSSIBLE_VALUE;
                screenRGBBuffer =
                        toRGB(
                                screenBuffer,
                                gifDataStream.logicalScreen.globalColorTable.colors,
                                transparentColor
                        );
            } else {
                int size = gifDataStream.logicalScreen.logicalScreenHeight *
                        gifDataStream.logicalScreen.logicalScreenWidth;
                screenRGBBuffer = new int[size];
            }
        }
        return screenRGBBuffer;
    }
    private void prepareConsumers() {
        GifLogicalScreen gls = gifDataStream.logicalScreen;
        setDimensions(gls.logicalScreenWidth,
                gls.logicalScreenHeight);
        setProperties(properties);
        currBlock = gifDataStream.graphicBlocks.get(0);
        if (forceRGB) {
            setColorModel(ColorModel.getRGBdefault());
        } else {
            setColorModel(gls.globalColorTable.getColorModel(currBlock.transparentColor));
        }
        if (forceRGB) {
            int fillColor = 0xFF000000;
            if (gls.backgroundColor != IMPOSSIBLE_VALUE) {
                fillColor = gls.backgroundColor;
            }
            Arrays.fill(getScreenRGBBuffer(), fillColor);
        } else {
            int fillColor = 0;
            if (gls.backgroundColor != IMPOSSIBLE_VALUE) {
                fillColor = gls.backgroundColor;
            } else {
                fillColor = gls.globalColorTable.cm.getTransparentPixel();
            }
            screenBuffer = new byte[gls.logicalScreenHeight*gls.logicalScreenWidth];
            Arrays.fill(screenBuffer, (byte) fillColor);
        }
        setHints(interlacedHints); 
    }
    @Override
    public void decodeImage() throws IOException {
        try {
            int bytesRead = 0;
            int needBytes, offset, bytesInBuffer = 0;
            boolean eosReached = false;
            GifGraphicBlock blockToDispose = null;
            if (currBlock == null) {
                currBlock = new GifGraphicBlock();
                gifDataStream.graphicBlocks.add(currBlock);
            }
            for (;;) {
                needBytes = BUFFER_SIZE - bytesInBuffer;
                offset = bytesInBuffer;
                bytesRead = inputStream.read(buffer, offset, needBytes);
                if (bytesRead < 0) {
                    eosReached = true;
                    bytesRead = 0;
                } 
                bytesInBuffer += bytesRead;
                int numLines = decode(
                        buffer,
                        bytesRead,
                        hNativeDecoder,
                        gifDataStream,
                        currBlock);
                bytesInBuffer -= bytesConsumed;
                if (
                        !consumersPrepared &&
                        gifDataStream.logicalScreen.completed &&
                        gifDataStream.logicalScreen.globalColorTable.completed &&
                        (currBlock.imageData != null || 
                        currBlock.rgbImageData != null)
                ) {
                    prepareConsumers();
                    consumersPrepared = true;
                }
                if (bytesConsumed < 0) {
                    break; 
                }
                if (currBlock != null) {
                    if (numLines != 0) {
                        if (blockToDispose != null) {
                            blockToDispose.dispose();
                            blockToDispose = null;
                        }
                        currBlock.sendNewData(this, numLines);
                    }
                    if (currBlock.completed && hNativeDecoder != 0) {
                        blockToDispose = currBlock; 
                        currBlock = new GifGraphicBlock();
                        gifDataStream.graphicBlocks.add(currBlock);
                    }
                }
                if (hNativeDecoder == 0) {
                    break;
                }
                if (eosReached && numLines == 0) { 
                    releaseNativeDecoder(hNativeDecoder);
                    break;
                }
            }
        } finally {
            closeStream();
        }
        if (gifDataStream.loopCount != 1) {
            if (currBlock.completed == false) {
                gifDataStream.graphicBlocks.remove(currBlock);
            }
            int numFrames = gifDataStream.graphicBlocks.size();
            GifGraphicBlock gb =
                    gifDataStream.graphicBlocks.get(numFrames-1);
            ImageLoader.beginAnimation();
            while (gifDataStream.loopCount != 1) {
                if (gifDataStream.loopCount != 0) {
                    gifDataStream.loopCount--;
                }
                for (int i=0; i<numFrames; i++) {
                    gb.dispose();
                    gb = gifDataStream.graphicBlocks.get(i);
                    if (forceRGB) {
                        setPixels(
                                gb.imageLeft,
                                gb.imageTop,
                                gb.imageWidth,
                                gb.imageHeight,
                                ColorModel.getRGBdefault(),
                                gb.getRgbImageData(),
                                0,
                                gb.imageWidth
                        );
                    } else {
                        setPixels(
                                gb.imageLeft,
                                gb.imageTop,
                                gb.imageWidth,
                                gb.imageHeight,
                                null,
                                gb.imageData,
                                0,
                                gb.imageWidth
                        );
                    }
                }
            }
            ImageLoader.endAnimation();
        }
        imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
    void setComment(String newComment) {
        Object currComment = properties.get("comment"); 
        if (currComment == null) {
            properties.put("comment", newComment); 
        } else {
            properties.put("comment", (String) currComment + "\n" + newComment); 
        }
        setProperties(properties);
    }
    class GifDataStream {
        boolean completed = false;
        int loopCount = 1;
        GifLogicalScreen logicalScreen = new GifLogicalScreen();
        List<GifGraphicBlock> graphicBlocks = new ArrayList<GifGraphicBlock>(10); 
        String comments[];
    }
    class GifLogicalScreen {
        boolean completed = false;
        int logicalScreenWidth;
        int logicalScreenHeight;
        int backgroundColor = IMPOSSIBLE_VALUE;
        GifColorTable globalColorTable = new GifColorTable();
    }
    class GifGraphicBlock {
        boolean completed = false;
        final static int DISPOSAL_NONE = 0;
        final static int DISPOSAL_NODISPOSAL = 1;
        final static int DISPOSAL_BACKGROUND = 2;
        final static int DISPOSAL_RESTORE = 3;
        int disposalMethod;
        int delayTime; 
        int transparentColor = IMPOSSIBLE_VALUE;
        int imageLeft;
        int imageTop;
        int imageWidth;
        int imageHeight;
        int imageRight;
        int imageBottom;
        boolean interlace;
        byte imageData[] = null;
        int rgbImageData[] = null;
        private int currY = 0; 
        int[] getRgbImageData() {
            if (rgbImageData == null) {
                rgbImageData =
                        toRGB(
                                imageData,
                                gifDataStream.logicalScreen.globalColorTable.colors,
                                transparentColor
                        );
                if (transparentColor != IMPOSSIBLE_VALUE) {
                    transparentColor =
                            gifDataStream.logicalScreen.globalColorTable.cm.getRGB(transparentColor);
                    transparentColor &= 0x00FFFFFF;
                }
            }
            return rgbImageData;
        }
        private void replaceTransparentPixels(int numLines) {
            List<GifGraphicBlock> graphicBlocks = gifDataStream.graphicBlocks;
            int prevBlockIndex = graphicBlocks.indexOf(this) - 1;
            if (prevBlockIndex >= 0) {
                int maxY = currY + numLines + imageTop;
                int offset = currY * imageWidth;
                imageRight = imageLeft + imageWidth;
                imageBottom = imageTop + imageHeight;
                int globalWidth = gifDataStream.logicalScreen.logicalScreenWidth;
                int pixelValue, imageOffset;
                int rgbData[] = forceRGB ? getRgbImageData() : null;
                for (int y = currY + imageTop; y < maxY; y++) {
                    imageOffset = globalWidth * y + imageLeft;
                    for (int x = imageLeft; x < imageRight; x++) {
                        pixelValue = forceRGB ?
                                rgbData[offset] :
                                imageData[offset] & 0xFF;
                        if (pixelValue == transparentColor) {
                            if (forceRGB) {
                                pixelValue = getScreenRGBBuffer() [imageOffset];
                                rgbData[offset] = pixelValue;
                            } else {
                                pixelValue = screenBuffer [imageOffset];
                                imageData[offset] = (byte) pixelValue;
                            }
                        }
                        offset++;
                        imageOffset++;
                    } 
                } 
            } 
        }
        public void sendNewData(GifDecoder decoder, int numLines) {
            if (transparentColor != IMPOSSIBLE_VALUE) {
                replaceTransparentPixels(numLines);
            }
            if (forceRGB) {
                decoder.setPixels(
                        imageLeft,
                        imageTop + currY,
                        imageWidth,
                        numLines,
                        ColorModel.getRGBdefault(),
                        getRgbImageData(),
                        currY*imageWidth,
                        imageWidth
                );
            } else {
                decoder.setPixels(
                        imageLeft,
                        imageTop + currY,
                        imageWidth,
                        numLines,
                        null,
                        imageData,
                        currY*imageWidth,
                        imageWidth
                );
            }
            currY += numLines;
        }
        public void dispose() {
            imageComplete(ImageConsumer.SINGLEFRAMEDONE);
            if (delayTime > 0) {
                try {
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Thread.yield(); 
            }
            if (imageLeft > gifDataStream.logicalScreen.logicalScreenWidth ||
                    imageTop > gifDataStream.logicalScreen.logicalScreenHeight) {
                disposalMethod = DISPOSAL_NONE;
            }
            switch(disposalMethod) {
                case DISPOSAL_BACKGROUND: {
                    if (forceRGB) {
                        getRgbImageData(); 
                        int data[] = new int[imageWidth*imageHeight];
                        if (transparentColor != IMPOSSIBLE_VALUE) {
                            Arrays.fill(
                                    data,
                                    transparentColor
                            );
                        } else {
                            Arrays.fill(
                                    data,
                                    gifDataStream.logicalScreen.backgroundColor
                            );
                        }
                        setPixels(
                                imageLeft,
                                imageTop,
                                imageWidth,
                                imageHeight,
                                ColorModel.getRGBdefault(),
                                data,
                                0,
                                imageWidth
                        );
                        sendToScreenBuffer(data);
                    } else {
                        byte data[] = new byte[imageWidth*imageHeight];
                        if (transparentColor != IMPOSSIBLE_VALUE) {
                            Arrays.fill(
                                    data,
                                    (byte) transparentColor
                            );
                        } else {
                            Arrays.fill(
                                    data,
                                    (byte) gifDataStream.logicalScreen.backgroundColor
                            );
                        }
                        setPixels(
                                imageLeft,
                                imageTop,
                                imageWidth,
                                imageHeight,
                                null,
                                data,
                                0,
                                imageWidth
                        );
                        sendToScreenBuffer(data);
                    }
                    break;
                }
                case DISPOSAL_RESTORE: {
                    screenBufferToScreen();
                    break;
                }
                case DISPOSAL_NONE:
                case DISPOSAL_NODISPOSAL:
                default: {
                    Object data = forceRGB ? (Object) getRgbImageData() : imageData;
                    sendToScreenBuffer(data);
                    break;
                }
            }
        }
        private void sendToScreenBuffer(Object data) {
            int dataInt[];
            byte dataByte[];
            int width = gifDataStream.logicalScreen.logicalScreenWidth;
            if (forceRGB) {
                dataInt = (int[]) data;
                if (imageWidth == width) {
                    System.arraycopy(dataInt,
                            0,
                            getScreenRGBBuffer(),
                            imageLeft + imageTop*width,
                            dataInt.length
                    );
                } else { 
                    copyScanlines(dataInt, getScreenRGBBuffer(), width);
                }
            } else {
                dataByte = (byte[]) data;
                if (imageWidth == width) {
                    System.arraycopy(dataByte,
                            0,
                            screenBuffer,
                            imageLeft + imageTop*width,
                            dataByte.length
                    );
                } else { 
                    copyScanlines(dataByte, screenBuffer, width);
                }
            }
        } 
        private void copyScanlines(Object src, Object dst, int width) {
            for (int i=0; i<imageHeight; i++) {
                System.arraycopy(src,
                        i*imageWidth,
                        dst,
                        imageLeft + i*width + imageTop*width,
                        imageWidth
                );
            } 
        }
        private void screenBufferToScreen() {
            int width = gifDataStream.logicalScreen.logicalScreenWidth;
            Object dst = forceRGB ?
                    (Object) new int[imageWidth*imageHeight] :
                    new byte[imageWidth*imageHeight];
            Object src = forceRGB ?
                    getScreenRGBBuffer() :
                    (Object) screenBuffer;
            int offset = 0;
            Object toSend;
            if (width == imageWidth) {
                offset = imageWidth * imageTop;
                toSend = src;
            } else {
                for (int i=0; i<imageHeight; i++) {
                    System.arraycopy(src,
                            imageLeft + i*width + imageTop*width,
                            dst,
                            i*imageWidth,
                            imageWidth
                    );
                } 
                toSend = dst;
            }
            if (forceRGB) {
                setPixels(
                        imageLeft,
                        imageTop,
                        imageWidth,
                        imageHeight,
                        ColorModel.getRGBdefault(),
                        (int [])toSend,
                        offset,
                        imageWidth
                );
            } else {
                setPixels(
                        imageLeft,
                        imageTop,
                        imageWidth,
                        imageHeight,
                        null,
                        (byte [])toSend,
                        offset,
                        imageWidth
                );
            }
        }
    }
    class GifColorTable {
        boolean completed = false;
        IndexColorModel cm = null;
        int size = 0; 
        byte colors[] = new byte[256*3];
        IndexColorModel getColorModel(int transparentColor) {
            if (cm != null) {
                if (transparentColor != cm.getTransparentPixel()) {
                    return cm = null; 
                }
                return cm;
            } else
                if (completed && size > 0) {
                    if (transparentColor == IMPOSSIBLE_VALUE) {
                        return cm =
                                new IndexColorModel(8, size, colors, 0, false);
                    }
                    if (transparentColor > size) {
                        size = transparentColor + 1;
                    }
                    return cm =
                            new IndexColorModel(8, size, colors, 0, false, transparentColor);
                }
            return cm = null; 
        }
    }
}
