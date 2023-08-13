public class ByteArrayDecodingImageSource extends DecodingImageSource {
    byte imagedata[];
    int imageoffset;
    int imagelength;
    public ByteArrayDecodingImageSource(byte imagedata[], int imageoffset,
            int imagelength){
        this.imagedata = imagedata;
        this.imageoffset = imageoffset;
        this.imagelength = imagelength;
    }
    public ByteArrayDecodingImageSource(byte imagedata[]){
        this(imagedata, 0, imagedata.length);
    }
    @Override
    protected boolean checkConnection() {
        return true;
    }
    @Override
    protected InputStream getInputStream() {
        return new BufferedInputStream(new ByteArrayInputStream(imagedata,
                        imageoffset, imagelength), 1024);
    }
}
