public class JPEGStreamMetadataFormatResources
       extends JPEGMetadataFormatResources {
    public JPEGStreamMetadataFormatResources() {}
    protected Object[][] getContents() {
        Object[][] commonCopy = new Object[commonContents.length][2];
        for (int i = 0; i < commonContents.length; i++) {
            commonCopy[i][0] = commonContents[i][0];
            commonCopy[i][1] = commonContents[i][1];
        }
        return commonCopy;
    }
}
