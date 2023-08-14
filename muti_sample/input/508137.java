@TestTargetClass(InvocationTargetException.class) 
public class InvocationTargetExceptionTest extends junit.framework.TestCase {
    static class TestMethod {
        public TestMethod() {
        }
        public void voidMethod() throws IllegalArgumentException {
        }
        public void parmTest(int x, short y, String s, boolean bool, Object o,
                long l, byte b, char c, double d, float f) {
        }
        public int intMethod() {
            return 1;
        }
        public static final void printTest(int x, short y, String s,
                boolean bool, Object o, long l, byte b, char c, double d,
                float f) {
        }
        public double doubleMethod() {
            return 1.0;
        }
        public short shortMethod() {
            return (short) 1;
        }
        public byte byteMethod() {
            return (byte) 1;
        }
        public float floatMethod() {
            return 1.0f;
        }
        public long longMethod() {
            return 1l;
        }
        public char charMethod() {
            return 'T';
        }
        public Object objectMethod() {
            return new Object();
        }
        private static void prstatic() {
        }
        public static void pustatic() {
        }
        public static synchronized void pustatsynch() {
        }
        public static int invokeStaticTest() {
            return 1;
        }
        public int invokeInstanceTest() {
            return 1;
        }
        private int privateInvokeTest() {
            return 1;
        }
        public int invokeExceptionTest() throws NullPointerException {
            throw new NullPointerException();
        }
        public static synchronized native void pustatsynchnat();
    }
    abstract class AbstractTestMethod {
        public abstract void puabs();
    }
    class SubInvocationTargetException extends InvocationTargetException {}
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvocationTargetException",
        args = {}
    )
    public void test_Constructor() throws Exception {
        Constructor<InvocationTargetException> ctor = InvocationTargetException.class
                .getDeclaredConstructor();
        assertNotNull("Parameterless constructor does not exist.", ctor);
        assertTrue("Constructor is not protected", Modifier.isProtected(ctor
                .getModifiers()));
        SubInvocationTargetException subException = new SubInvocationTargetException();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvocationTargetException",
        args = {java.lang.Throwable.class}
    )
    public void test_ConstructorLjava_lang_Throwable() {
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            return;
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        }
        fail("Failed to throw exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvocationTargetException",
        args = {java.lang.Throwable.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_ThrowableLjava_lang_String() {
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            return;
        } catch (NoSuchMethodException e) {
            ;
        } catch (IllegalAccessException e) {
        }
        fail("Failed to throw exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTargetException",
        args = {}
    )
    public void test_getTargetException() {
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = { Object.class };
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            assertTrue("Returned incorrect target exception", e
                    .getTargetException() instanceof NullPointerException);
            return;
        } catch (Exception e) {
            fail("Exception during constructor test : " + e.getMessage());
        }
        fail("Failed to throw exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        try {
            Method mth = TestMethod.class.getDeclaredMethod(
                    "invokeExceptionTest", new Class[0]);
            Object[] args = {Object.class};
            Object ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            assertTrue("Returned incorrect cause",
                    e.getCause() instanceof NullPointerException);
            return;
        } catch (Exception e) {
            fail("Exception during InvocationTargetException test : "
                    + e.getMessage());
        }
        fail("Failed to throw exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {}
    )
    public void test_printStackTrace() {
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bao);
            PrintStream oldErr = System.err;
            System.setErr(ps);
            InvocationTargetException ite = new InvocationTargetException(null);
            ite.printStackTrace();
            System.setErr(oldErr);
            String s = new String(bao.toByteArray());
            assertTrue("Incorrect Stack trace: " + s, s != null
                    && s.length() > 300);
        } catch (Exception e) {
            fail("printStackTrace() caused exception : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {java.io.PrintStream.class}
    )
    public void test_printStackTraceLjava_io_PrintStream() {
        assertTrue("Tested via test_printStackTrace().", true);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        InvocationTargetException ite = new InvocationTargetException(
                new InvocationTargetException(null));
        ite.printStackTrace(ps);
        String s = bao.toString();
        assertTrue("printStackTrace failed." + s.length(), s != null
                && s.length() > 400);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "printStackTrace",
        args = {java.io.PrintWriter.class}
    )    
    public void test_printStackTraceLjava_io_PrintWriter() {
        try {
            PrintWriter pw;
            InvocationTargetException ite;
            String s;
            CharArrayWriter caw = new CharArrayWriter();
            pw = new PrintWriter(caw);
            ite = new InvocationTargetException(new InvocationTargetException(
                    null));
            ite.printStackTrace(pw);
            s = caw.toString();
            assertTrue("printStackTrace failed." + s.length(), s != null
                    && s.length() > 400);
            pw.close();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            pw = new PrintWriter(bao);
            ite = new InvocationTargetException(new InvocationTargetException(
                    null));
            ite.printStackTrace(pw);
            pw.flush(); 
            s = bao.toString();
            assertTrue("printStackTrace failed." + s.length(), s != null
                    && s.length() > 400);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
