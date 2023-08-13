public class FoundType {
    private static final String TYPE = "a.halting.Problem";
    public static void main(String[] args) {
        AnnotationTypeMismatchException ex =
            new AnnotationTypeMismatchException(null, TYPE);
        if (!TYPE.equals(ex.foundType()))
            throw new Error();
    }
}
