public class JUnitLaunchConfigDelegateTest extends TestCase {
    public void testAbleToFetchJunitJar() throws IOException {
        assertTrue(JUnitLaunchConfigDelegate.getJunitJarLocation().endsWith("junit.jar"));
    }
    public void testFixBootpathExtWithAndroidJar() {
        String[][] testArray = {
                null,
                { "android.jar"},
                null,
                { "some_other_jar.jar" },
        };
        String[][] expectedArray = {
                null,
                null,
                null,
                { "some_other_jar.jar" },
        };
       assertEqualsArrays(expectedArray, JUnitLaunchConfigDelegate.fixBootpathExt(testArray));
    }
    public void testFixBootpathExtWithNoAndroidJar() {
        String[][] testArray = {
                null,
                { "somejar.jar"},
                null,
        };
        String[][] expectedArray = {
                null,
                { "somejar.jar"},
                null,
        };
        assertEqualsArrays(expectedArray, JUnitLaunchConfigDelegate.fixBootpathExt(testArray));
    }
    public void testFixClasspathWithJunitJar() throws IOException {
        String[] testArray = {
                JUnitLaunchConfigDelegate.getJunitJarLocation(),
        };
        String[] expectedArray = {
                JUnitLaunchConfigDelegate.getJunitJarLocation(),
        };
        assertEqualsArrays(expectedArray, 
                JUnitLaunchConfigDelegate.fixClasspath(testArray, "test"));
    }
    public void testFixClasspathWithoutJunitJar() throws IOException {
        String[] testArray = {
                "random.jar",
        };
        String[] expectedArray = {
                "random.jar",
                JUnitLaunchConfigDelegate.getJunitJarLocation(),
        };
        assertEqualsArrays(expectedArray, 
                JUnitLaunchConfigDelegate.fixClasspath(testArray, "test"));
    }
    public void testFixClasspathWithNoJars() throws IOException {
        String[] testArray = {
        };
        String[] expectedArray = {
                JUnitLaunchConfigDelegate.getJunitJarLocation(),
        };
        assertEqualsArrays(expectedArray, 
                JUnitLaunchConfigDelegate.fixClasspath(testArray, "test"));
    }
    private void assertEqualsArrays(String[][] a1, String[][] a2) {
        assertTrue(Arrays.deepEquals(a1, a2));        
    }
    private void assertEqualsArrays(String[] a1, String[] a2) {
        assertTrue(Arrays.deepEquals(a1, a2));        
    }
}
