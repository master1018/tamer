@TestTargetClass(URLEncoder.class) 
public class URLEncoderTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.lang.String.class}
    )
    public void test_encodeLjava_lang_String() {
        final String URL = "http:
        final String URL2 = "telnet:
        final String URL3 = "file:
        try {
            assertTrue("1. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL)).equals(URL));
            assertTrue("2. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL2)).equals(URL2));
            assertTrue("3. Incorrect encoding/decoding", URLDecoder.decode(
                    URLEncoder.encode(URL3)).equals(URL3));
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_encodeLjava_lang_StringLjava_lang_String() {
        String enc = "UTF-8";
        String [] urls = {"http:
                              "/test?hl=en&q=te st",
                              "file:
                              "jar:file:
                              "ftp:
        String [] expected = { "http%3A%2F%2Fjcltest.apache.org%2Ftest%3Fhl%" +
                "3Den%26q%3Dte+st", 
                "file%3A%2F%2Fa+b%2Fc%2Fd.e-f*g_+l",
                "jar%3Afile%3A%2F%2Fa.jar+%21%2Fb.c%2F%E1%81%92"};        
        for(int i = 0; i < urls.length-1; i++) {
            try {
                String encodedString = URLEncoder.encode(urls[i], enc);
                assertEquals(expected[i], encodedString);
                assertEquals(urls[i], URLDecoder.decode(encodedString, enc));
            } catch (UnsupportedEncodingException e) {
                fail("UnsupportedEncodingException: " + e.getMessage());
            }
        }
        try {
            String encodedString = URLEncoder.encode(urls[urls.length - 1], enc);
            assertEquals(urls[urls.length - 1], URLDecoder.decode(encodedString, enc));
        } catch (UnsupportedEncodingException e) {
            fail("UnsupportedEncodingException: " + e.getMessage());
        }
        try {
            URLDecoder.decode("", "");
            fail("UnsupportedEncodingException expected");
        } catch (UnsupportedEncodingException e) {
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
