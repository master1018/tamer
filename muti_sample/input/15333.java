public class IsAnnotationType {
    interface AnnotationPoseur extends Annotation {
    }
    static int test(Class clazz, boolean expected) {
        int status = (clazz.isAnnotation() == expected)?0:1;
        if (status == 1) {
            System.err.println("Unexpected annotation status for " + clazz);
        }
        return status;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += test(String.class, false);
        failures += test(Enum.class, false);
        failures += test(java.math.RoundingMode.class, false);
        failures += test(Annotation.class, false);
        failures += test(Retention.class, true);
        failures += test(RetentionPolicy.class, false);
        failures += test(Target.class, true);
        failures += test(AnnotationPoseur.class, false);
        if (failures > 0) {
            throw new RuntimeException("Unexepcted annotation " +
                                       "status detected.");
        }
    }
}
