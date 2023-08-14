@TestTargetClass(AttributedCharacterIterator.Attribute.class) 
public class AttributedCharacterIteratorAttributeTest extends
        junit.framework.TestCase {
    private class MockAttributedCharacterIteratorAttribute extends
            AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = 1L;
        public MockAttributedCharacterIteratorAttribute(String name) {
            super(name);
        }
        @Override
        public String getName() {
            return super.getName();
        }
        @Override
        public Object readResolve() throws InvalidObjectException {
            return super.readResolve();
        }
    }
    private class TestAttributedCharacterIteratorAttribute extends
            AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = -2917613373935785179L;
        public TestAttributedCharacterIteratorAttribute(String name) {
            super(name);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Attribute",
        args = {java.lang.String.class}
    )
    public void test_Constructor() {
        try {
            new MockAttributedCharacterIteratorAttribute("test");
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            MockAttributedCharacterIteratorAttribute mac2 = new MockAttributedCharacterIteratorAttribute(
                    "test2");
            assertFalse("Attributes are equal", mac2.equals(mac1));
            TestAttributedCharacterIteratorAttribute mac3 = new TestAttributedCharacterIteratorAttribute(
                    "test1");
            assertFalse("Attributes are equal", mac3.equals(mac1));
            AttributedCharacterIterator.Attribute mac4 = mac1;
            assertTrue("Attributes are non-equal", mac4.equals(mac1));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            assertEquals("Incorrect attribute name", "test1", mac1.getName());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            TestAttributedCharacterIteratorAttribute mac2 = new TestAttributedCharacterIteratorAttribute(
                    "test1");
            assertTrue("The hash codes of same attributes are not equal", mac1
                    .hashCode() != mac2.hashCode());
            MockAttributedCharacterIteratorAttribute mac3 = new MockAttributedCharacterIteratorAttribute(
                    "test2");
            assertTrue("The hash codes of different attributes are equal", mac1
                    .hashCode() != mac3.hashCode());
            AttributedCharacterIterator.Attribute mac4 = mac1;
            assertTrue("The hash codes of same attributes but different hierarchy classes are not equal",
                    mac1.hashCode() == mac4.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
    public void test_readResolve() {
        MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                "test");
        try {
            mac1.readResolve();
            fail("InvalidObjectException has not been thrown");
        } catch (InvalidObjectException e) {
        }
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);
            AttributedCharacterIterator.Attribute attr1, attr2;
            attr1 = AttributedCharacterIterator.Attribute.LANGUAGE;
            out.writeObject(attr1);
            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));
            try {
                attr2 = (AttributedCharacterIterator.Attribute) in.readObject();
                assertSame("resolved incorrectly", attr1, attr2);
            } catch (IllegalArgumentException e) {
                fail("Unexpected IllegalArgumentException: " + e);
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                null);
        assertEquals("Unexpected class representation string", mac1.toString(),
                getClass().getName()
                        + "$MockAttributedCharacterIteratorAttribute(null)");
        TestAttributedCharacterIteratorAttribute mac2 = new TestAttributedCharacterIteratorAttribute(
                "test1");
        assertEquals("Unexpected class representation string", mac2.toString(),
                getClass().getName()
                        + "$TestAttributedCharacterIteratorAttribute(test1)");
    }
}
