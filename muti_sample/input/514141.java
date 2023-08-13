public class MailTransportUnitTests extends AndroidTestCase {
    public void testUriParsing() throws URISyntaxException {
        URI uri = new URI("smtp:
        MailTransport transport = new MailTransport("SMTP");
        transport.setUri(uri, 888);
        assertEquals("server.com", transport.getHost());
        assertEquals(999, transport.getPort());
        String[] userInfoParts = transport.getUserInfoParts();
        assertNotNull(userInfoParts);
        assertEquals("user", userInfoParts[0]);
        assertEquals("password", userInfoParts[1]);
        uri = new URI("smtp:
        transport = new MailTransport("SMTP");
        transport.setUri(uri, 888);
        assertEquals("server.com", transport.getHost());
        assertEquals(999, transport.getPort());
        assertNull(transport.getUserInfoParts());
    }
}
