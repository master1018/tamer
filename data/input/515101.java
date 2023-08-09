public class SourceGeneratorTest {
    private SourceGenerator mGen;
    @Before
    public void setUp() throws Exception {
        mGen = new SourceGenerator();
    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testDumpClass() throws Exception {
        StringWriter sw = new StringWriter();
        ClassReader cr = new ClassReader("data/TestBaseClass");
        mGen.visitClassSource(sw, cr, new Filter());
        String s = sw.toString();
        Assert.assertNotNull(s);
    }
}
