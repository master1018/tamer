public class InvalidNameWithSlash {
    public static void main(String[] args) throws Exception {
        boolean exceptionOccurred = false;
        try {
            Class c = Class.forName("java/lang.Object");
        } catch (Exception e) {
            exceptionOccurred = true;
        }
        if (!exceptionOccurred) {
            throw new Exception("forName accepting names with slashes?");
        }
    }
}
