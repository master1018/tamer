public abstract class ConvertPackageTest extends AbstractConvertTest {
    @Test
    public void testPackageAnnotation() throws IOException {
        CompilationUnit packageSrc = new CompilationUnit("a.package-info", "@Deprecated package a;");
        CompilationUnit classSrc = new CompilationUnit("a.X", "package a; public class X{}");
        IApi api = convert(classSrc, packageSrc);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(1, sigPackage.getAnnotations().size());
        IAnnotation annotation = sigPackage.getAnnotations().iterator().next();        
        assertEquals("java.lang.Deprecated", annotation.getType().getClassDefinition().getQualifiedName());
    }
    @Test
    public void testNumberOfClasses1() throws IOException {
        CompilationUnit packageSrc = new CompilationUnit("a.package-info", "@Deprecated package a;");
        CompilationUnit classSrc = new CompilationUnit("a.X", "package a; public class X{}");
        IApi api = convert(classSrc, packageSrc);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(1, sigPackage.getClasses().size());
    }
    @Test
    public void testNumberOfClasses2() throws IOException {
        CompilationUnit src = new CompilationUnit("a.X",
                "package a;" +
                "public class X{" +
                "   public static class Y{}" +
                "}");
        IApi api = convert(src);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(2, sigPackage.getClasses().size());
    }
}