public class TestParameterAnnotations {
    class Inner {
        public Inner(@Marker Object o) {}
    }
    static class StaticNested {
        public StaticNested(@Marker Object o) {}
    }
    static int visitCtorParameterAnnotations(Class clazz) {
        int errors = 0;
        for(Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            try {
                System.out.printf("%nNormal:  %s%nGeneric: %s%n",
                                  ctor.toString(),
                                  ctor.toGenericString());
                Annotation[][] annotationArray = ctor.getParameterAnnotations();
                System.out.println("\tParameter Annotations: " +
                                   Arrays.deepToString(annotationArray));
            } catch (AnnotationFormatError afe) {
                System.err.println("\tWhoops, got an AnnotationFormatError on " +
                                   ctor.toGenericString());
                errors++;
            }
        }
        return errors;
    }
    public static void main(String... argv) {
        int errors = 0;
        class LocalClass {
            LocalClass(@Marker int i){}
        }
        Object anonymous = new Object() {public String toString(){return "Anonymous";}};
        errors +=
            visitCtorParameterAnnotations(Inner.class);
        errors +=
            visitCtorParameterAnnotations(StaticNested.class);
        errors +=
            visitCtorParameterAnnotations(CustomColors.class);
        errors +=
            visitCtorParameterAnnotations(TestParameterAnnotations.class);
        errors +=
            visitCtorParameterAnnotations(LocalClass.class);
        errors +=
            visitCtorParameterAnnotations(anonymous.getClass());
        if (errors > 0)
            throw new RuntimeException(errors +
                                       " failures calling Constructor.getParameterAnnotations");
    }
}
enum CustomColors {
    FUCHSIA(5),
    MULBERRY(6.0d);
    CustomColors(@Marker int arg) {}
    CustomColors(double arg) {}
}
@Retention(RUNTIME)
    @interface Marker {}
