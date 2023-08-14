public class BMPImageWriteParam extends ImageWriteParam {
    private boolean topDown; 
    public BMPImageWriteParam() {
        this(null);
    }
    public BMPImageWriteParam(Locale locale) {
        super(locale);
        canWriteCompressed = true;
        compressionTypes = new String[] {
                "BI_RGB", "BI_RLE8", "BI_RLE4", "BI_BITFIELDS"
        };
        compressionType = compressionTypes[0];
    }
    public void setTopDown(boolean topDown) {
        this.topDown = topDown;
    }
    public boolean isTopDown() {
        return topDown;
    }
}
