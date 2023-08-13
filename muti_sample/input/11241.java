public class NullName {
    public static void main(String[]args) throws Exception {
        int count = 0;
        try {
            try {
                PropertyPermission pp = new PropertyPermission(null, "read");
            } catch (NullPointerException e) {
                count++;
            }
            try {
                java.io.FilePermission fp =
                    new java.io.FilePermission(null, "read");
            } catch (NullPointerException e) {
                count++;
            }
            try {
                java.net.SocketPermission sp =
                    new java.net.SocketPermission(null, "connect");
            } catch (NullPointerException e) {
                count++;
            }
            try {
                RuntimePermission rp = new RuntimePermission(null);
            } catch (NullPointerException e) {
                count++;
            }
            try {
                UnresolvedPermission up = new UnresolvedPermission(null, "blah", "read", null);
            } catch (NullPointerException e) {
                count++;
            }
        } catch (Exception e) {
            throw new Exception("Test failed: Wrong exception thrown");
        }
        if (count != 5)
            throw new Exception("Test failed: didn't catch enough NullPointerExceptions");
    }
}
