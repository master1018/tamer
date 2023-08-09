public class RecurrenceSetTest extends TestCase {
    @SmallTest
    public void testRecurrenceSet0() throws Exception {
        String recurrence = "DTSTART;TZID=America/New_York:20080221T070000\n"
                + "DTEND;TZID=America/New_York:20080221T190000\n"
                + "RRULE:FREQ=DAILY;UNTIL=20080222T000000Z\n"
                + "EXDATE:20080222T120000Z";
        verifyPopulateContentValues(recurrence, "FREQ=DAILY;UNTIL=20080222T000000Z", null,
                null, "20080222T120000Z", 1203595200000L, "America/New_York", "P43200S", 0);
    }
    @SmallTest
    public void testRecurrenceSet1() throws Exception {
        String recurrence = "DTSTART;VALUE=DATE:20090821\nDTEND;VALUE=DATE:20090822\n"
                + "RRULE:FREQ=YEARLY;WKST=SU";
        verifyPopulateContentValues(recurrence, "FREQ=YEARLY;WKST=SU", null,
                null, null, 1250812800000L, null, "P1D", 1);
    }
    @SmallTest
    public void testRecurrenceSet2() throws Exception {
        String recurrence = "DTSTART;VALUE=DATE:20090821\nDTEND;VALUE=DATE:20090823\n"
                + "RRULE:FREQ=YEARLY;WKST=SU";
        verifyPopulateContentValues(recurrence, "FREQ=YEARLY;WKST=SU", null,
                null, null, 1250812800000L, null,  "P2D", 1);
    }
    private void verifyPopulateContentValues(String recurrence, String rrule, String rdate,
            String exrule, String exdate, long dtstart, String tzid, String duration, int allDay)
            throws ICalendar.FormatException {
        ICalendar.Component recurrenceComponent =
                new ICalendar.Component("DUMMY", null );
        ICalendar.parseComponent(recurrenceComponent, recurrence);
        ContentValues values = new ContentValues();
        RecurrenceSet.populateContentValues(recurrenceComponent, values);
        Log.d("KS", "values " + values);
        assertEquals(rrule, values.get(android.provider.Calendar.Events.RRULE));
        assertEquals(rdate, values.get(android.provider.Calendar.Events.RDATE));
        assertEquals(exrule, values.get(android.provider.Calendar.Events.EXRULE));
        assertEquals(exdate, values.get(android.provider.Calendar.Events.EXDATE));
        assertEquals(dtstart, (long) values.getAsLong(Calendar.Events.DTSTART));
        assertEquals(tzid, values.get(android.provider.Calendar.Events.EVENT_TIMEZONE));
        assertEquals(duration, values.get(android.provider.Calendar.Events.DURATION));
        assertEquals(allDay,
                (int) values.getAsInteger(android.provider.Calendar.Events.ALL_DAY));
    }
}
