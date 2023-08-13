public class MissingTest {
    private static void testAnnotation(AnnotatedElement element,
                                boolean exceptionExpected) {
        java.lang.annotation.Annotation[] annotations;
        try {
            annotations = element.getAnnotations();
            if (exceptionExpected) {
                System.err.println("Error: Did not get an exception reading annotations on "
                                   + element);
                System.err.println("Annotations found: "
                                   + java.util.Arrays.toString(annotations));
                throw new RuntimeException();
            }
            if (annotations.length == 0) {
                System.err.println("Error: no annotations found on " + element);
                throw new RuntimeException();
            }
        } catch (Throwable t) {
            if (!exceptionExpected) {
                System.err.println("Error: Got an unexpected exception reading annotations on "
                                   + element);
                throw new RuntimeException(t);
            }
        }
    }
    private static void testParameterAnnotation(Method m,
                                                boolean exceptionExpected) {
        java.lang.annotation.Annotation[][] annotationsArray;
        try {
            annotationsArray = m.getParameterAnnotations();
            if (exceptionExpected) {
                System.err.println("Error: Did not get an exception reading annotations on method"
                                   + m);
                System.err.println("Annotations found: "
                                   + java.util.Arrays.toString(annotationsArray));
                throw new RuntimeException();
            }
            if (annotationsArray.length == 0 ) {
                System.err.println("Error: no parameters for " + m);
                throw new RuntimeException();
            } else {
                java.lang.annotation.Annotation[] annotations = annotationsArray[0];
                if (annotations.length == 0) {
                    System.err.println("Error: no annotations on " + m);
                    throw new RuntimeException();
                }
            }
        } catch (Throwable t) {
            if (!exceptionExpected) {
                System.err.println("Error: Got an unexpected exception reading annotations on "
                                   + m);
                throw new RuntimeException(t);
            }
        }
    }
    public static void main(String argv[]) throws Exception {
        testAnnotation(A.class, false);
        testAnnotation(B.class, true);
        testParameterAnnotation(C.class.getDeclaredMethod("method1", Object.class),
                                false);
        testParameterAnnotation(D.class.getDeclaredMethod("method1", Object.class),
                                true);
    }
}
