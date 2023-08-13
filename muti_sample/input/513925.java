@TestTargetClass(ObjectInputStream.class) 
public class ObjectInputStreamTest extends junit.framework.TestCase implements
        Serializable {
    static final long serialVersionUID = 1L;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    ByteArrayOutputStream bao;
    boolean readStreamHeaderCalled;
    private final String testString = "Lorem ipsum...";
    private final int testLength = testString.length();
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the protected ObjectInputStream() constructor.",
        method = "ObjectInputStream",
        args = {}
    )     
    public void test_Constructor() throws IOException {
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(new Support_IOTestSecurityManager());
        try { 
            ois = new BasicObjectInputStream();
            fail("SecurityException expected.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(sm);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "ObjectInputStream",
        args = {java.io.InputStream.class}
    )     
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        oos.writeDouble(Double.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.close();
        oos.close();
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(new byte[90]));
            fail("StreamCorruptedException expected");
        } catch (StreamCorruptedException e) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "ObjectInputStream",
        args = {java.io.InputStream.class}
    )        
    public void test_ConstructorLjava_io_InputStream_IOException() throws IOException {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        sis.throwExceptionOnNextUse = true;
        try {
            ois = new ObjectInputStream(sis);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that object can be serialized and deserialized correctly with reading descriptor from serialization stream.",
            method = "readClassDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that object can be serialized and deserialized correctly with reading descriptor from serialization stream.",
            method = "readObject",
            args = {}
        )
    })    
    public void test_ClassDescriptor() throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStreamWithWriteDesc oos = new ObjectOutputStreamWithWriteDesc(
                baos);
        oos.writeObject(String.class);
        oos.close();
        Class<?> cls = TestClassForSerialization.class;
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStreamWithReadDesc ois = new ObjectInputStreamWithReadDesc(
                bais, cls);
        Object obj = ois.readObject();
        ois.close();
        assertEquals(cls, obj);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "available",
        args = {}
    )        
    public void test_available() throws IOException {
        oos.writeBytes(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        assertEquals("Test 1: Incorrect number of bytes;", testLength, ois.available());
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "available",
        args = {}
    )        
    public void test_available_IOException() throws IOException {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.available();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "close",
        args = {}
    )     
    public void test_close() throws Exception {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.close();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "defaultReadObject",
        args = {}
    )      
    public void test_defaultReadObject() throws Exception {
        String s = testString;
        oos.writeObject(s);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.defaultReadObject();
            fail("NotActiveException expected");
        } catch (NotActiveException e) {
        } finally {
            ois.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies enableResolveObject(boolean).",
        method = "enableResolveObject",
        args = {boolean.class}
    )     
    public void test_enableResolveObjectB() throws IOException {
        BasicObjectInputStream bois = new BasicObjectInputStream();
        assertFalse("Test 1: Object resolving must be disabled by default.",
                bois.enableResolveObject(true));
        assertTrue("Test 2: enableResolveObject did not return the previous value.",
                bois.enableResolveObject(false));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {}
    )       
    public void test_read() throws IOException {
        oos.write('T');
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect byte value", 'T', ois.read());
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "read",
        args = {}
    )        
    public void test_read_IOException() throws IOException {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.read();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )    
    public void test_read$BII() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeBytes(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.read(buf, 0, testLength);
        ois.close();
        assertEquals("Read incorrect bytes", testString, new String(buf));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks Exceptions.",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )        
    public void test_read$BII_Exception() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeObject(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.read(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ois.read(buf, -1,1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ois.read(buf, testLength, 1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        ois.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.read(buf, 0, testLength);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargets({
        @TestTargetNew(
                method = "readFields",
                args = {},
                level = TestLevel.COMPLETE
        ),
        @TestTargetNew(
                method = "writeFields",
                args = {},
                clazz = ObjectOutputStream.class,
                level = TestLevel.COMPLETE
        )
    })    
    public void test_readFields() throws Exception {
        SerializableTestHelper sth;
        oos.writeObject(new SerializableTestHelper("Gabba", "Jabba"));
        oos.flush();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        sth = (SerializableTestHelper) (ois.readObject());
        assertEquals("readFields / writeFields failed--first field not set",
                "Gabba", sth.getText1());
        assertNull(
                "readFields / writeFields failed--second field should not have been set",
                sth.getText2());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "readFully",
        args = {byte[].class}
    )     
    public void test_readFully$B() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeBytes(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.readFully(buf);
        assertEquals("Test 1: Incorrect bytes read;", 
                testString, new String(buf));
        ois.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.read();
        try {
            ois.readFully(buf);
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "readFully",
        args = {byte[].class}
    )        
    public void test_readFully$B_Exception() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.readFully(buf);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "readFully",
        args = {byte[].class, int.class, int.class}
    )      
    public void test_readFully$BII() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeBytes(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.readFully(buf, 0, testLength);
        assertEquals("Read incorrect bytes", testString, new String(buf));
        ois.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.read();
        try {
            ois.readFully(buf);
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks Exceptions.",
        method = "readFully",
        args = {byte[].class, int.class, int.class}
    )        
    public void test_readFully$BII_Exception() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeObject(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.readFully(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ois.readFully(buf, -1,1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            ois.readFully(buf, testLength, 1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        ois.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.readFully(buf, 0, 1);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @SuppressWarnings("deprecation")
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "readLine",
        args = {}
    )       
    public void test_readLine() throws IOException {
        String line;
        oos.writeBytes("Lorem\nipsum\rdolor sit amet...");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        line = ois.readLine();
        assertTrue("Test 1: Incorrect line written or read: " + line, 
                line.equals("Lorem"));
        line = ois.readLine();
        assertTrue("Test 2: Incorrect line written or read: " + line, 
                line.equals("ipsum"));
        line = ois.readLine();
        assertTrue("Test 3: Incorrect line written or read: " + line, 
                line.equals("dolor sit amet..."));
        ois.close();
    }
    @SuppressWarnings("deprecation")
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "readLine",
        args = {}
    )    
    public void test_readLine_IOException() throws IOException {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.readLine();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readObject",
        args = {}
    )     
    public void test_readObject() throws Exception {
        String s = testString;
        oos.writeObject(s);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect Object value", s, ois.readObject());
        ois.close();
        byte[] cName = C.class.getName().getBytes();
        byte[] aName = A.class.getName().getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] begStream = new byte[] { (byte) 0xac, (byte) 0xed, 
                (byte) 0x00, (byte) 0x05, 
                (byte) 0x73, 
                (byte) 0x72, 
                (byte) 0x00, 
        };
        out.write(begStream, 0, begStream.length);
        out.write(cName.length); 
        out.write(cName, 0, cName.length); 
        byte[] midStream = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x21, 
                (byte) 0x02, 
                (byte) 0x00, (byte) 0x00, 
                (byte) 0x78, 
                (byte) 0x72, 
                (byte) 0x00, 
        };
        out.write(midStream, 0, midStream.length);
        out.write(aName.length); 
        out.write(aName, 0, aName.length); 
        byte[] endStream = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x0b, 
                (byte) 0x02, 
                (byte) 0x00, (byte) 0x01, 
                (byte) 0x4c, 
                (byte) 0x00, (byte) 0x04, 
                (byte) 0x6e, (byte) 0x61, (byte) 0x6d, (byte) 0x65,
                (byte) 0x74, 
                (byte) 0x00, (byte) 0x12, 
                (byte) 0x4c, (byte) 0x6a, (byte) 0x61, (byte) 0x76,
                (byte) 0x61, (byte) 0x2f, (byte) 0x6c, (byte) 0x61,
                (byte) 0x6e, (byte) 0x67, (byte) 0x2f, (byte) 0x53,
                (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6e,
                (byte) 0x67, (byte) 0x3b,
                (byte) 0x78, 
                (byte) 0x70, 
                (byte) 0x74, 
                (byte) 0x00, (byte) 0x04, 
                (byte) 0x6e, (byte) 0x61, (byte) 0x6d, (byte) 0x65, 
        };
        out.write(endStream, 0, endStream.length);
        out.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                out.toByteArray()));
        Object o = ois.readObject();
        assertEquals(C.class, o.getClass());
        assertNull(new ObjectInputStream() {}.readObject());
    }
    private void fillStreamHeader(byte[] buffer) {
        short magic = java.io.ObjectStreamConstants.STREAM_MAGIC;
        short version = java.io.ObjectStreamConstants.STREAM_VERSION;
        if (buffer.length < 4) {
            throw new IllegalArgumentException("The buffer's minimal length must be 4.");
        }
        buffer[0] = (byte) (magic >> 8);
        buffer[1] = (byte) magic;
        buffer[2] = (byte) (version >> 8);
        buffer[3] = (byte) (version);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies readObjectOverride().",
        method = "readObjectOverride",
        args = {}
    )     
    public void test_readObjectOverride() throws Exception {
        byte[] buffer = new byte[4];
        fillStreamHeader(buffer);
        BasicObjectInputStream bois = new BasicObjectInputStream();
        assertNull("Test 1:", bois.readObjectOverride());
        bois = new BasicObjectInputStream(new ByteArrayInputStream(buffer));
        try {
            bois.readObjectOverride();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {}
        bois.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readObject",
        args = {}
    )     
    public void test_readObjectMissingClasses() throws Exception {
        SerializationTest.verifySelf(new A1(), new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                assertEquals(5, ((A1) deserialized).b1.i);
            }
        });
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readObject",
        args = {}
    )     
    public void test_readObjectCorrupt() {
        byte[] bytes = { 00, 00, 00, 0x64, 0x43, 0x48, (byte) 0xFD, 0x71, 00,
                00, 0x0B, (byte) 0xB8, 0x4D, 0x65 };
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        boolean exception = false;
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            in.readObject();
            fail("Unexpected read of corrupted stream");
        } catch (StreamCorruptedException e) {
            exception = true;
        } catch (IOException e) {
            fail("Unexpected: " + e);
        } catch (ClassNotFoundException e) {
            fail("Unexpected: " + e);
        }
        assertTrue("Expected StreamCorruptedException", exception);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies readStreamHeader().",
        method = "readStreamHeader",
        args = {}
    )     
    public void test_readStreamHeader() throws IOException {
        String testString = "Lorem ipsum";
        BasicObjectInputStream bois;
        short magic = java.io.ObjectStreamConstants.STREAM_MAGIC;
        short version = java.io.ObjectStreamConstants.STREAM_VERSION;
        byte[] buffer = new byte[20];
        fillStreamHeader(buffer);
        System.arraycopy(testString.getBytes(), 0, buffer, 4, testString.length());
        try {
            readStreamHeaderCalled = false;
            bois = new BasicObjectInputStream(new ByteArrayInputStream(buffer));
            bois.close();
        } catch (StreamCorruptedException e) {
            fail("Test 1: Unexpected StreamCorruptedException.");
        }
        assertTrue("Test 1: readStreamHeader() has not been called.", 
                    readStreamHeaderCalled);
        buffer[0] = (byte)magic;
        buffer[1] = (byte)(magic >> 8);
        try {
            readStreamHeaderCalled = false;
            bois = new BasicObjectInputStream(new ByteArrayInputStream(buffer));
            fail("Test 2: StreamCorruptedException expected.");
            bois.close();
        } catch (StreamCorruptedException e) {
        }
        assertTrue("Test 2: readStreamHeader() has not been called.", 
                    readStreamHeaderCalled);
        buffer[0] = (byte)(magic >> 8);
        buffer[1] = (byte)magic;
        buffer[2] = (byte)(version);
        buffer[3] = (byte)(version >> 8);
        try {
            readStreamHeaderCalled = false;
            bois = new BasicObjectInputStream(new ByteArrayInputStream(buffer));
            fail("Test 3: StreamCorruptedException expected.");
            bois.close();
        } catch (StreamCorruptedException e) {
        }
        assertTrue("Test 3: readStreamHeader() has not been called.", 
                    readStreamHeaderCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readUnsignedByte",
        args = {}
    )     
    public void test_readUnsignedByte() throws IOException {
        oos.writeByte(-1);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Test 1: Incorrect unsigned byte written or read.", 
                255, ois.readUnsignedByte());
        try {
            ois.readUnsignedByte();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
        }
        ois.close();
        try {
            ois.readUnsignedByte();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readUnsignedShort",
        args = {}
    )    
    public void test_readUnsignedShort() throws IOException {
        oos.writeShort(-1);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Test 1: Incorrect unsigned short written or read.", 
                65535, ois.readUnsignedShort());
        try {
            ois.readUnsignedShort();
            fail("Test 2: EOFException expected.");
        } catch (EOFException e) {
        }
        ois.close();
        try {
            ois.readUnsignedShort();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies resolveProxyClass(String[]).",
        method = "resolveProxyClass",
        args = {java.lang.String[].class}
    )      
    public void test_resolveProxyClass() throws IOException {
        BasicObjectInputStream bois;
        byte[] buffer = new byte[10];
        fillStreamHeader(buffer);
        bois = new BasicObjectInputStream(new ByteArrayInputStream(buffer));
        try {
            bois.resolveProxyClass(null);
            fail("Test 1: NullPointerException expected.");
        }
        catch (NullPointerException npe) {
        }
        catch (ClassNotFoundException cnfe) {
            fail("Test 1: Unexpected ClassNotFoundException.");
        }
        try {
            String[] interfaces = { "java.io.Closeable", 
                                    "java.lang.Cloneable" };
            bois.resolveProxyClass(interfaces);
        }
        catch (ClassNotFoundException cnfe) {
            fail("Test 2: Unexpected ClassNotFoundException.");
        }
        try {
            String[] interfaces = { "java.io.Closeable", 
                                    "java.io.Closeable" };
            bois.resolveProxyClass(interfaces);
            fail ("Test 3: ClassNotFoundException expected.");
        }
        catch (ClassNotFoundException cnfe) {
        }
        bois.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "skipBytes",
        args = {int.class}
    )    
    public void test_skipBytesI() throws IOException {
        byte[] buf = new byte[testLength];
        oos.writeBytes(testString);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.skipBytes(5);
        ois.read(buf, 0, 5);
        ois.close();
        assertEquals("Skipped incorrect bytes", testString.substring(5, 10), 
                new String(buf, 0, 5));
        try {
            new ObjectInputStream() {}.skipBytes(0);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "skipBytes",
        args = {int.class}
    )    
    public void test_skipBytesI_IOException() throws IOException {
        oos.writeObject(testString);
        oos.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.skipBytes(5);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readObject",
        args = {}
    )    
    public void test_readObject_withPrimitiveClass() throws Exception {
        String dir = System.getProperty("java.io.tmpdir");
        if (dir == null)
            throw new Exception("System property java.io.tmpdir not defined.");
        File file = new File(dir, "test.ser");
        file.deleteOnExit();
        Test test = new Test();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                file));
        out.writeObject(test);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        Test another = (Test) in.readObject();
        in.close();
        assertEquals(test, another);
    }
    public static class A implements Serializable {
        private static final long serialVersionUID = 11L;
        public String name = "name";
    }
    public static class B extends A {}
    public static class C extends B {
        private static final long serialVersionUID = 33L;
    }
    public static class A1 implements Serializable {
        static final long serialVersionUID = 5942584913446079661L;
        B1 b1 = new B1();
        B1 b2 = b1;
        Vector v = new Vector();
    }
    public static class B1 implements Serializable {
        int i = 5;
        Hashtable h = new Hashtable();
    }
    public class SerializableTestHelper implements Serializable {
        public String aField1;
        public String aField2;
        SerializableTestHelper() {
            aField1 = null;
            aField2 = null;
        }
        SerializableTestHelper(String s, String t) {
            aField1 = s;
            aField2 = t;
        }
        private void readObject(ObjectInputStream ois) throws Exception {
            ObjectInputStream.GetField fields = ois.readFields();
            aField1 = (String) fields.get("aField1", "Zap");
        }
        private void writeObject(ObjectOutputStream oos) throws IOException {
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("aField1", aField1);
            oos.writeFields();
        }
        public String getText1() {
            return aField1;
        }
        public void setText1(String s) {
            aField1 = s;
        }
        public String getText2() {
            return aField2;
        }
        public void setText2(String s) {
            aField2 = s;
        }
    }
    class BasicObjectInputStream extends ObjectInputStream {
        public BasicObjectInputStream() throws IOException, SecurityException {
            super();
        }
        public BasicObjectInputStream(InputStream input)
                throws StreamCorruptedException, IOException {
            super(input);
        }
        public boolean enableResolveObject(boolean enable) 
                throws SecurityException {
            return super.enableResolveObject(enable);
        }
        public Object readObjectOverride() throws OptionalDataException,
                ClassNotFoundException, IOException {
            return super.readObjectOverride();
        }
        public void readStreamHeader() throws IOException,
                StreamCorruptedException {
            readStreamHeaderCalled = true;
            super.readStreamHeader();
        }
        public Class<?> resolveProxyClass(String[] interfaceNames) 
                throws IOException, ClassNotFoundException {
            return super.resolveProxyClass(interfaceNames);
        }
    }
    public static class ObjectOutputStreamWithWriteDesc extends
            ObjectOutputStream {
        public ObjectOutputStreamWithWriteDesc(OutputStream os)
                throws IOException {
            super(os);
        }
        public void writeClassDescriptor(ObjectStreamClass desc)
                throws IOException {
        }
    }
    public static class ObjectInputStreamWithReadDesc extends
            ObjectInputStream {
        private Class returnClass;
        public ObjectInputStreamWithReadDesc(InputStream is, Class returnClass)
                throws IOException {
            super(is);
            this.returnClass = returnClass;
        }
        public ObjectStreamClass readClassDescriptor() throws IOException,
                ClassNotFoundException {
            return ObjectStreamClass.lookup(returnClass);
        }
    }
    static class TestClassForSerialization implements Serializable {
        private static final long serialVersionUID = 1L;
    }
    public static class ObjectOutputStreamWithWriteDesc1 extends
            ObjectOutputStream {
        public ObjectOutputStreamWithWriteDesc1(OutputStream os)
                throws IOException {
            super(os);
        }
        public void writeClassDescriptor(ObjectStreamClass desc)
                throws IOException {
            super.writeClassDescriptor(desc);
        }
    }
    public static class ObjectInputStreamWithReadDesc1 extends
            ObjectInputStream {        
        public ObjectInputStreamWithReadDesc1(InputStream is)
                throws IOException {
            super(is);            
        }
        public ObjectStreamClass readClassDescriptor() throws IOException,
                ClassNotFoundException {
            return super.readClassDescriptor();
        }
    }
    public static class ObjectInputStreamWithResolve extends ObjectInputStream {
        public ObjectInputStreamWithResolve(InputStream in) throws IOException {
            super(in);
        }
        protected Class<?> resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException {
            if (desc.getName().equals(
                    "org.apache.harmony.luni.tests.pkg1.TestClass")) {
                return org.apache.harmony.luni.tests.pkg2.TestClass.class;
            }
            return super.resolveClass(desc);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException testing since this seems not to be thrown.",
        method = "resolveClass",
        args = {java.io.ObjectStreamClass.class}
    )
    public void test_resolveClass() throws Exception {
        org.apache.harmony.luni.tests.pkg1.TestClass to1 = new org.apache.harmony.luni.tests.pkg1.TestClass();
        to1.i = 555;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(to1);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStreamWithResolve(bais);
        org.apache.harmony.luni.tests.pkg2.TestClass to2 = (org.apache.harmony.luni.tests.pkg2.TestClass) ois
                .readObject();
        if (to2.i != to1.i) {
            fail("Wrong object read. Expected val: " + to1.i + ", got: " + to2.i);
        }
    }
    static class ObjectInputStreamWithResolveObject extends ObjectInputStream {
        public static Integer intObj = Integer.valueOf(1000);
        public ObjectInputStreamWithResolveObject(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }
        protected Object resolveObject(Object obj) throws IOException {
            if(obj instanceof Integer){
                obj = intObj;
            }
            return super.resolveObject(obj);
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "resolveObject",
        args = {java.lang.Object.class}
    )
    public void test_resolveObjectLjava_lang_Object() throws Exception {
        Integer original = new Integer(10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        oos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStreamWithResolveObject ois = 
            new ObjectInputStreamWithResolveObject(bais);
        Integer actual = (Integer) ois.readObject();
        ois.close();
        assertEquals(ObjectInputStreamWithResolveObject.intObj, actual);
    }
    @TestTargets(
            { 
                @TestTargetNew(
                        method = "readClassDescriptor",
                        args = {},
                        level = TestLevel.PARTIAL_COMPLETE
                ),
                @TestTargetNew(
                    method = "writeClassDescriptor",
                    args = {ObjectStreamClass.class},
                    clazz = ObjectOutputStream.class,
                    level = TestLevel.COMPLETE
              )
            }
    )    
    public void test_readClassDescriptor() throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStreamWithWriteDesc1 oos = new ObjectOutputStreamWithWriteDesc1(
                baos);
        ObjectStreamClass desc = ObjectStreamClass
        .lookup(TestClassForSerialization.class);
        oos.writeClassDescriptor(desc);
        oos.close();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStreamWithReadDesc1 ois = new ObjectInputStreamWithReadDesc1(
                bais);
        Object obj = ois.readClassDescriptor();
        ois.close();
        assertEquals(desc.getClass(), obj.getClass());
        bais = new ByteArrayInputStream(bytes);
        ExceptionalBufferedInputStream bis = new ExceptionalBufferedInputStream(
                bais);
        ois = new ObjectInputStreamWithReadDesc1(bis);
        bis.setEOF(true);
        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
        } finally {
            ois.close();
        }
        bais = new ByteArrayInputStream(bytes);
        bis = new ExceptionalBufferedInputStream(bais);
        ois = new ObjectInputStreamWithReadDesc1(bis);
        bis.setException(new IOException());
        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
        } finally {
            ois.close();
        }
        bais = new ByteArrayInputStream(bytes);
        bis = new ExceptionalBufferedInputStream(bais);
        ois = new ObjectInputStreamWithReadDesc1(bis);
        bis.setCorrupt(true);
        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
        } finally {
            ois.close();
        }
    }
    static class ExceptionalBufferedInputStream extends BufferedInputStream {
        private boolean eof = false;
        private IOException exception = null; 
        private boolean corrupt = false; 
        public ExceptionalBufferedInputStream(InputStream in) {
            super(in);
        }
        public int read() throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (eof) {
                return -1;
            }
            if (corrupt) {
                return 0;
            }
            return super.read();
        }
        public void setEOF(boolean eof) {
            this.eof = eof;
        }
        public void setException(IOException exception) {
            this.exception = exception;
        }
        public void setCorrupt(boolean corrupt) {
            this.corrupt = corrupt;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "registerValidation",
        args = {java.io.ObjectInputValidation.class, int.class}
    )        
    public void test_registerValidation() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()));
        try {
            ois.registerValidation(null, 256);
            fail("NotActiveException should be thrown");
        } catch (NotActiveException nae) {
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
    }
}
class Test implements Serializable {
    private static final long serialVersionUID = 1L;
    Class<?> classes[] = new Class[] { byte.class, short.class, int.class,
            long.class, boolean.class, char.class, float.class, double.class };
    public boolean equals(Object o) {
        if (!(o instanceof Test)) {
            return false;
        }
        return Arrays.equals(classes, ((Test) o).classes);
    }
}
