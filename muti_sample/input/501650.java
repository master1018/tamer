public class AccessPrivateDataTest extends AndroidTestCase {
    private static final String APP_WITH_DATA_PKG = "com.android.cts.appwithdata";
    private static final String PRIVATE_FILE_NAME = "private_file.txt";
    public void testAccessPrivateData() throws IOException {
        try {
            String privateFilePath = String.format("/data/data/%s/%s", APP_WITH_DATA_PKG,
                    PRIVATE_FILE_NAME);
            FileInputStream inputStream = new FileInputStream(privateFilePath);
            inputStream.read();
            inputStream.close();
            fail("Was able to access another app's private data");
        } catch (FileNotFoundException e) {
        } catch (SecurityException e) {
        }
    }
}
