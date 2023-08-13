@TestTargetClass(Serializable.class) 
public class SerializationStressTest2 extends SerializationStressTest {
    private static class ReadWriteObjectAndPrimitiveData implements
            java.io.Serializable {
        transient long milliseconds;
        public boolean calledWriteObject = false;
        public boolean calledReadObject = false;
        public ReadWriteObjectAndPrimitiveData() {
            super();
        }
        private void readObject(java.io.ObjectInputStream in)
                throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            calledReadObject = true; 
            milliseconds = in.readLong();
        }
        private void writeObject(java.io.ObjectOutputStream out)
                throws java.io.IOException {
            calledWriteObject = true;
            out.defaultWriteObject();
            out.writeLong(milliseconds);
        }
    }
    private static class WithUnmatchingSerialPersistentFields implements
            java.io.Serializable {
        private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
                "value", String.class) };
        public int anInstanceVar = 5;
        public WithUnmatchingSerialPersistentFields() {
            super();
        }
    }
    private static class WithMatchingSerialPersistentFields implements
            java.io.Serializable {
        private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
                "anInstanceVar", String.class) };
        public String anInstanceVar = FOO + FOO;
        public WithMatchingSerialPersistentFields() {
            super();
        }
    }
    private static class SerialPersistentFields implements java.io.Serializable {
        private static final String SIMULATED_FIELD_NAME = "text";
        private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
                SIMULATED_FIELD_NAME, String.class) };
        public int anInstanceVar = 5;
        public SerialPersistentFields() {
            super();
        }
        private void readObject(java.io.ObjectInputStream in)
                throws java.io.IOException, ClassNotFoundException {
            ObjectInputStream.GetField fields = in.readFields();
            anInstanceVar = Integer.parseInt((String) fields.get(
                    SIMULATED_FIELD_NAME, "-5"));
        }
        private void writeObject(java.io.ObjectOutputStream out)
                throws java.io.IOException, ClassNotFoundException {
            ObjectOutputStream.PutField fields = out.putFields();
            fields.put(SIMULATED_FIELD_NAME, Integer.toString(anInstanceVar));
            out.writeFields();
        }
    }
    private static class WriteFieldsWithoutFetchingPutFields implements
            java.io.Serializable {
        private static final String SIMULATED_FIELD_NAME = "text";
        private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
                SIMULATED_FIELD_NAME, String.class) };
        public int anInstanceVar = 5;
        public WriteFieldsWithoutFetchingPutFields() {
            super();
        }
        private void readObject(java.io.ObjectInputStream in)
                throws java.io.IOException, ClassNotFoundException {
            in.readFields();
        }
        private void writeObject(java.io.ObjectOutputStream out)
                throws java.io.IOException, ClassNotFoundException {
            out.writeFields();
        }
    }
    private static class SerialPersistentFieldsWithoutField implements
            java.io.Serializable {
        public int anInstanceVar = 5;
        public SerialPersistentFieldsWithoutField() {
            super();
        }
        private void readObject(java.io.ObjectInputStream in)
                throws java.io.IOException, ClassNotFoundException {
            in.readFields();
        }
        private void writeObject(java.io.ObjectOutputStream out)
                throws java.io.IOException, ClassNotFoundException {
            out.putFields();
            out.writeFields();
        }
    }
    private static class OptionalDataNotRead implements java.io.Serializable {
        @SuppressWarnings("unused")
        private int field1, field2;
        public OptionalDataNotRead() {
        }
        private static final ObjectStreamField[] serialPersistentFields = {
                new ObjectStreamField("field1", Integer.TYPE),
                new ObjectStreamField("field2", Integer.TYPE),
                new ObjectStreamField("monthLength", byte[].class), };
        private void writeObject(ObjectOutputStream stream) throws IOException {
            ObjectOutputStream.PutField fields = stream.putFields();
            fields.put("field1", 1);
            fields.put("field2", 2);
            fields.put("monthLength", new byte[] { 7, 8, 9 });
            stream.writeFields();
            stream.writeInt(4);
            byte[] values = new byte[4];
            values[0] = (byte) 16;
            values[1] = (byte) 17;
            values[2] = (byte) 18;
            values[3] = (byte) 19;
            stream.writeObject(values);
        }
        private void readObject(ObjectInputStream stream) throws IOException,
                ClassNotFoundException {
            ObjectInputStream.GetField fields = stream.readFields();
            field1 = fields.get("field1", 0);
            field2 = fields.get("field1", 0);
        }
    }
    private static class NestedPutField implements java.io.Serializable {
        public OptionalDataNotRead field1;
        public NestedPutField() {
        }
        private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
                "field1", OptionalDataNotRead.class), };
        private void writeObject(ObjectOutputStream stream) throws IOException {
            ObjectOutputStream.PutField fields = stream.putFields();
            fields.put("field1", new OptionalDataNotRead());
            stream.writeFields();
        }
        private void readObject(ObjectInputStream stream) throws IOException,
                ClassNotFoundException {
            ObjectInputStream.GetField fields = stream.readFields();
            field1 = (OptionalDataNotRead) fields.get("field1", null);
        }
    }
    private static class StreamBasedReplacementWhenDumping extends
            java.io.ObjectOutputStream {
        public boolean calledArrayReplacement = false;
        public boolean calledStringReplacement = false;
        public boolean calledClassReplacement = false;
        public boolean calledObjectStreamClassReplacement = false;
        public StreamBasedReplacementWhenDumping(java.io.OutputStream output)
                throws java.io.IOException {
            super(output);
            enableReplaceObject(true);
        }
        protected Object replaceObject(Object obj) throws IOException {
            Class<?> objClass = obj.getClass();
            if (objClass == String.class)
                calledStringReplacement = true;
            if (objClass == Class.class)
                calledClassReplacement = true;
            if (objClass == ObjectStreamClass.class)
                calledObjectStreamClassReplacement = true;
            if (objClass.isArray())
                calledArrayReplacement = true;
            return obj;
        }
    }
    private static class ArrayOfSerializable implements Serializable {
        private Serializable[] testField = null;
        public ArrayOfSerializable() {
            testField = new Serializable[2];
            testField[0] = "Hi";
            testField[1] = "there!";
        }
    }
    private static class ClassSubClassTest0 extends java.lang.Object implements
            java.io.Serializable {
        String stringVar;
        public ClassSubClassTest0(String init) {
            stringVar = init;
        }
    }
    private static class ClassSubClassTest1 extends ClassSubClassTest0 {
        String subStringVar;
        public ClassSubClassTest1(String superString, String subString) {
            super(superString);
            subStringVar = subString;
        }
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof ClassSubClassTest1))
                return false;
            ClassSubClassTest1 inst = (ClassSubClassTest1) obj;
            return inst.subStringVar.equals(this.subStringVar)
                    && inst.stringVar.equals(this.stringVar);
        }
    }
    private static class ConstructorTestA {
        public String instVar_classA;
        public final static String ConstrA = "Init in Constructor Class A";
        public final static String ConstrB = "Init in Constructor Class B";
        public final static String ConstrC = "Init in Constructor Class C";
        public final static String ChangedC = "Changed before Serialize - Class C";
        public ConstructorTestA() {
            instVar_classA = ConstrA;
        }
    }
    private static class ConstructorTestB extends ConstructorTestA implements
            java.io.Serializable {
        public String instVar_classB;
        public ConstructorTestB() {
            instVar_classA = ConstrB;
            instVar_classB = ConstrB;
        }
    }
    private static class ConstructorTestC extends ConstructorTestB {
        public String instVar_classC;
        public ConstructorTestC() {
            instVar_classA = ConstrC;
            instVar_classB = ConstrC;
            instVar_classC = ConstrC;
        }
        public boolean verify(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof ConstructorTestC))
                return false;
            ConstructorTestC inst = (ConstructorTestC) obj;
            return inst.instVar_classC.equals(this.instVar_classC)
                    && inst.instVar_classB.equals(this.instVar_classB)
                    && inst.instVar_classA.equals(ConstrA);
        }
    }
    private static class HashCodeTest implements java.io.Serializable {
        private boolean serializationUsesHashCode = false;
        public int hashCode() {
            serializationUsesHashCode = true;
            return super.hashCode();
        }
    }
    private static class InitializerFieldsTest implements java.io.Serializable {
        public java.lang.String toBeSerialized;
        public static java.lang.String toBeNotSerialized;
        public static java.lang.String toBeNotSerialized2;
        {
            toBeSerialized = "NonStaticInitialValue";
        }
        static {
            toBeNotSerialized = "StaticInitialValue";
            toBeNotSerialized2 = new String(toBeNotSerialized);
        }
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof InitializerFieldsTest))
                return false;
            InitializerFieldsTest inst = (InitializerFieldsTest) obj;
            return inst.toBeSerialized.equals(this.toBeSerialized)
                    && InitializerFieldsTest.toBeNotSerialized.equals(InitializerFieldsTest.toBeNotSerialized2);
        }
    }
    private static class InitializerFieldsTest2 implements java.io.Serializable {
        public java.lang.String toBeSerialized;
        public static java.lang.String toBeNotSerialized;
        public static java.lang.String toBeNotSerialized2;
        {
            toBeSerialized = "NonStaticInitialValue";
        }
        public java.lang.String toBeSerialized3;
        public java.lang.String toBeSerialized4;
        static {
            toBeNotSerialized = "StaticInitialValue";
            toBeNotSerialized2 = new String(toBeNotSerialized);
        }
        public java.lang.String toBeSerialized5;
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof InitializerFieldsTest2))
                return false;
            InitializerFieldsTest2 inst = (InitializerFieldsTest2) obj;
            return inst.toBeSerialized.equals(this.toBeSerialized)
                    && inst.toBeSerialized3.equals(this.toBeSerialized3)
                    && inst.toBeSerialized4.equals(this.toBeSerialized4)
                    && inst.toBeSerialized5.equals(this.toBeSerialized5)
                    && InitializerFieldsTest2.toBeNotSerialized.equals(InitializerFieldsTest2.toBeNotSerialized2);
        }
    }
    private static class InitializerFieldsTest3 extends InitializerFieldsTest2
            implements java.io.Serializable {
        public java.lang.String sub_toBeSerialized;
        public static java.lang.String sub_toBeNotSerialized;
        public static java.lang.String sub_toBeNotSerialized2;
        {
            sub_toBeSerialized = "NonStaticInitialValue";
        }
        public java.lang.String sub_toBeSerialized3;
        public java.lang.String sub_toBeSerialized4;
        static {
            sub_toBeNotSerialized = "StaticInitialValue";
            sub_toBeNotSerialized2 = new String(sub_toBeNotSerialized);
        }
        public java.lang.String sub_toBeSerialized5;
        public boolean equals(Object obj) {
            if (!super.equals(obj))
                return false;
            if (!(obj instanceof InitializerFieldsTest3))
                return false;
            InitializerFieldsTest3 inst = (InitializerFieldsTest3) obj;
            return inst.sub_toBeSerialized.equals(this.sub_toBeSerialized)
                    && inst.sub_toBeSerialized3
                            .equals(this.sub_toBeSerialized3)
                    && inst.sub_toBeSerialized4
                            .equals(this.sub_toBeSerialized4)
                    && inst.sub_toBeSerialized5
                            .equals(this.sub_toBeSerialized5)
                    && InitializerFieldsTest3.sub_toBeNotSerialized
                            .equals(InitializerFieldsTest3.sub_toBeNotSerialized2);
        }
    }
    private static class DeepNesting implements java.io.Serializable {
        public float id;
        public DeepNesting next;
        public boolean dump;
        public boolean load;
        public DeepNesting(float id) {
            this.id = id;
            next = null;
            dump = false;
            load = false;
        }
        public DeepNesting(int howMany) {
            DeepNesting prev = new DeepNesting(0.0F);
            next(prev);
            for (int i = 1; i < howMany; i++) {
                prev = prev.next(new DeepNesting(i * 1.0F));
            }
        }
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof DeepNesting))
                return false;
            DeepNesting inst = (DeepNesting) obj;
            if (inst.dump != this.dump || inst.load != this.load)
                return false;
            if (inst.next == null || this.next == null)
                return inst.next == this.next; 
            return this.next.equals(inst.next);
        }
        public DeepNesting next(DeepNesting ivt) {
            next = ivt;
            return ivt;
        }
    }
    private static class DeepNestingWithWriteObject implements
            java.io.Serializable {
        public float id;
        public DeepNestingWithWriteObject next;
        public boolean dump;
        public boolean load;
        public DeepNestingWithWriteObject(float id) {
            this.id = id;
            next = null;
            dump = false;
            load = false;
        }
        public DeepNestingWithWriteObject(int howMany) {
            DeepNestingWithWriteObject prev = new DeepNestingWithWriteObject(
                    0.0F);
            next(prev);
            for (int i = 1; i < howMany; i++) {
                prev = prev.next(new DeepNestingWithWriteObject(i * 1.0F));
            }
        }
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof DeepNestingWithWriteObject))
                return false;
            DeepNestingWithWriteObject inst = (DeepNestingWithWriteObject) obj;
            if (inst.dump != this.dump || inst.load != this.load)
                return false;
            if (inst.next == null || this.next == null)
                return inst.next == this.next; 
            return this.next.equals(inst.next);
        }
        public DeepNestingWithWriteObject next(DeepNestingWithWriteObject ivt) {
            next = ivt;
            return ivt;
        }
        private void writeObject(java.io.ObjectOutputStream s)
                throws IOException {
            s.defaultWriteObject();
        }
        private void readObject(java.io.ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
        }
    }
    static class NonPublicClassTest extends java.lang.Object implements
            java.io.Serializable {
        int field = 1;
        public NonPublicClassTest() {
            field = 10;
        }
        public boolean equals(Object o) {
            if (o instanceof NonPublicClassTest)
                return field == ((NonPublicClassTest) o).field;
            return false;
        }
        public void x10() {
            field *= 10;
        }
    }
    private static class SameInstVarNameSuperClass {
        private int foo;
        public SameInstVarNameSuperClass() {
            super();
        }
        public SameInstVarNameSuperClass(int fooValue) {
            foo = fooValue;
        }
        public String toString() {
            return "foo = " + foo;
        }
    }
    private static class SameInstVarNameSubClass extends
            SameInstVarNameSuperClass implements java.io.Serializable {
        protected int foo;
        public SameInstVarNameSubClass() {
            super();
        }
        public SameInstVarNameSubClass(int fooValue) {
            super(-fooValue);
            foo = fooValue;
        }
    }
    private static class SInterfaceTest implements java.io.Serializable {
        public static int staticVar = 5;
        public transient int[] transVar = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
        public int instanceVar = 7;
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof SInterfaceTest))
                return false;
            SInterfaceTest inst = (SInterfaceTest) obj;
            if (this.instanceVar != inst.instanceVar)
                return false;
            if (inst.transVar == null || this.transVar == null)
                return inst.transVar == this.transVar; 
            for (int i = 0; i < transVar.length; i++)
                if (inst.transVar[i] != this.transVar[i])
                    return false;
            return true;
        }
        private void readObject(java.io.ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            Object arr;
            s.defaultReadObject();
            arr = s.readObject();
            transVar = (int[]) arr;
        }
        private void writeObject(java.io.ObjectOutputStream s)
                throws IOException {
            s.defaultWriteObject();
            s.writeObject(transVar);
        }
        public void x10() {
            for (int i = 0; i < transVar.length; i++)
                transVar[i] = transVar[i] * 10;
            instanceVar = instanceVar * 10;
        }
    }
    private static class SInterfaceTest2 extends SInterfaceTest {
        private void readObject(java.io.ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            Object arr;
            instanceVar = s.readInt();
            arr = s.readObject();
            transVar = (int[]) arr;
        }
        private void writeObject(java.io.ObjectOutputStream s)
                throws IOException {
            s.writeInt(instanceVar);
            s.writeObject(transVar);
        }
    }
    private static class SuperclassTest extends java.lang.Object implements
            java.io.Serializable {
        int superfield = 1;
        public SuperclassTest() {
            superfield = 10;
        }
        public boolean equals(Object o) {
            if (o.getClass() == this.getClass())
                return superfield == ((SuperclassTest) o).superfield;
            return false;
        }
        private void readObject(java.io.ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            superfield = s.readInt();
        }
        private void writeObject(java.io.ObjectOutputStream s)
                throws IOException {
            s.writeInt(superfield);
        }
        public void x10() {
            superfield *= 10;
        }
    }
    private static class SuperclassTest2 extends SuperclassTest {
        int subfield = 5;
        public SuperclassTest2() {
            subfield = 50;
        }
        public boolean equals(Object o) {
            if (o instanceof SuperclassTest2)
                if (subfield == ((SuperclassTest2) o).subfield)
                    return super.equals(o);
            return false;
        }
        private void readObject(java.io.ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            subfield = s.readInt();
        }
        private void writeObject(java.io.ObjectOutputStream s)
                throws IOException {
            s.writeInt(subfield);
        }
        public void x10() {
            subfield *= 10;
            super.x10();
        }
    }
    private static class SyntheticFieldTest implements java.io.Serializable {
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            return obj instanceof SyntheticFieldTest;
        }
        public int hashCode() {
            @SuppressWarnings("unused")
            Class<?>[] c = { String.class }; 
            return super.hashCode();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_41_writeObject() {
        Object objToSave = null;
        try {
            java.io.IOException ex = new java.io.WriteAbortedException(FOO,
                    null);
            objToSave = ex;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_42_writeObject() {
        Object objToSave = null;
        try {
            WithUnmatchingSerialPersistentFields spf = new WithUnmatchingSerialPersistentFields();
            objToSave = spf;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            boolean causedException = false;
            try {
                dumpAndReload(objToSave);
            } catch (InvalidClassException ce) {
                causedException = true;
            }
            assertTrue("serialPersistentFields do not match real fields",
                    causedException);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_43_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            WithMatchingSerialPersistentFields spf = new WithMatchingSerialPersistentFields();
            spf.anInstanceVar = FOO;
            objToSave = spf;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(
                    "serialPersistentFields do not work properly in this implementation",
                    FOO
                            .equals(((WithMatchingSerialPersistentFields) objLoaded).anInstanceVar));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_44_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SerialPersistentFields spf = new SerialPersistentFields();
            final int CONST = -500;
            spf.anInstanceVar = CONST;
            objToSave = spf;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(
                    "serialPersistentFields do not work properly in this implementation",
                    ((SerialPersistentFields) objLoaded).anInstanceVar == CONST);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_45_writeObject() {
        Object objToSave = null;
        try {
            WriteFieldsWithoutFetchingPutFields spf = new WriteFieldsWithoutFetchingPutFields();
            objToSave = spf;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            boolean causedException = false;
            try {
                dumpAndReload(objToSave);
            } catch (NotActiveException ce) {
                causedException = true;
            }
            assertTrue("WriteFieldsWithoutFetchingPutFields", causedException);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_46_writeObject() {
        Object objToSave = null;
        try {
            objToSave = SerialPersistentFields.class; 
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_47_writeObject() {
        Object objToSave = null;
        try {
            objToSave = ObjectStreamClass.lookup(SerialPersistentFields.class); 
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_48_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SerialPersistentFieldsWithoutField spf = new SerialPersistentFieldsWithoutField();
            final int CONST = -500;
            spf.anInstanceVar = CONST;
            objToSave = spf;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(
                    "serialPersistentFields do not work properly in this implementation",
                    ((SerialPersistentFieldsWithoutField) objLoaded).anInstanceVar != CONST);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_49_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            java.net.SocketPermission p = new java.net.SocketPermission(
                    "www.yahoo.com", "connect");
            objToSave = p;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue("SocketPermissions are not the same: " + p + "\t,\t"
                    + objLoaded, p.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_50_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            java.net.SocketPermission p = new java.net.SocketPermission(
                    "www.yahoo.com", "ReSoLVe,          ConNecT");
            objToSave = p;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue("SocketPermissions are not the same: " + p + "\t,\t"
                    + objLoaded, p.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_51_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            ReadWriteObjectAndPrimitiveData readWrite = new ReadWriteObjectAndPrimitiveData();
            objToSave = readWrite;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, readWrite.calledWriteObject);
            assertTrue(
                    MSG_TEST_FAILED + objToSave,
                    ((ReadWriteObjectAndPrimitiveData) objLoaded).calledReadObject);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_52_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(new String[] { "a",
                    "list", "of", "strings" }));
            objToSave = list;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
            assertNotNull(objLoaded);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_53_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            objToSave = Locale.CHINESE;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
            assertNotNull(objLoaded);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_OptionalDataNotRead() {
        Object objToSave = null;
        Object objLoaded;
        try {
            OptionalDataNotRead test = new OptionalDataNotRead();
            Date now = new Date();
            Object[] twoObjects = new Object[2];
            twoObjects[0] = test;
            twoObjects[1] = now;
            objToSave = twoObjects;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            Object[] twoLoadedObjects = (Object[]) objLoaded;
            assertTrue(MSG_TEST_FAILED + objToSave, twoLoadedObjects[0]
                    .getClass() == OptionalDataNotRead.class);
            assertTrue(MSG_TEST_FAILED + objToSave, twoLoadedObjects[1]
                    .getClass() == Date.class);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_55_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            Object[] threeObjects = new Object[3];
            threeObjects[0] = new Integer(2);
            threeObjects[1] = Date.class;
            threeObjects[2] = threeObjects[0]; 
            objToSave = threeObjects;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            Object[] threeLoadedObjects = (Object[]) objLoaded;
            assertTrue(MSG_TEST_FAILED + objToSave, threeLoadedObjects[0]
                    .getClass() == Integer.class);
            assertTrue(MSG_TEST_FAILED + objToSave,
                    threeLoadedObjects[1] == Date.class);
            assertTrue(MSG_TEST_FAILED + objToSave,
                    threeLoadedObjects[0] == threeLoadedObjects[2]);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_56_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            NestedPutField test = new NestedPutField();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertNotNull(MSG_TEST_FAILED + objToSave,
                    ((NestedPutField) objLoaded).field1);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    @KnownFailure("Executed replacement when it should not: class java.lang.String")
    public void test_18_57_writeObject() {
        Object objToSave = null;
        try {
            ByteArrayOutputStream out;
            StreamBasedReplacementWhenDumping streamBasedReplacementWhenDumping;
            out = new ByteArrayOutputStream();
            streamBasedReplacementWhenDumping = new StreamBasedReplacementWhenDumping(
                    out);
            ;
            objToSave = FOO.getClass();
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            streamBasedReplacementWhenDumping.writeObject(objToSave);
            assertTrue("Executed replacement when it should not: " + objToSave,
                    !streamBasedReplacementWhenDumping.calledClassReplacement);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + "\t->"
                    + e.toString());
        } catch (Error err) {
            System.out.println("Error " + err + " when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    @KnownFailure("Executed replacement when it should not: class java.lang.String")
    public void test_18_58_writeObject() {
        Object objToSave = null;
        try {
            ByteArrayOutputStream out;
            StreamBasedReplacementWhenDumping streamBasedReplacementWhenDumping;
            out = new ByteArrayOutputStream();
            streamBasedReplacementWhenDumping = new StreamBasedReplacementWhenDumping(
                    out);
            ;
            objToSave = ObjectStreamClass.lookup(FOO.getClass());
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            streamBasedReplacementWhenDumping.writeObject(objToSave);
            assertTrue(
                    "Executed replacement when it should not: " + objToSave,
                    !streamBasedReplacementWhenDumping.calledObjectStreamClassReplacement);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + "\t->"
                    + e.toString());
        } catch (Error err) {
            System.out.println("Error " + err + " when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_59_writeObject() {
        Object objToSave = null;
        try {
            ByteArrayOutputStream out;
            StreamBasedReplacementWhenDumping streamBasedReplacementWhenDumping;
            out = new ByteArrayOutputStream();
            streamBasedReplacementWhenDumping = new StreamBasedReplacementWhenDumping(
                    out);
            ;
            objToSave = new int[3];
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            streamBasedReplacementWhenDumping.writeObject(objToSave);
            assertTrue("DId not execute replacement when it should: "
                    + objToSave,
                    streamBasedReplacementWhenDumping.calledArrayReplacement);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + "\t->"
                    + e.toString());
        } catch (Error err) {
            System.out.println("Error " + err + " when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_60_writeObject() {
        Object objToSave = null;
        try {
            ByteArrayOutputStream out;
            StreamBasedReplacementWhenDumping streamBasedReplacementWhenDumping;
            out = new ByteArrayOutputStream();
            streamBasedReplacementWhenDumping = new StreamBasedReplacementWhenDumping(
                    out);
            ;
            objToSave = FOO;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            streamBasedReplacementWhenDumping.writeObject(objToSave);
            assertTrue("Did not execute replacement when it should: "
                    + objToSave,
                    streamBasedReplacementWhenDumping.calledStringReplacement);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + "\t->"
                    + e.toString());
        } catch (Error err) {
            System.out.println("Error " + err + " when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_61_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            ArrayOfSerializable test = new ArrayOfSerializable();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, true);
            assertNotNull(objLoaded);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_62_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            ClassSubClassTest1 test = new ClassSubClassTest1(
                    "SuperInitialString", "SubInitialString");
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_63_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            ConstructorTestC test = new ConstructorTestC();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.verify(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_64_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            HashCodeTest test = new HashCodeTest();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave,
                    !((HashCodeTest) objLoaded).serializationUsesHashCode);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_65_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            InitializerFieldsTest test = new InitializerFieldsTest();
            test.toBeSerialized = "serializing";
            InitializerFieldsTest.toBeNotSerialized = "It should not have this value after loaded from a File";
            InitializerFieldsTest.toBeNotSerialized2 = "Good-This is the rigth value.";
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dump(objToSave);
            InitializerFieldsTest.toBeNotSerialized = new String(
                    InitializerFieldsTest.toBeNotSerialized2);
            objLoaded = reload();
            assertTrue(MSG_TEST_FAILED + objToSave, (test.equals(objLoaded)));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_66_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            InitializerFieldsTest2 test = new InitializerFieldsTest2();
            test.toBeSerialized = "serializing";
            test.toBeSerialized3 = "serializing3";
            test.toBeSerialized4 = "serializing4";
            test.toBeSerialized5 = "serializing5";
            InitializerFieldsTest2.toBeNotSerialized = "It should not have this value after loaded from a File";
            InitializerFieldsTest2.toBeNotSerialized2 = "Good-This is the rigth value.";
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dump(objToSave);
            InitializerFieldsTest2.toBeNotSerialized = new String(
                    InitializerFieldsTest2.toBeNotSerialized2);
            objLoaded = reload();
            assertTrue(MSG_TEST_FAILED + objToSave, (test.equals(objLoaded)));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_67_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            InitializerFieldsTest3 test = new InitializerFieldsTest3();
            test.toBeSerialized = "serializing";
            test.toBeSerialized3 = "serializing3";
            test.toBeSerialized4 = "serializing4";
            test.toBeSerialized5 = "serializing5";
            InitializerFieldsTest2.toBeNotSerialized = "It should not have this value after loaded from a File";
            InitializerFieldsTest2.toBeNotSerialized2 = "Good-This is the rigth value.";
            test.sub_toBeSerialized = "serializingSub";
            test.sub_toBeSerialized3 = "serializing3sub";
            test.sub_toBeSerialized4 = "serializing4sub";
            test.sub_toBeSerialized5 = "serializing5sub";
            InitializerFieldsTest3.sub_toBeNotSerialized = "(Subclass) It should not have this value after loaded from a File";
            InitializerFieldsTest3.sub_toBeNotSerialized2 = "(Subclass) Good-This is the rigth value.";
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            dump(objToSave);
            InitializerFieldsTest2.toBeNotSerialized = new String(
                    InitializerFieldsTest2.toBeNotSerialized2);
            InitializerFieldsTest3.sub_toBeNotSerialized = new String(
                    InitializerFieldsTest3.sub_toBeNotSerialized2);
            objLoaded = reload();
            assertTrue(MSG_TEST_FAILED + objToSave, (test.equals(objLoaded)));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_DeepNesting() {
        Object objToSave = null;
        Object objLoaded;
        try {
            DeepNesting test = new DeepNesting(11);
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, (test.equals(objLoaded)));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error " + err + " when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_DeepNestingWithWriteObject() throws Throwable {
        Thread t = new Thread(null, null, "deep nested", 256*1024) {
            @Override
            public void run() {
                try {
                    deepNestingHelper();
                } catch (Throwable e) {
                    error = e;
                }
            }
        };
        t.start();
        t.join();
        if (error != null) {
            throw error;
        }
    }
    static Throwable error;
    public void deepNestingHelper() throws IOException,
            ClassNotFoundException {
        DeepNestingWithWriteObject test = new DeepNestingWithWriteObject(50);
        if (DEBUG) {
            System.out.println("Obj = " + test);
        }
        Object objLoaded = dumpAndReload(test);
        assertTrue(MSG_TEST_FAILED + test, (test.equals(objLoaded)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_69_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            NonPublicClassTest test = new NonPublicClassTest();
            test.x10();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, (test.equals(objLoaded)));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_70_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            int[] test = new int[1];
            int intValue = 0;
            test[0] = intValue;
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, Arrays.equals(test,
                    (int[]) objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_71_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            int i, j, maxJ = 3, maxI = 200;
            byte[][] obj = new byte[maxJ][maxI];
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    obj[j][i] = (byte) (i - 100);
            }
            objToSave = obj;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            byte[][] toCompare = (byte[][]) objLoaded;
            boolean ok = true;
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    if (obj[j][i] != toCompare[j][i]) {
                        ok = false;
                        break;
                    }
            }
            assertTrue(MSG_TEST_FAILED + objToSave, ok);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_72_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            int i, j, maxJ = 3, maxI = 200;
            int[][] obj = new int[maxJ][maxI];
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    obj[j][i] = (i - 100);
            }
            objToSave = obj;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            int[][] toCompare = (int[][]) objLoaded;
            boolean ok = true;
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    if (obj[j][i] != toCompare[j][i]) {
                        ok = false;
                        break;
                    }
            }
            assertTrue(MSG_TEST_FAILED + objToSave, ok);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_73_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            String org = "abcdefghijklmnopqrstuvxyz1234567890abcdefghijklmnopqrstuvxyz1234567890";
            int i, j, maxJ = 3, maxI = 70;
            String[][] obj = new String[maxJ][maxI];
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    obj[j][i] = org.substring(0, i);
            }
            objToSave = obj;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            String[][] toCompare = (String[][]) objLoaded;
            boolean ok = true;
            for (j = 0; j < maxJ; j++) {
                for (i = 0; i < maxI; i++)
                    if (!obj[j][i].equals(toCompare[j][i])) {
                        ok = false;
                        break;
                    }
            }
            assertTrue(MSG_TEST_FAILED + objToSave, ok);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_74_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SameInstVarNameSubClass test = new SameInstVarNameSubClass(100);
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave,
                    ((SameInstVarNameSubClass) objLoaded).foo == 100);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_75_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SInterfaceTest test = new SInterfaceTest();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_76_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SInterfaceTest2 test = new SInterfaceTest2();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_77_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SuperclassTest test = new SuperclassTest();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_78_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SuperclassTest2 test = new SuperclassTest2();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_79_writeObject() {
        Object objToSave = null;
        Object objLoaded;
        try {
            SyntheticFieldTest test = new SyntheticFieldTest();
            objToSave = test;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, test.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_18_80_writeObject() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            new ObjectOutputStream(dos); 
            dos.writeByte(ObjectStreamConstants.TC_BLOCKDATA);
            int length = 99;
            dos.writeByte(length);
            for (int i = 0; i < length; i++) {
                dos.writeByte(0); 
            }
            dos.flush();
            int lengthRead = 0;
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(out.toByteArray()));
                ois.readObject();
            } catch (OptionalDataException e) {
                lengthRead = e.length;
            }
            assertTrue("Did not throw exception with optional data size ",
                    length == lengthRead);
        } catch (ClassNotFoundException e) {
            fail("Unable to read BLOCKDATA: " + e.getMessage());
        } catch (IOException e) {
            fail("IOException testing BLOCKDATA : " + e.getMessage());
        } catch (Error err) {
            System.out.println("Error " + err + " when testing BLOCKDATA");
            throw err;
        }
    }
}
