public class CallerInfoIntegrationTest extends BaseContactsProvider2Test {
    public void testCallerInfo() {
        ContentValues values = new ContentValues();
        values.put(RawContacts.CUSTOM_RINGTONE, "ring");
        values.put(RawContacts.SEND_TO_VOICEMAIL, 1);
        Uri rawContactUri = mResolver.insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        insertStructuredName(rawContactId, "Hot", "Tamale");
        insertPhoneNumber(rawContactId, "800-466-4411");
        CallerInfo callerInfo = CallerInfo.getCallerInfo(getProvider().getContext(), "18004664411");
        assertEquals("800-466-4411", callerInfo.phoneNumber);
        assertEquals("Home", callerInfo.phoneLabel);
        assertEquals("Hot Tamale", callerInfo.name);
        assertEquals("ring", String.valueOf(callerInfo.contactRingtoneUri));
        assertEquals(true, callerInfo.shouldSendToVoicemail);
        assertEquals("content:
                String.valueOf(callerInfo.contactRefUri));
    }
}
