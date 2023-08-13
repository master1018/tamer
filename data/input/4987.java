public class Unresolved {
    public static void main(String[] args) throws Exception {
        InetSocketAddress remAddr =  InetSocketAddress.createUnresolved( "foo.bar", 161  );
        try {
            DatagramPacket packet1 = new DatagramPacket(  "Hellooo!".getBytes(), "Hellooo!".length(), remAddr  );
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new RuntimeException("Setting an unresolved address in a DatagramPacket shouldn't be allowed");
    }
}
