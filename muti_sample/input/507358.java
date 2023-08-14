public class CreatePrivateDataTest extends AndroidTestCase {
    private static final String PRIVATE_FILE_NAME = "private_file.txt";
    public void testCreatePrivateData() throws IOException {
        FileOutputStream outputStream = getContext().openFileOutput(PRIVATE_FILE_NAME,
                Context.MODE_PRIVATE);
        outputStream.write("file contents".getBytes());
        outputStream.close();
    }
}
