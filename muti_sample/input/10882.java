public class MethodAnnotations {
    static double d;
    @MySimple("value") @MyMarker
    @AnnotMarker @AnnotSimple("foo")
    @AnnotMarker2 @AnnotSimple2("bar")
    public void foo() {
        return;
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
