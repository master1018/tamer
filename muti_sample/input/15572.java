public class PrintWrappedException {
    public static void main(String[] args) throws Exception {
        try {
            FileInputStream fis =
                (FileInputStream) AccessController.doPrivileged(
                                    new PrivilegedExceptionAction() {
                    public Object run() throws FileNotFoundException {
                        return new FileInputStream("someFile");
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            String msg = e.toString();
            if (msg.indexOf("FileNotFoundException") == -1) {
                throw new Exception("Wrapped exception not printed");
            }
        }
    }
}
