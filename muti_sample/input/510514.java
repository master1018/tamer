public class ProjectHelperTest extends TestCase {
    private final static String OLD_CONTAINER_ID =
        "com.android.ide.eclipse.adt.project.AndroidClasspathContainerInitializer"; 
    private final static String CONTAINER_ID =
        "com.android.ide.eclipse.adt.ANDROID_FRAMEWORK"; 
    @Override
    public void setUp() throws Exception {
    }
    @Override
    public void tearDown() throws Exception {
    }
    public final void testFixProjectClasspathEntriesFromOldContainer() throws JavaModelException {
        JavaProjectMock javaProject = new JavaProjectMock(
                new IClasspathEntry[] {
                        new ClasspathEntryMock(new Path("Project/src"), 
                                IClasspathEntry.CPE_SOURCE),
                        new ClasspathEntryMock(new Path(OLD_CONTAINER_ID),
                                IClasspathEntry.CPE_CONTAINER),
                },
                new Path("Project/bin"));
        ProjectHelper.fixProjectClasspathEntries(javaProject);
        IClasspathEntry[] fixedEntries = javaProject.getRawClasspath();
        assertEquals(3, fixedEntries.length);
        assertEquals("Project/src", fixedEntries[0].getPath().toString());
        assertEquals(OLD_CONTAINER_ID, fixedEntries[1].getPath().toString());
        assertEquals(CONTAINER_ID, fixedEntries[2].getPath().toString());
    }
}
