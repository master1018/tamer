@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllDexTests.ClassTest.class,
    AllDexTests.PackageTest.class,
    AllDexTests.AnnotationTest.class,
    AllDexTests.VisibilityTest.class,
    AllDexTests.WildcardTest.class,
    AllDexTests.EnumTest.class
})
public class AllDexTests {
    private static ITestSourceConverter newConverter(){
        return new DexTestConverter();
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
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class EnumTest extends ConvertEnumTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
    public static class ParameterizedTypeTest extends ConvertParameterizedTypeTest {
        @Override public ITestSourceConverter createConverter() {
            return newConverter();
        }
    }
}
