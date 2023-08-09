public class GetClassLoader
{
    public static void main(String[] args) {
        System.err.println("\nTest for rfe 4240710\n");
        URL codebase1 = null;
        Class cl = null;
        ClassLoader loader = null;
        TestLibrary.suggestSecurityManager(null);
        try {
            codebase1 =
                TestLibrary.installClassInCodebase("Foo", "codebase1");
        } catch (MalformedURLException e) {
            TestLibrary.bomb(e);
        }
        try {
            System.err.println(
                "getClassLoader for codebase that I can't read");
            loader = RMIClassLoader.getClassLoader("file:/foo");
            TestLibrary.bomb(
                "Failed: no SecurityException obtaining loader");
        } catch (MalformedURLException e) {
            System.err.println(
                "Failed: MalformedURLException getting loader");
            TestLibrary.bomb(e);
        } catch (SecurityException e) {
            System.err.println(
                "Passed: SecurityException obtaining loader");
        }
        System.err.println("load Foo from codebase1");
        try {
            cl = RMIClassLoader.loadClass(codebase1.toString(), "Foo");
        } catch (Exception e) {
            System.err.println(
                "Failed: exception loading class from codebase1");
            TestLibrary.bomb(e);
        }
        System.err.println(
            "load Foo using loader obtained using getClassLoader");
        try {
            loader = RMIClassLoader.getClassLoader(codebase1.toString());
        } catch (MalformedURLException e) {
            System.err.println(
                "Failed: MalformedURLException getting codebase1 loader");
            TestLibrary.bomb(e);
        }
        try {
            if (cl == loader.loadClass("Foo")) {
                System.err.println("Passed: Foo classes are equal");
            } else {
                TestLibrary.bomb("Failed: Foo classes are not equal");
            }
        } catch (Exception e) {
            System.err.println(
                "Failed: exception loading class from codebase1");
            TestLibrary.bomb(e);
        }
        try {
            loader = RMIClassLoader.getClassLoader("malformed:
            TestLibrary.bomb(
                "Failed: getClassLoader should throw MalformedURLException");
        } catch (MalformedURLException e) {
            System.err.println(
                "Passed: getClassLoader threw MalformedURLException");
        }
    }
}
