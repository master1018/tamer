public class JPEGImageReaderResources extends ListResourceBundle {
    public JPEGImageReaderResources() {}
    protected Object[][] getContents() {
        return new Object[][] {
        {Integer.toString(JPEGImageReader.WARNING_NO_EOI),
         "Truncated File - Missing EOI marker"},
        {Integer.toString(JPEGImageReader.WARNING_NO_JFIF_IN_THUMB),
         "JFIF markers not allowed in JFIF JPEG thumbnail; ignored"},
        {Integer.toString(JPEGImageReader.WARNING_IGNORE_INVALID_ICC),
         "Embedded color profile is invalid; ignored"}
        };
    }
}
