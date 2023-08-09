@TestTargetClass(Timestamp.class)
public class TimestampTest extends TestCase {
    static class MockTimestamp extends Timestamp{
        private String holiday;
        public MockTimestamp(long theTime) {
            super(theTime);
            holiday = "Christmas";
        }
        public void setTime(long theTime){
            super.setTime(theTime);
            holiday.hashCode();
        }
    }
    static long TIME_TEST1 = 38720231; 
    static long TIME_TEST2 = 80279000; 
    static long TIME_TEST3 = -38720691; 
    static long TIME_COMPARE = 123498845;
    static long TIME_EARLY = -2347889122L;
    static long TIME_LATE = 2347889122L; 
    static String STRING_TEST1 = "1970-01-01 10:45:20.231"; 
    static String STRING_TEST2 = "1970-01-01 22:17:59.0"; 
    static String STRING_TEST3 = "1969-12-31 13:14:39.309"; 
    static String STRING_INVALID1 = "ABCDEFGHI";
    static String STRING_INVALID2 = "233104";
    static String STRING_INVALID3 = "21-43-48";
    static String STRING_OUTRANGE = "2999-15-99 35:99:66.875";
    static long[] TIME_ARRAY = { TIME_TEST1, TIME_TEST2, TIME_TEST3 };
    static int[] YEAR_ARRAY = { 70, 70, 69 };
    static int[] MONTH_ARRAY = { 0, 0, 11 };
    static int[] DATE_ARRAY = { 1, 1, 31 };
    static int[] HOURS_ARRAY = { 10, 22, 13 };
    static int[] MINUTES_ARRAY = { 45, 17, 14 };
    static int[] SECONDS_ARRAY = { 20, 59, 39 };
    static int[] NANOS_ARRAY = { 231000000, 000000000, 309000000 };
    static int[] NANOS_ARRAY2 = { 137891990, 635665198, 109985421 };
    static String[] STRING_NANOS_ARRAY = { "1970-01-01 10:45:20.13789199",
            "1970-01-01 22:17:59.635665198", "1969-12-31 13:14:39.109985421" };
    static String[] STRING_GMT_ARRAY = { STRING_TEST1, STRING_TEST2,
            STRING_TEST3 };
    static String[] STRING_LA_ARRAY = { "02:45:20", "14:17:59", "05:14:40" };
    static String[] STRING_JP_ARRAY = { "19:45:20", "07:17:59", "22:14:40" };
    static String[] INVALID_STRINGS = { STRING_INVALID1, STRING_INVALID2,
            STRING_INVALID3 };
    static String TZ_LONDON = "GMT"; 
    static String TZ_PACIFIC = "America/Los_Angeles"; 
    static String TZ_JAPAN = "Asia/Tokyo"; 
    static String[] TIMEZONES = { TZ_LONDON, TZ_PACIFIC, TZ_JAPAN };
    static String[][] STRING_ARRAYS = { STRING_GMT_ARRAY, STRING_LA_ARRAY,
            STRING_JP_ARRAY };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Incorrect parameter checking missed",
        method = "Timestamp",
        args = {long.class}
    )
    public void testTimestamplong() {
        Timestamp theTimestamp = new Timestamp(TIME_TEST1);
        assertNotNull(theTimestamp);
        Timestamp mockTimestamp = new MockTimestamp(TIME_TEST1);
        assertNotNull(mockTimestamp);
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timestamp",
        args = {int.class, int.class, int.class, int.class, int.class, int.class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void testTimestampintintintintintintint() {
        int[][] valid = { { 99, 2, 14, 17, 52, 3, 213577212 }, 
                { 0, 0, 1, 0, 0, 0, 0 }, 
                { 106, 11, 31, 23, 59, 59, 999999999 }, 
                { 106, 11, 31, 23, 59, 61, 999999999 }, 
                { 106, 11, 31, 23, 59, -1, 999999999 }, 
                { 106, 11, 31, 23, 61, 59, 999999999 }, 
                { 106, 11, 31, 23, -1, 59, 999999999 }, 
                { 106, 11, 31, 25, 59, 59, 999999999 }, 
                { 106, 11, 31, -1, 59, 59, 999999999 }, 
                { 106, 11, 35, 23, 59, 59, 999999999 }, 
                { 106, 11, -1, 23, 59, 59, 999999999 }, 
                { 106, 15, 31, 23, 59, 59, 999999999 }, 
                { 106, -1, 31, 23, 59, 59, 999999999 }, 
                { -10, 11, 31, 23, 59, 59, 999999999 }, 
        };
        for (int[] element : valid) {
            Timestamp theTimestamp = new Timestamp(element[0],
                    element[1], element[2], element[3],
                    element[4], element[5], element[6]);
            assertNotNull("Timestamp not generated: ", theTimestamp);
        } 
        int[][] invalid = {
                { 106, 11, 31, 23, 59, 59, 1999999999 }, 
                { 106, 11, 31, 23, 59, 59, -999999999 },
        };
        for (int[] element : invalid) {
            try {
                new Timestamp(element[0],
                        element[1], element[2], element[3],
                        element[4], element[5], element[6]);
                fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
            }
        }
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTime",
        args = {long.class}
    )
    public void testSetTimelong() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Timestamp theTimestamp = new Timestamp(TIME_TEST1);
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            theTimestamp.setTime(TIME_ARRAY[i]);
            assertEquals(TIME_ARRAY[i], theTimestamp.getTime());
            assertEquals(NANOS_ARRAY[i], theTimestamp.getNanos());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTime",
        args = {}
    )
    public void testGetTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertEquals(element, theTimestamp.getTime());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getYear",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetYear() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(YEAR_ARRAY[i], theTimestamp.getYear());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getMonth",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetMonth() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(MONTH_ARRAY[i], theTimestamp.getMonth());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getDate",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetDate() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(DATE_ARRAY[i], theTimestamp.getDate());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getHours",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetHours() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(HOURS_ARRAY[i], theTimestamp.getHours());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getMinutes",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetMinutes() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(MINUTES_ARRAY[i], theTimestamp.getMinutes());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getSeconds",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetSeconds() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(SECONDS_ARRAY[i], theTimestamp.getSeconds());
        } 
    } 
    static String theExceptionMessage = "Timestamp format must be yyyy-mm-dd hh:mm:ss.fffffffff";
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOfString() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            Timestamp theTimestamp2 = Timestamp.valueOf(STRING_GMT_ARRAY[i]);
            assertEquals(theTimestamp, theTimestamp2);
        } 
        Timestamp theTimestamp = Timestamp.valueOf(STRING_OUTRANGE);
        assertNotNull(theTimestamp);
        for (String element : INVALID_STRINGS) {
            try {
                Timestamp.valueOf(element);
                fail("Should throw IllegalArgumentException.");
            } catch (IllegalArgumentException e) {
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOfString1() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Timestamp theReturn;
        theReturn = Timestamp.valueOf("1970-01-01 10:45:20.231");
        assertEquals("Wrong result for time test", 38720231,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 231000000,
                theReturn.getNanos());
        theReturn = Timestamp.valueOf("1970-01-01 10:45:20.231987654");
        assertEquals("Wrong result for time test", 38720231,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 231987654,
                theReturn.getNanos());
        theReturn = Timestamp.valueOf("1970-01-01 22:17:59.0");
        assertEquals("Wrong result for time test", 80279000,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 0,
                theReturn.getNanos());
        theReturn = Timestamp.valueOf("1969-12-31 13:14:39.309");
        assertEquals("Wrong result for time test", -38720691,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 309000000,
                theReturn.getNanos());
        theReturn = Timestamp.valueOf("1970-01-01 10:45:20");
        assertEquals("Wrong result for time test", 38720000,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 0,
                theReturn.getNanos());
        String[] invalid = {
                null,
                "ABCDEFGHI", 
                "233104", "1970-01-01 22:17:59.",
                "1970-01-01 10:45:20.231987654690645322",
                "1970-01-01 10:45:20&231987654",
                "1970-01-01 10:45:20.-31987654",
                "1970-01-01 10:45:20.ABCD87654", 
                "21-43-48",
        };
        for (String element : invalid) {
            try {
                theReturn = Timestamp.valueOf(element);
                fail("Should throw IllegalArgumentException for " + element);
            } catch (IllegalArgumentException e) {
            }
        }
        String date = "1970-01-01 22:17:59.0                 ";
        Timestamp t = Timestamp.valueOf(date);
        assertEquals(80279000,t.getTime());
    } 
    public void testValueOf_IAE() {
        try {
            java.sql.Timestamp.valueOf("2008-12-22 15:00:01.");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            java.sql.Timestamp.valueOf("178548938-12-22 15:00:01.000000001");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            java.sql.Timestamp.valueOf("2008-12-22 15:00:01.0000000011");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals("Wrong conversion for test " + i, STRING_GMT_ARRAY[i],
                    theTimestamp.toString());
        } 
		Timestamp t1 = new Timestamp(Long.MIN_VALUE);
		assertEquals("292278994-08-17 07:12:55.192", t1.toString()); 
		Timestamp t2 = new Timestamp(Long.MIN_VALUE + 1);
		assertEquals("292278994-08-17 07:12:55.193", t2.toString()); 
		Timestamp t3 = new Timestamp(Long.MIN_VALUE + 807);
		assertEquals("292278994-08-17 07:12:55.999", t3.toString()); 
		Timestamp t4 = new Timestamp(Long.MIN_VALUE + 808);
		assertEquals("292269055-12-02 16:47:05.0", t4.toString()); 
    } 
    private void testToString(String timeZone, long[] theTimeStamps, String[] theTimeStampStrings) {
    	TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(theTimeStamps[i]);
            assertEquals(theTimeStampStrings[i], theTimestamp.toString());
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNanos",
        args = {}
    )
    public void testGetNanos() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals("Wrong conversion for test " + i, NANOS_ARRAY[i],
                    theTimestamp.getNanos());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNanos",
        args = {int.class}
    )
    public void testSetNanosint() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        int[] NANOS_INVALID = { -137891990, 1635665198, -1 };
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            theTimestamp.setNanos(NANOS_ARRAY2[i]);
            assertEquals("Wrong conversion for test " + i, NANOS_ARRAY2[i],
                    theTimestamp.getNanos());
            assertEquals("Wrong conversion for test " + i,
                    STRING_NANOS_ARRAY[i], theTimestamp.toString());
        } 
        for (int i = 0; i < NANOS_INVALID.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            int originalNanos = theTimestamp.getNanos();
            try {
                theTimestamp.setNanos(NANOS_INVALID[i]);
                fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
            } 
            assertEquals(originalNanos, theTimestamp.getNanos());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.sql.Timestamp.class}
    )
    public void testEqualsTimestamp() {
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Timestamp theTimestamp2 = new Timestamp(element);
            assertTrue(theTimestamp.equals(theTimestamp2));
        } 
        Timestamp theTest = new Timestamp(TIME_COMPARE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.equals(theTest));
        } 
        assertFalse(new Timestamp(0).equals((Timestamp) null));
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject() {
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Object theTimestamp2 = new Timestamp(element);
            assertTrue(theTimestamp.equals(theTimestamp2));
        } 
        Object theTest = new Timestamp(TIME_COMPARE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.equals(theTest));
        } 
        Object nastyTest = new String("Test ");
        Timestamp theTimestamp = new Timestamp(TIME_ARRAY[1]);
        assertFalse(theTimestamp.equals(nastyTest));
        assertFalse(new Timestamp(0).equals((Object) null));
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "before",
        args = {java.sql.Timestamp.class}
    )
    public void testBeforeTimestamp() {
        Timestamp theTest = new Timestamp(TIME_LATE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertTrue(theTimestamp.before(theTest));
        } 
        theTest = new Timestamp(TIME_EARLY);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.before(theTest));
        } 
        for (long element : TIME_ARRAY) {
            theTest = new Timestamp(element);
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.before(theTest));
            theTest.setNanos(theTest.getNanos() + 1);
            assertTrue(theTimestamp.before(theTest));
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "after",
        args = {java.sql.Timestamp.class}
    )
    public void testAfterTimestamp() {
        Timestamp theTest = new Timestamp(TIME_LATE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.after(theTest));
        } 
        theTest = new Timestamp(TIME_EARLY);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertTrue(theTimestamp.after(theTest));
        } 
        for (long element : TIME_ARRAY) {
            theTest = new Timestamp(element);
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.after(theTest));
            theTimestamp.setNanos(theTimestamp.getNanos() + 1);
            assertTrue(theTimestamp.after(theTest));
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.sql.Timestamp.class}
    )
    @SuppressWarnings("deprecation")
    public void testCompareToTimestamp() {
        Timestamp theTest = new Timestamp(TIME_EARLY);
        Timestamp theTest2 = new Timestamp(TIME_LATE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Timestamp theTimestamp2 = new Timestamp(element);
            assertTrue(theTimestamp.compareTo(theTest) > 0);
            assertTrue(theTimestamp.compareTo(theTest2) < 0);
            assertEquals(0, theTimestamp.compareTo(theTimestamp2));
        } 
        Timestamp t1 = new Timestamp(-1L);
        Timestamp t2 = new Timestamp(-1L);
        t1.setTime(Long.MIN_VALUE);
        t2.setDate(Integer.MIN_VALUE);
        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t1));
        t1.setTime(Long.MAX_VALUE);
        t2.setTime(Long.MAX_VALUE - 1);
        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t1));
        t1.setTime(Integer.MAX_VALUE);
        t2.setTime(Integer.MAX_VALUE);
        assertEquals(0, t1.compareTo(t2));
        assertEquals(0, t2.compareTo(t1));
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.util.Date.class}
    )
    public void testCompareToDate() {
        Date theTest = new Timestamp(TIME_EARLY);
        Date theTest2 = new Timestamp(TIME_LATE);
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Date theTimestamp2 = new Timestamp(element);
            assertTrue(theTimestamp.compareTo(theTest) > 0);
            assertTrue(theTimestamp.compareTo(theTest2) < 0);
            assertEquals(0, theTimestamp.compareTo(theTimestamp2));
        } 
        Date nastyTest = new Date();
        Timestamp theTimestamp = new Timestamp(TIME_ARRAY[1]);
        try {
            theTimestamp.compareTo(nastyTest);
            fail("testCompareToObject: Did not get expected ClassCastException");
        } catch (ClassCastException e) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Serialization test",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        Timestamp object = new Timestamp(100L);
        SerializationTest.verifySelf(object);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Serialization test",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        Timestamp object = new Timestamp(100L);
        SerializationTest.verifyGolden(this, object);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Timestamp t1 = new Timestamp(Long.MIN_VALUE);
        assertEquals("292278994-08-17 07:12:55.192", t1.toString()); 
        Timestamp t2 = new Timestamp(Long.MIN_VALUE + 1);
        assertEquals("292278994-08-17 07:12:55.193", t2.toString()); 
        Timestamp t3 = new Timestamp(Long.MIN_VALUE + 807);
        assertEquals("292278994-08-17 07:12:55.999", t3.toString()); 
        Timestamp t4 = new Timestamp(Long.MIN_VALUE + 808);
        assertEquals("292269055-12-02 16:47:05.0", t4.toString()); 
    }
    TimeZone defaultTimeZone = TimeZone.getDefault();
    protected void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }
} 
