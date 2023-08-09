public class BMPImageWriteParam extends ImageWriteParam {
    private boolean topDown = false;
    public BMPImageWriteParam(Locale locale) {
        super(locale);
        compressionTypes = BMPConstants.compressionTypeNames.clone();
        canWriteCompressed = true;
        compressionMode = MODE_COPY_FROM_METADATA;
        compressionType = compressionTypes[BMPConstants.BI_RGB];
    }
    public BMPImageWriteParam() {
        this(null);
    }
    public void setTopDown(boolean topDown) {
        this.topDown = topDown;
    }
    public boolean isTopDown() {
        return topDown;
    }
}
