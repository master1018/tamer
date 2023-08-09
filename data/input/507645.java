@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllDocletTests.ClassTest.class,
    AllDocletTests.PackageTest.class,
    AllDocletTests.AnnotationTest.class,
    AllDocletTests.VisibilityTest.class,
    AllDocletTests.WildcardTest.class,
    AllDocletTests.EnumTest.class
})
public class AllDocletTests {
    private static ITestSourceConverter newConverter(){
        return new DocletTestConverter();
    }
    public static class ClassTest extends ConvertClassTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class AnnotationTest extends ConvertAnnotationTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class PackageTest extends ConvertPackageTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class VisibilityTest extends ConvertVisibilityTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class WildcardTest extends ConvertWildcardTest {
        @Override
        public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class EnumTest extends ConvertEnumTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
}
