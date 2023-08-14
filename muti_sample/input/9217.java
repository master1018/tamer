public class getAnnotationTest {
    public static void main (String[] args) throws Throwable {
        Class c = Class.forName("java.lang.annotation.Retention");
        Annotation result  = c.getAnnotation(Retention.class);
        Class meta_c = c.getClass();
        Method meta_getAnnotation = meta_c.getMethod("getAnnotation",
                                                     (Retention.class).getClass());
        Object meta_result = meta_getAnnotation.invoke(c, Retention.class);
        if (!meta_result.equals(result)) {
            throw new RuntimeException("Base and meta results are not equal.");
        }
        meta_getAnnotation.getGenericExceptionTypes();
        meta_getAnnotation.getGenericParameterTypes();
        meta_getAnnotation.getGenericReturnType();
    }
}
