@TestTargetClass(GenericSignatureFormatError.class)
public class GenericSignatureFormatErrorTest extends TestCase{
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GenericSignatureFormatError",
        args = {}
    )
    public void test_Constructor() {
        assertNotNull(new GenericSignatureFormatError());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GenericSignatureFormatError",
        args = {}
    )
    public void test_readResource() throws Exception {
        File tf = File.createTempFile("classes", ".dex");
        InputStream is = this.getClass().getResourceAsStream("dex1.bytes");
        assertNotNull(is);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "GenericSignatureFormatError",
        args = {}
    )    
    @AndroidOnly("Uses Android specific class dalvik.system.DexFile " +
            "for loading classes.")
    @SideEffect("strange issue (exception: 'could not open dex file', " +
            "dalvikvm: 'waitpid failed' log msg  - only occurs when @SideEffect is removed " +
            "and this test is run via running tests.luni.AllTestsLang TestSuite")
    public void test_signatureFormatError() throws Exception {
        File tf = File.createTempFile("classes", ".dex");
        InputStream is = this.getClass().getResourceAsStream("dex1.bytes");
        assertNotNull(is);
        OutputStream fos = new FileOutputStream(tf);
        copy(is, fos);
        fos.flush();
        fos.close();
        try {
            ClassLoader cl = Support_ClassLoader.getInstance(tf.toURL(),
                    getClass().getClassLoader());
            Class clazz = cl.loadClass("demo/HelloWorld");
            TypeVariable[] tvs = clazz.getTypeParameters();
            fail("expecting a GenericSignatureFormatError");
        } catch (GenericSignatureFormatError gsfe) {
        }
    }
    private void copy(InputStream is, OutputStream os) {
        try {
            int b; 
            while ((b = is.read()) != -1) {
                os.write(b);
            }
            is.close();
        } catch (IOException ex) {
            throw new RuntimeException("io error", ex);
        }
    }
}
