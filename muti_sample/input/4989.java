public class exceptionCauseTest {
    public static void main(String args[]) {
        Throwable cause = new Throwable("because");
        Throwable par   = new Throwable(cause);
        TypeNotPresentException cnp = new TypeNotPresentException("test", par);
        try {
            throw cnp;
        } catch (TypeNotPresentException e) {
            if (par != e.getCause() )
                throw new RuntimeException("Unexpected value of cause.");
        }
    }
}
