public class CalendarSyncAdapterTests extends SyncAdapterTestCase<CalendarSyncAdapter> {
    public CalendarSyncAdapterTests() {
        super();
    }
    public void testSetTimeRelatedValues_NonRecurring() throws IOException {
        CalendarSyncAdapter adapter = getTestSyncAdapter(CalendarSyncAdapter.class);
        EasCalendarSyncParser p = adapter.new EasCalendarSyncParser(getTestInputStream(), adapter);
        ContentValues cv = new ContentValues();
        GregorianCalendar startCalendar = new GregorianCalendar(2010, 5, 10, 8, 30);
        Long startTime = startCalendar.getTimeInMillis();
        GregorianCalendar endCalendar = new GregorianCalendar(2010, 5, 10, 9, 30);
        Long endTime = endCalendar.getTimeInMillis();
        p.setTimeRelatedValues(cv, startTime, endTime, 0);
        assertNull(cv.getAsInteger(Events.DURATION));
        assertEquals(startTime, cv.getAsLong(Events.DTSTART));
        assertEquals(endTime, cv.getAsLong(Events.DTEND));
        assertEquals(endTime, cv.getAsLong(Events.LAST_DATE));
        assertNull(cv.getAsString(Events.EVENT_TIMEZONE));
    }
    public void testSetTimeRelatedValues_Recurring() throws IOException {
        CalendarSyncAdapter adapter = getTestSyncAdapter(CalendarSyncAdapter.class);
        EasCalendarSyncParser p = adapter.new EasCalendarSyncParser(getTestInputStream(), adapter);
        ContentValues cv = new ContentValues();
        GregorianCalendar startCalendar = new GregorianCalendar(2010, 5, 10, 8, 30);
        Long startTime = startCalendar.getTimeInMillis();
        GregorianCalendar endCalendar = new GregorianCalendar(2010, 5, 10, 9, 30);
        Long endTime = endCalendar.getTimeInMillis();
        cv.put(Events.RRULE, "FREQ=DAILY");
        p.setTimeRelatedValues(cv, startTime, endTime, 0);
        assertEquals("P60M", cv.getAsString(Events.DURATION));
        assertEquals(startTime, cv.getAsLong(Events.DTSTART));
        assertNull(cv.getAsLong(Events.DTEND));
        assertNull(cv.getAsLong(Events.LAST_DATE));
        assertNull(cv.getAsString(Events.EVENT_TIMEZONE));
    }
    public void testSetTimeRelatedValues_AllDay() throws IOException {
        CalendarSyncAdapter adapter = getTestSyncAdapter(CalendarSyncAdapter.class);
        EasCalendarSyncParser p = adapter.new EasCalendarSyncParser(getTestInputStream(), adapter);
        ContentValues cv = new ContentValues();
        GregorianCalendar startCalendar = new GregorianCalendar(2010, 5, 10, 8, 30);
        Long startTime = startCalendar.getTimeInMillis();
        GregorianCalendar endCalendar = new GregorianCalendar(2010, 5, 11, 8, 30);
        Long endTime = endCalendar.getTimeInMillis();
        cv.put(Events.RRULE, "FREQ=WEEKLY;BYDAY=MO");
        p.setTimeRelatedValues(cv, startTime, endTime, 1);
        startCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        startCalendar.set(2010, 5, 10, 0, 0, 0);
        startCalendar.set(GregorianCalendar.MILLISECOND, 0);
        startTime = startCalendar.getTimeInMillis();
        assertEquals(startTime, cv.getAsLong(Events.DTSTART));
        assertEquals("P1D", cv.getAsString(Events.DURATION));
        assertNull(cv.getAsLong(Events.DTEND));
        assertNull(cv.getAsLong(Events.LAST_DATE));
        assertNotNull(cv.getAsString(Events.EVENT_TIMEZONE));
    }
    public void testSetTimeRelatedValues_Recurring_AllDay_Exception () throws IOException {
        CalendarSyncAdapter adapter = getTestSyncAdapter(CalendarSyncAdapter.class);
        EasCalendarSyncParser p = adapter.new EasCalendarSyncParser(getTestInputStream(), adapter);
        ContentValues cv = new ContentValues();
        GregorianCalendar startCalendar = new GregorianCalendar(2010, 5, 17, 8, 30);
        Long startTime = startCalendar.getTimeInMillis();
        GregorianCalendar endCalendar = new GregorianCalendar(2010, 5, 17, 9, 30);
        Long endTime = endCalendar.getTimeInMillis();
        cv.put(Events.ORIGINAL_ALL_DAY, 1);
        GregorianCalendar instanceCalendar = new GregorianCalendar(2010, 5, 17, 8, 30);
        cv.put(Events.ORIGINAL_INSTANCE_TIME, instanceCalendar.getTimeInMillis());
        p.setTimeRelatedValues(cv, startTime, endTime, 0);
        GregorianCalendar testCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        testCalendar.set(2010, 5, 17, 0, 0, 0);
        testCalendar.set(GregorianCalendar.MILLISECOND, 0);
        Long testTime = testCalendar.getTimeInMillis();
        assertEquals(testTime, cv.getAsLong(Events.ORIGINAL_INSTANCE_TIME));
        assertNull(cv.getAsString(Events.DURATION));
        assertEquals(endTime, cv.getAsLong(Events.DTEND));
        assertEquals(endTime, cv.getAsLong(Events.LAST_DATE));
        assertNull(cv.getAsString(Events.EVENT_TIMEZONE));
    }
    public void testIsValidEventValues() throws IOException {
        CalendarSyncAdapter adapter = getTestSyncAdapter(CalendarSyncAdapter.class);
        EasCalendarSyncParser p = adapter.new EasCalendarSyncParser(getTestInputStream(), adapter);
        long validTime = System.currentTimeMillis();
        String validData = "foo-bar-bletch";
        String validDuration = "P30M";
        String validRrule = "FREQ=DAILY";
        ContentValues cv = new ContentValues();
        cv.put(Events.DTSTART, validTime);
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events._SYNC_DATA, validData);
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events.DURATION, validDuration);
        assertTrue(p.isValidEventValues(cv));
        cv.remove(Events.DURATION);
        cv.put(Events.ORIGINAL_INSTANCE_TIME, validTime);
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events.DTEND, validTime);
        cv.remove(Events.ORIGINAL_INSTANCE_TIME);
        assertTrue(p.isValidEventValues(cv));
        cv.remove(Events.DTSTART);
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events.DTSTART, validTime);
        cv.put(Events.RRULE, validRrule);
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events.DURATION, "P30M");
        assertTrue(p.isValidEventValues(cv));
        cv.put(Events.ALL_DAY, "1");
        assertFalse(p.isValidEventValues(cv));
        cv.put(Events.DURATION, "P1D");
        assertTrue(p.isValidEventValues(cv));
    }
}
