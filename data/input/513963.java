public class AndroidJarLoaderTest extends TestCase {
    private AndroidJarLoader mFrameworkClassLoader;
    @Override
    public void setUp() throws Exception {
        String jarfilePath = AdtTestData.getInstance().getTestFilePath(
                "com/android/ide/eclipse/testdata/jar_example.jar");  
        mFrameworkClassLoader = new AndroidJarLoader(jarfilePath);
    }
    @Override
    public void tearDown() throws Exception {
        mFrameworkClassLoader = null;
        System.gc();
    }
    public final void testPreLoadClasses() throws Exception {
        mFrameworkClassLoader.preLoadClasses("jar.example.", null, null); 
        HashMap<String, Class<?>> map = getPrivateClassCache();
        assertEquals(0, map.size());
        HashMap<String,byte[]> data = getPrivateEntryCache();
        assertTrue(data.containsKey("jar.example.Class1"));                    
        assertTrue(data.containsKey("jar.example.Class2"));                    
        assertTrue(data.containsKey("jar.example.Class1$InnerStaticClass1"));  
        assertTrue(data.containsKey("jar.example.Class1$InnerClass2"));  
        assertEquals(4, data.size());
    }
    public final void testPreLoadClasses_classNotFound() throws Exception {
        mFrameworkClassLoader.preLoadClasses("not.a.package.", null, null);  
        HashMap<String, Class<?>> map = getPrivateClassCache();
        assertEquals(0, map.size());
        HashMap<String,byte[]> data = getPrivateEntryCache();
        assertEquals(0, data.size());
    }
    public final void testFindClass_classFound() throws Exception {
        Class<?> c = _findClass(mFrameworkClassLoader, "jar.example.Class2");  
        assertEquals("jar.example.Class2", c.getName());              
        HashMap<String, Class<?>> map = getPrivateClassCache();
        assertTrue(map.containsKey("jar.example.Class1"));            
        assertTrue(map.containsKey("jar.example.Class2"));            
        assertEquals(2, map.size());
    }
    private Class<?> _findClass(AndroidJarLoader jarLoader, String name) throws Exception {
        Method findClassMethod = AndroidJarLoader.class.getDeclaredMethod(
                "findClass", String.class);  
        findClassMethod.setAccessible(true);
        try {
            return (Class<?>)findClassMethod.invoke(jarLoader, name);
        }
        catch (InvocationTargetException e) {
           throw (Exception)e.getCause();
        }
    }
    public final void testFindClass_classNotFound() throws Exception {
        try {
            _findClass(mFrameworkClassLoader, "not.a.valid.ClassName");  
        } catch (ClassNotFoundException e) {
            assertEquals("not.a.valid.ClassName", e.getMessage());  
            return;
        }
        fail("Expected ClassNotFoundException not thrown");
    }
    public final void testFindClassesDerivingFrom() throws Exception {
        HashMap<String, ArrayList<IClassDescriptor>> found =
            mFrameworkClassLoader.findClassesDerivingFrom("jar.example.", new String[] {  
                "jar.example.Class1",       
                "jar.example.Class2" });    
        assertTrue(found.containsKey("jar.example.Class1"));  
        assertTrue(found.containsKey("jar.example.Class2"));  
        assertEquals(2, found.size());  
        assertEquals("jar.example.Class2",  
                found.get("jar.example.Class1").get(0).getFullClassName());  
        assertEquals(1, found.get("jar.example.Class1").size());      
        assertEquals(0, found.get("jar.example.Class2").size());      
    }
    @SuppressWarnings("unchecked")
    private HashMap<String, Class<?> > getPrivateClassCache()
            throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
        Field field = AndroidJarLoader.class.getDeclaredField("mClassCache");  
        field.setAccessible(true);
        return (HashMap<String, Class<?>>) field.get(mFrameworkClassLoader);
    }
    @SuppressWarnings("unchecked")
    private HashMap<String,byte[]> getPrivateEntryCache()
            throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
        Field field = AndroidJarLoader.class.getDeclaredField("mEntryCache");  
        field.setAccessible(true);
        return (HashMap<String, byte[]>) field.get(mFrameworkClassLoader);
    }
}
