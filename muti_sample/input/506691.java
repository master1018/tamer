public class ImageDescriptor {
    int width;
    int height;
    int codingScheme;
    int imageId;
    int highOffset;
    int lowOffset;
    int length;
    static final int CODING_SCHEME_BASIC = 0x11;
    static final int CODING_SCHEME_COLOUR = 0x21;
    ImageDescriptor() {
        width = 0;
        height = 0;
        codingScheme = 0;
        imageId = 0;
        highOffset = 0;
        lowOffset = 0;
        length = 0;
    }
    static ImageDescriptor parse(byte[] rawData, int valueIndex) {
        ImageDescriptor d = new ImageDescriptor();
        try {
            d.width = rawData[valueIndex++] & 0xff;
            d.height = rawData[valueIndex++] & 0xff;
            d.codingScheme = rawData[valueIndex++] & 0xff;
            d.imageId = (rawData[valueIndex++] & 0xff) << 8;
            d.imageId |= rawData[valueIndex++] & 0xff;
            d.highOffset = (rawData[valueIndex++] & 0xff); 
            d.lowOffset = rawData[valueIndex++] & 0xff; 
            d.length = ((rawData[valueIndex++] & 0xff) << 8 | (rawData[valueIndex++] & 0xff));
        } catch (IndexOutOfBoundsException e) {
            StkLog.d("ImageDescripter", "parse; failed parsing image descriptor");
            d = null;
        }
        return d;
    }
}
