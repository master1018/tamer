public class test {
    @Test
    public void testUnauthorizedMirror() throws IOException {
        final URL url = new URL("http:
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("testline1", "1234567890");
        assertEquals("Expecting request to be unauthorized.", HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
        final InputStream err = con.getErrorStream();
        assertNotNull("Expecting an error stream.", err);
    }
}
