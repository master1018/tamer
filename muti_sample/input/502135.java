@TestTargetClass(DateFormatSymbols.class) 
public class DateFormatSymbolsTest extends junit.framework.TestCase {
    private DateFormatSymbols dfs;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DateFormatSymbols",
        args = {}
    )
    public void test_Constructor() {
        new DateFormatSymbols();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DateFormatSymbols",
        args = {java.util.Locale.class}
    )
    public void test_ConstructorLjava_util_Locale() {
        new DateFormatSymbols(new Locale("en", "us"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        DateFormatSymbols symbols = new DateFormatSymbols();
        DateFormatSymbols clone = (DateFormatSymbols) symbols.clone();
        assertTrue("Not equal", symbols.equals(clone));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        assertTrue("Equal object returned true", dfs.equals(dfs.clone()));
        dfs.setLocalPatternChars("KKKKKKKKK");
        assertTrue("Un-Equal objects returned false", !dfs
                .equals(new DateFormatSymbols()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAmPmStrings",
        args = {}
    )
    public void test_getAmPmStrings() {
        String[] retVal = dfs.getAmPmStrings();
        String[] val = { "AM", "PM" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEras",
        args = {}
    )
    public void test_getEras() {
        String[] retVal = dfs.getEras();
        String[] val = { "BC", "AD" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPatternChars",
        args = {}
    )
    public void test_getLocalPatternChars() {
        String retVal = dfs.getLocalPatternChars();
        String val = "GyMdkHmsSEDFwWahKzZ";
        assertEquals("Returned incorrect pattern string", val, retVal);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMonths",
        args = {}
    )
    public void test_getMonths() {
        String[] retVal = dfs.getMonths();
        String[] val = { "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November",
                "December", ""};
        assertEquals("Returned wrong array: ", val.length, retVal.length);
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getShortMonths",
        args = {}
    )
    public void test_getShortMonths() {
        String[] retVal = dfs.getShortMonths();
        String[] val = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec", ""};
        assertEquals("Returned wrong array: ", val.length, retVal.length);
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getShortWeekdays",
        args = {}
    )
    public void test_getShortWeekdays() {
        String[] retVal = dfs.getShortWeekdays();
        String[] val = { "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getWeekdays",
        args = {}
    )
    public void test_getWeekdays() {
        String[] retVal = dfs.getWeekdays();
        String[] val = { "", "Sunday", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getZoneStrings",
        args = {}
    )
    public void test_getZoneStrings() {
        String[][] val = { { "XX" }, { "YY" } };
        dfs.setZoneStrings(val);
        String[][] retVal = dfs.getZoneStrings();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", Arrays
                    .equals(retVal[i], val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        int hc1 = dfs.hashCode();
        int hc2 = dfs.hashCode();
        assertTrue("hashCode() returned inconsistent number : " + hc1 + " - "
                + hc2, hc1 == hc2);
        assertTrue("hashCode() returns different values for equal() objects",
                dfs.hashCode() == dfs.clone().hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setAmPmStrings",
        args = {java.lang.String[].class}
    )
    public void test_setAmPmStrings$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setAmPmStrings(val);
        String[] retVal = dfs.getAmPmStrings();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEras",
        args = {java.lang.String[].class}
    )
    public void test_setEras$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setEras(val);
        String[] retVal = dfs.getEras();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLocalPatternChars",
        args = {java.lang.String.class}
    )
    public void test_setLocalPatternCharsLjava_lang_String() {
        dfs.setLocalPatternChars("GyMZZkHmsSEHHFwWahKz");
        String retVal = dfs.getLocalPatternChars();
        String val = "GyMZZkHmsSEHHFwWahKz";
        assertTrue("Returned incorrect pattern string", retVal.equals(val));
        try {
            new DateFormatSymbols().setLocalPatternChars(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMonths",
        args = {java.lang.String[].class}
    )
    public void test_setMonths$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setMonths(val);
        String[] retVal = dfs.getMonths();
        assertTrue("Return is identical", retVal != dfs.getMonths());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setShortMonths",
        args = {java.lang.String[].class}
    )
    public void test_setShortMonths$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setShortMonths(val);
        String[] retVal = dfs.getShortMonths();
        assertTrue("Return is identical", retVal != dfs.getShortMonths());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setShortWeekdays",
        args = {java.lang.String[].class}
    )
    public void test_setShortWeekdays$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setShortWeekdays(val);
        String[] retVal = dfs.getShortWeekdays();
        assertTrue("Return is identical", retVal != dfs.getShortWeekdays());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setWeekdays",
        args = {java.lang.String[].class}
    )
    public void test_setWeekdays$Ljava_lang_String() {
        String[] val = { "XX", "YY" };
        dfs.setWeekdays(val);
        String[] retVal = dfs.getWeekdays();
        assertTrue("Return is identical", retVal != dfs.getWeekdays());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setZoneStrings",
        args = {java.lang.String[][].class}
    )
    public void test_setZoneStrings$$Ljava_lang_String() {
        String[][] val = { { "XX" }, { "YY" } };
        dfs.setZoneStrings(val);
        String[][] retVal = dfs.getZoneStrings();
        assertTrue("get returns identical", retVal != dfs.getZoneStrings());
        assertTrue("get[0] returns identical", retVal[0] != dfs
                .getZoneStrings()[0]);
        assertTrue("get returned identical", retVal != val);
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings: " + retVal[i], Arrays.equals(
                    retVal[i], val[i]));
    }
    protected void setUp() {
        dfs = new DateFormatSymbols(new Locale("en", "us"));
    }
    protected void tearDown() {
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks serialization mechanism.",
            method = "!SerializationSelf",
            args = {}
    )
    public void test_serialization() throws Exception {
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.FRANCE);
        String[][] zoneStrings = symbols.getZoneStrings();
        assertNotNull(zoneStrings);
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOStream = new ObjectOutputStream(byteOStream);
        objectOStream.writeObject(symbols);
        ObjectInputStream objectIStream = new ObjectInputStream(
                new ByteArrayInputStream(byteOStream.toByteArray()));
        DateFormatSymbols symbolsD = (DateFormatSymbols) objectIStream
                .readObject();
        String[][] zoneStringsD = symbolsD.getZoneStrings();
        assertNotNull(zoneStringsD);
        assertEquals(symbols, symbolsD);
    }
}
