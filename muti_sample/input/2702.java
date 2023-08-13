public class NestedClassAnnotations {
    static double d;
    public void foo() {
        return;
    }
    public static void baz() {
    }
    private double bar(int baz) {
        @AnnotShangri_la
        int local = 0;
        return (double) baz;
    }
    @MySimple("value") @MyMarker
    @AnnotMarker @AnnotSimple("foo")
    @AnnotMarker2 @AnnotSimple2("bar")
    static class NestedClass {
        protected int field;
    }
}
