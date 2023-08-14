public class GenericSignatureParserTest extends DexTestsCommon{
    private DexToSigConverter converter;
    private JavaSourceToDexUtil dexUtil;
    private GenericSignatureParser parser;
    @Before
    public void setupConverter(){
        converter = new DexToSigConverter();
        dexUtil = new JavaSourceToDexUtil();
        parser = new GenericSignatureParser(new TypePool(), converter);
    }
    @Test
    public void getGenericSignatureTest() throws IOException {
        DexFile  dexFile =  dexUtil.getFrom(new JavaSource("B", "public class B<T>{}"));
        DexClass dexClass = getClass(dexFile, "LB;");
        assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Object;", DexUtil.getGenericSignature(dexClass));
        SigClassDefinition sigClass = converter.convertClass(dexClass);
        parser.parseForClass(sigClass, DexUtil.getGenericSignature(dexClass));
        assertEquals(1, parser.formalTypeParameters.size());
        assertEquals("T", parser.formalTypeParameters.get(0).getName());
        assertSame(sigClass, parser.formalTypeParameters.get(0).getGenericDeclaration());
        assertEquals(1, parser.formalTypeParameters.get(0).getUpperBounds().size());
    }
}
