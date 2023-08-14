@TestTargetClass(DateFormat.Field.class) 
public class DataFormatFieldTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class, int.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        MyField field = new MyField("day of month", Calendar.ERA);
        assertEquals("field has wrong name", "day of month", field.getName());
        assertEquals("field has wrong Calendar field number", Calendar.ERA,
                field.getCalendarField());
        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.ERA);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.ERA, realField);
        DateFormat.Field realField2 = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_MONTH);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_MONTH, realField2);
    }
    static class MyField extends DateFormat.Field {
        private static final long serialVersionUID = 1L;
        protected MyField(String fieldName, int calendarField) {
            super(fieldName, calendarField);
        }
        protected String getName() {
            return super.getName();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class, int.class}
    )
    public void test_ConstructorLjava_lang_StringI() {
        MyField field = new MyField("a field", Calendar.DAY_OF_WEEK);
        assertEquals("field has wrong name", "a field", field.getName());
        assertEquals("field has wrong Calendar field number",
                Calendar.DAY_OF_WEEK, field.getCalendarField());
        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_WEEK);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_WEEK, realField);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class, int.class}
    )
    public void test_Constructor2() {
        MyField field = new MyField("day of month", Calendar.ERA);
        assertEquals("field has wrong name", "day of month", field.getName());
        assertEquals("field has wrong Calendar field number", Calendar.ERA,
                field.getCalendarField());
        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.ERA);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.ERA, realField);
        DateFormat.Field realField2 = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_MONTH);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_MONTH, realField2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCalendarField",
        args = {}
    )
    public void test_getCalendarField() {
        assertEquals("Field.AM_PM.getCalendarField() returned the wrong value",
                Calendar.AM_PM, Field.AM_PM.getCalendarField());
        assertEquals(
                "Field.TIME_ZONE.getCalendarField() returned the wrong value",
                -1, Field.TIME_ZONE.getCalendarField());
        assertEquals("Field.HOUR0.getCalendarField() returned the wrong value",
                Calendar.HOUR, Field.HOUR0.getCalendarField());
        assertEquals("Field.HOUR1.getCalendarField() returned the wrong value",
                -1, Field.HOUR1.getCalendarField());
        assertEquals(
                "Field.HOUR_OF_DAY0.getCalendarField() returned the wrong value",
                Calendar.HOUR_OF_DAY, Field.HOUR_OF_DAY0.getCalendarField());
        assertEquals(
                "Field.HOUR_OF_DAY1.getCalendarField() returned the wrong value",
                -1, Field.HOUR_OF_DAY1.getCalendarField());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ofCalendarField",
        args = {int.class}
    )
    public void test_ofCalendarFieldI() {
        assertSame("ofCalendarField(Calendar.AM_PM) returned the wrong value",
                Field.AM_PM, Field.ofCalendarField(Calendar.AM_PM));
        assertSame("ofCalendarField(Calendar.HOUR) returned the wrong value",
                Field.HOUR0, Field.ofCalendarField(Calendar.HOUR));
        assertSame(
                "ofCalendarField(Calendar.HOUR_OF_DAY) returned the wrong value",
                Field.HOUR_OF_DAY0, Field.ofCalendarField(Calendar.HOUR_OF_DAY));
        try {
            DateFormat.Field.ofCalendarField(-1);
            fail("Expected IllegalArgumentException for ofCalendarField(-1)");
        } catch (IllegalArgumentException e) {
        }
        try {
            DateFormat.Field.ofCalendarField(Calendar.FIELD_COUNT);
            fail("Expected IllegalArgumentException for ofCalendarField(Calendar.FIELD_COUNT)");
        } catch (IllegalArgumentException e) {
        }
        assertNull(
                "ofCalendarField(Calendar.DST_OFFSET) returned the wrong value",
                DateFormat.Field.ofCalendarField(Calendar.DST_OFFSET));
        assertNull(
                "ofCalendarField(Calendar.ZONE_OFFSET) returned the wrong value",
                DateFormat.Field.ofCalendarField(Calendar.ZONE_OFFSET));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public void test_readResolve() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);
            DateFormat.Field dfield, dfield2;
            MyField field;
            dfield = DateFormat.Field.MILLISECOND;
            field = new MyField(null, Calendar.AM_PM);
            out.writeObject(dfield);
            out.writeObject(field);
            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));
            try {
                dfield2 = (Field) in.readObject();
                assertSame("resolved incorrectly", dfield, dfield2);
            } catch (IllegalArgumentException e) {
                fail("Unexpected IllegalArgumentException: " + e);
            }
            try {
                in.readObject();
                fail("Expected InvalidObjectException for subclass instance with null name");
            } catch (InvalidObjectException e) {
            }
        } catch (IOException e) {
            fail("unexpected IOException" + e);
        } catch (ClassNotFoundException e) {
            fail("unexpected ClassNotFoundException" + e);
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
    }
}
