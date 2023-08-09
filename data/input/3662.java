public class Ctor {
    public static void main(String[] args) {
        try {
            SocketPermission sp = new java.net.SocketPermission(":", "connect");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Test passed!!!");
    }
}
