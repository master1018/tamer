public class ActionSpace {
    public static void main(String[]args) throws Exception {
        try {
            SocketPermission sp =
                new SocketPermission("*", "connect , accept");
        } catch (Exception e) {
            throw new Exception("should not have caught an exception");
        }
    }
}
