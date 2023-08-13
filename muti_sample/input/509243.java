public class MethodSourcerTest extends TestHelper {
    private StringWriter mWriter;
    private Output mOutput;
    @Before
    public void setUp() throws Exception {
        mWriter = new StringWriter();
        mOutput = new Output(mWriter);
    }
    @After
    public void tearDown() throws Exception {
        mWriter = null;
    }
    @Test
    public void testVoid() {
        MethodSourcer m = new MethodSourcer(mOutput,
                "foo", 
                Opcodes.ACC_PUBLIC, 
                "testVoid", 
                "()V", 
                null, 
                null); 
        m.visitEnd();
        assertSourceEquals(
                "public void testVoid() { }",
                mWriter.toString());
    }
    @Test
    public void testVoidThrow() {
        MethodSourcer m = new MethodSourcer(mOutput,
                "foo", 
                Opcodes.ACC_PUBLIC, 
                "testVoid", 
                "()V", 
                null, 
                new String[] { "java/lang/Exception" }); 
        m.visitEnd();
        assertSourceEquals(
                "public void testVoid() throws java.lang.Exception { }",
                mWriter.toString());
    }
    @Test
    public void testReturnMap() {
        MethodSourcer m = new MethodSourcer(mOutput,
                "foo", 
                Opcodes.ACC_PUBLIC, 
                "getMap_T_U", 
                "()Ljava/util/Map;", 
                "()Ljava/util/Map<TT;TU;>;", 
                null); 
        m.visitEnd();
        assertSourceEquals(
                "public java.util.Map<T, U> getMap_T_U() { }",
                mWriter.toString());
    }
}
