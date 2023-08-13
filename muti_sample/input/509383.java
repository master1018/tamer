public class PNGImageWriterSpi extends ImageWriterSpi {
    public PNGImageWriterSpi() {
        super("Intel Corporation",
                "1.0",
                new String[] {
                        "png", "PNG" },
                new String[] {
                        "png", "PNG" },
                new String[] {
                    "image/png" },
                "org.apache.harmony.x.imageio.plugins.png.PNGImageWriter",
                STANDARD_OUTPUT_TYPE,
                new String[] {
                    "org.apache.harmony.x.imageio.plugins.png.PNGImageWriterSpi" },
                false,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null
        );
    }
    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        boolean canEncode = true;
        int numBands = type.getSampleModel().getNumBands();
        ColorModel colorModel = type.getColorModel();
        int bitDepth = colorModel.getPixelSize() / numBands;
        if (colorModel instanceof IndexColorModel) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8) {
                canEncode = false;
            }
            if (numBands != 1) {
                canEncode = false;
            }
        }
        else if (numBands == 1) {
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16) {
                canEncode = false;
            }
        }
        else if (numBands == 2) {
            if (bitDepth != 8 && bitDepth != 16) {
                canEncode = false;
            }
        }
        else if (numBands == 3) {
            if (bitDepth != 8 && bitDepth != 16) {
                canEncode = false;
            }
        }
        else if (numBands == 4) {
            if (bitDepth != 8 && bitDepth != 16) {
                canEncode = false;
            }
        }
        return canEncode;
    }
    @Override
    public ImageWriter createWriterInstance(Object arg0) throws IOException {
        return new PNGImageWriter(this);
    }
    @Override
    public String getDescription(Locale arg0) {
        return "DRL PNG encoder";
    }
}
