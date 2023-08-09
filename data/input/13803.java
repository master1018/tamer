public class ByteArrayImageSource extends InputStreamImageSource {
    byte[] imagedata;
    int imageoffset;
    int imagelength;
    public ByteArrayImageSource(byte[] data) {
        this(data, 0, data.length);
    }
    public ByteArrayImageSource(byte[] data, int offset, int length) {
        imagedata = data;
        imageoffset = offset;
        imagelength = length;
    }
    final boolean checkSecurity(Object context, boolean quiet) {
        return true;
    }
    protected ImageDecoder getDecoder() {
        InputStream is =
            new BufferedInputStream(new ByteArrayInputStream(imagedata,
                                                             imageoffset,
                                                             imagelength));
        return getDecoder(is);
    }
}
