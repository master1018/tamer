public class DexUtilTest extends DexTestsCommon {
    private JavaSourceToDexUtil dexUtil;
    private DexToSigConverter converter;
    @Before
    public void setupDexUtil(){
        dexUtil = new JavaSourceToDexUtil();
        converter = new DexToSigConverter();
    }
    @Test
    public void convertPackageTest(){
        SigPackage aPackage = converter.convertPackage("a");
        assertEquals("a", aPackage.getName());
        aPackage = converter.convertPackage("a.b.c");
        assertEquals("a.b.c", aPackage.getName());
    }
    @Test
    public void getPackageNameTest(){
        assertEquals("",getPackageName("LA;"));
        assertEquals("a",getPackageName("La/A;"));
        assertEquals("a.b.c",getPackageName("La/b/c/A;"));
    }
    @Test
    public void getClassNameTest(){
        assertEquals("A",getClassName("LA;"));
        assertEquals("A",getClassName("La/A;"));
        assertEquals("A",getClassName("La/b/c/A;"));
    }
    @Test
    public void hasGenericSignatureTest() throws IOException {
        DexFile dexFile = dexUtil.getFrom(new JavaSource("A", "public class A{}"));
        DexClass dexClass = getClass(dexFile, "LA;");
        assertFalse(hasGenericSignature(dexClass));
        dexFile = dexUtil.getFrom(new JavaSource("B", "public class B<T>{}"));
        dexClass = getClass(dexFile, "LB;");
        assertTrue(hasGenericSignature(dexClass));
    }
    @Test
    public void getGenericSignatureTest() throws IOException {
        DexFile dexFile =  dexUtil.getFrom(new JavaSource("A", "public class A{}"));
        DexClass dexClass = getClass(dexFile, "LA;");
        assertNull(getGenericSignature(dexClass));
        dexFile =  dexUtil.getFrom(new JavaSource("B", "public class B<T>{}"));
        dexClass = getClass(dexFile, "LB;");
        assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Object;", getGenericSignature(dexClass));
    }
}
