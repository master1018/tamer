public class DefaultProperty {
    public static void main(String[] args) throws Exception {
        ServiceConfiguration.installServiceConfigurationFile();
        System.setProperty(
            "java.rmi.server.RMIClassLoaderSpi", "default");
        String classname = "Foo";
        URL codebaseURL = null;
        try {
            codebaseURL = TestLibrary.installClassInCodebase(
                classname, "remote_codebase");
        } catch (MalformedURLException e) {
            TestLibrary.bomb(e);
        }
        TestLibrary.suggestSecurityManager(null);
        Class fooClass = RMIClassLoader.loadClass(codebaseURL, classname);
        if (!fooClass.getName().equals(classname)) {
            throw new RuntimeException(
                "wrong class name, expected: " + classname +
                ", received: " + fooClass.getName());
        }
        String annotation = RMIClassLoader.getClassAnnotation(fooClass);
        if (!annotation.equals(codebaseURL.toString())) {
            throw new RuntimeException(
                "wrong class annotation, expected: " + codebaseURL.toString() +
                ", received: " + annotation);
        }
        System.err.println("TEST PASSED");
    }
}
