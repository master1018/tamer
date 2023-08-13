public class MimeBodyPartTest extends TestCase {
    public void testGetContentId() throws MessagingException {
        MimeBodyPart bp = new MimeBodyPart();
        assertNull(bp.getContentId());
        final String cid1 = "cid.1@android.com";
        bp.setHeader(MimeHeader.HEADER_CONTENT_ID, cid1);
        assertEquals(cid1, bp.getContentId());
        bp.setHeader(MimeHeader.HEADER_CONTENT_ID, "<" + cid1 + ">");
        assertEquals(cid1, bp.getContentId());
    }
}
