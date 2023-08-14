@TestTargetClass(ObjectInputStream.GetField.class) 
public class ObjectInputStreamGetFieldTest extends junit.framework.TestCase {
    private ObjectInputStream ois = null;
    private final String FILENAME = 
            "/tests/api/java/io/testFields.ser";
    private final String DEFAULTED_FILENAME = 
            "/tests/api/java/io/testFieldsDefaulted.ser";
    public boolean booleanValue;
    public byte byteValue;
    public char charValue;
    public int intValue;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, short.class}
        )
    })
    public void test_get() throws Exception {
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        Support_GetPutFields newObject = new Support_GetPutFields();
        newObject.initTestValues();
        assertTrue("Test 1: The object read from the reference file does " + 
                   "not match a locally created instance of the same class.", 
                   object.equals(newObject));
        initOis(DEFAULTED_FILENAME);
        Support_GetPutFieldsDefaulted defaulted = 
                (Support_GetPutFieldsDefaulted) ois.readObject();
        Support_GetPutFieldsDefaulted newDefaulted = 
                new Support_GetPutFieldsDefaulted();
        newDefaulted.initTestValues();
        assertTrue("Test 2: The object read from the reference file does " + 
                   "not match a locally created instance of the same class.", 
                   defaulted.equals(newDefaulted));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies defaulted(String).",
        method = "defaulted",
        args = {java.lang.String.class}
    )
    public void test_defaultedLjava_lang_String() throws Exception {
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        ObjectInputStream.GetField fields = object.getField;
        try {
            fields.defaulted("noField");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {}
        assertFalse("The field longValue should not be defaulted.", 
                   fields.defaulted("longValue"));
        initOis(DEFAULTED_FILENAME);
        Support_GetPutFieldsDefaulted defaultedObject = 
            (Support_GetPutFieldsDefaulted) ois.readObject();
        fields = defaultedObject.getField;
        assertTrue("The field longValue should be defaulted.", 
                   fields.defaulted("longValue"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, short.class}
        )
    })
    public void test_getException() throws Exception {
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        ObjectInputStream.GetField fields = object.getField;
        try {
            fields.get("noValue", false);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, boolean).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", (byte) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, byte).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", (char) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, char).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", 0.0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, double).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", 0.0f);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, float).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", (long) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, long).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, int).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", new Object());
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, Object).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("noValue", (short) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, short).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("byteValue", false);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, boolean).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("booleanValue", (byte) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, byte).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("intValue", (char) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, char).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("floatValue", 0.0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, double).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("doubleValue", 0.0f);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, float).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("intValue", (long) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, long).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("shortValue", 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, int).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("booleanValue", new Object());
            fail("IllegalArgumentException expected for non-matching name " + 
                 "and type arguments in get(String, Object).");
        } catch (IllegalArgumentException e) {}
        try {
            fields.get("longValue", (short) 0);
            fail("IllegalArgumentException expected for non-matching name " + 
                 "and type arguments in get(String, short).");
        } catch (IllegalArgumentException e) {}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that getObjectStreamClass() does not return null.",
        method = "getObjectStreamClass",
        args = {}
    )
    public void test_getObjectStreamClass() throws Exception {
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        assertNotNull("Return value of getObjectStreamClass() should not be null.",
                      object.getField.getObjectStreamClass());
    }
    private void initOis(String fileName) throws Exception {
        if (ois != null) {
            ois.close();
        }
        ois = new ObjectInputStream(
                    getClass().getResourceAsStream(fileName));
    }
    protected void tearDown() throws Exception {
        if (ois != null) {
            ois.close();
        }
        super.tearDown();
    }
}
