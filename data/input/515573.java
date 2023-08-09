public class CalendarUtilitiesTests extends AndroidTestCase {
    private static final String ASIA_CALCUTTA_TIME =
        "tv7
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEkAbgBkAGkAYQAgAEQAYQB5AGwAaQBnAGgAdAAgAFQAaQBtAGUA" +
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    private static final String AMERICA_DAWSON_TIME =
        "4AEAAFAAYQBjAGkAZgBpAGMAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAA" +
        "AAAAAAAAAAsAAAABAAIAAAAAAAAAAAAAAFAAYQBjAGkAZgBpAGMAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkA" +
        "bQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAACAAIAAAAAAAAAxP
    private static final String AUSTRALIA_ACT_TIME =
        "qP3
        "AAAAAAAAAAQAAAABAAMAAAAAAAAAAAAAAEEAVQBTACAARQBhAHMAdABlAHIAbgAgAEQAYQB5AGwAaQBnAGgA" +
        "dAAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAoAAAABAAIAAAAAAAAAxP
    private static final String EUROPE_MOSCOW_TIME =
        "TP
        "AAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAdQBzAHMAaQBhAG4AIABEAGEAeQBsAGkAZwBoAHQAIABUAGkA" +
        "bQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP
    private static final String GMT_UNKNOWN_DAYLIGHT_TIME =
        "AAAAACgARwBNAFQAKwAwADAAOgAwADAAKQAgAFQAaQBtAGUAIABaAG8AbgBlAAAAAAAAAAAAAAAAAAAAAAAA" +
        "AAAAAAAAAAEAAAABAAAAAAAAAAAAAAAAACgARwBNAFQAKwAwADAAOgAwADAAKQAgAFQAaQBtAGUAIABaAG8A" +
        "bgBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAEAAAAAAAAAxP
    private static final String ORGANIZER = "organizer@server.com";
    private static final String ATTENDEE = "attendee@server.com";
    public void testGetSet() {
        byte[] bytes = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
        assertEquals(0x0100, CalendarUtilities.getWord(bytes, 0));
        assertEquals(0x03020100, CalendarUtilities.getLong(bytes, 0));
        assertEquals(0x07060504, CalendarUtilities.getLong(bytes, 4));
        CalendarUtilities.setWord(bytes, 0, 0xDEAD);
        CalendarUtilities.setLong(bytes, 2, 0xBEEFBEEF);
        CalendarUtilities.setWord(bytes, 6, 0xCEDE);
        assertEquals(0xDEAD, CalendarUtilities.getWord(bytes, 0));
        assertEquals(0xBEEFBEEF, CalendarUtilities.getLong(bytes, 2));
        assertEquals(0xCEDE, CalendarUtilities.getWord(bytes, 6));
    }
    public void testParseTimeZoneEndToEnd() {
        TimeZone tz = CalendarUtilities.tziStringToTimeZone(AMERICA_DAWSON_TIME);
        assertEquals("America/Dawson", tz.getID());
        tz = CalendarUtilities.tziStringToTimeZone(ASIA_CALCUTTA_TIME);
        assertEquals("Asia/Calcutta", tz.getID());
        tz = CalendarUtilities.tziStringToTimeZone(AUSTRALIA_ACT_TIME);
        assertEquals("Australia/ACT", tz.getID());
        tz = CalendarUtilities.tziStringToTimeZone(EUROPE_MOSCOW_TIME);
        assertEquals("Europe/Moscow", tz.getID());
        tz = CalendarUtilities.tziStringToTimeZone(GMT_UNKNOWN_DAYLIGHT_TIME);
        int bias = tz.getOffset(System.currentTimeMillis());
        assertEquals(0, bias);
    }
    public void testGenerateEasDayOfWeek() {
        String byDay = "TU,WE,SA";
        assertEquals("76", CalendarUtilities.generateEasDayOfWeek(byDay));
        byDay = "MO,TU,WE,TH,FR";
        assertEquals("62", CalendarUtilities.generateEasDayOfWeek(byDay));
        byDay = "SU";
        assertEquals("1", CalendarUtilities.generateEasDayOfWeek(byDay));
    }
    public void testTokenFromRrule() {
        String rrule = "FREQ=DAILY;INTERVAL=1;BYDAY=WE,TH,SA;BYMONTHDAY=17";
        assertEquals("DAILY", CalendarUtilities.tokenFromRrule(rrule, "FREQ="));
        assertEquals("1", CalendarUtilities.tokenFromRrule(rrule, "INTERVAL="));
        assertEquals("17", CalendarUtilities.tokenFromRrule(rrule, "BYMONTHDAY="));
        assertEquals("WE,TH,SA", CalendarUtilities.tokenFromRrule(rrule, "BYDAY="));
        assertNull(CalendarUtilities.tokenFromRrule(rrule, "UNTIL="));
    }
    public void testRecurrenceUntilToEasUntil() {
        assertEquals("YYYYMMDDT000000Z",
                CalendarUtilities.recurrenceUntilToEasUntil("YYYYMMDDTHHMMSSZ"));
        assertEquals("YYYYMMDDT000000Z",
                CalendarUtilities.recurrenceUntilToEasUntil("YYYYMMDD"));
    }
    public void testParseEmailDateTimeToMillis(String date) {
        String dateString = "2010-02-23T15:16:17.000Z";
        long dateTime = Utility.parseEmailDateTimeToMillis(dateString);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(dateTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(cal.get(Calendar.YEAR), 2010);
        assertEquals(cal.get(Calendar.MONTH), 1);  
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 16);
        assertEquals(cal.get(Calendar.SECOND), 17);
    }
    public void testParseDateTimeToMillis(String date) {
        String dateString = "20100223T151617000Z";
        long dateTime = Utility.parseDateTimeToMillis(dateString);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(dateTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(cal.get(Calendar.YEAR), 2010);
        assertEquals(cal.get(Calendar.MONTH), 1);  
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 16);
        assertEquals(cal.get(Calendar.SECOND), 17);
    }
    private Entity setupTestEventEntity(String organizer, String attendee, String title) {
        ContentValues entityValues = new ContentValues();
        Entity entity = new Entity(entityValues);
        String location = "Meeting Location";
        entityValues.put("DTSTAMP",
                CalendarUtilities.convertEmailDateTimeToCalendarDateTime("2010-04-05T14:30:51Z"));
        entityValues.put(Events.DTSTART,
                Utility.parseEmailDateTimeToMillis("2010-04-12T18:30:00Z"));
        entityValues.put(Events.DTEND,
                Utility.parseEmailDateTimeToMillis("2010-04-12T19:30:00Z"));
        entityValues.put(Events.EVENT_LOCATION, location);
        entityValues.put(Events.TITLE, title);
        entityValues.put(Events.ORGANIZER, organizer);
        entityValues.put(Events._SYNC_DATA, "31415926535");
        ContentValues attendeeValues = new ContentValues();
        attendeeValues.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ATTENDEE);
        attendeeValues.put(Attendees.ATTENDEE_EMAIL, attendee);
        entity.addSubValue(Attendees.CONTENT_URI, attendeeValues);
        ContentValues organizerValues = new ContentValues();
        organizerValues.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ORGANIZER);
        organizerValues.put(Attendees.ATTENDEE_EMAIL, organizer);
        entity.addSubValue(Attendees.CONTENT_URI, organizerValues);
        return entity;
    }
    private Entity setupTestExceptionEntity(String organizer, String attendee, String title) {
        Entity entity = setupTestEventEntity(organizer, attendee, title);
        ContentValues entityValues = entity.getEntityValues();
        entityValues.put(Events.ORIGINAL_EVENT, 69);
        entityValues.put(Events.ORIGINAL_INSTANCE_TIME,
                Utility.parseEmailDateTimeToMillis("2010-04-26T18:30:00Z"));
        return entity;
    }
    public void testCreateMessageForEntity_Reply() {
        String title = "Discuss Unit Tests";
        Entity entity = setupTestEventEntity(ORGANIZER, ATTENDEE, title);
        Account account = new Account();
        account.mEmailAddress = ATTENDEE;
        String uid = "31415926535";
        Message msg = CalendarUtilities.createMessageForEntity(mContext, entity,
                Message.FLAG_OUTGOING_MEETING_ACCEPT, uid, account);
        assertNotNull(msg);
        assertEquals(Address.pack(new Address[] {new Address(ORGANIZER)}), msg.mTo);
        Resources resources = getContext().getResources();
        String accept = resources.getString(R.string.meeting_accepted, title);
        assertEquals(accept, msg.mSubject);
        assertNotNull(msg.mText);
        assertTrue(msg.mText.contains(resources.getString(R.string.meeting_where, "")));
        assertNotNull(msg.mAttachments);
        assertEquals(1, msg.mAttachments.size());
        Attachment att = msg.mAttachments.get(0);
        assertEquals("invite.ics", att.mFileName);
        assertEquals(Attachment.FLAG_ICS_ALTERNATIVE_PART,
                att.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART);
        assertEquals("text/calendar; method=REPLY", att.mMimeType);
        assertNotNull(att.mContentBytes);
        assertEquals(att.mSize, att.mContentBytes.length);
    }
    public void testCreateMessageForEntity_Invite_AllDay() throws IOException {
        String title = "Discuss Unit Tests";
        Entity entity = setupTestEventEntity(ORGANIZER, ATTENDEE, title);
        entity.getEntityValues().put(Events.ALL_DAY, 1);
        Account account = new Account();
        account.mEmailAddress = ORGANIZER;
        String uid = "31415926535";
        Message msg = CalendarUtilities.createMessageForEntity(mContext, entity,
                Message.FLAG_OUTGOING_MEETING_INVITE, uid, account);
        assertNotNull(msg);
        assertEquals(Address.pack(new Address[] {new Address(ATTENDEE)}), msg.mTo);
        assertEquals(title, msg.mSubject);
        assertNotNull(msg.mAttachments);
        assertEquals(1, msg.mAttachments.size());
        Attachment att = msg.mAttachments.get(0);
        assertEquals("invite.ics", att.mFileName);
        assertEquals(Attachment.FLAG_ICS_ALTERNATIVE_PART,
                att.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART);
        assertEquals("text/calendar; method=REQUEST", att.mMimeType);
        assertNotNull(att.mContentBytes);
        assertEquals(att.mSize, att.mContentBytes.length);
        BlockHash vcalendar = parseIcsContent(att.mContentBytes);
        assertNotNull(vcalendar);
        assertEquals("VCALENDAR", vcalendar.name);
        assertEquals("REQUEST", vcalendar.get("METHOD"));
        assertEquals(1, vcalendar.blocks.size());
        BlockHash vevent = vcalendar.blocks.get(0);
        assertEquals("VEVENT", vevent.name);
        assertEquals("Meeting Location", vevent.get("LOCATION"));
        assertEquals("0", vevent.get("SEQUENCE"));
        assertEquals("Discuss Unit Tests", vevent.get("SUMMARY"));
        assertEquals(uid, vevent.get("UID"));
        assertEquals("MAILTO:" + ATTENDEE,
                vevent.get("ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE"));
        assertEquals("20100412", vevent.get("DTSTART;VALUE=DATE"));
        assertEquals("20100412", vevent.get("DTEND;VALUE=DATE"));
        assertEquals("TRUE", vevent.get("X-MICROSOFT-CDO-ALLDAYEVENT"));
    }
    public void testCreateMessageForEntity_Invite() throws IOException {
        String title = "Discuss Unit Tests";
        Entity entity = setupTestEventEntity(ORGANIZER, ATTENDEE, title);
        Account account = new Account();
        account.mEmailAddress = ORGANIZER;
        String uid = "31415926535";
        Message msg = CalendarUtilities.createMessageForEntity(mContext, entity,
                Message.FLAG_OUTGOING_MEETING_INVITE, uid, account);
        assertNotNull(msg);
        assertEquals(Address.pack(new Address[] {new Address(ATTENDEE)}), msg.mTo);
        assertEquals(title, msg.mSubject);
        assertNotNull(msg.mAttachments);
        assertEquals(1, msg.mAttachments.size());
        Attachment att = msg.mAttachments.get(0);
        assertEquals("invite.ics", att.mFileName);
        assertEquals(Attachment.FLAG_ICS_ALTERNATIVE_PART,
                att.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART);
        assertEquals("text/calendar; method=REQUEST", att.mMimeType);
        assertNotNull(att.mContentBytes);
        assertEquals(att.mSize, att.mContentBytes.length);
        BlockHash vcalendar = parseIcsContent(att.mContentBytes);
        assertNotNull(vcalendar);
        assertEquals("VCALENDAR", vcalendar.name);
        assertEquals("REQUEST", vcalendar.get("METHOD"));
        assertEquals(1, vcalendar.blocks.size());
        BlockHash vevent = vcalendar.blocks.get(0);
        assertEquals("VEVENT", vevent.name);
        assertEquals("Meeting Location", vevent.get("LOCATION"));
        assertEquals("0", vevent.get("SEQUENCE"));
        assertEquals("Discuss Unit Tests", vevent.get("SUMMARY"));
        assertEquals(uid, vevent.get("UID"));
        assertEquals("MAILTO:" + ATTENDEE,
                vevent.get("ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE"));
        assertNotNull(vevent.get("DTSTART"));
        assertNotNull(vevent.get("DTEND"));
        assertNull(vevent.get("DTSTART;VALUE=DATE"));
        assertNull(vevent.get("DTEND;VALUE=DATE"));
        assertNull(vevent.get("X-MICROSOFT-CDO-ALLDAYEVENT"));
    }
    public void testCreateMessageForEntity_Recurring() throws IOException {
        String title = "Discuss Unit Tests";
        Entity entity = setupTestEventEntity(ORGANIZER, ATTENDEE, title);
        entity.getEntityValues().put(Events.RRULE, "FREQ=DAILY");
        Account account = new Account();
        account.mEmailAddress = ORGANIZER;
        String uid = "31415926535";
        Message msg = CalendarUtilities.createMessageForEntity(mContext, entity,
                Message.FLAG_OUTGOING_MEETING_INVITE, uid, account);
        assertNotNull(msg);
        assertEquals(Address.pack(new Address[] {new Address(ATTENDEE)}), msg.mTo);
        assertEquals(title, msg.mSubject);
        assertNotNull(msg.mAttachments);
        assertEquals(1, msg.mAttachments.size());
        Attachment att = msg.mAttachments.get(0);
        assertEquals("invite.ics", att.mFileName);
        assertEquals(Attachment.FLAG_ICS_ALTERNATIVE_PART,
                att.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART);
        assertEquals("text/calendar; method=REQUEST", att.mMimeType);
        assertNotNull(att.mContentBytes);
        assertEquals(att.mSize, att.mContentBytes.length);
        BlockHash vcalendar = parseIcsContent(att.mContentBytes);
        assertNotNull(vcalendar);
        assertEquals("VCALENDAR", vcalendar.name);
        assertEquals("REQUEST", vcalendar.get("METHOD"));
        assertEquals(2, vcalendar.blocks.size());
        TimeZone timeZone = TimeZone.getDefault();
        BlockHash vtimezone = vcalendar.blocks.get(0);
        assertEquals("VTIMEZONE", vtimezone.name);
        assertEquals(timeZone.getID(), vtimezone.get("TZID"));
        BlockHash vevent = vcalendar.blocks.get(1);
        assertEquals("VEVENT", vevent.name);
        assertEquals("Meeting Location", vevent.get("LOCATION"));
        assertEquals("0", vevent.get("SEQUENCE"));
        assertEquals("Discuss Unit Tests", vevent.get("SUMMARY"));
        assertEquals(uid, vevent.get("UID"));
        assertEquals("MAILTO:" + ATTENDEE,
                vevent.get("ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE"));
        assertNotNull(vevent.get("DTSTART;TZID=" + timeZone.getID()));
        assertNotNull(vevent.get("DTEND;TZID=" + timeZone.getID()));
        assertNull(vevent.get("DTSTART"));
        assertNull(vevent.get("DTEND"));
        assertNull(vevent.get("DTSTART;VALUE=DATE"));
        assertNull(vevent.get("DTEND;VALUE=DATE"));
        assertNull(vevent.get("X-MICROSOFT-CDO-ALLDAYEVENT"));
    }
    public void testCreateMessageForEntity_Exception_Cancel() throws IOException {
        String title = "Discuss Unit Tests";
        Entity entity = setupTestExceptionEntity(ORGANIZER, ATTENDEE, title);
        ContentValues entityValues = entity.getEntityValues();
        entityValues.put(Events._SYNC_DIRTY, 1);
        entityValues.put(Events.STATUS, Events.STATUS_CANCELED);
        Account account = new Account();
        account.mEmailAddress = ORGANIZER;
        String uid = "31415926535";
        Message msg = CalendarUtilities.createMessageForEntity(mContext, entity,
                Message.FLAG_OUTGOING_MEETING_CANCEL, uid, account);
        assertNotNull(msg);
        assertEquals(Address.pack(new Address[] {new Address(ATTENDEE)}), msg.mTo);
        String cancel = getContext().getResources().getString(R.string.meeting_canceled, title);
        assertEquals(cancel, msg.mSubject);
        assertNotNull(msg.mAttachments);
        assertEquals(1, msg.mAttachments.size());
        Attachment att = msg.mAttachments.get(0);
        assertEquals("invite.ics", att.mFileName);
        assertEquals(Attachment.FLAG_ICS_ALTERNATIVE_PART,
                att.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART);
        assertEquals("text/calendar; method=CANCEL", att.mMimeType);
        assertNotNull(att.mContentBytes);
        BlockHash vcalendar = parseIcsContent(att.mContentBytes);
        assertNotNull(vcalendar);
        assertEquals("VCALENDAR", vcalendar.name);
        assertEquals("CANCEL", vcalendar.get("METHOD"));
        TimeZone timeZone = TimeZone.getDefault();
        assertEquals(2, vcalendar.blocks.size());
        BlockHash vtimezone = vcalendar.blocks.get(0);
        assertEquals("VTIMEZONE", vtimezone.name);
        assertEquals(timeZone.getID(), vtimezone.get("TZID"));
        BlockHash vevent = vcalendar.blocks.get(1);
        assertEquals("VEVENT", vevent.name);
        assertEquals("Meeting Location", vevent.get("LOCATION"));
        assertEquals("0", vevent.get("SEQUENCE"));
        assertEquals("Discuss Unit Tests", vevent.get("SUMMARY"));
        assertEquals(uid, vevent.get("UID"));
        assertEquals("MAILTO:" + ATTENDEE,
                vevent.get("ATTENDEE;ROLE=REQ-PARTICIPANT"));
        long originalTime = entityValues.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
        assertNotSame(0, originalTime);
        assertEquals(CalendarUtilities.millisToEasDateTime(originalTime, timeZone,
                true ), vevent.get("RECURRENCE-ID" + ";TZID=" + timeZone.getID()));
    }
    public void testUtcOffsetString() {
        assertEquals(CalendarUtilities.utcOffsetString(540), "+0900");
        assertEquals(CalendarUtilities.utcOffsetString(-480), "-0800");
        assertEquals(CalendarUtilities.utcOffsetString(0), "+0000");
    }
    public void testFindTransitionDate() {
        TimeZone tz = TimeZone.getTimeZone("US/Central");
        GregorianCalendar calendar = new GregorianCalendar(tz);
        calendar.set(CalendarUtilities.sCurrentYear, Calendar.JANUARY, 1);
        long startTime = calendar.getTimeInMillis();
        long endTime = startTime + (365*CalendarUtilities.DAYS);
        GregorianCalendar transitionCalendar =
            CalendarUtilities.findTransitionDate(tz, startTime, endTime, false);
        long transitionTime = transitionCalendar.getTimeInMillis();
        Date beforeDate = new Date(transitionTime - CalendarUtilities.HOURS);
        Date afterDate = new Date(transitionTime + CalendarUtilities.HOURS);
        assertFalse(tz.inDaylightTime(beforeDate));
        assertTrue(tz.inDaylightTime(afterDate));
        transitionCalendar = CalendarUtilities.findTransitionDate(tz, transitionTime +
                CalendarUtilities.DAYS, endTime, true);
        transitionTime = transitionCalendar.getTimeInMillis();
        beforeDate = new Date(transitionTime - CalendarUtilities.HOURS);
        afterDate = new Date(transitionTime + CalendarUtilities.HOURS);
        assertTrue(tz.inDaylightTime(beforeDate));
        assertFalse(tz.inDaylightTime(afterDate));
        tz = TimeZone.getTimeZone("Africa/Casablanca");
        calendar = new GregorianCalendar(tz);
        calendar.set(CalendarUtilities.sCurrentYear, Calendar.JANUARY, 1);
        startTime = calendar.getTimeInMillis();
        endTime = startTime + (365*CalendarUtilities.DAYS);
        transitionCalendar = CalendarUtilities.findTransitionDate(tz, startTime, endTime, false);
        assertNull(transitionCalendar);
    }
    public void testRruleFromRecurrence() {
        String rrule = CalendarUtilities.rruleFromRecurrence(
                1 , 2 , 1 , 2 , 0, 0, 0, null);
        assertEquals("FREQ=WEEKLY;INTERVAL=1;COUNT=2;BYDAY=MO", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                1 , 0 , 0 , 36 , 0, 0, 0, null);
        assertEquals("FREQ=WEEKLY;BYDAY=TU,FR", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                3 , 0, 0, 64 , 0, 5 , 0, null);
        assertEquals("FREQ=MONTHLY;BYDAY=-1SA", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                3 , 0, 0, 24 , 0, 3 , 0, null);
        assertEquals("FREQ=MONTHLY;BYDAY=3WE,3TH", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                2 , 0, 0, 0, 14 , 0, 0, null);
        assertEquals("FREQ=MONTHLY;BYMONTHDAY=14", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                5 , 0, 0, 0, 31 , 0, 10 , null);
        assertEquals("FREQ=YEARLY;BYMONTHDAY=31;BYMONTH=10", rrule);
        rrule = CalendarUtilities.rruleFromRecurrence(
                6 , 0, 0, 4 , 0, 1 , 6 , null);
        assertEquals("FREQ=YEARLY;BYDAY=1TU;BYMONTH=6", rrule);
    }
    private class UnterminatedBlockException extends IOException {
        private static final long serialVersionUID = 1L;
        UnterminatedBlockException(String name) {
            super(name);
        }
    }
    private class BlockHash {
        String name;
        HashMap<String, String> hash = new HashMap<String, String>();
        ArrayList<BlockHash> blocks = new ArrayList<BlockHash>();
        BlockHash (String _name, BufferedReader reader) throws IOException {
            name = _name;
            String lastField = null;
            String lastValue = null;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    throw new UnterminatedBlockException(name);
                }
                int length = line.length();
                if (length == 0) {
                    throw new IllegalArgumentException();
                }
                if (line.charAt(0) == '\t') {
                    lastValue = line.substring(1);
                    hash.put(lastField, hash.get(lastField) + lastValue);
                    continue;
                }
                int pos = line.indexOf(':');
                if (pos < 0 || pos >= length) {
                    throw new IllegalArgumentException();
                }
                lastField = line.substring(0, pos);
                lastValue = line.substring(pos + 1);
                if (lastField.equals("BEGIN")) {
                    blocks.add(new BlockHash(lastValue, reader));
                    continue;
                } else if (lastField.equals("END")) {
                    if (!lastValue.equals(name)) {
                        throw new UnterminatedBlockException(name);
                    }
                    break;
                }
                hash.put(lastField, lastValue);
            }
        }
        String get(String field) {
            return hash.get(field);
        }
    }
    private BlockHash parseIcsContent(byte[] bytes) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(Utility.fromUtf8(bytes)));
        String line = reader.readLine();
        if (!line.equals("BEGIN:VCALENDAR")) {
            throw new IllegalArgumentException();
        }
        return new BlockHash("VCALENDAR", reader);
    }
    public void testBuildMessageTextFromEntityValues() {
        String title = "Event Title";
        Entity entity = setupTestEventEntity(ORGANIZER, ATTENDEE, title);
        ContentValues entityValues = entity.getEntityValues();
        Resources resources = mContext.getResources();
        Date date = new Date(entityValues.getAsLong(Events.DTSTART));
        String dateTimeString = DateFormat.getDateTimeInstance().format(date);
        StringBuilder sb = new StringBuilder();
        CalendarUtilities.buildMessageTextFromEntityValues(mContext, entityValues, sb);
        String text = sb.toString();
        assertTrue(text.contains(resources.getString(R.string.meeting_when, dateTimeString)));
        String location = entityValues.getAsString(Events.EVENT_LOCATION);
        assertTrue(text.contains(resources.getString(R.string.meeting_where, location)));
        entity.getEntityValues().put(Events.RRULE, "FREQ=WEEKLY;BYDAY=MO");
        sb = new StringBuilder();
        CalendarUtilities.buildMessageTextFromEntityValues(mContext, entityValues, sb);
        text = sb.toString();
        assertTrue(text.contains(resources.getString(R.string.meeting_recurring, dateTimeString)));
    }
    public void testTimeZoneToVTimezone() throws IOException {
        SimpleIcsWriter writer = new SimpleIcsWriter();
        int rule = 0;
        int nodst = 0;
        int norule = 0;
        ArrayList<String> norulelist = new ArrayList<String>();
        for (String tzs: TimeZone.getAvailableIDs()) {
            TimeZone tz = TimeZone.getTimeZone(tzs);
            writer = new SimpleIcsWriter();
            CalendarUtilities.timeZoneToVTimezone(tz, writer);
            String vc = writer.toString();
            boolean hasRule = vc.indexOf("RRULE") > 0;
            if (hasRule) {
                rule++;
            } else if (tz.useDaylightTime()) {
                norule++;
                norulelist.add(tz.getID());
            } else {
                nodst++;
            }
        }
        assertTrue(norule < rule/10);
        Log.d("TimeZoneGeneration",
                "Rule: " + rule + ", No DST: " + nodst + ", No rule: " + norule);
        for (String nr: norulelist) {
            Log.d("TimeZoneGeneration", "No rule: " + nr);
        }
    }
    public void testGetUidFromGlobalObjId() {
        String globalObjId = "BAAAAIIA4AB0xbcQGoLgCAAAAAAAAAAAAAAAAAAAAAAAAAAAMQAAA" +
                "HZDYWwtVWlkAQAAADI3NjU1NmRkLTg1MzAtNGZiZS1iMzE0LThiM2JlYTYwMjE0OQA=";
        String uid = CalendarUtilities.getUidFromGlobalObjId(globalObjId);
        assertEquals(uid, "276556dd-8530-4fbe-b314-8b3bea602149");
        globalObjId =
            "BAAAAIIA4AB0xbcQGoLgCAAAAADACTu7KbPKAQAAAAAAAAAAEAAAAObgsG6HVt1Fmy+7GlLbGhY=";
        uid = CalendarUtilities.getUidFromGlobalObjId(globalObjId);
        assertEquals(uid, "040000008200E00074C5B7101A82E00800000000C0093BBB29B3CA" +
                "01000000000000000010000000E6E0B06E8756DD459B2FBB1A52DB1A16");
    }
    public void testSelfAttendeeStatusFromBusyStatus() {
        assertEquals(Attendees.ATTENDEE_STATUS_ACCEPTED,
                CalendarUtilities.attendeeStatusFromBusyStatus(
                        CalendarUtilities.BUSY_STATUS_BUSY));
        assertEquals(Attendees.ATTENDEE_STATUS_TENTATIVE,
                CalendarUtilities.attendeeStatusFromBusyStatus(
                        CalendarUtilities.BUSY_STATUS_TENTATIVE));
        assertEquals(Attendees.ATTENDEE_STATUS_NONE,
                CalendarUtilities.attendeeStatusFromBusyStatus(
                        CalendarUtilities.BUSY_STATUS_FREE));
        assertEquals(Attendees.ATTENDEE_STATUS_NONE,
                CalendarUtilities.attendeeStatusFromBusyStatus(
                        CalendarUtilities.BUSY_STATUS_OUT_OF_OFFICE));
    }
    public void testBusyStatusFromSelfStatus() {
        assertEquals(CalendarUtilities.BUSY_STATUS_FREE,
                CalendarUtilities.busyStatusFromAttendeeStatus(
                        Attendees.ATTENDEE_STATUS_DECLINED));
        assertEquals(CalendarUtilities.BUSY_STATUS_FREE,
                CalendarUtilities.busyStatusFromAttendeeStatus(
                        Attendees.ATTENDEE_STATUS_NONE));
        assertEquals(CalendarUtilities.BUSY_STATUS_FREE,
                CalendarUtilities.busyStatusFromAttendeeStatus(
                        Attendees.ATTENDEE_STATUS_INVITED));
        assertEquals(CalendarUtilities.BUSY_STATUS_TENTATIVE,
                CalendarUtilities.busyStatusFromAttendeeStatus(
                        Attendees.ATTENDEE_STATUS_TENTATIVE));
        assertEquals(CalendarUtilities.BUSY_STATUS_BUSY,
                CalendarUtilities.busyStatusFromAttendeeStatus(
                        Attendees.ATTENDEE_STATUS_ACCEPTED));
    }
    public void testGetUtcAllDayCalendarTime() {
        GregorianCalendar correctUtc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        correctUtc.set(2011, 2, 10, 0, 0, 0);
        long correctUtcTime = correctUtc.getTimeInMillis();
        TimeZone localTimeZone = TimeZone.getTimeZone("GMT-0700");
        GregorianCalendar localCalendar = new GregorianCalendar(localTimeZone);
        localCalendar.set(2011, 2, 10, 12, 23, 34);
        long localTimeMillis = localCalendar.getTimeInMillis();
        long convertedUtcTime =
            CalendarUtilities.getUtcAllDayCalendarTime(localTimeMillis, localTimeZone);
        assertEquals(convertedUtcTime/1000, correctUtcTime/1000);
        localTimeZone = TimeZone.getTimeZone("GMT+0700");
        localCalendar = new GregorianCalendar(localTimeZone);
        localCalendar.set(2011, 2, 10, 12, 23, 34);
        localTimeMillis = localCalendar.getTimeInMillis();
        convertedUtcTime =
            CalendarUtilities.getUtcAllDayCalendarTime(localTimeMillis, localTimeZone);
        assertEquals(convertedUtcTime/1000, correctUtcTime/1000);
    }
    public void testGetLocalAllDayCalendarTime() {
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        TimeZone localTimeZone = TimeZone.getTimeZone("GMT-0700");
        GregorianCalendar correctLocal = new GregorianCalendar(localTimeZone);
        correctLocal.set(2011, 2, 10, 0, 0, 0);
        long correctLocalTime = correctLocal.getTimeInMillis();
        GregorianCalendar utcCalendar = new GregorianCalendar(utcTimeZone);
        utcCalendar.set(2011, 2, 10, 12, 23, 34);
        long utcTimeMillis = utcCalendar.getTimeInMillis();
        long convertedLocalTime =
            CalendarUtilities.getLocalAllDayCalendarTime(utcTimeMillis, localTimeZone);
        assertEquals(convertedLocalTime/1000, correctLocalTime/1000);
        localTimeZone = TimeZone.getTimeZone("GMT+0700");
        correctLocal = new GregorianCalendar(localTimeZone);
        correctLocal.set(2011, 2, 10, 0, 0, 0);
        correctLocalTime = correctLocal.getTimeInMillis();
        utcCalendar = new GregorianCalendar(utcTimeZone);
        utcCalendar.set(2011, 2, 10, 12, 23, 34);
        utcTimeMillis = utcCalendar.getTimeInMillis();
        convertedLocalTime =
            CalendarUtilities.getLocalAllDayCalendarTime(utcTimeMillis, localTimeZone);
        assertEquals(convertedLocalTime/1000, correctLocalTime/1000);
    }
}
