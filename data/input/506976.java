@TestTargetClass(Format.class) 
public class FormatTest extends TestCase {
    private class MockFormat extends Format {
        public StringBuffer format(Object obj, StringBuffer toAppendTo,
                FieldPosition pos) {
            if (obj == null)
                throw new NullPointerException("obj is null");
            return new StringBuffer("");
        }
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Format",
        args = {}
    )
    public void test_Constructor() {
        try {
            new MockFormat();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        try {
            Format fm = new MockFormat();
            Format fmc = (Format) fm.clone();
            assertEquals(fm.getClass(), fmc.getClass());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that format(Object) calls format(Object, StringBuffer, FieldPosition) method.",
        method = "format",
        args = {java.lang.Object.class}
    )
    public void test_formatLjava_lang_Object() {
        MockFormat mf = new MockFormat();
        assertEquals("", mf.format(""));
        assertTrue("It calls an abstract metod format", true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "formatToCharacterIterator",
        args = {java.lang.Object.class}
    )
    public void test_formatToCharacterIteratorLjava_lang_Object() {
        MockFormat mf = new MockFormat();
        AttributedCharacterIterator aci = 
                                  mf.formatToCharacterIterator("Test 123 Test");
        assertEquals(0, aci.getBeginIndex());
        try {
            mf.formatToCharacterIterator(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        try {
            mf.formatToCharacterIterator("");
        } catch(IllegalArgumentException  iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that parseObject(String) method calls parseObject(String source, ParsePosition pos) method.",
        method = "parseObject",
        args = {java.lang.String.class}
    )
    public void test_parseObjectLjava_lang_String() {
        MockFormat mf = new MockFormat();
        try {
            assertNull(mf.parseObject(""));
            fail("ParseException was not thrown.");
        } catch (ParseException e) {
        }
    }
}
