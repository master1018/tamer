public class test {
    @Test
    public void testAuthorizedFileNotFound() throws IOException {
        final URL url = new URL("http:
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        con.setRequestProperty("WWW-Authenticate", "Basic realm=\"karatasi\"");
        assertEquals("Expecting resource to not exist.", HttpURLConnection.HTTP_NOT_FOUND, con.getResponseCode());
        final InputStream err = con.getErrorStream();
        assertNotNull("Expecting an error stream.", err);
    }
}
