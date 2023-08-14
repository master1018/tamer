public class JoinGroup {
    public static void main(String args[]) throws Exception  {
        MulticastSocket soc = null;
        InetAddress sin = null;
        soc = new MulticastSocket();
        sin = InetAddress.getByName("224.80.80.80");
        soc.joinGroup(sin);
        soc.leaveGroup(sin);
    }
}
