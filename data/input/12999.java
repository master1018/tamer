public class ImageTypeSpecifier {
    protected ColorModel colorModel;
    protected SampleModel sampleModel;
    private static ImageTypeSpecifier[] BISpecifier;
    private static ColorSpace sRGB;
    static {
        sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        BISpecifier =
            new ImageTypeSpecifier[BufferedImage.TYPE_BYTE_INDEXED + 1];
    }
    private ImageTypeSpecifier() {}
    public ImageTypeSpecifier(ColorModel colorModel, SampleModel sampleModel) {
        if (colorModel == null) {
            throw new IllegalArgumentException("colorModel == null!");
        }
        if (sampleModel == null) {
            throw new IllegalArgumentException("sampleModel == null!");
        }
        if (!colorModel.isCompatibleSampleModel(sampleModel)) {
            throw new IllegalArgumentException
                ("sampleModel is incompatible with colorModel!");
        }
        this.colorModel = colorModel;
        this.sampleModel = sampleModel;
    }
    public ImageTypeSpecifier(RenderedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        colorModel = image.getColorModel();
        sampleModel = image.getSampleModel();
    }
    static class Packed extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int redMask;
        int greenMask;
        int blueMask;
        int alphaMask;
        int transferType;
        boolean isAlphaPremultiplied;
        public Packed(ColorSpace colorSpace,
                      int redMask,
                      int greenMask,
                      int blueMask,
                      int alphaMask, 
                      int transferType,
                      boolean isAlphaPremultiplied) {
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (colorSpace.getType() != ColorSpace.TYPE_RGB) {
                throw new IllegalArgumentException
                    ("colorSpace is not of type TYPE_RGB!");
            }
            if (transferType != DataBuffer.TYPE_BYTE &&
                transferType != DataBuffer.TYPE_USHORT &&
                transferType != DataBuffer.TYPE_INT) {
                throw new IllegalArgumentException
                    ("Bad value for transferType!");
            }
            if (redMask == 0 && greenMask == 0 &&
                blueMask == 0 && alphaMask == 0) {
                throw new IllegalArgumentException
                    ("No mask has at least 1 bit set!");
            }
            this.colorSpace = colorSpace;
            this.redMask = redMask;
            this.greenMask = greenMask;
            this.blueMask = blueMask;
            this.alphaMask = alphaMask;
            this.transferType = transferType;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            int bits = 32;
            this.colorModel =
                new DirectColorModel(colorSpace,
                                     bits,
                                     redMask, greenMask, blueMask,
                                     alphaMask, isAlphaPremultiplied,
                                     transferType);
            this.sampleModel = colorModel.createCompatibleSampleModel(1, 1);
        }
    }
    public static ImageTypeSpecifier
        createPacked(ColorSpace colorSpace,
                     int redMask,
                     int greenMask,
                     int blueMask,
                     int alphaMask, 
                     int transferType,
                     boolean isAlphaPremultiplied) {
        return new ImageTypeSpecifier.Packed(colorSpace,
                                             redMask,
                                             greenMask,
                                             blueMask,
                                             alphaMask, 
                                             transferType,
                                             isAlphaPremultiplied);
    }
    static ColorModel createComponentCM(ColorSpace colorSpace,
                                        int numBands,
                                        int dataType,
                                        boolean hasAlpha,
                                        boolean isAlphaPremultiplied) {
        int transparency =
            hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
        int[] numBits = new int[numBands];
        int bits = DataBuffer.getDataTypeSize(dataType);
        for (int i = 0; i < numBands; i++) {
            numBits[i] = bits;
        }
        return new ComponentColorModel(colorSpace,
                                       numBits,
                                       hasAlpha,
                                       isAlphaPremultiplied,
                                       transparency,
                                       dataType);
    }
    static class Interleaved extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int[] bandOffsets;
        int dataType;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;
        public Interleaved(ColorSpace colorSpace,
                           int[] bandOffsets,
                           int dataType,
                           boolean hasAlpha,
                           boolean isAlphaPremultiplied) {
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (bandOffsets == null) {
                throw new IllegalArgumentException("bandOffsets == null!");
            }
            int numBands = colorSpace.getNumComponents() +
                (hasAlpha ? 1 : 0);
            if (bandOffsets.length != numBands) {
                throw new IllegalArgumentException
                    ("bandOffsets.length is wrong!");
            }
            if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_SHORT &&
                dataType != DataBuffer.TYPE_USHORT &&
                dataType != DataBuffer.TYPE_INT &&
                dataType != DataBuffer.TYPE_FLOAT &&
                dataType != DataBuffer.TYPE_DOUBLE) {
                throw new IllegalArgumentException
                    ("Bad value for dataType!");
            }
            this.colorSpace = colorSpace;
            this.bandOffsets = (int[])bandOffsets.clone();
            this.dataType = dataType;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.colorModel =
                ImageTypeSpecifier.createComponentCM(colorSpace,
                                                     bandOffsets.length,
                                                     dataType,
                                                     hasAlpha,
                                                     isAlphaPremultiplied);
            int minBandOffset = bandOffsets[0];
            int maxBandOffset = minBandOffset;
            for (int i = 0; i < bandOffsets.length; i++) {
                int offset = bandOffsets[i];
                minBandOffset = Math.min(offset, minBandOffset);
                maxBandOffset = Math.max(offset, maxBandOffset);
            }
            int pixelStride = maxBandOffset - minBandOffset + 1;
            int w = 1;
            int h = 1;
            this.sampleModel =
                new PixelInterleavedSampleModel(dataType,
                                                w, h,
                                                pixelStride,
                                                w*pixelStride,
                                                bandOffsets);
        }
        public boolean equals(Object o) {
            if ((o == null) ||
                !(o instanceof ImageTypeSpecifier.Interleaved)) {
                return false;
            }
            ImageTypeSpecifier.Interleaved that =
                (ImageTypeSpecifier.Interleaved)o;
            if ((!(this.colorSpace.equals(that.colorSpace))) ||
                (this.dataType != that.dataType) ||
                (this.hasAlpha != that.hasAlpha) ||
                (this.isAlphaPremultiplied != that.isAlphaPremultiplied) ||
                (this.bandOffsets.length != that.bandOffsets.length)) {
                return false;
            }
            for (int i = 0; i < bandOffsets.length; i++) {
                if (this.bandOffsets[i] != that.bandOffsets[i]) {
                    return false;
                }
            }
            return true;
        }
        public int hashCode() {
            return (super.hashCode() +
                    (4 * bandOffsets.length) +
                    (25 * dataType) +
                    (hasAlpha ? 17 : 18));
        }
    }
    public static ImageTypeSpecifier
        createInterleaved(ColorSpace colorSpace,
                          int[] bandOffsets,
                          int dataType,
                          boolean hasAlpha,
                          boolean isAlphaPremultiplied) {
        return new ImageTypeSpecifier.Interleaved(colorSpace,
                                                  bandOffsets,
                                                  dataType,
                                                  hasAlpha,
                                                  isAlphaPremultiplied);
    }
    static class Banded extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int[] bankIndices;
        int[] bandOffsets;
        int dataType;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;
        public Banded(ColorSpace colorSpace,
                      int[] bankIndices,
                      int[] bandOffsets,
                      int dataType,
                      boolean hasAlpha,
                      boolean isAlphaPremultiplied) {
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (bankIndices == null) {
                throw new IllegalArgumentException("bankIndices == null!");
            }
            if (bandOffsets == null) {
                throw new IllegalArgumentException("bandOffsets == null!");
            }
            if (bankIndices.length != bandOffsets.length) {
                throw new IllegalArgumentException
                    ("bankIndices.length != bandOffsets.length!");
            }
            if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_SHORT &&
                dataType != DataBuffer.TYPE_USHORT &&
                dataType != DataBuffer.TYPE_INT &&
                dataType != DataBuffer.TYPE_FLOAT &&
                dataType != DataBuffer.TYPE_DOUBLE) {
                throw new IllegalArgumentException
                    ("Bad value for dataType!");
            }
            int numBands = colorSpace.getNumComponents() +
                (hasAlpha ? 1 : 0);
            if (bandOffsets.length != numBands) {
                throw new IllegalArgumentException
                    ("bandOffsets.length is wrong!");
            }
            this.colorSpace = colorSpace;
            this.bankIndices = (int[])bankIndices.clone();
            this.bandOffsets = (int[])bandOffsets.clone();
            this.dataType = dataType;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.colorModel =
                ImageTypeSpecifier.createComponentCM(colorSpace,
                                                     bankIndices.length,
                                                     dataType,
                                                     hasAlpha,
                                                     isAlphaPremultiplied);
            int w = 1;
            int h = 1;
            this.sampleModel = new BandedSampleModel(dataType,
                                                     w, h,
                                                     w,
                                                     bankIndices,
                                                     bandOffsets);
        }
        public boolean equals(Object o) {
            if ((o == null) ||
                !(o instanceof ImageTypeSpecifier.Banded)) {
                return false;
            }
            ImageTypeSpecifier.Banded that =
                (ImageTypeSpecifier.Banded)o;
            if ((!(this.colorSpace.equals(that.colorSpace))) ||
                (this.dataType != that.dataType) ||
                (this.hasAlpha != that.hasAlpha) ||
                (this.isAlphaPremultiplied != that.isAlphaPremultiplied) ||
                (this.bankIndices.length != that.bankIndices.length) ||
                (this.bandOffsets.length != that.bandOffsets.length)) {
                return false;
            }
            for (int i = 0; i < bankIndices.length; i++) {
                if (this.bankIndices[i] != that.bankIndices[i]) {
                    return false;
                }
            }
            for (int i = 0; i < bandOffsets.length; i++) {
                if (this.bandOffsets[i] != that.bandOffsets[i]) {
                    return false;
                }
            }
            return true;
        }
        public int hashCode() {
            return (super.hashCode() +
                    (3 * bandOffsets.length) +
                    (7 * bankIndices.length) +
                    (21 * dataType) +
                    (hasAlpha ? 19 : 29));
        }
    }
    public static ImageTypeSpecifier
        createBanded(ColorSpace colorSpace,
                     int[] bankIndices,
                     int[] bandOffsets,
                     int dataType,
                     boolean hasAlpha,
                     boolean isAlphaPremultiplied) {
        return new ImageTypeSpecifier.Banded(colorSpace,
                                             bankIndices,
                                             bandOffsets,
                                             dataType,
                                             hasAlpha,
                                             isAlphaPremultiplied);
    }
    static class Grayscale extends ImageTypeSpecifier {
        int bits;
        int dataType;
        boolean isSigned;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;
        public Grayscale(int bits,
                         int dataType,
                         boolean isSigned,
                         boolean hasAlpha,
                         boolean isAlphaPremultiplied)
        {
            if (bits != 1 && bits != 2 && bits != 4 &&
                bits != 8 && bits != 16)
            {
                throw new IllegalArgumentException("Bad value for bits!");
            }
            if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_SHORT &&
                dataType != DataBuffer.TYPE_USHORT)
            {
                throw new IllegalArgumentException
                    ("Bad value for dataType!");
            }
            if (bits > 8 && dataType == DataBuffer.TYPE_BYTE) {
                throw new IllegalArgumentException
                    ("Too many bits for dataType!");
            }
            this.bits = bits;
            this.dataType = dataType;
            this.isSigned = isSigned;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            if ((bits == 8 && dataType == DataBuffer.TYPE_BYTE) ||
                (bits == 16 &&
                 (dataType == DataBuffer.TYPE_SHORT ||
                  dataType == DataBuffer.TYPE_USHORT))) {
                int numBands = hasAlpha ? 2 : 1;
                int transparency =
                    hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
                int[] nBits = new int[numBands];
                nBits[0] = bits;
                if (numBands == 2) {
                    nBits[1] = bits;
                }
                this.colorModel =
                    new ComponentColorModel(colorSpace,
                                            nBits,
                                            hasAlpha,
                                            isAlphaPremultiplied,
                                            transparency,
                                            dataType);
                int[] bandOffsets = new int[numBands];
                bandOffsets[0] = 0;
                if (numBands == 2) {
                    bandOffsets[1] = 1;
                }
                int w = 1;
                int h = 1;
                this.sampleModel =
                    new PixelInterleavedSampleModel(dataType,
                                                    w, h,
                                                    numBands, w*numBands,
                                                    bandOffsets);
            } else {
                int numEntries = 1 << bits;
                byte[] arr = new byte[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    arr[i] = (byte)(i*255/(numEntries - 1));
                }
                this.colorModel =
                    new IndexColorModel(bits, numEntries, arr, arr, arr);
                this.sampleModel =
                    new MultiPixelPackedSampleModel(dataType, 1, 1, bits);
            }
        }
    }
    public static ImageTypeSpecifier
        createGrayscale(int bits,
                        int dataType,
                        boolean isSigned) {
        return new ImageTypeSpecifier.Grayscale(bits,
                                                dataType,
                                                isSigned,
                                                false,
                                                false);
    }
    public static ImageTypeSpecifier
        createGrayscale(int bits,
                        int dataType,
                        boolean isSigned,
                        boolean isAlphaPremultiplied) {
        return new ImageTypeSpecifier.Grayscale(bits,
                                                dataType,
                                                isSigned,
                                                true,
                                                isAlphaPremultiplied);
    }
    static class Indexed extends ImageTypeSpecifier {
        byte[] redLUT;
        byte[] greenLUT;
        byte[] blueLUT;
        byte[] alphaLUT = null;
        int bits;
        int dataType;
        public Indexed(byte[] redLUT,
                       byte[] greenLUT,
                       byte[] blueLUT,
                       byte[] alphaLUT,
                       int bits,
                       int dataType) {
            if (redLUT == null || greenLUT == null || blueLUT == null) {
                throw new IllegalArgumentException("LUT is null!");
            }
            if (bits != 1 && bits != 2 && bits != 4 &&
                bits != 8 && bits != 16) {
                throw new IllegalArgumentException("Bad value for bits!");
            }
            if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_SHORT &&
                dataType != DataBuffer.TYPE_USHORT &&
                dataType != DataBuffer.TYPE_INT) {
                throw new IllegalArgumentException
                    ("Bad value for dataType!");
            }
            if ((bits > 8 && dataType == DataBuffer.TYPE_BYTE) ||
                (bits > 16 && dataType != DataBuffer.TYPE_INT)) {
                throw new IllegalArgumentException
                    ("Too many bits for dataType!");
            }
            int len = 1 << bits;
            if (redLUT.length != len ||
                greenLUT.length != len ||
                blueLUT.length != len ||
                (alphaLUT != null && alphaLUT.length != len)) {
                throw new IllegalArgumentException("LUT has improper length!");
            }
            this.redLUT = (byte[])redLUT.clone();
            this.greenLUT = (byte[])greenLUT.clone();
            this.blueLUT = (byte[])blueLUT.clone();
            if (alphaLUT != null) {
                this.alphaLUT = (byte[])alphaLUT.clone();
            }
            this.bits = bits;
            this.dataType = dataType;
            if (alphaLUT == null) {
                this.colorModel = new IndexColorModel(bits,
                                                      redLUT.length,
                                                      redLUT,
                                                      greenLUT,
                                                      blueLUT);
            } else {
                this.colorModel = new IndexColorModel(bits,
                                                      redLUT.length,
                                                      redLUT,
                                                      greenLUT,
                                                      blueLUT,
                                                      alphaLUT);
            }
            if ((bits == 8 && dataType == DataBuffer.TYPE_BYTE) ||
                (bits == 16 &&
                 (dataType == DataBuffer.TYPE_SHORT ||
                  dataType == DataBuffer.TYPE_USHORT))) {
                int[] bandOffsets = { 0 };
                this.sampleModel =
                    new PixelInterleavedSampleModel(dataType,
                                                    1, 1, 1, 1,
                                                    bandOffsets);
            } else {
                this.sampleModel =
                    new MultiPixelPackedSampleModel(dataType, 1, 1, bits);
            }
        }
    }
    public static ImageTypeSpecifier
        createIndexed(byte[] redLUT,
                      byte[] greenLUT,
                      byte[] blueLUT,
                      byte[] alphaLUT,
                      int bits,
                      int dataType) {
        return new ImageTypeSpecifier.Indexed(redLUT,
                                              greenLUT,
                                              blueLUT,
                                              alphaLUT,
                                              bits,
                                              dataType);
    }
    public static
        ImageTypeSpecifier createFromBufferedImageType(int bufferedImageType) {
        if (bufferedImageType >= BufferedImage.TYPE_INT_RGB &&
            bufferedImageType <= BufferedImage.TYPE_BYTE_INDEXED) {
            return getSpecifier(bufferedImageType);
        } else if (bufferedImageType == BufferedImage.TYPE_CUSTOM) {
            throw new IllegalArgumentException("Cannot create from TYPE_CUSTOM!");
        } else {
            throw new IllegalArgumentException("Invalid BufferedImage type!");
        }
    }
    public static
        ImageTypeSpecifier createFromRenderedImage(RenderedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        if (image instanceof BufferedImage) {
            int bufferedImageType = ((BufferedImage)image).getType();
            if (bufferedImageType != BufferedImage.TYPE_CUSTOM) {
                return getSpecifier(bufferedImageType);
            }
        }
        return new ImageTypeSpecifier(image);
    }
    public int getBufferedImageType() {
        BufferedImage bi = createBufferedImage(1, 1);
        return bi.getType();
    }
    public int getNumComponents() {
        return colorModel.getNumComponents();
    }
    public int getNumBands() {
        return sampleModel.getNumBands();
    }
    public int getBitsPerBand(int band) {
        if (band < 0 | band >= getNumBands()) {
            throw new IllegalArgumentException("band out of range!");
        }
        return sampleModel.getSampleSize(band);
    }
    public SampleModel getSampleModel() {
        return sampleModel;
    }
    public SampleModel getSampleModel(int width, int height) {
        if ((long)width*height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                ("width*height > Integer.MAX_VALUE!");
        }
        return sampleModel.createCompatibleSampleModel(width, height);
    }
    public ColorModel getColorModel() {
        return colorModel;
    }
    public BufferedImage createBufferedImage(int width, int height) {
        try {
            SampleModel sampleModel = getSampleModel(width, height);
            WritableRaster raster =
                Raster.createWritableRaster(sampleModel,
                                            new Point(0, 0));
            return new BufferedImage(colorModel, raster,
                                     colorModel.isAlphaPremultiplied(),
                                     new Hashtable());
        } catch (NegativeArraySizeException e) {
            throw new IllegalArgumentException
                ("Array size > Integer.MAX_VALUE!");
        }
    }
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof ImageTypeSpecifier)) {
            return false;
        }
        ImageTypeSpecifier that = (ImageTypeSpecifier)o;
        return (colorModel.equals(that.colorModel)) &&
            (sampleModel.equals(that.sampleModel));
    }
    public int hashCode() {
        return (9 * colorModel.hashCode()) + (14 * sampleModel.hashCode());
    }
    private static ImageTypeSpecifier getSpecifier(int type) {
        if (BISpecifier[type] == null) {
            BISpecifier[type] = createSpecifier(type);
        }
        return BISpecifier[type];
    }
    private static ImageTypeSpecifier createSpecifier(int type) {
        switch(type) {
          case BufferedImage.TYPE_INT_RGB:
              return createPacked(sRGB,
                                  0x00ff0000,
                                  0x0000ff00,
                                  0x000000ff,
                                  0x0,
                                  DataBuffer.TYPE_INT,
                                  false);
          case BufferedImage.TYPE_INT_ARGB:
              return createPacked(sRGB,
                                  0x00ff0000,
                                  0x0000ff00,
                                  0x000000ff,
                                  0xff000000,
                                  DataBuffer.TYPE_INT,
                                  false);
          case BufferedImage.TYPE_INT_ARGB_PRE:
              return createPacked(sRGB,
                                  0x00ff0000,
                                  0x0000ff00,
                                  0x000000ff,
                                  0xff000000,
                                  DataBuffer.TYPE_INT,
                                  true);
          case BufferedImage.TYPE_INT_BGR:
              return createPacked(sRGB,
                                  0x000000ff,
                                  0x0000ff00,
                                  0x00ff0000,
                                  0x0,
                                  DataBuffer.TYPE_INT,
                                  false);
          case BufferedImage.TYPE_3BYTE_BGR:
              return createInterleaved(sRGB,
                                       new int[] { 2, 1, 0 },
                                       DataBuffer.TYPE_BYTE,
                                       false,
                                       false);
          case BufferedImage.TYPE_4BYTE_ABGR:
              return createInterleaved(sRGB,
                                       new int[] { 3, 2, 1, 0 },
                                       DataBuffer.TYPE_BYTE,
                                       true,
                                       false);
          case BufferedImage.TYPE_4BYTE_ABGR_PRE:
              return createInterleaved(sRGB,
                                       new int[] { 3, 2, 1, 0 },
                                       DataBuffer.TYPE_BYTE,
                                       true,
                                       true);
          case BufferedImage.TYPE_USHORT_565_RGB:
              return createPacked(sRGB,
                                  0xF800,
                                  0x07E0,
                                  0x001F,
                                  0x0,
                                  DataBuffer.TYPE_USHORT,
                                  false);
          case BufferedImage.TYPE_USHORT_555_RGB:
              return createPacked(sRGB,
                                  0x7C00,
                                  0x03E0,
                                  0x001F,
                                  0x0,
                                  DataBuffer.TYPE_USHORT,
                                  false);
          case BufferedImage.TYPE_BYTE_GRAY:
            return createGrayscale(8,
                                   DataBuffer.TYPE_BYTE,
                                   false);
          case BufferedImage.TYPE_USHORT_GRAY:
            return createGrayscale(16,
                                   DataBuffer.TYPE_USHORT,
                                   false);
          case BufferedImage.TYPE_BYTE_BINARY:
              return createGrayscale(1,
                                     DataBuffer.TYPE_BYTE,
                                     false);
          case BufferedImage.TYPE_BYTE_INDEXED:
          {
              BufferedImage bi =
                  new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
              IndexColorModel icm = (IndexColorModel)bi.getColorModel();
              int mapSize = icm.getMapSize();
              byte[] redLUT = new byte[mapSize];
              byte[] greenLUT = new byte[mapSize];
              byte[] blueLUT = new byte[mapSize];
              byte[] alphaLUT = new byte[mapSize];
              icm.getReds(redLUT);
              icm.getGreens(greenLUT);
              icm.getBlues(blueLUT);
              icm.getAlphas(alphaLUT);
              return createIndexed(redLUT, greenLUT, blueLUT, alphaLUT,
                                   8,
                                   DataBuffer.TYPE_BYTE);
          }
          default:
              throw new IllegalArgumentException("Invalid BufferedImage type!");
        }
    }
}
