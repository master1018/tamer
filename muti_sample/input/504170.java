@TestTargetClass(SimpleTimeZone.class) 
public class SimpleTimeZoneTest extends junit.framework.TestCase {
    SimpleTimeZone st1;
    SimpleTimeZone st2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SimpleTimeZone",
        args = {int.class, java.lang.String.class}
    )
    public void test_ConstructorILjava_lang_String() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "TEST");
        assertEquals("Incorrect TZ constructed", "TEST", st.getID());
        assertTrue("Incorrect TZ constructed: " + "returned wrong offset", st
                .getRawOffset() == 1000);
        assertTrue("Incorrect TZ constructed" + "using daylight savings", !st
                .useDaylightTime());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SimpleTimeZone",
        args = {int.class, java.lang.String.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class}
    )
    public void test_ConstructorILjava_lang_StringIIIIIIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                0);
        assertTrue("Incorrect TZ constructed", st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime()));
        assertTrue("Incorrect TZ constructed", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        assertEquals("Incorrect TZ constructed", "TEST", st.getID());
        assertEquals("Incorrect TZ constructed", 1000, st.getRawOffset());
        assertTrue("Incorrect TZ constructed", st.useDaylightTime());
        try {
            new SimpleTimeZone(1000, "TEST", 12,
                    1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    10, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    1, 10, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.DECEMBER,
                    1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -10, Calendar.SUNDAY,
                    0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SimpleTimeZone",
        args = {int.class, java.lang.String.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class}
    )
    public void test_ConstructorILjava_lang_StringIIIIIIIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                0, 1000 * 60 * 60);
        assertTrue("Incorrect TZ constructed", st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime()));
        assertTrue("Incorrect TZ constructed", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        assertEquals("Incorrect TZ constructed", "TEST", st.getID());
        assertEquals("Incorrect TZ constructed", 1000, st.getRawOffset());
        assertTrue("Incorrect TZ constructed", st.useDaylightTime());
        assertTrue("Incorrect TZ constructed",
                st.getDSTSavings() == 1000 * 60 * 60);
        try {
            new SimpleTimeZone(1000, "TEST", 12,
                    1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    10, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    1, 10, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.DECEMBER,
                    1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -10, Calendar.SUNDAY,
                    0, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SimpleTimeZone",
        args = {int.class, java.lang.String.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class}
    )
    public void test_ConstructorILjava_lang_StringIIIIIIIIIII() {
        assertNotNull(new SimpleTimeZone(
                TimeZone.LONG,
                "Europe/Paris",
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.WALL_TIME,
                SimpleTimeZone.WALL_TIME,
                TimeZone.SHORT,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG));
        assertNotNull(new SimpleTimeZone(
                TimeZone.LONG,
                "Europe/Paris",
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.WALL_TIME,
                Integer.MAX_VALUE,
                TimeZone.SHORT,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG,
                SimpleTimeZone.UTC_TIME,
                Integer.MIN_VALUE,
                TimeZone.LONG));
        try {
            new SimpleTimeZone(1000, "TEST", 12,
                    1, Calendar.SUNDAY, 0, Integer.MAX_VALUE, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0,  Integer.MAX_VALUE, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    10, Calendar.SUNDAY, 0, Integer.MAX_VALUE, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                    0, Integer.MAX_VALUE, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                    1, 10, 0, Calendar.NOVEMBER, Integer.MAX_VALUE, -1, Calendar.SUNDAY,
                    0, Integer.MAX_VALUE, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SimpleTimeZone(1000, "TEST", Calendar.DECEMBER,
                    1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, Integer.MAX_VALUE, -10, Calendar.SUNDAY,
                    0, Integer.MAX_VALUE, 1000 * 60 * 60);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        SimpleTimeZone st1 = new SimpleTimeZone(1000, "TEST",
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
                -1, Calendar.SUNDAY, 0);
        SimpleTimeZone stA = new SimpleTimeZone(1, "Gah");
        assertTrue("Clone resulted in same reference", st1.clone() != st1);
        assertTrue("Clone resulted in unequal object", ((SimpleTimeZone) st1
                .clone()).equals(st1));
        assertTrue("Clone resulted in same reference", stA.clone() != stA);
        assertTrue("Clone resulted in unequal object", ((SimpleTimeZone) stA
                .clone()).equals(stA));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        st1 = new SimpleTimeZone(tz.getRawOffset(), "EST");
        st2 = new SimpleTimeZone(0, "EST");
        assertFalse(st1.equals(st2));
        st1.setRawOffset(st2.getRawOffset());
        assertTrue(st1.equals(st2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDSTSavings",
        args = {}
    )
    public void test_getDSTSavings() {
        st1 = new SimpleTimeZone(0, "TEST");
        assertEquals("Non-zero default daylight savings",
                0, st1.getDSTSavings());
        st1.setStartRule(0, 1, 1, 1);
        st1.setEndRule(11, 1, 1, 1);
        assertEquals("Incorrect default daylight savings",
                3600000, st1.getDSTSavings());
        st1 = new SimpleTimeZone(-5 * 3600000, "EST", Calendar.APRIL, 1,
                -Calendar.SUNDAY, 2 * 3600000, Calendar.OCTOBER, -1,
                Calendar.SUNDAY, 2 * 3600000, 7200000);
        assertEquals("Incorrect daylight savings from constructor", 7200000, st1
                .getDSTSavings());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOffset",
        args = {int.class, int.class, int.class, int.class, int.class, int.class}
    )
    public void test_getOffsetIIIIII() {
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        assertTrue("Incorrect offset returned", st1.getOffset(
                GregorianCalendar.AD, 1998, Calendar.NOVEMBER, 11,
                Calendar.WEDNESDAY, 0) == -(5 * 60 * 60 * 1000));
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        assertEquals("Incorrect offset returned", -(5 * 60 * 60 * 1000), st1
                .getOffset(GregorianCalendar.AD, 1998, Calendar.JUNE, 11,
                        Calendar.THURSDAY, 0));
        st1 = new SimpleTimeZone(TimeZone.getDefault().getRawOffset(), TimeZone.getDefault().getID());
        int fourHours = 4*60*60*1000; 
        st1.setRawOffset(fourHours); 
        assertEquals(fourHours, st1.getOffset(1, 2099, 01, 1, 5, 0));
        try {
            st1.getOffset(-1, 2099, 01, 1, 5, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            st1.getOffset(1, 2099, 15, 1, 5, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            st1.getOffset(1, 2099, 01, 100, 5, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            st1.getOffset(1, 2099, 01, 1, 50, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            st1.getOffset(1, 2099, 01, 1, 5, -10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRawOffset",
        args = {}
    )
    public void test_getRawOffset() {
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        assertTrue("Incorrect offset returned",
                st1.getRawOffset() == -(5 * 60 * 60 * 1000));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        st1 = new SimpleTimeZone(-5 * 3600000, "EST", Calendar.APRIL, 1,
                -Calendar.SUNDAY, 2 * 3600000, Calendar.OCTOBER, -1,
                Calendar.SUNDAY, 2 * 3600000);
        assertTrue(TimeZone.getTimeZone("EST").hashCode() != 0);
        assertTrue(st1.hashCode() != 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasSameRules",
        args = {java.util.TimeZone.class}
    )
    public void test_hasSameRulesLjava_util_TimeZone() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
                1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
                0);
        SimpleTimeZone sameAsSt = new SimpleTimeZone(1000, "REST",
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
                -1, Calendar.SUNDAY, 0);
        SimpleTimeZone notSameAsSt = new SimpleTimeZone(1000, "PEST",
                Calendar.NOVEMBER, 2, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
                -1, Calendar.SUNDAY, 0);
        assertTrue("Time zones have same rules but return false", st
                .hasSameRules(sameAsSt));
        assertTrue("Time zones have different rules but return true", !st
                .hasSameRules(notSameAsSt));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "inDaylightTime",
        args = {java.util.Date.class}
    )
    public void test_inDaylightTimeLjava_util_Date() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        SimpleTimeZone zone = new SimpleTimeZone(tz.getRawOffset(), "EST",
                Calendar.APRIL, 1, -Calendar.SUNDAY, 7200000, Calendar.OCTOBER, -1, Calendar.SUNDAY, 7200000, 3600000); 
        GregorianCalendar gc = new GregorianCalendar(1998, Calendar.JUNE, 11);
        assertTrue("Returned incorrect daylight value1", zone.inDaylightTime(gc
                .getTime()));
        gc = new GregorianCalendar(1998, Calendar.NOVEMBER, 11);
        assertTrue("Returned incorrect daylight value2", !(zone
                .inDaylightTime(gc.getTime())));
        gc = new GregorianCalendar(zone);
        gc.set(1999, Calendar.APRIL, 4, 1, 59, 59);
        assertTrue("Returned incorrect daylight value3", !(zone
                .inDaylightTime(gc.getTime())));
        Date date = new Date(gc.getTime().getTime() + 1000);
        assertTrue("Returned incorrect daylight value4", zone
                .inDaylightTime(date));
        gc.set(1999, Calendar.OCTOBER, 31, 1, 0, 0);
        assertTrue("Returned incorrect daylight value5", !(zone
                .inDaylightTime(gc.getTime())));
        date = new Date(gc.getTime().getTime() - 1000);
        assertTrue("Returned incorrect daylight value6", zone
                .inDaylightTime(date));
        assertTrue("Returned incorrect daylight value7", !zone
                .inDaylightTime(new Date(891752400000L + 7200000 - 1)));
        assertTrue("Returned incorrect daylight value8", zone
                .inDaylightTime(new Date(891752400000L + 7200000)));
        assertTrue("Returned incorrect daylight value9", zone
                .inDaylightTime(new Date(909288000000L + 7200000 - 1)));
        assertTrue("Returned incorrect daylight value10", !zone
                .inDaylightTime(new Date(909288000000L + 7200000)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDSTSavings",
        args = {int.class}
    )
    public void test_setDSTSavingsI() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(0, 1, 1, 1);
        st.setEndRule(11, 1, 1, 1);
        st.setDSTSavings(1);
        assertEquals("Daylight savings amount not set", 1, st.getDSTSavings());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEndRule",
        args = {int.class, int.class, int.class}
    )
    public void test_setEndRuleIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 1, 0);
        st.setEndRule(Calendar.NOVEMBER, 20, 0);
        assertTrue("StartRule improperly set1", st.useDaylightTime());
        assertTrue("StartRule improperly set2", st.inDaylightTime(
                new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime()));
        assertTrue("StartRule improperly set3", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        try {
            st.setEndRule(13, 20, 0);    
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(1, 32, 0);    
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(1, 30, 10);    
            fail("IllegalArgumentException is not thrown.");           
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEndRule",
        args = {int.class, int.class, int.class, int.class}
    )
    public void test_setEndRuleIIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
        st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
        assertTrue("StartRule improperly set1", st.useDaylightTime());
        assertTrue("StartRule improperly set2", st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime()));
        assertTrue("StartRule improperly set3", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        try {
            st.setEndRule(12, -1, Calendar.SUNDAY, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, 10, Calendar.SUNDAY, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, -1, 8, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, -10);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEndRule",
        args = {int.class, int.class, int.class, int.class, boolean.class}
    )
    public void test_setEndRuleIIIIZ() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 8, Calendar.SUNDAY, 1, false);
        st.setEndRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, 1, true);
        assertTrue("StartRule improperly set1", st.useDaylightTime());
        assertTrue("StartRule improperly set2", st
                .inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
                        7, 12, 0).getTime())));
        assertTrue("StartRule improperly set3", st
                .inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
                        20, 12, 0).getTime())));
        assertTrue("StartRule improperly set4", !(st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
                        6, 12, 0).getTime())));
        assertTrue("StartRule improperly set5", !(st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
                        21, 12, 0).getTime())));
        try {
            st.setEndRule(20, 15, Calendar.SUNDAY, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, 35, Calendar.SUNDAY, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, 15, 12, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setEndRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, -1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setRawOffset",
        args = {int.class}
    )
    public void test_setRawOffsetI() {
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        int off = st1.getRawOffset();
        st1.setRawOffset(1000);
        boolean val = st1.getRawOffset() == 1000;
        st1.setRawOffset(off);
        assertTrue("Incorrect offset set", val);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setStartRule",
        args = {int.class, int.class, int.class}
    )
    public void test_setStartRuleIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 1, 1);
        st.setEndRule(Calendar.DECEMBER, 1, 1);
        assertTrue("StartRule improperly set", st.useDaylightTime());
        assertTrue("StartRule improperly set", st
                .inDaylightTime((new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime())));
        assertTrue("StartRule improperly set", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        try {
            st.setStartRule(13, 20, 0);    
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(1, 32, 0);    
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(1, 30, 10);    
            fail("IllegalArgumentException is not thrown.");           
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setStartRule",
        args = {int.class, int.class, int.class, int.class}
    )
    public void test_setStartRuleIIII() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
        st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
        assertTrue("StartRule improperly set1", st.useDaylightTime());
        assertTrue("StartRule improperly set2", st
                .inDaylightTime((new GregorianCalendar(1998, Calendar.NOVEMBER,
                        13).getTime())));
        assertTrue("StartRule improperly set3", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        try {
            st.setStartRule(12, -1, Calendar.SUNDAY, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, 10, Calendar.SUNDAY, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, -1, 8, 0);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, -10);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setStartRule",
        args = {int.class, int.class, int.class, int.class, boolean.class}
    )
    public void test_setStartRuleIIIIZ() {
        SimpleTimeZone st = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 1, true);
        st.setEndRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, 1, false);
        assertTrue("StartRule improperly set1", st.useDaylightTime());
        assertTrue("StartRule improperly set2", st
                .inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
                        7, 12, 0).getTime())));
        assertTrue("StartRule improperly set3", st
                .inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
                        13, 12, 0).getTime())));
        assertTrue("StartRule improperly set4", !(st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
                        6, 12, 0).getTime())));
        assertTrue("StartRule improperly set5", !(st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
                        14, 12, 0).getTime())));
        try {
            st.setStartRule(20, 15, Calendar.SUNDAY, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, 35, Calendar.SUNDAY, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, 15, 12, 1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            st.setStartRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, -1, true);
            fail("IllegalArgumentException is not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setStartYear",
        args = {int.class}
    )
    public void test_setStartYearI() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
        st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
        st.setStartYear(1999);
        assertTrue("set year improperly set1", !(st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.JULY, 12)
                        .getTime())));
        assertTrue("set year improperly set2", !(st
                .inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
                        13).getTime())));
        assertTrue("set year improperly set3", (st
                .inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
                        13).getTime())));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        String string = TimeZone.getTimeZone("EST").toString();
        assertNotNull("toString() returned null", string);
        assertTrue("toString() is empty", string.length() != 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "useDaylightTime",
        args = {}
    )
    public void test_useDaylightTime() {
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
        assertTrue("useDaylightTime returned incorrect value", !st
                .useDaylightTime());
        st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
        st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
        assertTrue("useDaylightTime returned incorrect value", st
                .useDaylightTime());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOffset",
        args = {long.class}
    )
    public void test_getOffsetJ() {
        Calendar cal = Calendar.getInstance();
        cal.set(1998, Calendar.NOVEMBER, 11, 0, 0);
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        assertTrue("Incorrect offset returned", st1.getOffset(cal.getTimeInMillis()) ==
            -(5 * 60 * 60 * 1000));
        st1 = new SimpleTimeZone(TimeZone.getTimeZone("EST").getRawOffset(), "EST");
        cal.set(1998, Calendar.JUNE, 11, 0, 0);
        assertEquals("Incorrect offset returned", -(5 * 60 * 60 * 1000), st1
                .getOffset(cal.getTimeInMillis()));
        st1 = new SimpleTimeZone(TimeZone.getDefault().getRawOffset(), TimeZone.getDefault().getID());
        int fourHours = 4*60*60*1000; 
        st1.setRawOffset(fourHours); 
        cal.set(2099, 01, 1, 0, 0);
        assertEquals(fourHours, st1.getOffset(cal.getTimeInMillis()));
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
