@TestTargetClass(Date.class)
public class DateTest extends TestCase {
    static Calendar aCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    static long TIME_AN_HOUR = 3600000; 
    static long TIME_EPOCH = 0;
    static long TIME_NOW = System.currentTimeMillis();
    static long TIME_NEGATIVE = -3600001;
    static long TIME_TESTDATE1 = getTime(1999, Calendar.DECEMBER, 31, 23, 59,
            59);
    static long TIME_TESTDATE2 = getTime(2010, Calendar.JUNE, 10, 20, 3, 16);
    static long TIME_TESTDATE3 = getTime(1931, Calendar.APRIL, 21, 1, 25, 1);
    static long TIME_LOWERLIMIT = Long.MIN_VALUE;
    static long TIME_UPPERLIMIT = Long.MAX_VALUE;
    static String SQL_DATESTRING1 = "1999-12-31";
    static String SQL_DATESTRING2 = "2010-06-10";
    static String SQL_DATESTRING3 = "1931-04-21";
    static String SQL_EPOCHSTRING = "1970-01-01";
    static String SQL_DATEDAY1 = "1970-01-02";
    static String SQL_NEGATIVE = "1969-12-31";
    static long[] TIME_ARRAY = new long[] { TIME_TESTDATE1, TIME_TESTDATE2,
            TIME_TESTDATE3, TIME_NEGATIVE, TIME_EPOCH };
    static String[] SQL_DATEARRAY = new String[] { SQL_DATESTRING1,
            SQL_DATESTRING2, SQL_DATESTRING3, SQL_NEGATIVE, SQL_EPOCHSTRING };
    static String[] SQL_NYARRAY = new String[] { "1999-12-31", "2010-06-10",
            "1931-04-20", "1969-12-31", "1969-12-31" };
    static String[] SQL_JAPANARRAY = new String[] { "2000-01-01", "2010-06-11",
            "1931-04-21", "1970-01-01", "1970-01-01" };
    static String[][] SQL_TZ_DATEARRAYS = new String[][] { SQL_DATEARRAY,
            SQL_NYARRAY, SQL_JAPANARRAY };
    static String TZ_LONDON = "Europe/London"; 
    static String TZ_PACIFIC = "America/Los_Angeles"; 
    static String TZ_JAPAN = "Asia/Tokyo"; 
    static String[] TIMEZONES = { TZ_LONDON, TZ_PACIFIC, TZ_JAPAN };
    static private long getTime(int year, int month, int date, int hour,
            int minute, int second) {
        aCal.set(year, month, date, hour, minute, second);
        return aCal.getTimeInMillis();
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Date",
        args = {int.class, int.class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void testDateintintint() {
        int init1[] = { 99, 8099, 9000, 99999, 99, 99, -1, -100 };
        int init2[] = { 11, 0, 0, 0, 999, 0, 0, -111 };
        int init3[] = { 31, 0, 0, 0, 0, 999, 0, -999 };
        for (int i = 0; i < init1.length; i++) {
            Date theDate = new Date(init1[i], init2[i], init3[i]);
            assertNotNull(theDate);
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Date",
        args = {long.class}
    )
    public void testDatelong() {
        long init1[] = { TIME_TESTDATE1, TIME_TESTDATE2, TIME_TESTDATE3,
                TIME_NEGATIVE, TIME_LOWERLIMIT, TIME_UPPERLIMIT, TIME_EPOCH,
                TIME_NOW };
        for (long element : init1) {
            Date theDate = new Date(element);
            assertNotNull(theDate);
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHours",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetHours() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.getHours();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMinutes",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetMinutes() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.getMinutes();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSeconds",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetSeconds() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.getSeconds();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setHours",
        args = {int.class}
    )
    @SuppressWarnings("deprecation")
    public void testSetHours() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.setHours(22);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMinutes",
        args = {int.class}
    )
    @SuppressWarnings("deprecation")
    public void testSetMinutes() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.setMinutes(54);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSeconds",
        args = {int.class}
    )
    @SuppressWarnings("deprecation")
    public void testSetSeconds() {
        Date theDate = new Date(TIME_TESTDATE1);
        try {
            theDate.setSeconds(36);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
		for (int i = 0; i < TIMEZONES.length; i++) {
			testToString(TIMEZONES[i], TIME_ARRAY, SQL_TZ_DATEARRAYS[i]);
        } 
    } 
    private void testToString(String timeZone, long[] theDates, String[] theDateStrings) {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        for (int i = 0; i < theDates.length; i++) {
            Date theDate = new Date(theDates[i]);
            String JDBCString = theDate.toString();
            assertEquals(theDateStrings[i], JDBCString);
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTime",
        args = {long.class}
    )
    public void testSetTimelong() {
        for (int i = 0; i < TIMEZONES.length; i++) {
            testSetTimelong(TIMEZONES[i], SQL_TZ_DATEARRAYS[i]);
        } 
    } 
    private void testSetTimelong(String timeZoneName, String[] dateArray) {
        if (timeZoneName != null) {
            TimeZone.setDefault(TimeZone.getTimeZone(timeZoneName));
        } 
        Date theDate = new Date(TIME_TESTDATE1);
        for (int i = 0; i < dateArray.length; i++) {
            theDate.setTime(TIME_ARRAY[i]);
            assertEquals(dateArray[i], theDate.toString());
        } 
    } 
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        String SQL_NOTVALID1 = "ABCDEF"; 
        String SQL_NOTVALID2 = "12321.43.56"; 
        String SQL_NOTVALID3 = null; 
        String[] SQL_INVALIDARRAY = { SQL_NOTVALID1, SQL_NOTVALID2,
                SQL_NOTVALID3 };
        Date theDate;
        for (String element : SQL_DATEARRAY) {
            theDate = Date.valueOf(element);
            assertEquals(element, theDate.toString());
        } 
        for (String element : SQL_INVALIDARRAY) {
            try {
                theDate = Date.valueOf(element);
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
    public void test_valueOf_IllegalArgumentException() {
        try {
            Date.valueOf("1996-10-07-01");
            fail("should throw NumberFormatException");
        } catch (NumberFormatException e) {
        }
        try {
            Date.valueOf("-10-07-01");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("--01");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("1991--");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("-01-");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("-10-w2-01");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("07-w2-");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Date.valueOf("1997-w2-w2");
            fail("should throw NumberFormatException");
        } catch (NumberFormatException e) {
        }
        try {
            Date.valueOf("1996--01");
            fail("should throw NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }
    static TimeZone defaultTimeZone = TimeZone.getDefault();
    protected void tearDown(){
    	TimeZone.setDefault(defaultTimeZone);
    }
} 
