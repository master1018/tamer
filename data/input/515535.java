public class AsmGeneratorTest {
    private MockLog mLog;
    private ArrayList<String> mOsJarPath;
    private String mOsDestJar;
    private File mTempFile;
    @Before
    public void setUp() throws Exception {
        mLog = new LogTest.MockLog();
        URL url = this.getClass().getClassLoader().getResource("data/mock_android.jar");
        mOsJarPath = new ArrayList<String>();
        mOsJarPath.add(url.getFile());
        mTempFile = File.createTempFile("mock", "jar");
        mOsDestJar = mTempFile.getAbsolutePath();
        mTempFile.deleteOnExit();
    }
    @After
    public void tearDown() throws Exception {
        if (mTempFile != null) {
            mTempFile.delete();
            mTempFile = null;
        }
    }
    @Test
    public void testClassRenaming() throws IOException, LogAbortException {
        AsmGenerator agen = new AsmGenerator(mLog, mOsDestJar,
            null, 
            null,  
            new String[] {  
                "mock_android.view.View", "mock_android.view._Original_View",
                "not.an.actual.ClassName", "anoter.fake.NewClassName",
            },
            null 
            );
        AsmAnalyzer aa = new AsmAnalyzer(mLog, mOsJarPath, agen,
                null,                 
                new String[] {        
                    "**"
                });
        aa.analyze();
        agen.generate();
        Set<String> notRenamed = agen.getClassesNotRenamed();
        assertArrayEquals(new String[] { "not/an/actual/ClassName" }, notRenamed.toArray());
    }
}
