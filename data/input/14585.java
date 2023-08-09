@Rat public class RecursiveAnnotation {
    public static void main(String[] args) {
        if (!RecursiveAnnotation.class.isAnnotationPresent(Rat.class))
            throw new RuntimeException("RecursiveAnnotation");
        if (!Rat.class.isAnnotationPresent(Rat.class))
            throw new RuntimeException("Rat");
    }
}
@Retention(RUNTIME) @Rat @interface Rat { }
