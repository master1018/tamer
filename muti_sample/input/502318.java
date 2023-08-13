public class EclipseTestCollector {
    public EclipseTestCollector() {
    }
    public void addTestCases(TestSuite suite, Plugin plugin, String expectedPackage) {
        if (plugin != null) {
            Enumeration<?> entries = plugin.getBundle().findEntries("/", "*.class", true);
            while (entries.hasMoreElements()) {
                URL entry = (URL)entries.nextElement();
                String filePath = entry.getPath().replace(".class", "");
                try {
                  Class<?> testClass = getClass(filePath, expectedPackage);
                  if (isTestClass(testClass)) {
                      suite.addTestSuite(testClass);
                  }
                } 
                catch (ClassNotFoundException e) {
              }
            }
        }
    }
    protected boolean isTestClass(Class<?> testClass) {
        return TestCase.class.isAssignableFrom(testClass) &&
          Modifier.isPublic(testClass.getModifiers()) &&
          hasPublicConstructor(testClass);
    }
    protected boolean hasPublicConstructor(Class<?> testClass) {
        try {
            TestSuite.getTestConstructor(testClass);
        } catch(NoSuchMethodException e) {
            return false;
        }
        return true;
    }
    protected Class<?> getClass(String filePath, String expectedPackage) throws ClassNotFoundException {
        String dotPath = filePath.replace('/', '.');
        int index = dotPath.indexOf(expectedPackage);
        if (index == -1) {
            throw new ClassNotFoundException();
        }
        String packagePath = dotPath.substring(index);
        return Class.forName(packagePath);   
    }
}
