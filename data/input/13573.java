public class StaticMethodAnnotations {
    static double d;
    public void foo() {
        return;
    }
    @MySimple("value") @MyMarker
    @AnnotMarker @AnnotSimple("foo")
    @AnnotMarker2 @AnnotSimple2("bar")
    public static void baz() {
    }
    private double bar(int baz) {
        @AnnotShangri_la
        int local = 0;
        return (double) baz;
    }
    static class NestedClass {
        protected int field;
    }
}
