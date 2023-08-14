@TestTargetClass(MessageFormat.Field.class) 
public class MessageFormatFieldTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String name = "new Message format";
        MyMessageFormat field = new MyMessageFormat(name);
        assertEquals("field has wrong name", name, field.getName());
        field = new MyMessageFormat(null);
        assertEquals("field has wrong name", null, field.getName());
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
            MessageFormat.Field mfield, mfield2;
            MyMessageFormat field;
            mfield = MessageFormat.Field.ARGUMENT;
            field = new MyMessageFormat(null);
            out.writeObject(mfield);
            out.writeObject(field);
            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));
            try {
                mfield2 = (MessageFormat.Field) in.readObject();
                assertSame("resolved incorrectly", mfield, mfield2);
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
    static class MyMessageFormat extends MessageFormat.Field {
        static final long serialVersionUID = 1L;
        protected MyMessageFormat(String attr) {
            super(attr);
        }
        protected String getName() {
            return super.getName();
        }
    }
}
