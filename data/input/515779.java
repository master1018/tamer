public class AsmAnalyzerTest {
    private MockLog mLog;
    private ArrayList<String> mOsJarPath;
    private AsmAnalyzer mAa;
    @Before
    public void setUp() throws Exception {
        mLog = new LogTest.MockLog();
        URL url = this.getClass().getClassLoader().getResource("data/mock_android.jar");
        mOsJarPath = new ArrayList<String>();
        mOsJarPath.add(url.getFile());
        mAa = new AsmAnalyzer(mLog, mOsJarPath, null ,
                null , null  );
    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testParseZip() throws IOException {
        Map<String, ClassReader> map = mAa.parseZip(mOsJarPath);
        assertArrayEquals(new String[] {
                "mock_android.dummy.InnerTest",
                "mock_android.dummy.InnerTest$DerivingClass",
                "mock_android.dummy.InnerTest$MyGenerics1",
                "mock_android.dummy.InnerTest$MyIntEnum",
                "mock_android.dummy.InnerTest$MyStaticInnerClass",   
                "mock_android.dummy.InnerTest$NotStaticInner1", 
                "mock_android.dummy.InnerTest$NotStaticInner2",  
                "mock_android.view.View",
                "mock_android.view.ViewGroup",
                "mock_android.view.ViewGroup$LayoutParams",
                "mock_android.view.ViewGroup$MarginLayoutParams",
                "mock_android.widget.LinearLayout",
                "mock_android.widget.LinearLayout$LayoutParams",
                "mock_android.widget.TableLayout",
                "mock_android.widget.TableLayout$LayoutParams"
            },
            map.keySet().toArray());
    }
    @Test
    public void testFindClass() throws IOException, LogAbortException {
        Map<String, ClassReader> zipClasses = mAa.parseZip(mOsJarPath);
        TreeMap<String, ClassReader> found = new TreeMap<String, ClassReader>();
        ClassReader cr = mAa.findClass("mock_android.view.ViewGroup$LayoutParams",
                zipClasses, found);
        assertNotNull(cr);
        assertEquals("mock_android/view/ViewGroup$LayoutParams", cr.getClassName());
        assertArrayEquals(new String[] { "mock_android.view.ViewGroup$LayoutParams" },
                found.keySet().toArray());
        assertArrayEquals(new ClassReader[] { cr }, found.values().toArray());
    }
    @Test
    public void testFindGlobs() throws IOException, LogAbortException {
        Map<String, ClassReader> zipClasses = mAa.parseZip(mOsJarPath);
        TreeMap<String, ClassReader> found = new TreeMap<String, ClassReader>();
        found.clear();
        mAa.findGlobs("mock_android.view", zipClasses, found);
        assertArrayEquals(new String[] { },
            found.keySet().toArray());
        mAa.findGlobs("mock_android.*.*Group$*Layout*", zipClasses, found);
        assertArrayEquals(new String[] {
                "mock_android.view.ViewGroup$LayoutParams",
                "mock_android.view.ViewGroup$MarginLayoutParams"
            },
            found.keySet().toArray());
        mAa.findGlobs("mock_android.**Group*", zipClasses, found);
        assertArrayEquals(new String[] {
                "mock_android.view.ViewGroup",
                "mock_android.view.ViewGroup$LayoutParams",
                "mock_android.view.ViewGroup$MarginLayoutParams"
            },
            found.keySet().toArray());
        found.clear();
        mAa.findGlobs("mock_android.view.View", zipClasses, found);
        assertArrayEquals(new String[] {
                "mock_android.view.View"
            },
            found.keySet().toArray());
        found.clear();
        mAa.findGlobs("mock_android.view.*", zipClasses, found);
        assertArrayEquals(new String[] {
                "mock_android.view.View",
                "mock_android.view.ViewGroup",
                "mock_android.view.ViewGroup$LayoutParams",
                "mock_android.view.ViewGroup$MarginLayoutParams"
            },
            found.keySet().toArray());
        for (String key : found.keySet()) {
            ClassReader value = found.get(key);
            assertNotNull("No value for " + key, value);
            assertEquals(key, AsmAnalyzer.classReaderToClassName(value));
        }
    }
    @Test
    public void testFindClassesDerivingFrom() throws LogAbortException, IOException {
        Map<String, ClassReader> zipClasses = mAa.parseZip(mOsJarPath);
        TreeMap<String, ClassReader> found = new TreeMap<String, ClassReader>();
        mAa.findClassesDerivingFrom("mock_android.view.View", zipClasses, found);
        assertArrayEquals(new String[] {
                "mock_android.view.View",
                "mock_android.view.ViewGroup",
                "mock_android.widget.LinearLayout",
                "mock_android.widget.TableLayout",
            },
            found.keySet().toArray());
        for (String key : found.keySet()) {
            ClassReader value = found.get(key);
            assertNotNull("No value for " + key, value);
            assertEquals(key, AsmAnalyzer.classReaderToClassName(value));
        }
    }
    @Test
    public void testDependencyVisitor() throws IOException, LogAbortException {
        Map<String, ClassReader> zipClasses = mAa.parseZip(mOsJarPath);
        TreeMap<String, ClassReader> keep = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> new_keep = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> in_deps = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> out_deps = new TreeMap<String, ClassReader>();
        ClassReader cr = mAa.findClass("mock_android.widget.TableLayout", zipClasses, keep);
        DependencyVisitor visitor = mAa.getVisitor(zipClasses, keep, new_keep, in_deps, out_deps);
        cr.accept(visitor, 0 );
        assertArrayEquals(new String[] {
                "mock_android.view.ViewGroup",
                "mock_android.widget.TableLayout$LayoutParams",
            },
            out_deps.keySet().toArray());
        in_deps.putAll(out_deps);
        out_deps.clear();
        for (ClassReader cr2 : in_deps.values()) {
            cr2.accept(visitor, 0 );
        }
        assertArrayEquals(new String[] {
                "mock_android.view.View",
                "mock_android.view.ViewGroup$LayoutParams",
                "mock_android.view.ViewGroup$MarginLayoutParams",
            },
            out_deps.keySet().toArray());
        in_deps.putAll(out_deps);
        out_deps.clear();
        for (ClassReader cr2 : in_deps.values()) {
            cr2.accept(visitor, 0 );
        }
        assertArrayEquals(new String[] { }, out_deps.keySet().toArray());
    }
}
