public class Leave {
    public static void main(String args[]) throws Exception {
        MulticastSocket socket = null;
        InetAddress mca = null;
        mca = InetAddress.getByName("224.80.80.80");
        socket = new MulticastSocket();
        socket.joinGroup(mca);
        socket.leaveGroup(mca);
        socket.close();
    }
}
