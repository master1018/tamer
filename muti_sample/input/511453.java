@TestTargetClass(Serializable.class) 
public class SerializationStressTest0 extends SerializationStressTest {
    private static class ObjectInputStreamSubclass extends ObjectInputStream {
        private Vector resolvedClasses = new Vector();
        public ObjectInputStreamSubclass(InputStream in) throws IOException,
                StreamCorruptedException {
            super(in);
        }
        public Class resolveClass(ObjectStreamClass osClass)
                throws IOException, ClassNotFoundException {
            Class result = super.resolveClass(osClass);
            resolvedClasses.addElement(result);
            return result;
        }
        public Class[] resolvedClasses() {
            return (Class[]) resolvedClasses.toArray(new Class[resolvedClasses
                    .size()]);
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_1_Constructor() {
            try {
                oos.close();
                oos = new ObjectOutputStream(new ByteArrayOutputStream());
                oos.close();
            } catch (Exception e) {
                fail("Failed to create ObjectOutputStream : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_2_close() {
            try {
                oos.close();
                oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
                oos.close();
                oos.writeChar('T');
                oos.writeObject(FOO);
            } catch (IOException e) {
                fail("Operation on closed stream threw IOException : "
                        + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_3_defaultWriteObject() {
            try {
                oos.defaultWriteObject();
            } catch (NotActiveException e) {
                return;
            } catch (IOException e) {
            }
            fail(
                    "Failed to throw NotActiveException when invoked outside readObject");
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_4_flush() {
            try {
                oos.close();
                oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
                int size = bao.size();
                oos.writeByte(127);
                assertTrue("Data flushed already", bao.size() == size);
                oos.flush();
                assertTrue("Failed to flush data", bao.size() > size);
                oos.close();
                oos = null;
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_5_reset() {
            try {
                String o = "HelloWorld";
                oos.writeObject(o);
                oos.writeObject(o);
                oos.reset();
                oos.writeObject(o);
                ois = new ObjectInputStream(loadStream());
                ois.close();
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_6_write() {
            try {
                byte[] buf = new byte[255];
                byte[] output = new byte[255];
                for (int i = 0; i < output.length; i++)
                    output[i] = (byte) i;
                oos.write(output, 0, output.length);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                ois.readFully(buf);
                ois.close();
                for (int i = 0; i < output.length; i++)
                    if (buf[i] != output[i])
                        fail("Read incorrect byte: " + i);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_6a_write() {
            try {
                byte[] buf = new byte[256];
                byte[] output = new byte[256];
                for (int i = 0; i < output.length; i++)
                    output[i] = (byte) (i & 0xff);
                oos.write(output, 0, output.length);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                ois.readFully(buf);
                ois.close();
                for (int i = 0; i < output.length; i++)
                    if (buf[i] != output[i])
                        fail("Read incorrect byte: " + i);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_7_write() {
            try {
                byte[] buf = new byte[10];
                oos.write('T');
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertEquals("Read incorrect byte", 'T', ois.read());
                ois.close();
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_8_write() {
            try {
                byte[] buf = new byte[10];
                oos.write("HelloWorld".getBytes());
                oos.close();
                ois = new ObjectInputStream(loadStream());
                ois.read(buf, 0, 10);
                ois.close();
                assertEquals("Read incorrect bytes", "HelloWorld", new String(buf, 0, 10)
                        );
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_9_writeBoolean() {
            try {
                oos.writeBoolean(true);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertTrue("Wrote incorrect byte value", ois.readBoolean());
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_10_writeByte() {
            try {
                oos.writeByte(127);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertEquals("Wrote incorrect byte value", 127, ois.readByte());
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_11_writeBytes() {
            try {
                byte[] buf = new byte[10];
                oos.writeBytes("HelloWorld");
                oos.close();
                ois = new ObjectInputStream(loadStream());
                ois.readFully(buf);
                ois.close();
                assertEquals("Wrote incorrect bytes value", "HelloWorld", new String(buf, 0, 10)
                        );
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_12_writeChar() {
            try {
                oos.writeChar('T');
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertEquals("Wrote incorrect char value", 'T', ois.readChar());
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_13_writeChars() {
            try {
                int avail = 0;
                char[] buf = new char[10];
                oos.writeChars("HelloWorld");
                oos.close();
                ois = new ObjectInputStream(loadStream());
                avail = ois.available() / 2;
                for (int i = 0; i < avail; ++i)
                    buf[i] = ois.readChar();
                ois.close();
                assertEquals("Wrote incorrect chars", "HelloWorld", new String(buf, 0, 10)
                        );
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_14_writeDouble() {
            try {
                oos.writeDouble(Double.MAX_VALUE);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertTrue("Wrote incorrect double value",
                        ois.readDouble() == Double.MAX_VALUE);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_15_writeFloat() {
            try {
                oos.writeFloat(Float.MAX_VALUE);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertTrue("Wrote incorrect double value",
                        ois.readFloat() == Float.MAX_VALUE);
                ois.close();
                ois = null;
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_16_writeInt() {
            try {
                oos.writeInt(Integer.MAX_VALUE);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertTrue("Wrote incorrect double value",
                        ois.readInt() == Integer.MAX_VALUE);
                ois.close();
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_17_writeLong() {
            try {
                oos.writeLong(Long.MAX_VALUE);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertTrue("Wrote incorrect double value",
                        ois.readLong() == Long.MAX_VALUE);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_19_writeShort() {
            try {
                oos.writeShort(127);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertEquals("Wrote incorrect short value", 127, ois.readShort());
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_20_writeUTF() {
            try {
                oos.writeUTF("HelloWorld");
                oos.close();
                ois = new ObjectInputStream(loadStream());
                assertEquals("Wrote incorrect UTF value", 
                        "HelloWorld", ois.readUTF());
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_25_available() {
            try {
                oos.writeObject(FOO);
                oos.writeObject(FOO);
                oos.flush();
                int available1 = 0;
                int available2 = 0;
                Object obj1 = null;
                Object obj2 = null;
                ObjectInputStream ois = new ObjectInputStream(loadStream());
                available1 = ois.available();
                obj1 = ois.readObject();
                available2 = ois.available();
                obj2 = ois.readObject();
                assertEquals("available returned incorrect value", 0, available1);
                assertEquals("available returned incorrect value", 0, available2);
                assertTrue("available caused incorrect reading", FOO.equals(obj1));
                assertTrue("available returned incorrect value", FOO.equals(obj2));
            } catch (IOException e) {
                fail("IOException serializing object : " + e.getMessage());
            } catch (ClassNotFoundException e) {
                fail("Unable to read Object type : " + e.toString());
            } catch (Error err) {
                System.out.println("Error " + err);
                throw err;
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_resolveClass() {
            try {
                oos.writeObject(new Object[] { Integer.class, new Integer(1) });
                oos.close();
                ois = new ObjectInputStreamSubclass(loadStream());
                ois.readObject();
                ois.close();
            } catch (IOException e1) {
                fail("IOException : " + e1.getMessage());
            } catch (ClassNotFoundException e2) {
                fail("ClassNotFoundException : " + e2.getMessage());
            }
            Class[] resolvedClasses = ((ObjectInputStreamSubclass) ois)
                    .resolvedClasses();
            assertEquals("missing resolved", 3, resolvedClasses.length);
            assertTrue("resolved class 1", resolvedClasses[0] == Object[].class);
            assertTrue("resolved class 2", resolvedClasses[1] == Integer.class);
            assertTrue("resolved class 3", resolvedClasses[2] == Number.class);
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_reset() {
            try {
                oos.reset();
                oos.writeObject("R");
                oos.reset();
                oos.writeByte(24);
                oos.close();
                DataInputStream dis = new DataInputStream(loadStream());
                byte[] input = new byte[dis.available()];
                dis.readFully(input);
                byte[] result = new byte[] { (byte) 0xac, (byte) 0xed, (byte) 0,
                        (byte) 5, (byte) 0x79, (byte) 0x74, (byte) 0, (byte) 1,
                        (byte) 'R', (byte) 0x79, (byte) 0x77, (byte) 1, (byte) 24 };
                assertTrue("incorrect output", Arrays.equals(input, result));
                ois = new ObjectInputStreamSubclass(loadStream());
                assertEquals("Wrong result from readObject()", "R", ois.readObject()
                        );
                assertEquals("Wrong result from readByte()", 24, ois.readByte());
                ois.close();
            } catch (IOException e1) {
                fail("IOException : " + e1.getMessage());
            } catch (ClassNotFoundException e2) {
                fail("ClassNotFoundException : " + e2.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_serialVersionUID(Class clazz, long svUID) {
            final String idWrong = "serialVersionUID is wrong for: ";
            long reflectedSvUID = 0L;
            try {
                reflectedSvUID = clazz.getField("serialVersionUID").getLong(null);
            } catch (Exception e) {
                fail("Unable to determine serialVersionUID of " + clazz);
            }
            assertTrue(idWrong + clazz + ": " + reflectedSvUID + " does not equal "
                    + svUID, reflectedSvUID == svUID);
        }
        private static class ResolveObjectTest implements Serializable {
            Object field1, field2;
        }
        private static class ResolveObjectInputStream extends ObjectInputStream {
            ResolveObjectInputStream(InputStream in)
                    throws StreamCorruptedException, IOException {
                super(in);
            }
            public void enableResolve() {
                enableResolveObject(true);
            }
            public Object resolveObject(Object obj) {
                if (obj instanceof Vector) 
                    return new Hashtable();
                else if ("abc".equals(obj)) 
                    return "ABC";
                else if (obj instanceof String) 
                    return String.valueOf(((String) obj).length());
                else if (obj instanceof int[]) 
                    return new Object[1];
                else if (obj instanceof Object[] && ((Object[]) obj).length == 2) 
                    return new char[1];
                return obj;
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_1_resolveObject() {
            try {
                ResolveObjectTest obj = new ResolveObjectTest();
                obj.field1 = new Vector();
                obj.field2 = obj.field1;
                oos.writeObject(obj);
                oos.close();
                ois = new ResolveObjectInputStream(loadStream());
                ((ResolveObjectInputStream) ois).enableResolve();
                ResolveObjectTest result = null;
                try {
                    result = (ResolveObjectTest) ois.readObject();
                } catch (ClassNotFoundException e) {
                    fail(e.toString());
                }
                assertTrue("Object not resolved",
                        result.field1 instanceof Hashtable);
                assertTrue("Second reference not resolved",
                        result.field1 == result.field2);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_2_resolveObject() {
            try {
                ResolveObjectTest obj = new ResolveObjectTest();
                obj.field1 = "abc";
                obj.field2 = obj.field1;
                oos.writeObject(obj);
                oos.close();
                ois = new ResolveObjectInputStream(loadStream());
                ((ResolveObjectInputStream) ois).enableResolve();
                ResolveObjectTest result = null;
                try {
                    result = (ResolveObjectTest) ois.readObject();
                } catch (ClassNotFoundException e) {
                    fail(e.toString());
                }
                assertEquals("String not resolved", "ABC", result.field1);
                assertTrue("Second reference not resolved",
                        result.field1 == result.field2);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_3_resolveObject() {
            try {
                ResolveObjectTest obj = new ResolveObjectTest();
                char[] lchars = new char[70000];
                obj.field1 = new String(lchars);
                obj.field2 = obj.field1;
                oos.writeObject(obj);
                oos.close();
                ois = new ResolveObjectInputStream(loadStream());
                ((ResolveObjectInputStream) ois).enableResolve();
                ResolveObjectTest result = null;
                try {
                    result = (ResolveObjectTest) ois.readObject();
                } catch (ClassNotFoundException e) {
                    fail(e.toString());
                }
                assertTrue("Long String not resolved", "70000"
                        .equals(result.field1));
                assertTrue("Second reference not resolved",
                        result.field1 == result.field2);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_4_resolveObject() {
            try {
                ResolveObjectTest obj = new ResolveObjectTest();
                obj.field1 = new int[5];
                obj.field2 = obj.field1;
                oos.writeObject(obj);
                oos.close();
                ois = new ResolveObjectInputStream(loadStream());
                ((ResolveObjectInputStream) ois).enableResolve();
                ResolveObjectTest result = null;
                try {
                    result = (ResolveObjectTest) ois.readObject();
                } catch (ClassNotFoundException e) {
                    fail(e.toString());
                }
                Class cl = new Object[0].getClass();
                assertTrue("int[] not resolved", result.field1.getClass() == cl);
                assertTrue("Second reference not resolved",
                        result.field1 == result.field2);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_5_resolveObject() {
            try {
                ResolveObjectTest obj = new ResolveObjectTest();
                obj.field1 = new Object[2];
                obj.field2 = obj.field1;
                oos.writeObject(obj);
                oos.close();
                ois = new ResolveObjectInputStream(loadStream());
                ((ResolveObjectInputStream) ois).enableResolve();
                ResolveObjectTest result = null;
                try {
                    result = (ResolveObjectTest) ois.readObject();
                } catch (ClassNotFoundException e) {
                    fail(e.toString());
                }
                Class cl = new char[0].getClass();
                assertTrue("int[] not resolved", result.field1.getClass() == cl);
                assertTrue("Second reference not resolved",
                        result.field1 == result.field2);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            }
        }
        static class WriteReplaceTestA implements Serializable {
            public Object writeReplace() throws ObjectStreamException {
                return new ReadResolveTestB();
            }
        }
        static class WriteReplaceTestB extends WriteReplaceTestA {
        }
        static class WriteReplaceTestC extends WriteReplaceTestA {
            public Object writeReplace() throws ObjectStreamException {
                return new ReadResolveTestC();
            }
        }
        static class WriteReplaceTestD implements Serializable {
            private Object writeReplace() throws ObjectStreamException {
                return new ReadResolveTestD();
            }
        }
        static class WriteReplaceTestE extends WriteReplaceTestD {
        }
        static class WriteReplaceTestF implements Serializable {
            int type, readType;
            public WriteReplaceTestF(int type, int readType) {
                this.type = type;
                this.readType = readType;
            }
            public Object writeReplace() throws ObjectStreamException {
                switch (type) {
                case 0:
                    throw new InvalidObjectException("invalid");
                case 1:
                    throw new RuntimeException("runtime");
                case 2:
                    throw new Error("error");
                default:
                    return new ReadResolveTestE(readType);
                }
            }
        }
        static class ReadResolveTestA implements Serializable {
            public Object readResolve() throws ObjectStreamException {
                return new ReadResolveTestA();
            }
        }
        static class ReadResolveTestB extends ReadResolveTestA {
        }
        static class ReadResolveTestC implements Serializable {
            private Object readResolve() throws ObjectStreamException {
                return new ReadResolveTestB();
            }
        }
        static class ReadResolveTestD extends ReadResolveTestC {
        }
        static class ReadResolveTestE implements Serializable {
            int type;
            public ReadResolveTestE(int type) {
                this.type = type;
            }
            public Object readResolve() throws ObjectStreamException {
                switch (type) {
                case 0:
                    throw new InvalidObjectException("invalid");
                case 1:
                    throw new RuntimeException("runtime");
                case 2:
                    throw new Error("error");
                case 3:
                    return this;
                default:
                    return new ReadResolveTestF();
                }
            }
        }
        static class ReadResolveTestF implements Serializable {
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_1_writeReplace() {
            try {
                Vector v = new Vector();
                v.addElement(new WriteReplaceTestA());
                v.addElement(new WriteReplaceTestB());
                v.addElement(new WriteReplaceTestB());
                v.addElement(new WriteReplaceTestC());
                v.addElement(new WriteReplaceTestD());
                v.addElement(new WriteReplaceTestE());
                oos.writeObject(v);
                oos.close();
                ois = new ObjectInputStream(loadStream());
                Vector result = (Vector) ois.readObject();
                assertTrue("invalid 0 : " + result.elementAt(0), result
                        .elementAt(0).getClass() == ReadResolveTestA.class);
                assertTrue("invalid 1 : " + result.elementAt(1), result
                        .elementAt(1).getClass() == ReadResolveTestA.class);
                assertTrue("invalid 2 : " + result.elementAt(2), result
                        .elementAt(2).getClass() == ReadResolveTestA.class);
                assertTrue("invalid 3 : " + result.elementAt(3), result
                        .elementAt(3).getClass() == ReadResolveTestB.class);
                assertTrue("invalid 4 : " + result.elementAt(4), result
                        .elementAt(4).getClass() == ReadResolveTestD.class);
                assertTrue("invalid 5 : " + result.elementAt(5), result
                        .elementAt(5).getClass() == WriteReplaceTestE.class);
            } catch (IOException e) {
                fail("IOException serializing data : " + e.getMessage());
            } catch (ClassNotFoundException e) {
                fail("ClassNotFoundException serializing data : " + e.getMessage());
            }
        }
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization.",
            method = "!Serialization",
            args = {}
        )
        public void test_2_writeReplace_01() throws IOException {
            try {
                oos.writeObject(new WriteReplaceTestF(0, -1));
                fail("Should throw ObjectStreamException");
            } catch (ObjectStreamException e) {
            }
        }
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "Verifies serialization.",
                method = "!Serialization",
                args = {}
            )
        public void test_2_writeReplace_02() throws IOException {
            try {
                oos.writeObject(new WriteReplaceTestF(1, -1));
                fail("Should throw RuntimeException");
            } catch (RuntimeException e) {
            }
        }
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "Verifies serialization.",
                method = "!Serialization",
                args = {}
            )
        public void test_2_writeReplace_03() throws IOException {
            try {
                oos.writeObject(new WriteReplaceTestF(2, -1));
                fail("Should throw Error");
            } catch (Error e) {
            }
        }
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "Verifies serialization.",
                method = "!Serialization",
                args = {}
            )
        public void test_2_writeReplace_04() throws IOException,
                ClassNotFoundException {
            oos.writeObject(new WriteReplaceTestF(3, 0));
            oos.writeObject(new WriteReplaceTestF(3, 1));
            oos.writeObject(new WriteReplaceTestF(3, 2));
            WriteReplaceTestF test = new WriteReplaceTestF(3, 3);
            oos.writeObject(test);
            oos.writeObject(test);
            WriteReplaceTestF test2 = new WriteReplaceTestF(3, 4);
            oos.writeObject(test2);
            oos.writeObject(test2);
            oos.close();
            ois = new ObjectInputStream(loadStream());
            try {
                ois.readObject();
                fail("Expected InvalidObjectException");
            } catch (InvalidObjectException e) {
            }
            try {
                ois.readObject();
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
            }
            try {
                ois.readObject();
                fail("Expected Error");
            } catch (Error e) {
            }
            Object readE1 = ois.readObject();
            Object readE2 = ois.readObject();
            assertTrue("Replaced objects should be identical", readE1 == readE2);
            Object readF1 = ois.readObject();
            Object readF2 = ois.readObject();
            assertTrue("Replaced resolved objects should be identical: "
                    + readF1 + " " + readF2, readF1 == readF2);
        }
}
