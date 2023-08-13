@TestTargetClass(AttributedString.class) 
public class AttributedStringTest extends junit.framework.TestCase {
    static void assertEqualString (String msg, String expected, AttributedString attrString) {
        AttributedCharacterIterator it = attrString.getIterator();
        StringBuffer buf = new StringBuffer();
        buf.append(it.first());
        char ch;
        while ((ch = it.next()) != CharacterIterator.DONE)
            buf.append(ch);
        assertEquals(msg, expected, buf.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AttributedString",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String test = "Test string";
        AttributedString attrString = new AttributedString(test);
        assertEqualString("String must match!", test, attrString);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functional Test",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class}
    )
    public void test_ConstructorLAttributedCharacterIterator_1() {
        String testString = "Test string";
        AttributedString attrString = new AttributedString(testString);
        AttributedCharacterIterator iter = attrString.getIterator();
        AttributedString attrString2 = new AttributedString(iter);
        assertEqualString("String must match!", testString, attrString2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression for HARMONY-1354 (no functional test)",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class}
    )
    public void test_ConstructorLAttributedCharacterIterator() {
        assertNotNull(new AttributedString(
                new testAttributedCharacterIterator()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functional Test",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class, int.class, int.class}
    )
    public void test_ConstructorLAttributedCharacterIterator_2() {
        String testString = "Test string";
        AttributedString attrString = new AttributedString(testString);
        AttributedCharacterIterator iter = attrString.getIterator();
        AttributedString attrString2 = new AttributedString(iter, 2, 7);
        assertEqualString("String must match!", "st st", attrString2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "No functional test.",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class, int.class, int.class}
    )
    public void test_ConstructorLAttributedCharacterIteratorII() {
        try {
            new AttributedString(new testAttributedCharacterIterator(), 0, 0);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            new AttributedString(new testAttributedCharacterIterator(), -1, 0);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            new AttributedString(new testAttributedCharacterIterator(), 0, -1);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functional Test",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class, int.class, int.class, java.text.AttributedCharacterIterator.Attribute[].class}
    )
    public void test_ConstructorLAttributedCharacterIterator_3() {
        String testString = "Test string";
        AttributedString attrString = new AttributedString(testString);
        AttributedCharacterIterator iter = attrString.getIterator();
        AttributedString attrString2;
        attrString2 = new AttributedString(iter, 2, 7, new AttributedCharacterIterator.Attribute[] {});
        assertEqualString("String must match!", "st st", attrString2);
        attrString2 = new AttributedString(iter, 2, 7, null);
        assertEqualString("String must match!", "st st", attrString2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "No functional test.",
        method = "AttributedString",
        args = {java.text.AttributedCharacterIterator.class, int.class, int.class, java.text.AttributedCharacterIterator.Attribute[].class}
    )
    public void test_ConstructorLAttributedCharacterIteratorII$Ljava_text_AttributedCharacterIterator$Attribute() {
        try {
            new AttributedString(new testAttributedCharacterIterator(), 0, 0,
                    null);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            new AttributedString(new testAttributedCharacterIterator(), -1, 0,
                    null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            new AttributedString(new testAttributedCharacterIterator(), 0, -1,
                    null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            AttributedCharacterIterator.Attribute[] attributes = new AttributedCharacterIterator.Attribute[1];
            attributes[0] = new TestAttributedCharacterIteratorAttribute("test");
            new AttributedString(new testAttributedCharacterIterator(), 0, 0,
                    attributes);
        } catch (IllegalArgumentException e) {
            fail("Unexpected expected " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AttributedString",
        args = {java.lang.String.class, java.util.Map.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_util_Map() {
        String test = "Test string";
        try {
            AttributedString attrString = new AttributedString(
                    test,
                    new WeakHashMap<AttributedCharacterIterator.Attribute, String>());
            AttributedCharacterIterator it = attrString.getIterator();
            StringBuffer buf = new StringBuffer();
            buf.append(it.first());
            char ch;
            while ((ch = it.next()) != CharacterIterator.DONE)
                buf.append(ch);
            assertTrue("Wrong string: " + buf, buf.toString().equals(test));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            Map<AttributedCharacterIterator.Attribute, String> whm = new WeakHashMap<AttributedCharacterIterator.Attribute, String>();
            whm.put(new TestAttributedCharacterIteratorAttribute("test"),
                    "value");
            new AttributedString("", whm);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (Exception e) {
        }
    }
    private class TestAttributedCharacterIteratorAttribute extends
            AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = -2917613373935785179L;
        public TestAttributedCharacterIteratorAttribute(String name) {
            super(name);
        }
    }
    private class testAttributedCharacterIterator implements
            AttributedCharacterIterator {
        public Set getAllAttributeKeys() {
            return null;
        }
        public Object getAttribute(AttributedCharacterIterator.Attribute p) {
            return null;
        }
        public Map getAttributes() {
            return null;
        }
        public int getRunLimit(Set p) {
            return 0;
        }
        public int getRunLimit(AttributedCharacterIterator.Attribute p) {
            return 0;
        }
        public int getRunLimit() {
            return 0;
        }
        public int getRunStart(Set p) {
            return 0;
        }
        public int getRunStart(AttributedCharacterIterator.Attribute p) {
            return 0;
        }
        public int getRunStart() {
            return 0;
        }
        public Object clone() {
            return null;
        }
        public int getIndex() {
            return 0;
        }
        public int getEndIndex() {
            return 0;
        }
        public int getBeginIndex() {
            return 0;
        }
        public char setIndex(int p) {
            return 'a';
        }
        public char previous() {
            return 'a';
        }
        public char next() {
            return 'a';
        }
        public char current() {
            return 'a';
        }
        public char last() {
            return 'a';
        }
        public char first() {
            return 'a';
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAttribute",
        args = {java.text.AttributedCharacterIterator.Attribute.class, java.lang.Object.class, int.class, int.class}
    )
    public void test_addAttributeLjava_text_AttributedCharacterIterator$AttributeLjava_lang_ObjectII() {
        AttributedString as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "a", 2,
                3);
        AttributedCharacterIterator it = as.getIterator();
        assertEquals("non-null value limit", 2, it
                .getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));
        as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, null,
                2, 3);
        it = as.getIterator();
        assertEquals("null value limit", 4, it
                .getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));
        try {
            as = new AttributedString("test");
            as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE,
                    null, -1, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        as = new AttributedString("123", new WeakHashMap());
        try {
            as.addAttribute(null, new TreeSet(), 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            as.addAttribute(null, new TreeSet(), -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAttribute",
        args = {java.text.AttributedCharacterIterator.Attribute.class, java.lang.Object.class}
    )
    public void test_addAttributeLjava_text_AttributedCharacterIterator$AttributeLjava_lang_Object() {
        AttributedString as = new AttributedString("123", new WeakHashMap());
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "english");
        as.addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT, 
                                                                "input method");
        as.addAttribute(AttributedCharacterIterator.Attribute.READING, "reading");
        try {
            as.addAttribute(null, new TreeSet());
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            as.addAttribute(null, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAttributes",
        args = {java.util.Map.class, int.class, int.class}
    )
    public void test_addAttributesLjava_util_MapII() {
        AttributedString as = new AttributedString("test");
        Map<AttributedCharacterIterator.Attribute, String> whm = new WeakHashMap<AttributedCharacterIterator.Attribute, String>();
        try {
            whm.put(new TestAttributedCharacterIteratorAttribute("test1"),
                    "value1");
            whm.put(new TestAttributedCharacterIteratorAttribute("test2"),
                    "value2");
            whm.put(new TestAttributedCharacterIteratorAttribute("test3"),
                    "value3");
            as.addAttributes(whm, 0, 3);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            as.addAttributes(null, 0, 3);
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
        }
        try {
            as.addAttributes(whm, 0, 0);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterator",
        args = {}
    )
    public void test_getIterator() {
        String test = "Test string";
        try {
            AttributedString attrString = new AttributedString(test);
            AttributedCharacterIterator it = attrString.getIterator();
            assertEquals("Incorrect iteration on AttributedString", it.first(),
                    test.charAt(0));
        } catch (Exception e) {
            fail("Unexpected exceptiption " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterator",
        args = {java.text.AttributedCharacterIterator.Attribute[].class}
    )
    public void test_getIterator$Ljava_text_AttributedCharacterIterator$Attribute() {
        String test = "Test string";
        try {
            Map<AttributedCharacterIterator.Attribute, String> hm = new HashMap<AttributedCharacterIterator.Attribute, String>();
            AttributedCharacterIterator.Attribute[] aci = new AttributedCharacterIterator.Attribute[3];
            aci[0] = new TestAttributedCharacterIteratorAttribute("att1");
            aci[1] = new TestAttributedCharacterIteratorAttribute("att2");
            aci[2] = new TestAttributedCharacterIteratorAttribute("att3");
            hm.put(aci[0], "value1");
            hm.put(aci[1], "value2");
            AttributedString attrString = new AttributedString(test, hm);
            AttributedCharacterIterator it = attrString.getIterator(aci);
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[0]).equals("value1"));
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[1]).equals("value2"));
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[2]) == null);
        } catch (Exception e) {
            fail("Unexpected exceptiption " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterator",
        args = {java.text.AttributedCharacterIterator.Attribute[].class, int.class, int.class}
    )
    public void test_getIterator$Ljava_text_AttributedCharacterIterator$AttributeII() {
        String test = "Test string";
        try {
            Map<AttributedCharacterIterator.Attribute, String> hm = new HashMap<AttributedCharacterIterator.Attribute, String>();
            AttributedCharacterIterator.Attribute[] aci = new AttributedCharacterIterator.Attribute[3];
            aci[0] = new TestAttributedCharacterIteratorAttribute("att1");
            aci[1] = new TestAttributedCharacterIteratorAttribute("att2");
            aci[2] = new TestAttributedCharacterIteratorAttribute("att3");
            hm.put(aci[0], "value1");
            hm.put(aci[1], "value2");
            AttributedString attrString = new AttributedString(test);
            attrString.addAttributes(hm, 2, 4);
            AttributedCharacterIterator it = attrString.getIterator(aci, 1, 5);
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[0]) == null);
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[1]) == null);
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[2]) == null);
            it.next();
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[0]).equals("value1"));
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[1]).equals("value2"));
            assertTrue("Incorrect iteration on AttributedString", it
                    .getAttribute(aci[2]) == null);
            try {
                attrString.getIterator(aci, -1, 5);
                fail("IllegalArgumentException is not thrown.");
            } catch(IllegalArgumentException iae) {
            }
            try {
                attrString.getIterator(aci, 6, 5);
                fail("IllegalArgumentException is not thrown.");
            } catch(IllegalArgumentException iae) {
            }
            try {
                attrString.getIterator(aci, 3, 2);
                fail("IllegalArgumentException is not thrown.");
            } catch(IllegalArgumentException iae) {
            }            
        } catch (Exception e) {
            fail("Unexpected exceptiption " + e.toString());
        }
    }
}
