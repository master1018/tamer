public class ExceptionHidingLoader extends ClassLoader {
    protected Class findClass(String name) throws ClassNotFoundException {
        return null;
    }
    public static void main(String[] args) throws Exception {
        boolean exception = false;
        try {
            Class.forName("aha", false, new ExceptionHidingLoader());
        } catch (ClassNotFoundException e) {
            exception = true;
        }
        if (!exception) {
            throw new Exception("Bogus loader behavior not being corrected");
        }
    }
}
