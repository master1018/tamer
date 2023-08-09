@TestTargetClass(NumberFormat.Field.class) 
public class NumberFormatFieldTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String name = "new number format";
        MyNumberFormat field = new MyNumberFormat(name);
        assertEquals("field has wrong name", name, field.getName());
        field = new MyNumberFormat(null);
        assertEquals("field has wrong name", null, field.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    @KnownFailure("readResolve does not work properly")
    public void test_readResolve() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);
            NumberFormat.Field nfield, nfield2;
            MyNumberFormat field;
            nfield = NumberFormat.Field.CURRENCY;
            field = new MyNumberFormat(null);
            out.writeObject(nfield);
            out.writeObject(field);
            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));
            try {
                nfield2 = (NumberFormat.Field) in.readObject();
                assertSame("resolved incorrectly", nfield, nfield2);
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
    static class MyNumberFormat extends NumberFormat.Field {
        static final long serialVersionUID = 1L;
        static boolean flag = false;
        protected MyNumberFormat(String attr) {
            super(attr);
        }
        protected String getName() {
            return super.getName();
        }
    }
}
