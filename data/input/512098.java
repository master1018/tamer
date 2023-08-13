public abstract class ConvertVisibilityTest extends AbstractConvertTest {
    @Test
    public void testVisibilityMethods1() throws IOException {
        CompilationUnit src = new CompilationUnit("a.A", 
                "package a;" +
                "public class A{" +
                "   public void foo1(){}" +
                "   protected void foo2(){}" +
                "   void foo3(){}" +
                "   private void foo4(){}" +
                "}");
        IApi api = convert(Visibility.PUBLIC, src);
        IPackage p = ModelUtil.getPackage(api, "a");
        IClassDefinition c = ModelUtil.getClass(p, "A");
        Set<IMethod> methods = c.getMethods();
        assertEquals(1, methods.size());
    }
    @Test
    public void testVisibilityMethods2() throws IOException {
        CompilationUnit src = new CompilationUnit("a.A", 
                "package a;" +
                "public class A{" +
                "   public void foo1(){}" +
                "   protected void foo2(){}" +
                "   void foo3(){}" +
                "   private void foo4(){}" +
                "}");
        IApi api = convert(Visibility.PROTECTED, src);
        IPackage p = ModelUtil.getPackage(api, "a");
        IClassDefinition c = ModelUtil.getClass(p, "A");
        Set<IMethod> methods = c.getMethods();
        assertEquals(2, methods.size());
    }
    @Test
    public void testVisibilityMethods3() throws IOException {
        CompilationUnit src = new CompilationUnit("a.A", 
                "package a;" +
                "public class A{" +
                "   public void foo1(){}" +
                "   protected void foo2(){}" +
                "   void foo3(){}" +
                "   private void foo4(){}" +
                "}");
        IApi api = convert(Visibility.PACKAGE, src);
        IPackage p = ModelUtil.getPackage(api, "a");
        IClassDefinition c = ModelUtil.getClass(p, "A");
        Set<IMethod> methods = c.getMethods();
        assertEquals(3, methods.size());
    }
    @Test
    public void testVisibilityMethods4() throws IOException {
        CompilationUnit src = new CompilationUnit("a.A", 
                "package a;" +
                "public class A{" +
                "   public void foo1(){}" +
                "   protected void foo2(){}" +
                "   void foo3(){}" +
                "   private void foo4(){}" +
                "}");
        IApi api = convert(Visibility.PRIVATE, src);
        IPackage p = ModelUtil.getPackage(api, "a");
        IClassDefinition c = ModelUtil.getClass(p, "A");
        Set<IMethod> methods = c.getMethods();
        assertEquals(4, methods.size());
    }
    @Test
    public void testVisibility1() throws IOException {
        CompilationUnit src = new CompilationUnit("a.X1", 
                "package a;" +
                "public class X1{" +
                "   static class X2{}" +
                "   protected static class X3{}" +
                "   private static class X4{}" +
                "}");
        IApi api = convert(Visibility.PUBLIC, src);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(1, sigPackage.getClasses().size());
    }
    @Test
    public void testVisibility2() throws IOException {
        CompilationUnit src = new CompilationUnit("a.X1", 
                "package a;" +
                "public class X1{" +
                "   static class X2{}" +
                "   protected static class X3{}" +
                "   private static class X4{}" +
                "}");
        IApi api = convert(Visibility.PROTECTED, src);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(2, sigPackage.getClasses().size());
        assertNotNull(ModelUtil.getClass(sigPackage, "X1.X3"));
    }
    @Test
    public void testVisibility3() throws IOException {
        CompilationUnit src = new CompilationUnit("a.X1", 
                "package a;" +
                "public class X1{" +
                "   static class X2{}" +
                "   protected static class X3{}" +
                "   private static class X4{}" +
                "}");
        IApi api = convert(Visibility.PACKAGE, src);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(3, sigPackage.getClasses().size());
        assertNotNull(ModelUtil.getClass(sigPackage, "X1.X2"));
    }
    @Test
    public void testVisibility4() throws IOException {
        CompilationUnit src = new CompilationUnit("a.X1", 
                "package a;" +
                "public class X1{" +
                "   static class X2{}" +
                "   protected static class X3{}" +
                "   private static class X4{}" +
                "}");
        IApi api = convert(Visibility.PRIVATE, src);
        IPackage sigPackage = ModelUtil.getPackage(api, "a");
        assertEquals(4, sigPackage.getClasses().size());
        assertNotNull(ModelUtil.getClass(sigPackage, "X1.X4"));
    }
}