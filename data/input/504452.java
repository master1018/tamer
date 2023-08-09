@TestTargetClass(FileNameMap.class) 
public class FileNameMapTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getContentTypeFor",
        args = {java.lang.String.class}
    )
    public void test_getContentTypeFor() {
        String [] files = {"text", "txt", "htm", "html"}; 
        String [] mimeTypes = {"text/plain", "text/plain", 
                "text/html", "text/html"}; 
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        for(int i = 0; i < files.length; i++) {
            String mimeType = fileNameMap.getContentTypeFor("test." + files[i]);
            assertEquals("getContentTypeFor returns incorrect MIME type for " +
                    files[i], mimeTypes[i], mimeType);
        }
    } 
}
