@TestTargetClass(Level.class) 
public class LevelTest extends TestCase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks  the constructor without resource bundle parameter using normal values.",
            method = "Level",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks  the constructor without resource bundle parameter using normal values.",
            method = "getName",
            args = {}
        )
    })
    public void testConstructorNoResBundle_Normal() {
        MockLevel l = new MockLevel("level1", 1);
        assertEquals("level1", l.getName());
        assertEquals(1, l.intValue());
        assertNull(l.getResourceBundleName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks the constructor without resource bundle parameter using null name.",
            method = "Level",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks the constructor without resource bundle parameter using null name.",
            method = "getName",
            args = {}
        )
    })
    public void testConstructorNoResBundle_NullName() {
        try {
            new MockLevel(null, -2);
            fail("No expected NullPointerException");
        } catch (NullPointerException ignore) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks the constructor without resource bundle parameter using empty name.",
            method = "Level",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks the constructor without resource bundle parameter using empty name.",
            method = "getName",
            args = {}
        )
    })
     public void testConstructorNoResBundle_EmptyName() {
        MockLevel l = new MockLevel("", -3);
        assertEquals("", l.getName());
        assertEquals(-3, l.intValue());
        assertNull(l.getResourceBundleName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Level",
            args = {java.lang.String.class, int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getName",
            args = {}
        )
    })
    public void testConstructorHavingResBundle_Normal() {
        MockLevel l = new MockLevel("level1", 1, "resourceBundle");
        assertEquals("level1", l.getName());
        assertEquals(1, l.intValue());
        assertEquals("resourceBundle", l.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks NullPointerException.",
        method = "Level",
        args = {java.lang.String.class, int.class, java.lang.String.class}
    )
    public void testConstructorHavingResBundle_NullName() {
        try {
            new MockLevel(null, -123, "qwe");
            fail("No expected NullPointerException");
        } catch (NullPointerException ignore) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor having resource bundle parameter using empty names.",
        method = "Level",
        args = {java.lang.String.class, int.class, java.lang.String.class}
    )
     public void testConstructorHavingResBundle_EmptyName() {
     MockLevel l = new MockLevel("", -1000, "");
     assertEquals("", l.getName());
     assertEquals(-1000, l.intValue());
     assertEquals("", l.getResourceBundleName());
     }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies parse, with the pre-defined string consts.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_PredefinedConstStrings() {
        assertSame(Level.SEVERE, Level.parse("SEVERE"));
        assertSame(Level.WARNING, Level.parse("WARNING"));
        assertSame(Level.INFO, Level.parse("INFO"));
        assertSame(Level.CONFIG, Level.parse("CONFIG"));
        assertSame(Level.FINE, Level.parse("FINE"));
        assertSame(Level.FINER, Level.parse("FINER"));
        assertSame(Level.FINEST, Level.parse("FINEST"));
        assertSame(Level.OFF, Level.parse("OFF"));
        assertSame(Level.ALL, Level.parse("ALL"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalArgumentException is verified.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_IllegalConstString() {
        try {
            Level.parse("SEVERe");
            fail("Should throw IllegalArgumentException if undefined string.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_NullString() {
        try {
            Level.parse(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies parse, with pre-defined valid number strings.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_PredefinedNumber() {
        assertSame(Level.SEVERE, Level.parse("SEVERE"));
        assertSame(Level.WARNING, Level.parse("WARNING"));
        assertSame(Level.INFO, Level.parse("INFO"));
        assertSame(Level.CONFIG, Level.parse("CONFIG"));
        assertSame(Level.FINE, Level.parse("FINE"));
        assertSame(Level.FINER, Level.parse("FINER"));
        assertSame(Level.FINEST, Level.parse("FINEST"));
        assertSame(Level.OFF, Level.parse("OFF"));
        assertSame(Level.ALL, Level.parse("ALL"));
        assertSame(Level.SEVERE, Level.parse("1000"));
        assertSame(Level.WARNING, Level.parse("900"));
        assertSame(Level.INFO, Level.parse("800"));
        assertSame(Level.CONFIG, Level.parse("700"));
        assertSame(Level.FINE, Level.parse("500"));
        assertSame(Level.FINER, Level.parse("400"));
        assertSame(Level.FINEST, Level.parse("300"));
        assertSame(Level.OFF, Level.parse(String.valueOf(Integer.MAX_VALUE)));
        assertSame(Level.ALL, Level.parse(String.valueOf(Integer.MIN_VALUE)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies parse, with an undefined valid number strings.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_UndefinedNumber() {
        Level l = Level.parse("0");
        assertEquals(0, l.intValue());
        assertEquals("0", l.getName());
        assertNull(l.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies parse, with an undefined valid number strings with spaces.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_UndefinedNumberWithSpaces() {
        try {
            Level.parse(" 0");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative number.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_NegativeNumber() {
        Level l = Level.parse("-4");
        assertEquals(-4, l.intValue());
        assertEquals("-4", l.getName());
        assertNull(l.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies parse, expecting the same objects will be returned given the same name, even for non-predefined levels.",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testParse_SameObject() {
        Level l = Level.parse("-100");
        assertSame(l, Level.parse("-100"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode_Normal() {
        assertEquals(100, Level.parse("100").hashCode());
        assertEquals(-1, Level.parse("-1").hashCode());
        assertEquals(0, Level.parse("0").hashCode());
        assertEquals(Integer.MIN_VALUE, Level.parse("ALL").hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check negative case.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals_Equal() {
        MockLevel l1 = new MockLevel("level1", 1);
        MockLevel l2 = new MockLevel("level2", 1);
        assertEquals(l1, l2);
        assertEquals(l2, l1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks negative case.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals_NotEqual() {
        MockLevel l1 = new MockLevel("level1", 1);
        MockLevel l2 = new MockLevel("level1", 2);
        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks null as a parameter.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals_Null() {
        assertFalse(Level.ALL.equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks negative case.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals_NotLevel() {
        assertFalse(Level.ALL.equals(new Object()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks equals when the other object is itself.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals_Itself() {
        assertTrue(Level.ALL.equals(Level.ALL));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString_Normal() {
        assertEquals("ALL", Level.ALL.toString());
        MockLevel l = new MockLevel("name", 2);
        assertEquals("name", l.toString());
        MockLevel emptyLevel = new MockLevel("", 3);
        assertEquals("", emptyLevel.toString());
    }
    private static final SerializableAssert LEVEL_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            Level init = (Level) initial;
            Level dser = (Level) deserialized;
            assertEquals("Class", init.getClass(), dser.getClass());
            assertEquals("Name", init.getName(), dser.getName());
            assertEquals("Value", init.intValue(), dser.intValue());
            assertEquals("ResourceBundleName", init.getResourceBundleName(),
                    dser.getResourceBundleName());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Serialization of pre-defined const levels. ",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerialization_ConstLevel() throws Exception {
        SerializationTest.verifySelf(Level.ALL,
                SerializationTest.SAME_COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test serialization of normal instance of Level.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerialization_InstanceLevel() throws Exception {
        Level[] objectsToTest = new Level[] { Level.parse("550")};
        SerializationTest.verifySelf(objectsToTest,
                SerializationTest.SAME_COMPARATOR);
        objectsToTest = new Level[] {
                new MockLevel("123", 123, "bundle"),
                new MockLevel("123", 123, null) };
        SerializationTest.verifySelf(objectsToTest, LEVEL_COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Serialization/deserialization compatibility",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new MockLevel("123", 123, "bundle"), LEVEL_COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalizedName",
        args = {}
    )
    public void testGetLocalName() {
        ResourceBundle rb = ResourceBundle.getBundle("bundles/java/util/logging/res");
        Level l = new MockLevel("level1", 120,
                "bundles/java/util/logging/res");
        assertEquals(rb.getString("level1"), l.getLocalizedName());
        rb = ResourceBundle.getBundle(
                "org.apache.harmony.logging.tests.java.util.logging.LevelTestResource");
        l = new MockLevel("Level_error", 120, 
                "org.apache.harmony.logging.tests.java.util.logging.LevelTestResource");
        assertEquals(rb.getString("Level_error"), l.getLocalizedName());
        l = new MockLevel("bad name", 120, "res");
        assertEquals("bad name", l.getLocalizedName());
        l = new MockLevel("level1", 11120, "bad name");
        assertEquals("level1", l.getLocalizedName());
        l = new MockLevel("level1", 1120);
        assertEquals("level1", l.getLocalizedName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getResourceBundleName",
        args = {}
    )
     public void testGetResourceBundleName() {
        String bundleName = "bundles/java/util/logging/res";
        Level l = new MockLevel("level1", 120);
        assertNull("level's localization resource bundle name is not null", l
                .getResourceBundleName());
        l = new MockLevel("level1", 120, bundleName);
        assertEquals("bundleName is non equal to actual value", bundleName, l
                .getResourceBundleName());
        l = new MockLevel("level1", 120, bundleName + "+abcdef");
        assertEquals("bundleName is non equal to actual value", bundleName
                + "+abcdef", l.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "intValue",
        args = {}
    )
    public void testIntValue() {
        int value1 = 120;
        Level l = new MockLevel("level1", value1);
        assertEquals(
                "integer value for this level is non equal to actual value",
                value1, l.intValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test defining new levels in subclasses of Level",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void testSubclassNewLevel() {
        MyLevel.DUPLICATENAME.getName();
        assertEquals("INFO", MyLevel.parse("800").getName());
        assertEquals(800, MyLevel.parse("INFO").intValue());
        assertEquals("FINE", MyLevel.parse("499").getName());
        assertEquals("FINE", MyLevel.parse("500").getName());
        assertEquals(500, MyLevel.parse("FINE").intValue());
        assertEquals("FINEST", MyLevel.parse("300").getName());
        assertEquals(300, MyLevel.parse("FINEST").intValue());
        assertEquals(300, MyLevel.parse("MYLEVEL1").intValue());
        assertEquals("MYLEVEL2", MyLevel.parse("299").getName());
        assertEquals(299, MyLevel.parse("MYLEVEL2").intValue());
    }
    static class MyLevel extends Level implements Serializable {
        private static final long serialVersionUID = 1L;
        public MyLevel(String name, int value) {
            super(name, value);
        }
        public static final Level DUPLICATENAMENUM = new MyLevel("INFO", 800);
        public static final Level DUPLICATENAME = new MyLevel("FINE", 499);
        public static final Level DUPLICATENUM = new MyLevel("MYLEVEL1", 300);
        public static final Level NORMAL = new MyLevel("MYLEVEL2", 299);
    }
    public class MockLevel extends Level implements Serializable {
        private static final long serialVersionUID = 1L;
        public MockLevel(String name, int value) {
            super(name, value);
        }
        public MockLevel(String name, int value, String resourceBundleName) {
            super(name, value, resourceBundleName);
        }
    }
}
