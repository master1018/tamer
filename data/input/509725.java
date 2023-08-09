@TestTargetClass(ObjectStreamField.class) 
public class ObjectStreamFieldTest extends junit.framework.TestCase {
    static class DummyClass implements Serializable {
        private static final long serialVersionUID = 999999999999998L;
        boolean bField = true;
        char cField = 'c';
        double dField = 424242.4242;
        float fField = 24.12F;
        int iField = 1965;
        long lField = 9999999L;
        short sField = 42;
        long bam = 999L;
        int ham = 9999;
        int sam = 8888;
        Object hola = new Object();
        public static long getUID() {
            return serialVersionUID;
        }
    }
    class MyObjectStreamField extends ObjectStreamField {
        public MyObjectStreamField(String name, Class<?> cl) {
            super(name, cl);
        }
        public void setOffset(int newValue) {
            super.setOffset(newValue);
        }
    }
    ObjectStreamClass osc;
    ObjectStreamField hamField;
    ObjectStreamField samField;
    ObjectStreamField bamField;
    ObjectStreamField holaField;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies ObjectStreamField(String, Class<T>)",
        method = "ObjectStreamField",
        args = {java.lang.String.class, java.lang.Class.class}
    )      
    public void test_ConstructorLjava_lang_StringLjava_lang_Class() {
        ObjectStreamField osf = new ObjectStreamField("aField", int.class);
        assertTrue("Test 1: Name member not set correctly.", 
                   osf.getName().equals("aField"));
        assertTrue("Test 2: Type member not set correctly.",
                   osf.getType().equals(int.class));
        osf = new ObjectStreamField("anotherField", String.class);
        assertTrue("Test 3: Name member not set correctly.", 
                   osf.getName().equals("anotherField"));
        assertTrue("Test 4: Type member not set correctly.",
                   osf.getType().equals(String.class));
        try {
            osf = new ObjectStreamField(null, boolean.class);
            fail("Test 5: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        try {
            osf = new ObjectStreamField("thisField", null);
            fail("Test 6: NullPointerException expected.");
        } catch (NullPointerException e) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies ObjectStreamField(String, Class<T>, boolean)",
        method = "ObjectStreamField",
        args = {java.lang.String.class, java.lang.Class.class, boolean.class}
    )      
    public void test_ConstructorLjava_lang_StringLjava_lang_ClassB() {
        ObjectStreamField osf = new ObjectStreamField("aField", int.class, false);
        assertTrue("Test 1: Name member not set correctly.", 
                   osf.getName().equals("aField"));
        assertTrue("Test 2: Type member not set correctly.",
                   osf.getType().equals(int.class));
        assertFalse("Test 3: Unshared member not set correctly.", 
                    osf.isUnshared());
        osf = new ObjectStreamField("anotherField", String.class, true);
        assertTrue("Test 4: Name member not set correctly.", 
                   osf.getName().equals("anotherField"));
        assertTrue("Test 5: Type member not set correctly.",
                   osf.getType().equals(String.class));
        assertTrue("Test 6: Unshared member not set correctly.", 
                   osf.isUnshared());
        try {
            osf = new ObjectStreamField(null, boolean.class);
            fail("Test 7: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        try {
            osf = new ObjectStreamField("thisField", null);
            fail("Test 8: NullPointerException expected.");
        } catch (NullPointerException e) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.Object.class}
    )      
    public void test_compareToLjava_lang_Object() {
        assertTrue("Object compared to int did not return > 0", holaField
                .compareTo(hamField) > 0);
        assertEquals("Int compared to itself did not return 0", 0, hamField
                .compareTo(hamField));
        assertTrue("(Int)ham compared to (Int)sam did not return < 0", hamField
                .compareTo(samField) < 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )    
    public void test_getName() {
        assertEquals("Field did not return correct name", "hola", holaField.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOffset",
        args = {}
    )    
    public void test_getOffset() {
        ObjectStreamField[] osfArray;
        osfArray = osc.getFields();
        int[] expectedOffsets = {0, 1, 9, 11, 19, 23, 27, 31, 39, 41, 0};
        assertTrue("getOffset() did not return reasonable values.", osfArray[0]
                .getOffset() != osfArray[1].getOffset());
        for (int i = 0; i < expectedOffsets.length; i++) {
            assertEquals(String.format("Unexpected value for osfArray[%d].getOffset(): ", i),
                    expectedOffsets[i], osfArray[i].getOffset());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )     
    public void test_getType() {
        assertTrue("getType on an Object field did not answer Object",
                holaField.getType().equals(Object.class));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTypeCode",
        args = {}
    )
    public void test_getTypeCode() {
        assertEquals("getTypeCode on an Object field did not answer 'L'",
                'L', holaField.getTypeCode());
        assertEquals("getTypeCode on a long field did not answer 'J'", 'J', bamField
                .getTypeCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTypeString",
        args = {}
    )
    public void test_getTypeString() {
        assertTrue("getTypeString returned: " + holaField.getTypeString(),
                holaField.getTypeString().indexOf("Object") >= 0);
        assertNull("Primitive types' strings should be null", hamField.getTypeString());
        ObjectStreamField osf = new ObjectStreamField("s", String.class, true);
        assertTrue(osf.getTypeString() == "Ljava/lang/String;");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertTrue("toString on a long returned: " + bamField.toString(),
                bamField.toString().indexOf("bam") >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )      
    public void test_getType_Deserialized() throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new SerializableObject());
        oos.close();
        baos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableObject obj = (SerializableObject) ois.readObject();
        ObjectStreamClass oc = obj.getObjectStreamClass();
        ObjectStreamField field = oc.getField("i");
        assertEquals(Object.class, field.getType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setOffset",
        args = {int.class}
    )    
    public void test_setOffsetI() {
        MyObjectStreamField f = new MyObjectStreamField("aField", int.class);
        f.setOffset(42);
        assertEquals("Test 1: Unexpected offset value.", 42, f.getOffset());
        f.setOffset(2008);
        assertEquals("Test 2: Unexpected offset value.", 2008, f.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )    
    public void test_getType_MockObjectInputStream() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new SerializableObject());
        oos.close();
        baos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        MockObjectInputStream ois = new MockObjectInputStream(bais);
        ois.readObject();
        ObjectStreamClass oc = ois.getObjectStreamClass();
        ObjectStreamField field = oc.getField("i");
        assertEquals(Object.class, field.getType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isPrimitive",
        args = {}
    )    
    public void test_isPrimitive() {
        ObjectStreamField[] osfArray;
        osfArray = osc.getFields();
        for (int i = 0; i < (osfArray.length - 1); i++) {
            assertTrue(String.format("osfArray[%d].isPrimitive() should return true.", i),
                       osfArray[i].isPrimitive());
        }
        assertFalse(String.format("osfArray[%d].isPrimitive() should return false.", 
                                  osfArray.length - 1),
                    osfArray[(osfArray.length - 1)].isPrimitive());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isUnshared",
        args = {}
    )
    public void test_isUnshared() throws Exception {
        SerializableObject2 obj = new SerializableObject2();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        baos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableObject2 newObj = (SerializableObject2) ois.readObject();
        ObjectInputStream.GetField getField = newObj.getGetField();
        ObjectStreamClass objectStreamClass = getField.getObjectStreamClass();
        assertTrue(objectStreamClass.getField("i").isUnshared());
        assertFalse(objectStreamClass.getField("d").isUnshared());
        assertTrue(objectStreamClass.getField("s").isUnshared());
        assertEquals(1000, getField.get("i", null));
        assertEquals(SerializableObject2.today, getField.get("d", null));
        assertEquals("Richard", getField.get("s", null));
        assertTrue(objectStreamClass.getField("s").getTypeString() == "Ljava/lang/String;");
        assertEquals(0, objectStreamClass.getField("d").getOffset());
        assertEquals(1, objectStreamClass.getField("i").getOffset());
        assertEquals(2, objectStreamClass.getField("s").getOffset());
    }
    protected void setUp() {
        osc = ObjectStreamClass.lookup(DummyClass.class);
        bamField = osc.getField("bam");
        samField = osc.getField("sam");
        hamField = osc.getField("ham");
        holaField = osc.getField("hola");
    }
    protected void tearDown() {
    }
}
class SerializableObject implements Serializable {
    public ObjectInputStream.GetField getField = null;
    private static final long serialVersionUID = -2953957835918368056L;
    public Date d;
    public Integer i;
    public Exception e;
    public SerializableObject() {
        d = new Date();
        i = new Integer(1);
        e = new Exception("e");
    }
    private void writeObject(ObjectOutputStream o) throws IOException {
        o.putFields().put("d", new Date());
        o.putFields().put("i", new Integer(11));
        o.writeFields();
    }
    private void readObject(ObjectInputStream in) throws NotActiveException,
            IOException, ClassNotFoundException {
        getField = in.readFields();
        d = (Date) getField.get("d", null);
        i = (Integer) getField.get("i", null);
    }
    public ObjectStreamClass getObjectStreamClass() {
        return getField.getObjectStreamClass();
    }
}
class MockObjectInputStream extends ObjectInputStream {
    private ObjectStreamClass temp = null;
    public MockObjectInputStream() throws SecurityException, IOException {
        super();
    }
    public MockObjectInputStream(InputStream in)
            throws StreamCorruptedException, IOException {
        super(in);
    }
    public ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {
        ObjectStreamClass osc = super.readClassDescriptor();
        if (osc.getSerialVersionUID() == -2953957835918368056L) {
            temp = osc;
        }
        return osc;
    }
    public ObjectStreamClass getObjectStreamClass() {
        return temp;
    }
}
class SerializableObject2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("i", Integer.class, true),
        new ObjectStreamField("d", Date.class, false),
        new ObjectStreamField("s", String.class, true),
    };
    private ObjectInputStream.GetField getField;
    public static Date today = new Date(1172632429156l);
    public ObjectInputStream.GetField getGetField() {
        return getField;
    }
    private void writeObject(ObjectOutputStream o) throws IOException {
        ObjectOutputStream.PutField putField = o.putFields();
        putField.put("i", new Integer(1000));
        putField.put("d", today);
        putField.put("s", "Richard");
        o.writeFields();
    }
    private void readObject(ObjectInputStream in) throws NotActiveException,
            IOException, ClassNotFoundException {
        getField = in.readFields();
    }
}
