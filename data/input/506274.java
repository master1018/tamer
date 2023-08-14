public class RenameClassAdapterTest {
    private RenameClassAdapter mOuter;
    private RenameClassAdapter mInner;
    @Before
    public void setUp() throws Exception {
        mOuter = new RenameClassAdapter(null, 
                                         "com.pack.Old",
                                         "org.blah.New");
        mInner = new RenameClassAdapter(null, 
                                         "com.pack.Old$Inner",
                                         "org.blah.New$Inner"); 
    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testRenameTypeDesc() {
        assertEquals("I", mOuter.renameTypeDesc("I"));
        assertEquals("D", mOuter.renameTypeDesc("D"));
        assertEquals("V", mOuter.renameTypeDesc("V"));
        assertEquals("Lcom.package.MyClass;", mOuter.renameTypeDesc("Lcom.package.MyClass;"));
        assertEquals("Lcom.package.MyClass;", mInner.renameTypeDesc("Lcom.package.MyClass;"));
        assertEquals("Lorg.blah.New;", mOuter.renameTypeDesc("Lcom.pack.Old;"));
        assertEquals("Lorg.blah.New$Inner;", mInner.renameTypeDesc("Lcom.pack.Old$Inner;"));
        assertEquals("Lorg.blah.New$Other;", mOuter.renameTypeDesc("Lcom.pack.Old$Other;"));
        assertEquals("Lorg.blah.New$Other;", mInner.renameTypeDesc("Lcom.pack.Old$Other;"));
        assertEquals("[Lorg.blah.New;",  mOuter.renameTypeDesc("[Lcom.pack.Old;"));
        assertEquals("[[Lorg.blah.New;", mOuter.renameTypeDesc("[[Lcom.pack.Old;"));
        assertEquals("[Lorg.blah.New;",  mInner.renameTypeDesc("[Lcom.pack.Old;"));
        assertEquals("[[Lorg.blah.New;", mInner.renameTypeDesc("[[Lcom.pack.Old;"));
    }
    @Test
    public void testRenameType() {
    }
    @Test
    public void testRenameInternalType() {
        assertEquals("Lorg.blah.New;", mOuter.renameInternalType("Lcom.pack.Old;"));
        assertEquals("Lorg.blah.New$Inner;", mOuter.renameInternalType("Lcom.pack.Old$Inner;"));
        assertEquals("org.blah.New", mOuter.renameInternalType("com.pack.Old"));
        assertEquals("org.blah.New$Inner", mOuter.renameInternalType("com.pack.Old$Inner"));
        assertEquals("org.blah.New$Other", mInner.renameInternalType("com.pack.Old$Other"));
        assertEquals("org.blah.New$Other", mInner.renameInternalType("com.pack.Old$Other"));
    }
    @Test
    public void testRenameMethodDesc() {
        assertEquals("(IDLorg.blah.New;[Lorg.blah.New$Inner;)Lorg.blah.New$Other;",
               mOuter.renameMethodDesc("(IDLcom.pack.Old;[Lcom.pack.Old$Inner;)Lcom.pack.Old$Other;"));
    }
}
