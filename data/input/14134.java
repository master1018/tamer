public class AnnoMirror extends Tester {
    public static void main(String[] args) {
        (new AnnoMirror()).run();
    }
    @Test(result={"AT1"})
    @AT1
    AnnotationType getAnnotationType() {
        AnnotationMirror anno = getAnno("getAnnotationType", "AT1");
        return anno.getAnnotationType();
    }
    @Test(result={})
    @AT1
    Set getElementValuesNone() {
        AnnotationMirror anno = getAnno("getElementValuesNone", "AT1");
        return anno.getElementValues().entrySet();
    }
    @Test(result={"i()=2",
                  "b()=true",
                  "k()=java.lang.Boolean.class",
                  "a()=@AT1"})
    @AT2(i = 1+1,
         b = true,
         k = Boolean.class,
         a = @AT1)
    Set getElementValues() {
        AnnotationMirror anno = getAnno("getElementValues", "AT2");
        return anno.getElementValues().entrySet();
    }
    @Test(result={"@AT1(\"zax\")",
                  "@AT2(i=2, b=true, k=java.lang.Boolean.class, a=@AT1)",
                  "@AT3(arr={1})",
                  "@AT4({2, 3, 4})"})
    Collection<AnnotationMirror> toStringTests() {
        for (MethodDeclaration m : thisClassDecl.getMethods()) {
            if (m.getSimpleName().equals("toStringTestsHelper")) {
                return m.getAnnotationMirrors();
            }
        }
        throw new AssertionError();
    }
    @AT1("zax")
    @AT2(i = 1+1,
         b = true,
         k = Boolean.class,
         a = @AT1)
    @AT3(arr={1})
    @AT4({2,3,4})
    private void toStringTestsHelper() {
    }
}
@interface AT1 {
    String value() default "";
}
@interface AT2 {
    int i();
    boolean b();
    Class k();
    AT1 a();
}
@interface AT3 {
    int[] arr();
}
@interface AT4 {
    int[] value();
}
