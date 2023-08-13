public class NoAction {
    public static void main(String[] args) throws Exception {
        SocketPermission sp = null;
        try {
            sp = new SocketPermission("localhost", "");
            throw new Exception("Expected IllegalArgumentException not "
                                + "thrown");
        } catch (IllegalArgumentException iae) {
            System.out.println("Expected IllegalArgumentException thrown");
        }
        try {
            sp = new SocketPermission("localhost", null);
            throw new Exception("Expected NullPointerException not "
                                + "thrown");
        } catch (NullPointerException npe) {
            System.out.println("Expected NullPointerException thrown");
        }
    }
}
