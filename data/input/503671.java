@TestTargetClass(FieldPosition.class) 
public class FieldPositionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FieldPosition",
        args = {int.class}
    )
    public void test_ConstructorI() {
        FieldPosition fpos = new FieldPosition(DateFormat.MONTH_FIELD);
        assertEquals("Test1: Constructor failed to set field identifier!",
                DateFormat.MONTH_FIELD, fpos.getField());
        assertNull("Constructor failed to set field attribute!", fpos
                .getFieldAttribute());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FieldPosition",
        args = {java.text.Format.Field.class}
    )
    public void test_ConstructorLjava_text_Format$Field() {
        FieldPosition fpos = new FieldPosition(DateFormat.Field.MONTH);
        assertSame("Constructor failed to set field attribute!",
                DateFormat.Field.MONTH, fpos.getFieldAttribute());
        assertEquals("Test1: Constructor failed to set field identifier!", -1,
                fpos.getField());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FieldPosition",
        args = {java.text.Format.Field.class, int.class}
    )
    public void test_ConstructorLjava_text_Format$FieldI() {
        FieldPosition fpos = new FieldPosition(DateFormat.Field.MONTH,
                DateFormat.MONTH_FIELD);
        assertSame("Constructor failed to set field attribute!",
                DateFormat.Field.MONTH, fpos.getFieldAttribute());
        assertEquals("Test1: Constructor failed to set field identifier!",
                DateFormat.MONTH_FIELD, fpos.getField());
        FieldPosition fpos2 = new FieldPosition(DateFormat.Field.HOUR1,
                DateFormat.HOUR1_FIELD);
        assertSame("Constructor failed to set field attribute!",
                DateFormat.Field.HOUR1, fpos2.getFieldAttribute());
        assertEquals("Test2: Constructor failed to set field identifier!",
                DateFormat.HOUR1_FIELD, fpos2.getField());
        FieldPosition fpos3 = new FieldPosition(DateFormat.Field.TIME_ZONE,
                DateFormat.MONTH_FIELD);
        assertSame("Constructor failed to set field attribute!",
                DateFormat.Field.TIME_ZONE, fpos3.getFieldAttribute());
        assertEquals("Test3: Constructor failed to set field identifier!",
                DateFormat.MONTH_FIELD, fpos3.getField());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        FieldPosition fpos = new FieldPosition(1);
        FieldPosition fpos1 = new FieldPosition(1);
        assertTrue("Identical objects were not equal!", fpos.equals(fpos1));
        FieldPosition fpos2 = new FieldPosition(2);
        assertTrue("Objects with a different ID should not be equal!", !fpos
                .equals(fpos2));
        fpos.setBeginIndex(1);
        fpos1.setBeginIndex(2);
        assertTrue("Objects with a different beginIndex were still equal!",
                !fpos.equals(fpos1));
        fpos1.setBeginIndex(1);
        fpos1.setEndIndex(2);
        assertTrue("Objects with a different endIndex were still equal!", !fpos
                .equals(fpos1));
        FieldPosition fpos3 = new FieldPosition(DateFormat.Field.ERA, 1);
        assertTrue("Objects with a different attribute should not be equal!",
                !fpos.equals(fpos3));
        FieldPosition fpos4 = new FieldPosition(DateFormat.Field.AM_PM, 1);
        assertTrue("Objects with a different attribute should not be equal!",
                !fpos3.equals(fpos4));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBeginIndex",
        args = {}
    )
    public void test_getBeginIndex() {
        FieldPosition fpos = new FieldPosition(1);
        fpos.setEndIndex(3);
        fpos.setBeginIndex(2);
        assertEquals("getBeginIndex should have returned 2", 2, fpos
                .getBeginIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEndIndex",
        args = {}
    )
    public void test_getEndIndex() {
        FieldPosition fpos = new FieldPosition(1);
        fpos.setBeginIndex(2);
        fpos.setEndIndex(3);
        assertEquals("getEndIndex should have returned 3", 3, fpos
                .getEndIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getField",
        args = {}
    )
    public void test_getField() {
        FieldPosition fpos = new FieldPosition(65);
        assertEquals(
                "FieldPosition(65) should have caused getField to return 65",
                65, fpos.getField());
        FieldPosition fpos2 = new FieldPosition(DateFormat.Field.MINUTE);
        assertEquals(
                "FieldPosition(DateFormat.Field.MINUTE) should have caused getField to return -1",
                -1, fpos2.getField());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFieldAttribute",
        args = {}
    )
    public void test_getFieldAttribute() {
        FieldPosition fpos = new FieldPosition(DateFormat.Field.TIME_ZONE);
        assertTrue(
                "FieldPosition(DateFormat.Field.TIME_ZONE) should have caused getFieldAttribute to return DateFormat.Field.TIME_ZONE",
                fpos.getFieldAttribute() == DateFormat.Field.TIME_ZONE);
        FieldPosition fpos2 = new FieldPosition(DateFormat.TIMEZONE_FIELD);
        assertNull(
                "FieldPosition(DateFormat.TIMEZONE_FIELD) should have caused getFieldAttribute to return null",
                fpos2.getFieldAttribute());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        FieldPosition fpos1 = new FieldPosition(1);
        FieldPosition fpos2 = new FieldPosition(1);
        assertTrue("test 1: hash codes are not equal for equal objects.", 
                fpos1.hashCode() == fpos2.hashCode());        
        fpos1.setBeginIndex(5);
        fpos1.setEndIndex(110);
        assertTrue("test 2: hash codes are equal for non equal objects.", 
                fpos1.hashCode() != fpos2.hashCode());
        fpos2.setBeginIndex(5);
        fpos2.setEndIndex(110);
        assertTrue("test 3: hash codes are not equal for equal objects.", 
                fpos1.hashCode() == fpos2.hashCode());
        FieldPosition fpos3 = new FieldPosition(
                DateFormat.Field.DAY_OF_WEEK_IN_MONTH);
        assertTrue("test 4: hash codes are equal for non equal objects.", 
                fpos2.hashCode() != fpos3.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setBeginIndex",
        args = {int.class}
    )
    public void test_setBeginIndexI() {
        FieldPosition fpos = new FieldPosition(1);
        fpos.setBeginIndex(2);
        fpos.setEndIndex(3);
        assertEquals("beginIndex should have been set to 2", 2, fpos
                .getBeginIndex());
        fpos.setBeginIndex(Integer.MAX_VALUE);
        assertEquals("beginIndex should have been set to Integer.MAX_VALUE", 
                Integer.MAX_VALUE, fpos.getBeginIndex());
        fpos.setBeginIndex(-1);
        assertEquals("beginIndex should have been set to -1", 
                -1, fpos.getBeginIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEndIndex",
        args = {int.class}
    )
    public void test_setEndIndexI() {
        FieldPosition fpos = new FieldPosition(1);
        fpos.setEndIndex(3);
        fpos.setBeginIndex(2);
        assertEquals("EndIndex should have been set to 3", 3, fpos
                .getEndIndex());
        fpos.setEndIndex(Integer.MAX_VALUE);
        assertEquals("endIndex should have been set to Integer.MAX_VALUE", 
                Integer.MAX_VALUE, fpos.getEndIndex());
        fpos.setEndIndex(-1);
        assertEquals("endIndex should have been set to -1", 
                -1, fpos.getEndIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        FieldPosition fpos = new FieldPosition(1);
        fpos.setBeginIndex(2);
        fpos.setEndIndex(3);
        assertNotNull(
                "toString returned null",
                fpos.toString());
        FieldPosition fpos2 = new FieldPosition(DateFormat.Field.ERA);
        fpos2.setBeginIndex(4);
        fpos2.setEndIndex(5);
        assertNotNull("ToString returned the wrong value:",
                      fpos2.toString());
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
