public class GetPackage {
    public static void main(String arg[]) throws Exception {
        TestClassLoader parent = new TestClassLoader();
        TestClassLoader child = new TestClassLoader(parent);
        child.defineEmptyPackage("foo");
        parent.defineEmptyPackage("foo");
        if (!child.testPackageView("foo"))
            throw new Exception("Inconsistent packages view");
    }
}
class TestClassLoader extends ClassLoader {
    public TestClassLoader() {
        super();
    }
    public TestClassLoader(ClassLoader parent) {
        super(parent);
    }
    public Package defineEmptyPackage(String name) {
        return definePackage(name, null, null, null, null, null, null, null);
    }
    public boolean testPackageView(String name) {
        Package[] pkgs = getPackages();
        Package pkg = getPackage(name);
        for(int i = 0; i < pkgs.length; i++)
            if (pkgs[i].getName().equals(name) && pkgs[i] == pkg)
                return true;
        return false;
    }
}
