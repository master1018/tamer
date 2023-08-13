public class UtilityUnitTests extends AndroidTestCase {
    public void testImapQuote() {
        assertEquals("\"abcd\"", Utility.imapQuoted("abcd"));
        assertEquals("\"ab\\\"cd\"", Utility.imapQuoted("ab\"cd"));
        assertEquals("\"ab\\\\cd\"", Utility.imapQuoted("ab\\cd"));
    }
    public void testGetDisplayName() {
        Context context = getContext();
        String expect, name;
        expect = context.getString(R.string.mailbox_name_display_inbox);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_INBOX);
        assertEquals(expect, name);
        expect = null;
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_MAIL);
        assertEquals(expect, name);
        expect = null;
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_PARENT);
        assertEquals(expect, name);
        expect = context.getString(R.string.mailbox_name_display_drafts);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_DRAFTS);
        assertEquals(expect, name);
        expect = context.getString(R.string.mailbox_name_display_outbox);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_OUTBOX);
        assertEquals(expect, name);
        expect = context.getString(R.string.mailbox_name_display_sent);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_SENT);
        assertEquals(expect, name);
        expect = context.getString(R.string.mailbox_name_display_trash);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_TRASH);
        assertEquals(expect, name);
        expect = context.getString(R.string.mailbox_name_display_junk);
        name = Utility.FolderProperties.getInstance(context).getDisplayName(Mailbox.TYPE_JUNK);
        assertEquals(expect, name);
        expect = null;
        name = Utility.FolderProperties.getInstance(context).getDisplayName(8);
        assertEquals(expect, name);
    }
    public void testSpecialIcons() {
        Utility.FolderProperties fp = Utility.FolderProperties.getInstance(mContext);
        Drawable inbox = fp.getIconIds(Mailbox.TYPE_INBOX);
        Drawable mail = fp.getIconIds(Mailbox.TYPE_MAIL);
        Drawable parent = fp.getIconIds(Mailbox.TYPE_PARENT);
        Drawable drafts = fp.getIconIds(Mailbox.TYPE_DRAFTS);
        Drawable outbox = fp.getIconIds(Mailbox.TYPE_OUTBOX);
        Drawable sent = fp.getIconIds(Mailbox.TYPE_SENT);
        Drawable trash = fp.getIconIds(Mailbox.TYPE_TRASH);
        Drawable junk = fp.getIconIds(Mailbox.TYPE_JUNK);
        Set<Drawable> set = new HashSet<Drawable>();
        set.add(inbox);
        set.add(mail);
        set.add(parent);
        set.add(drafts);
        set.add(outbox);
        set.add(sent);
        set.add(trash);
        set.add(junk);
        assertEquals(8, set.size());
    }
    private static byte[] b(int... array) {
        return TestUtils.b(array);
    }
    public void testToUtf8() {
        assertNull(Utility.toUtf8(null));
        MoreAsserts.assertEquals(new byte[] {}, Utility.toUtf8(""));
        MoreAsserts.assertEquals(b('a'), Utility.toUtf8("a"));
        MoreAsserts.assertEquals(b('A', 'B', 'C'), Utility.toUtf8("ABC"));
        MoreAsserts.assertEquals(b(0xE6, 0x97, 0xA5, 0xE6, 0x9C, 0xAC, 0xE8, 0xAA, 0x9E),
                Utility.toUtf8("\u65E5\u672C\u8A9E"));
    }
    public void testFromUtf8() {
        assertNull(Utility.fromUtf8(null));
        assertEquals("", Utility.fromUtf8(new byte[] {}));
        assertEquals("a", Utility.fromUtf8(b('a')));
        assertEquals("ABC", Utility.fromUtf8(b('A', 'B', 'C')));
        assertEquals("\u65E5\u672C\u8A9E",
                Utility.fromUtf8(b(0xE6, 0x97, 0xA5, 0xE6, 0x9C, 0xAC, 0xE8, 0xAA, 0x9E)));
    }
    public void testIsFirstUtf8Byte() {
        checkIsFirstUtf8Byte("0"); 
        checkIsFirstUtf8Byte("A"); 
        checkIsFirstUtf8Byte("\u00A2"); 
        checkIsFirstUtf8Byte("\u20AC"); 
        checkIsFirstUtf8Byte("\uD852\uDF62"); 
    }
    private void checkIsFirstUtf8Byte(String aChar) {
        byte[] bytes = Utility.toUtf8(aChar);
        assertTrue("0", Utility.isFirstUtf8Byte(bytes[0]));
        for (int i = 1; i < bytes.length; i++) {
            assertFalse(Integer.toString(i), Utility.isFirstUtf8Byte(bytes[i]));
        }
    }
    public void testByteToHex() {
        for (int i = 0; i <= 0xFF; i++) {
            String hex = Utility.byteToHex((byte) i);
            assertEquals("val=" + i, 2, hex.length());
            assertEquals("val=" + i, i, Integer.parseInt(hex, 16));
        }
    }
    public void testReplaceBareLfWithCrlf() {
        assertEquals("", Utility.replaceBareLfWithCrlf(""));
        assertEquals("", Utility.replaceBareLfWithCrlf("\r"));
        assertEquals("\r\n", Utility.replaceBareLfWithCrlf("\r\n"));
        assertEquals("\r\n", Utility.replaceBareLfWithCrlf("\n"));
        assertEquals("\r\n\r\n\r\n", Utility.replaceBareLfWithCrlf("\n\n\n"));
        assertEquals("A\r\nB\r\nC\r\nD", Utility.replaceBareLfWithCrlf("A\nB\r\nC\nD"));
    }
    public void testGetConsistentDeviceId() {
        TelephonyManager tm =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            Log.w(Email.LOG_TAG, "TelephonyManager not supported.  Skipping.");
            return;
        }
        final String deviceId = Utility.getConsistentDeviceId(getContext());
        assertNotNull(deviceId);
        final String deviceId2 = Utility.getConsistentDeviceId(getContext());
        assertEquals(deviceId, deviceId2);
    }
    public void testGetSmallSha1() {
        byte[] sha1 = new byte[20];
        assertEquals(0, Utility.getSmallHashFromSha1(sha1));
        for (int i = 0; i < sha1.length; i++) {
            sha1[i] = (byte) 0xFF;
        }
        assertEquals(Integer.MAX_VALUE, Utility.getSmallHashFromSha1(sha1));
        for (int i = 0; i < 16; i++) {
            sha1[19] = (byte) i;
            Utility.getSmallHashFromSha1(sha1);
        }
    }
}
