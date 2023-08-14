public class LayoutParamsParserTest extends TestCase {
    private static final String MOCK_DATA_PATH =
        "com/android/ide/eclipse/testdata/mock_attrs.xml"; 
    private static class MockFrameworkClassLoader extends AndroidJarLoader {
        MockFrameworkClassLoader() {
            super(null );
        }
        @Override
        public HashMap<String, ArrayList<IClassDescriptor>> findClassesDerivingFrom(
                String rootPackage, String[] superClasses) throws ClassFormatError {
            return new HashMap<String, ArrayList<IClassDescriptor>>();
        }
    }
    private static class MockLayoutParamsParser extends LayoutParamsParser {
        public MockLayoutParamsParser() {
            super(new MockFrameworkClassLoader(),
                  new AttrsXmlParser(
                          AdtTestData.getInstance().getTestFilePath(MOCK_DATA_PATH)).preload());
            mTopViewClass = new ClassWrapper(mock_android.view.View.class);
            mTopGroupClass = new ClassWrapper(mock_android.view.ViewGroup.class);
            mTopLayoutParamsClass = new ClassWrapper(mock_android.view.ViewGroup.LayoutParams.class);
            mViewList = new ArrayList<IClassDescriptor>();
            mGroupList = new ArrayList<IClassDescriptor>();
            mViewMap = new TreeMap<String, ExtViewClassInfo>();
            mGroupMap = new TreeMap<String, ExtViewClassInfo>();
            mLayoutParamsMap = new HashMap<String, LayoutParamsInfo>();
        }
    }
    private MockLayoutParamsParser mParser;
    @Override
    public void setUp() throws Exception {
        mParser = new MockLayoutParamsParser();
    }
    @Override
    public void tearDown() throws Exception {
    }
    public final void testFindLayoutParams() throws Exception {
        assertEquals(mock_android.view.ViewGroup.LayoutParams.class,
            ((ClassWrapper)_findLayoutParams(mock_android.view.ViewGroup.class)).wrappedClass());
        assertEquals(mock_android.widget.LinearLayout.LayoutParams.class,
            ((ClassWrapper)_findLayoutParams(mock_android.widget.LinearLayout.class)).wrappedClass());
        assertEquals(mock_android.widget.TableLayout.LayoutParams.class,
            ((ClassWrapper)_findLayoutParams(mock_android.widget.TableLayout.class)).wrappedClass());
    }
    public final void testGetLayoutParamsInfo() throws Exception {
        LayoutParamsInfo info1 = _getLayoutParamsInfo(
                mock_android.view.ViewGroup.LayoutParams.class);
        assertNotNull(info1);
        assertNull(info1.getSuperClass());
        LayoutParamsInfo info2 = _getLayoutParamsInfo(
                mock_android.widget.LinearLayout.LayoutParams.class);
        assertNotNull(info2);
        assertSame(info1, info2.getSuperClass());
        LayoutParamsInfo info3 = _getLayoutParamsInfo(
                mock_android.widget.TableLayout.LayoutParams.class);
        assertNotNull(info3);
        assertNotSame(info1, info3.getSuperClass());
        assertNotSame(info2, info3.getSuperClass());
        assertSame(info1, info3.getSuperClass().getSuperClass());
    }
    public final void testGetLayoutClasses() throws Exception {
    }
    @SuppressWarnings("unused")
    private AndroidTargetParser _Constructor(String osJarPath) throws Exception {
        Constructor<AndroidTargetParser> constructor =
            AndroidTargetParser.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(osJarPath);
    }
    @SuppressWarnings("unused")
    private void _getLayoutClasses() throws Exception {
        Method method = AndroidTargetParser.class.getDeclaredMethod("getLayoutClasses");  
        method.setAccessible(true);
        method.invoke(mParser);
    }
    @SuppressWarnings("unused")
    private ViewClassInfo _addGroup(Class<?> groupClass) throws Exception {
        Method method = LayoutParamsParser.class.getDeclaredMethod("addGroup",  
                IClassDescriptor.class);
        method.setAccessible(true);
        return (ViewClassInfo) method.invoke(mParser, new ClassWrapper(groupClass));
    }
    @SuppressWarnings("unused")
    private LayoutParamsInfo _addLayoutParams(Class<?> groupClass) throws Exception {
        Method method = LayoutParamsParser.class.getDeclaredMethod("addLayoutParams",   
                IClassDescriptor.class);
        method.setAccessible(true);
        return (LayoutParamsInfo) method.invoke(mParser, new ClassWrapper(groupClass));
    }
    private LayoutParamsInfo _getLayoutParamsInfo(Class<?> layoutParamsClass) throws Exception {
        Method method = LayoutParamsParser.class.getDeclaredMethod("getLayoutParamsInfo",   
                IClassDescriptor.class);
        method.setAccessible(true);
        return (LayoutParamsInfo) method.invoke(mParser, new ClassWrapper(layoutParamsClass));
    }
    private IClassDescriptor _findLayoutParams(Class<?> groupClass) throws Exception {
        Method method = LayoutParamsParser.class.getDeclaredMethod("findLayoutParams",  
                IClassDescriptor.class);
        method.setAccessible(true);
        return (IClassDescriptor) method.invoke(mParser, new ClassWrapper(groupClass));
    }
}
