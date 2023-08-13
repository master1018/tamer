@TestTargetClass(URLDecoder.class) 
public class URLDecoderTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "URLDecoder",
        args = {}
    )
    public void test_Constructor() throws Exception {
        URLDecoder ud = new URLDecoder();
        assertNotNull("Constructor failed.", ud);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class}
    )
    public void test_decodeLjava_lang_String() throws Exception {
        final String URL = "http:
        final String URL2 = "telnet:
        final String URL3 = "file:
        assertTrue("1. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL)).equals(URL));
        assertTrue("2. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL2)).equals(URL2));
        assertTrue("3. Incorrect encoding/decoding", URLDecoder.decode(
                URLEncoder.encode(URL3)).equals(URL3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_decodeLjava_lang_String_Ljava_lang_String() {
        String enc = "UTF-8";
        String [] urls = { "http:
                           "/test?hl=en&q=te+st", 
                           "file:
                           "jar:file:
                           "ftp:
                           "%D0%A2%D0%B5%D1%81%D1%82+URL+for+test"}; 
        String [] expected = {"http:
                              "/test?hl=en&q=te st",
                              "file:
                              "jar:file:
        for(int i = 0; i < urls.length - 2; i++) {
            try {
                assertEquals(expected[i], URLDecoder.decode(urls[i], enc));
            } catch (UnsupportedEncodingException e) {
                fail("UnsupportedEncodingException: " + e.getMessage());
            }
        }
        try {
            URLDecoder.decode(urls[urls.length - 2], enc);
            URLDecoder.decode(urls[urls.length - 1], enc);
        } catch (UnsupportedEncodingException e) {
            fail("UnsupportedEncodingException: " + e.getMessage());
        }
        try {
            URLDecoder.decode("", "");
            fail("UnsupportedEncodingException expected");
        } catch (UnsupportedEncodingException e) {
        }
    }
}
