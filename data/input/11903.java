public class StackTraceTest {
    public static void main(String[] args) {
        try {
            URL url = new URL("http:
            URLConnection uc = url.openConnection();
            System.out.println("key = "+uc.getHeaderFieldKey(20));
            uc.getInputStream();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (!(ioe instanceof ConnectException)) {
                throw new RuntimeException("Expect ConnectException, got "+ioe);
            }
            if (ioe.getMessage() == null) {
                throw new RuntimeException("Exception message is null");
            }
            if (ioe.getCause() == null) {
                throw new RuntimeException("Excepting a chained exception, but got: ", ioe);
            }
        }
    }
}
