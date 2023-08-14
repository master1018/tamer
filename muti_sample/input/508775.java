@TestTargetClass(PrintStream.class) 
public class PrintStreamTest extends junit.framework.TestCase {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] ibuf = new byte[4096];
    private File testFile = null;
    private String testFilePath = null;
    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";
    private static class MockPrintStream extends PrintStream {
        public MockPrintStream(String fileName) throws FileNotFoundException {
            super(fileName);
        }
        public MockPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
            super(fileName, csn);
        }
        public MockPrintStream(OutputStream os) {
            super(os);
        }
        @Override
        public void setError() {
            super.setError();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.io.File.class}
    )
    public void test_Constructor_Ljava_io_File() throws IOException {
        PrintStream tobj;
        tobj = new PrintStream(testFile);
        assertNotNull(tobj);
        tobj.write(1);
        tobj.close();
        assertEquals("output file has wrong length", 1, testFile.length());
        tobj = new PrintStream(testFile);
        assertNotNull(tobj);
        tobj.close();
        assertEquals("output file should be empty", 0, testFile.length());
        File file = new File("/invalidDirectory/Dummy");
        try {
            tobj = new PrintStream(file);
            fail("FileNotFoundException not thrown.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.io.File.class, java.lang.String.class}
    )
    public void test_Constructor_Ljava_io_File_Ljava_lang_String() throws Exception {
        PrintStream tobj;
        tobj = new PrintStream(testFile, "utf-8");
        assertNotNull(tobj);
        tobj.write(1);
        tobj.close();
        assertEquals("output file has wrong length", 1, testFile.length());
        tobj = new PrintStream(testFile, "utf-8");
        assertNotNull(tobj);
        tobj.close();
        assertEquals("output file should be empty", 0, testFile.length());
        File file = new File("/invalidDirectory/Dummy");
        try {
            tobj = new PrintStream(file, "utf-8");
            fail("FileNotFoundException not thrown.");
        } catch (FileNotFoundException e) {
        }
        try {
            tobj = new PrintStream(testFile, "invalidEncoding");
            fail("UnsupportedEncodingException not thrown.");
        } catch (UnsupportedEncodingException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.lang.String.class}
    )
    public void test_Constructor_Ljava_lang_String() throws IOException {
        PrintStream tobj;
        tobj = new PrintStream(testFilePath);
        assertNotNull(tobj);
        tobj.write(1);
        tobj.close();
        assertEquals("output file has wrong length", 1, testFile.length());
        tobj = new PrintStream(testFilePath);
        assertNotNull(tobj);
        tobj.close();
        assertEquals("output file should be empty", 0, testFile.length());
        try {
            tobj = new PrintStream("/invalidDirectory/Dummy");
            fail("FileNotFoundException not thrown.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_Constructor_Ljava_lang_String_Ljava_lang_String() throws Exception {
        PrintStream tobj;
        tobj = new PrintStream(testFilePath, "utf-8");
        assertNotNull(tobj);
        tobj.write(1);
        tobj.close();
        assertEquals("output file has wrong length", 1, testFile.length());
        tobj = new PrintStream(testFilePath, "utf-8");
        assertNotNull(tobj);
        tobj.close();
        assertEquals("output file should be empty", 0, testFile.length());
        try {
            tobj = new PrintStream("/invalidDirectory/", "utf-8");
            fail("FileNotFoundException not thrown.");
        } catch (FileNotFoundException e) {
        }
        try {
            tobj = new PrintStream(testFilePath, "invalidEncoding");
            fail("UnsupportedEncodingException not thrown.");
        } catch (UnsupportedEncodingException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.io.OutputStream.class}
    )
    public void test_ConstructorLjava_io_OutputStream() throws Exception {
        PrintStream os = new PrintStream(baos);
        os.print(2345.76834720202);
        os.close();
        try {
            os = new PrintStream(baos, true, null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {}
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Passing FALSE for autoFlush not tested.",
        method = "PrintStream",
        args = {java.io.OutputStream.class, boolean.class}
    )
    public void test_ConstructorLjava_io_OutputStreamZ() throws FileNotFoundException {
        PrintStream tobj;
        tobj = new PrintStream(baos, true);
        tobj.println(2345.76834720202);
        assertTrue("Bytes not written", baos.size() > 0);
        tobj.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrintStream",
        args = {java.io.OutputStream.class, boolean.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_io_OutputStream_Z_Ljava_lang_String() {
        PrintStream tobj;
        tobj = new PrintStream(baos, true);
        tobj.println(2345.76834720202);
        assertTrue("Bytes not written", baos.size() > 0);
        tobj.close();
        try {
            tobj = new PrintStream(baos, true, "invalidEncoding");
            fail("UnsupportedEncodingException not thrown.");
        } catch (UnsupportedEncodingException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkError",
        args = {}
    )
    public void test_checkError() throws Exception {
        PrintStream os = new PrintStream(new OutputStream() {
            public void write(int b) throws IOException {
                throw new IOException();
            }
            public void write(byte[] b, int o, int l) throws IOException {
                throw new IOException();
            }
        });
        os.print(fileString.substring(0, 501));
        assertTrue("Checkerror should return true", os.checkError());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setError",
        args = {}
    )
    public void test_setError() throws Exception {
        MockPrintStream os = new MockPrintStream(new ByteArrayOutputStream());
        assertFalse("Test 1: Error flag should not be set.", os.checkError());
        os.setError();
        assertTrue("Test 2: Error flag should be set.", os.checkError());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void test_close() throws Exception {
        PrintStream os = new PrintStream(baos);
        os.close();
        baos.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "flush",
        args = {}
    )
    public void test_flush() throws Exception {
        PrintStream os = new PrintStream(baos);
        os.print(fileString.substring(0, 501));
        os.flush();
        assertEquals("Bytes not written after flush", 501, baos.size());
        baos.close();
        os.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {char[].class}
    )
    public void test_print$C() {
        PrintStream os = new PrintStream(baos, true);
        try {
            os.print((char[]) null);
            fail("NPE expected");
        } catch (NullPointerException ok) {}
        os = new PrintStream(baos, true);
        char[] sc = new char[4000];
        fileString.getChars(0, fileString.length(), sc, 0);
        os.print(sc);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        os.close();
        byte[] rbytes = new byte[4000];
        bis.read(rbytes, 0, fileString.length());
        assertEquals("Incorrect char[] written", fileString, new String(rbytes,
                0, fileString.length()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {char.class}
    )
    public void test_printC() {
        PrintStream os = new PrintStream(baos, true);
        os.print('t');
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        assertEquals("Incorrect char written", 't', bis.read());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {double.class}
    )
    public void test_printD() {
        byte[] rbuf = new byte[100];
        PrintStream os = new PrintStream(baos, true);
        os.print(2345.76834720202);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 16);
        assertEquals("Incorrect double written", "2345.76834720202",
                new String(rbuf, 0, 16));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {float.class}
    )
    public void test_printF() {
        PrintStream os = new PrintStream(baos, true);
        byte rbuf[] = new byte[10];
        os.print(29.08764f);
        os.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 8);
        assertEquals("Incorrect float written", "29.08764", new String(rbuf, 0,
                8));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {int.class}
    )
    public void test_printI() {
        PrintStream os = new PrintStream(baos, true);
        os.print(768347202);
        byte[] rbuf = new byte[18];
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 9);
        assertEquals("Incorrect int written", "768347202", new String(rbuf, 0,
                9));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {long.class}
    )
    public void test_printJ() {
        byte[] rbuf = new byte[100];
        PrintStream os = new PrintStream(baos, true);
        os.print(9875645283333L);
        os.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 13);
        assertEquals("Incorrect long written", "9875645283333", new String(
                rbuf, 0, 13));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {java.lang.Object.class}
    )
    public void test_printLjava_lang_Object() throws Exception {
        PrintStream os = new PrintStream(baos, true);
        os.print((Object) null);
        os.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] nullbytes = new byte[4];
        bis.read(nullbytes, 0, 4);
        assertEquals("null should be written", "null", new String(nullbytes, 0,
                4));
        bis.close();
        baos.close();
        os.close();
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        os = new PrintStream(bos1, true);
        os.print(new java.util.Vector());
        bis = new ByteArrayInputStream(bos1.toByteArray());
        byte[] rbytes = new byte[2];
        bis.read(rbytes, 0, 2);
        assertEquals("Incorrect Object written", "[]", new String(rbytes, 0, 2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {java.lang.String.class}
    )
    public void test_printLjava_lang_String() throws Exception {
        PrintStream os = new PrintStream(baos, true);
        os.print((String) null);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] nullbytes = new byte[4];
        bis.read(nullbytes, 0, 4);
        assertEquals("null should be written", "null", new String(nullbytes, 0,
                4));
        bis.close();
        baos.close();
        os.close();
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        os = new PrintStream(bos1, true);
        os.print("Hello World");
        bis = new ByteArrayInputStream(bos1.toByteArray());
        byte rbytes[] = new byte[100];
        bis.read(rbytes, 0, 11);
        assertEquals("Incorrect string written", "Hello World", new String(
                rbytes, 0, 11));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "print",
        args = {boolean.class}
    )
    public void test_printZ() throws Exception {
        PrintStream os = new PrintStream(baos, true);
        os.print(true);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos
                .toByteArray()));
        assertTrue("Incorrect boolean written", dis.readBoolean());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {}
    )
    public void test_println() {
        char c;
        PrintStream os = new PrintStream(baos, true);
        os.println("");
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        assertTrue("Newline not written", (c = (char) bis.read()) == '\r'
                || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {char[].class}
    )
    public void test_println$C() {
        PrintStream os = new PrintStream(baos, true);
        char[] sc = new char[4000];
        fileString.getChars(0, fileString.length(), sc, 0);
        os.println(sc);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[4000];
        bis.read(rbytes, 0, fileString.length());
        assertEquals("Incorrect char[] written", fileString, new String(rbytes,
                0, fileString.length()));
        int r;
        boolean newline = false;
        while ((r = bis.read()) != -1) {
            if (r == '\r' || r == '\n')
                newline = true;
        }
        assertTrue("Newline not written", newline);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {char.class}
    )
    public void test_printlnC() {
        int c;
        PrintStream os = new PrintStream(baos, true);
        os.println('t');
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        assertEquals("Incorrect char written", 't', bis.read());
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {double.class}
    )
    public void test_printlnD() {
        int c;
        PrintStream os = new PrintStream(baos, true);
        os.println(2345.76834720202);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbuf = new byte[100];
        bis.read(rbuf, 0, 16);
        assertEquals("Incorrect double written", "2345.76834720202",
                new String(rbuf, 0, 16));
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {float.class}
    )
    public void test_printlnF() {
        int c;
        byte[] rbuf = new byte[100];
        PrintStream os = new PrintStream(baos, true);
        os.println(29.08764f);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 8);
        assertEquals("Incorrect float written", "29.08764", new String(rbuf, 0,
                8));
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {int.class}
    )
    public void test_printlnI() {
        int c;
        PrintStream os = new PrintStream(baos, true);
        os.println(768347202);
        byte[] rbuf = new byte[100];
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        bis.read(rbuf, 0, 9);
        assertEquals("Incorrect int written", "768347202", new String(rbuf, 0,
                9));
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {long.class}
    )
    public void test_printlnJ() {
        int c;
        PrintStream os = new PrintStream(baos, true);
        os.println(9875645283333L);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbuf = new byte[100];
        bis.read(rbuf, 0, 13);
        assertEquals("Incorrect long written", "9875645283333", new String(
                rbuf, 0, 13));
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {java.lang.Object.class}
    )
    public void test_printlnLjava_lang_Object() {
        char c;
        PrintStream os = new PrintStream(baos, true);
        os.println(new java.util.Vector());
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[2];
        bis.read(rbytes, 0, 2);
        assertEquals("Incorrect Vector written", "[]", new String(rbytes, 0, 2));
        assertTrue("Newline not written", (c = (char) bis.read()) == '\r'
                || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {java.lang.String.class}
    )
    public void test_printlnLjava_lang_String() {
        char c;
        PrintStream os = new PrintStream(baos, true);
        os.println("Hello World");
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte rbytes[] = new byte[100];
        bis.read(rbytes, 0, 11);
        assertEquals("Incorrect string written", "Hello World", new String(
                rbytes, 0, 11));
        assertTrue("Newline not written", (c = (char) bis.read()) == '\r'
                || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {boolean.class}
    )
    public void test_printlnZ() {
        int c;
        PrintStream os = new PrintStream(baos, true);
        os.println(true);
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbuf = new byte[100];
        bis.read(rbuf, 0, 4);
        assertEquals("Incorrect boolean written", "true",
                new String(rbuf, 0, 4));
        assertTrue("Newline not written", (c = bis.read()) == '\r' || c == '\n');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() {
        PrintStream os = new PrintStream(baos, true);
        os.write(fileString.getBytes(), 0, fileString.length());
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte rbytes[] = new byte[4000];
        bis.read(rbytes, 0, fileString.length());
        assertTrue("Incorrect bytes written", new String(rbytes, 0, fileString
                .length()).equals(fileString));
        try {
            os.write(rbytes, -1, 1);
            fail("IndexOutOfBoundsException should have been thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            os.write(rbytes, 0, -1);
            fail("IndexOutOfBoundsException should have been thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            os.write(rbytes, 1, rbytes.length);
            fail("IndexOutOfBoundsException should have been thrown.");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() {
        PrintStream os = new PrintStream(baos, true);
        os.write('t');
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        assertEquals("Incorrect char written", 't', bis.read());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {char.class}
    )
    public void test_appendChar() throws IOException {
        char testChar = ' ';
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.append(testChar);
        printStream.flush();
        assertEquals(String.valueOf(testChar), out.toString());
        printStream.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {java.lang.CharSequence.class}
    )
    public void test_appendCharSequence() {
        String testString = "My Test String";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.append(testString);
        printStream.flush();
        assertEquals(testString, out.toString());
        printStream.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {java.lang.CharSequence.class, int.class, int.class}
    )
    public void test_appendCharSequenceIntInt() {
        String testString = "My Test String";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.append(testString, 1, 3);
        printStream.flush();
        assertEquals(testString.substring(1, 3), out.toString());
        try {
            printStream.append(testString, 4, 100);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            printStream.append(testString, 100, 1);
            fail("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        printStream.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.String.class, java.lang.Object[].class}
    )
    public void test_formatLjava_lang_String$Ljava_lang_Object() {
        PrintStream tobj;
        tobj = new PrintStream(baos, false);
        tobj.format("%s %s", "Hello", "World");
        tobj.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[11];
        bis.read(rbytes, 0, rbytes.length);
        assertEquals("Wrote incorrect string", "Hello World",
                new String(rbytes));
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.format("%1$.3G, %1$.5f, 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1.23E+04, 12345.67800, 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        try {
            tobj.format("%1$.3G, %1$x", 12345.678);
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.format("%s %q", "Hello", "World");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.format("%s %s", "Hello");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.Locale.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void test_formatLjava_util_Locale_Ljava_lang_String_$Ljava_lang_Object() {
        Locale[] requiredLocales = {Locale.US, Locale.GERMANY};
        if (!Support_Locale.areLocalesAvailable(requiredLocales)) {
            return;
        }
        PrintStream tobj;
        tobj = new PrintStream(baos, false);
        tobj.format(Locale.US, "%s %s", "Hello", "World");
        tobj.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[11];
        bis.read(rbytes, 0, rbytes.length);
        assertEquals("Wrote incorrect string", "Hello World",
                new String(rbytes));
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.format(Locale.GERMANY, "%1$.3G; %1$.5f; 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1,23E+04; 12345,67800; 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.format(Locale.US, "%1$.3G, %1$.5f, 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1.23E+04, 12345.67800, 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        try {
            tobj.format(Locale.US, "%1$.3G, %1$x", 12345.678);
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.format(Locale.US, "%s %q", "Hello", "World");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.format(Locale.US, "%s %s", "Hello");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printf",
        args = {java.lang.String.class, java.lang.Object[].class}
    )
    public void test_printfLjava_lang_String$Ljava_lang_Object() {
        PrintStream tobj;
        tobj = new PrintStream(baos, false);
        tobj.printf("%s %s", "Hello", "World");
        tobj.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[11];
        bis.read(rbytes, 0, rbytes.length);
        assertEquals("Wrote incorrect string", "Hello World",
                new String(rbytes));
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.printf("%1$.3G, %1$.5f, 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1.23E+04, 12345.67800, 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        try {
            tobj.printf("%1$.3G, %1$x", 12345.678);
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.printf("%s %q", "Hello", "World");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.printf("%s %s", "Hello");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printf",
        args = {java.util.Locale.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void test_printfLjava_util_Locale_Ljava_lang_String_$Ljava_lang_Object() {
        Locale[] requiredLocales = {Locale.US, Locale.GERMANY};
        if (!Support_Locale.areLocalesAvailable(requiredLocales)) {
            return;
        }
        PrintStream tobj;
        tobj = new PrintStream(baos, false);
        tobj.printf(Locale.US, "%s %s", "Hello", "World");
        tobj.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        byte[] rbytes = new byte[11];
        bis.read(rbytes, 0, rbytes.length);
        assertEquals("Wrote incorrect string", "Hello World",
                new String(rbytes));
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.printf(Locale.GERMANY, "%1$.3G; %1$.5f; 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1,23E+04; 12345,67800; 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        tobj.printf(Locale.US, "%1$.3G, %1$.5f, 0%2$xx", 12345.678, 123456);
        tobj.flush();
        assertEquals("Wrong output!", "1.23E+04, 12345.67800, 01e240x", new String(baos.toByteArray()));
        tobj.close();
        baos.reset();
        tobj = new PrintStream(baos);
        try {
            tobj.printf(Locale.US, "%1$.3G, %1$x", 12345.678);
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.printf(Locale.US, "%s %q", "Hello", "World");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
        try {
            tobj.printf(Locale.US, "%s %s", "Hello");
            fail("IllegalFormatException not thrown");
        } catch (IllegalFormatException e) {
        }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFile = File.createTempFile("test", null);
        testFilePath = testFile.getAbsolutePath();
    }
    @Override
    protected void tearDown() throws Exception {
        testFile.delete();
        testFile = null;
        testFilePath = null;
        super.tearDown();
    }
}
