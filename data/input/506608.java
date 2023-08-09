public class MimeUtilityTest extends TestCase {
    private final String SHORT_UNICODE = "\u2191\u2193\u2190\u2192";
    private final String SHORT_UNICODE_ENCODED = "=?UTF-8?B?4oaR4oaT4oaQ4oaS?=";
    private final String PADDED2_UNICODE = "$\u20AC";
    private final String PADDED2_UNICODE_ENCODED = "=?UTF-8?B?JOKCrA==?=";
    private final String PADDED1_UNICODE = "$$\u20AC";
    private final String PADDED1_UNICODE_ENCODED = "=?UTF-8?B?JCTigqw=?=";
    private final String PADDED0_UNICODE = "$$$\u20AC";
    private final String PADDED0_UNICODE_ENCODED = "=?UTF-8?B?JCQk4oKs?=";
    private final String SHORT_PLAIN = "abcd";
    private final String LONG_UNICODE_SPLIT =
        "$" +
        "\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC" +
        "\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC\u20AC";
    private final String LONG_UNICODE_SPLIT_ENCODED =
        "=?UTF-8?B?JOKCrOKCrOKCrOKCrOKCrOKCrOKCrOKCrA==?=" + "\r\n " +
        "=?UTF-8?B?4oKs4oKs4oKs4oKs4oKs4oKs4oKs4oKs4oKs4oKs4oKs4oKs?=";
    private final String SHORT_SUPPLEMENTAL = "\uD801\uDC00";
    private final String SHORT_SUPPLEMENTAL_ENCODED = "=?UTF-8?B?8JCQgA==?=";
    private final String LONG_SUPPLEMENTAL = SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL +
        SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL +
        SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL;
    private final String LONG_SUPPLEMENTAL_ENCODED =
        "=?UTF-8?B?8JCQgPCQkIDwkJCA8JCQgA==?=" + "\r\n " + 
        "=?UTF-8?B?8JCQgPCQkIDwkJCA8JCQgPCQkIDwkJCA?=";
    private final String LONG_SUPPLEMENTAL_2 = "a" + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL +
        SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL +
        SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL + SHORT_SUPPLEMENTAL;
    private final String LONG_SUPPLEMENTAL_ENCODED_2 =
        "=?UTF-8?B?YfCQkIDwkJCA8JCQgPCQkIA=?=" + "\r\n " +
        "=?UTF-8?B?8JCQgPCQkIDwkJCA8JCQgPCQkIDwkJCA?="; 
    private final String LONG_SUPPLEMENTAL_QP =
        "*Monogram for Earth \uD834\uDF00. Monogram for Human \u268b."; 
    private final String LONG_SUPPLEMENTAL_QP_ENCODED =
        "=?UTF-8?Q?*Monogram_for_Earth_?=" + "\r\n " +
        "=?UTF-8?Q?=F0=9D=8C=80._Monogram_for_Human_=E2=9A=8B.?=";
    private final String HEADER_NO_PARAMETER = 
            "header";
    private final String HEADER_MULTI_PARAMETER = 
            "header; Param1Name=Param1Value; Param2Name=Param2Value";
    private final String CALENDAR_SUBJECT_UNICODE = 
        "=?windows-1252?Q?=5BReminder=5D_test_=40_Fri_Mar_20_10=3A30am_=96_11am_=28andro?=" +
        "\r\n\t" +
        "=?windows-1252?Q?id=2Etr=40gmail=2Ecom=29?=";
    private final String CALENDAR_SUBJECT_PLAIN =
        "[Reminder] test @ Fri Mar 20 10:30am \u2013 11am (android.tr@gmail.com)";
    private final String CALENDAR_DEGENERATE_UNICODE_1 =
        "=?windows-1252?Q=5B?=";
    private final String CALENDAR_DEGENERATE_UNICODE_2 =
        "=?windows-1252Q?=5B?=";
    private final String CALENDAR_DEGENERATE_UNICODE_3 =
        "=?windows-1252?=";
    private final String CALENDAR_DEGENERATE_UNICODE_4 =
        "=?windows-1252";
    public void testEfficientUnfoldAndDecode() {
        String result1 = MimeUtility.unfold(SHORT_PLAIN);
        String result2 = MimeUtility.decode(SHORT_PLAIN);
        String result3 = MimeUtility.unfoldAndDecode(SHORT_PLAIN);
        assertSame(SHORT_PLAIN, result1);
        assertSame(SHORT_PLAIN, result2);
        assertSame(SHORT_PLAIN, result3);
    }
    public void testDecodeSimple() {
        String result1 = MimeUtility.decode(SHORT_UNICODE_ENCODED);
        assertEquals(SHORT_UNICODE, result1);
    }
    public void testUnfoldAndDecodeSimple() {
        String result1 = MimeUtility.unfoldAndDecode(SHORT_UNICODE_ENCODED);
        assertEquals(SHORT_UNICODE, result1);
    }
    public void testComplexDecode() {
        String result1 = MimeUtility.unfoldAndDecode(CALENDAR_SUBJECT_UNICODE);
        assertEquals(CALENDAR_SUBJECT_PLAIN, result1);
        String degenerate1 = MimeUtility.unfoldAndDecode(CALENDAR_DEGENERATE_UNICODE_1);
        assertEquals("degenerate case 1", CALENDAR_DEGENERATE_UNICODE_1, degenerate1);
        String degenerate2 = MimeUtility.unfoldAndDecode(CALENDAR_DEGENERATE_UNICODE_2);
        assertEquals("degenerate case 2", CALENDAR_DEGENERATE_UNICODE_2, degenerate2);
        String degenerate3 = MimeUtility.unfoldAndDecode(CALENDAR_DEGENERATE_UNICODE_3);
        assertEquals("degenerate case 3", CALENDAR_DEGENERATE_UNICODE_3, degenerate3);
        String degenerate4 = MimeUtility.unfoldAndDecode(CALENDAR_DEGENERATE_UNICODE_4);
        assertEquals("degenerate case 4", CALENDAR_DEGENERATE_UNICODE_4, degenerate4);
    }
    public void testEfficientFoldAndEncode() {
        String result1 = MimeUtility.foldAndEncode(SHORT_PLAIN);
        String result2 = MimeUtility.foldAndEncode2(SHORT_PLAIN, 10);
        String result3 = MimeUtility.fold(SHORT_PLAIN, 10);
        assertSame(SHORT_PLAIN, result1);
        assertSame(SHORT_PLAIN, result2);
        assertSame(SHORT_PLAIN, result3);
    }
    public void testPaddingOfFoldAndEncode2() {
        String result1 = MimeUtility.foldAndEncode2(PADDED2_UNICODE, 0);
        String result2 = MimeUtility.foldAndEncode2(PADDED1_UNICODE, 0);
        String result3 = MimeUtility.foldAndEncode2(PADDED0_UNICODE, 0);
        assertEquals("padding 2", PADDED2_UNICODE_ENCODED, result1);
        assertEquals("padding 1", PADDED1_UNICODE_ENCODED, result2);
        assertEquals("padding 0", PADDED0_UNICODE_ENCODED, result3);
    }
    public void testFoldAndEncode2() {
        String result1 = MimeUtility.foldAndEncode2(SHORT_UNICODE, 10);
        assertEquals(SHORT_UNICODE_ENCODED, result1);
    }
    public void testFoldAndEncode2WithLongSplit() {
        String result = MimeUtility.foldAndEncode2(LONG_UNICODE_SPLIT, "Subject: ".length()); 
        assertEquals("long string", LONG_UNICODE_SPLIT_ENCODED, result);
    }
    public void testFoldAndEncode2Supplemental() {
        String result1 = MimeUtility.foldAndEncode2(SHORT_SUPPLEMENTAL, "Subject: ".length());
        String result2 = MimeUtility.foldAndEncode2(LONG_SUPPLEMENTAL, "Subject: ".length());
        String result3 = MimeUtility.foldAndEncode2(LONG_SUPPLEMENTAL_2, "Subject: ".length());
        assertEquals("short supplemental", SHORT_SUPPLEMENTAL_ENCODED, result1);
        assertEquals("long supplemental", LONG_SUPPLEMENTAL_ENCODED, result2);
        assertEquals("long supplemental 2", LONG_SUPPLEMENTAL_ENCODED_2, result3);
    }
    public void testFoldAndEncode2SupplementalQuotedPrintable() {
        String result = MimeUtility.foldAndEncode2(LONG_SUPPLEMENTAL_QP, "Subject: ".length());
        assertEquals("long supplement quoted printable",
                     LONG_SUPPLEMENTAL_QP_ENCODED, result);
    }
    public void testGetHeaderParameter() {
        assertNull("null header check", MimeUtility.getHeaderParameter(null, "name"));
        assertEquals("null name first param per code", "header", 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, null));
        assertEquals("null name full header", HEADER_NO_PARAMETER, 
                MimeUtility.getHeaderParameter(HEADER_NO_PARAMETER, null));
        assertEquals("get 1st param", "Param1Value", 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, "Param1Name"));
        assertEquals("get 2nd param", "Param2Value", 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, "Param2Name"));
        assertEquals("get missing param", null, 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, "Param3Name"));
        assertEquals("get 2nd param all LC", "Param2Value", 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, "param2name"));
        assertEquals("get 2nd param all UC", "Param2Value", 
                MimeUtility.getHeaderParameter(HEADER_MULTI_PARAMETER, "PARAM2NAME"));
    }
    public void testFindPartByContentIdTestCase() throws MessagingException, Exception {
        final String cid1 = "cid.1@android.com";
        final Part cid1bp = MessageTestUtils.bodyPart("image/gif", cid1);
        final String cid2 = "cid.2@android.com";
        final Part cid2bp = MessageTestUtils.bodyPart("image/gif", "<" + cid2 + ">");
        final Message msg1 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/related")
                 .addBodyPart(MessageTestUtils.bodyPart("text/html", null))
                 .addBodyPart((BodyPart)cid1bp)
                 .build())
            .build();
        final Part actual1_1 = MimeUtility.findPartByContentId(msg1, cid1);
        assertEquals("could not found expected content-id part", cid1bp, actual1_1);
        final Message msg2 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/mixed")
                .addBodyPart(MessageTestUtils.bodyPart("image/tiff", "cid.4@android.com"))
                .addBodyPart(new MultipartBuilder("multipart/related")
                    .addBodyPart(new MultipartBuilder("multipart/alternative")
                        .addBodyPart(MessageTestUtils.bodyPart("text/plain", null))
                        .addBodyPart(MessageTestUtils.bodyPart("text/html", null))
                        .buildBodyPart())
                    .addBodyPart((BodyPart)cid1bp)
                    .buildBodyPart())
                .addBodyPart(MessageTestUtils.bodyPart("image/gif", "cid.3@android.com"))
                .addBodyPart((BodyPart)cid2bp)
                .build())
            .build();
        final Part actual2_1 = MimeUtility.findPartByContentId(msg2, cid1);
        assertEquals("found part from related multipart", cid1bp, actual2_1);
        final Part actual2_2 = MimeUtility.findPartByContentId(msg2, cid2);
        assertEquals("found part from mixed multipart", cid2bp, actual2_2);
    }
    public void testGetTextFromPartContentTypeCase() throws MessagingException {
        final String theText = "This is the text of the part";
        TextBody tb = new TextBody(theText);
        MimeBodyPart p = new MimeBodyPart();
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/plain");
        p.setBody(tb);
        String gotText = MimeUtility.getTextFromPart(p);
        assertEquals(theText, gotText);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "TEXT/PLAIN");
        p.setBody(tb);
        gotText = MimeUtility.getTextFromPart(p);
        assertEquals(theText, gotText);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/other");
        p.setBody(tb);
        gotText = MimeUtility.getTextFromPart(p);
        assertEquals(theText, gotText);
    }
    public void testContentTypeCharset() throws MessagingException {
        final String UNICODE_EXPECT = "This is some happy unicode text \u263a";
        final String WINDOWS1252_EXPECT = "This is some happy unicode text \u00e2\u02dc\u00ba";
        TextBody tb = new TextBody(UNICODE_EXPECT);
        MimeBodyPart p = new MimeBodyPart();
        String gotText, mimeType, charset;
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/html; charset=utf-8");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(UNICODE_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/html; charset=windows-1252");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(WINDOWS1252_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                    "text/html; prop1 = \"test\"; charset = \"utf-8\"; prop2 = \"test\"");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(UNICODE_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                    "text/html; prop1 = \"test\"; charset = \"windows-1252\"; prop2 = \"test\"");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(WINDOWS1252_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "TEXT/HtmL ; CHARseT=utf-8");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(UNICODE_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "TEXT/HtmL ; CHARseT=windows-1252");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        assertEquals(WINDOWS1252_EXPECT, gotText);
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                    "text/html; charset=utf-8 (Plain text)");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
        p.setBody(tb);
        p.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                    "text/html; charset=windows-1252 (Plain text)");
        gotText = MimeUtility.getTextFromPart(p);
        assertTrue(MimeUtility.mimeTypeMatches(p.getMimeType(), "text/html"));
    }
    public void testMimeTypeMatches() {
        assertFalse(MimeUtility.mimeTypeMatches("foo/bar", "TEXT/PLAIN"));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", "text/plain"));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", "TEXT/PLAIN"));
        assertTrue(MimeUtility.mimeTypeMatches("TEXT/PLAIN", "text/plain"));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", "*/plain"));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", "text*"));
        assertFalse(MimeUtility.mimeTypeMatches("foo/bar", "*/plain"));
        assertFalse(MimeUtility.mimeTypeMatches("foo/bar", "text
    public void testMimeTypeMatchesArray() {
        String[] arrayZero = new String[0];
        assertFalse(MimeUtility.mimeTypeMatches("text/plain", arrayZero));
        String[] arrayOne = new String[] { "text/plain" };
        assertFalse(MimeUtility.mimeTypeMatches("foo/bar", arrayOne));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", arrayOne));
        String[] arrayTwo = new String[] { "text/plain", "match/this" };
        assertFalse(MimeUtility.mimeTypeMatches("foo/bar", arrayTwo));
        assertTrue(MimeUtility.mimeTypeMatches("text/plain", arrayTwo));
        assertTrue(MimeUtility.mimeTypeMatches("match/this", arrayTwo));
    }
}
