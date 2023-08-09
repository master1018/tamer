public class AccessSourcerTest {
    private StringWriter mWriter;
    private AccessSourcer mSourcer;
    @Before
    public void setUp() throws Exception {
        mWriter = new StringWriter();
        mSourcer = new AccessSourcer(new Output(mWriter));
    }
    @After
    public void tearDown() throws Exception {
        mWriter = null;
        mSourcer = null;
    }
    @Test
    public void testAbstractPublic() throws Exception {
        mSourcer.write(Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC, AccessSourcer.IS_CLASS);
        String s = mWriter.toString();
        Assert.assertEquals("public abstract", s);
    }
    @Test
    public void testPrivateFinalStatic() throws Exception {
        mSourcer.write(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC,
                AccessSourcer.IS_METHOD);
        String s = mWriter.toString();
        Assert.assertEquals("private static final", s);
    }
}
