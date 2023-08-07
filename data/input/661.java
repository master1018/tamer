public class DICOMImageReader extends ImageReader {
    private ImageInputStream stream;
    private boolean gotHeader = false;
    public DICOMImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    @Override
    public int getHeight(int imageIndex) throws IOException {
        return 0;
    }
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return null;
    }
    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 0;
    }
    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }
    @Override
    public int getWidth(int imageIndex) throws IOException {
        return 0;
    }
    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        readHeader();
        return null;
    }
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input != null) {
            if (input instanceof ImageInputStream) {
                this.stream = (ImageInputStream) input;
            } else {
                throw new IllegalArgumentException("Input is not an ImageInputStream!");
            }
        }
    }
    public void readHeader() throws IOException {
        if (this.gotHeader) {
            return;
        }
        this.gotHeader = true;
        if (this.stream == null) {
            throw new IllegalStateException("No input stream");
        }
        byte[] b = new byte[132];
        try {
            stream.readFully(b);
        } catch (IOException e) {
            throw new IOException("Error reading signature", e);
        }
        if (b[128] != (byte) 'D' || b[129] != (byte) 'I' || b[130] != (byte) 'C' || b[131] != (byte) 'M') {
            throw new IOException("Bad file signature");
        }
        stream.mark();
        b = new byte[2];
        stream.readFully(b, 0, 2);
        System.out.println("File Meta Information Group length VR " + StringUtils.decode(b, "ISO-8859-1"));
        b = new byte[4];
        stream.readFully(b, 0, 4);
        System.out.println("File Meta Information Group length (Big endian) " + ByteUtils.bytesBE2int(b, 0));
        System.out.println("File Meta Information Group length (Little Endian) " + ByteUtils.bytesLE2int(b, 0));
        b = new byte[2];
        stream.readFully(b, 0, 2);
        System.out.println("File Meta Information Version VR " + StringUtils.decode(b, "ISO-8859-1"));
        b = new byte[4];
        stream.readFully(b, 0, 4);
        System.out.println("File Meta Information Version (BE) " + ByteUtils.bytesBE2int(b, 0));
        System.out.println("File Meta Information Version (LE) " + ByteUtils.bytesLE2int(b, 0));
    }
}
