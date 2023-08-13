public abstract class AnnotationCompareTest extends AbstractComparatorTest {
    @Test
    public void testAnnotationValue() throws IOException{
        CompilationUnit A0 = new CompilationUnit("a.A0", 
                "package a; " +
                "public @interface A0 {" +
                "  A1 value() default @A1;" + 
                "}");
        CompilationUnit A1 = new CompilationUnit("a.A1", 
                "package a; " +
                "public @interface A1 {" +
                "}");
         CompilationUnit AnnotBDefault = new CompilationUnit("a.B", 
                    "package a; " +
                    "@A0 " +
                    "public class B {}");
         CompilationUnit AnnotB = new CompilationUnit("a.B", 
                    "package a; " +
                    "@A0 " +
                    "public class B {}");
          IApi fromApi = convert(A0, A1, AnnotBDefault);
          IApi toApi = convert(A0, A1, AnnotB);
          assertNull(compare(fromApi, toApi));
    }
       @Test
        public void testDefaultAnnotationValue() throws IOException{
             CompilationUnit A0 = new CompilationUnit("a.A0", 
                    "package a; " +
                    "public @interface A0 {" +
                    "  String value() default \"bla\";" +
                    "}");
             CompilationUnit A1 = new CompilationUnit("a.A0", 
                    "package a; " +
                    "public @interface A0 {" +
                    "  String value();" + 
                    "}");
              IApi fromApi = convert(A0);
              IApi toApi = convert(A1);
              assertNotNull(compare(fromApi, toApi));
        }
}
