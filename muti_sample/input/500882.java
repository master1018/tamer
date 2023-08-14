public class FieldSourcerTest {
    private StringWriter mWriter;
    @Before
    public void setUp() throws Exception {
        mWriter = new StringWriter();
    }
    @After
    public void tearDown() throws Exception {
        mWriter = null;
    }
    @Test
    public void testStringField() throws Exception {
        FieldSourcer fs = new FieldSourcer(new Output(mWriter),
                Opcodes.ACC_PUBLIC, 
                "mArg", 
                "Ljava/lang/String;", 
                null 
                );
        fs.visitEnd();
        String s = mWriter.toString();
        Assert.assertEquals("public java.lang.String mArg;\n", s);
    }
    @Test
    public void testTemplateTypeField() throws Exception {
        FieldSourcer fs = new FieldSourcer(new Output(mWriter),
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, 
                "mList", 
                "Ljava/util/ArrayList;", 
                "Ljava/util/ArrayList<Ljava/lang/String;>;" 
                );
        fs.visitEnd();
        String s = mWriter.toString();
        Assert.assertEquals("private final java.util.ArrayList<java.lang.String> mList;\n", s);
    }
}
