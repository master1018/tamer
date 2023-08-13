public class IsHostReachableBug {
    public static void main(String[] args) throws Exception{
                String hostName = "fec0::1:a00:20ff:feed:b08d";
                BufferedReader in = null;
                FileWriter fw = null;
                String inString = " ";
                if (args.length > 0)
                        hostName = args[0];
                InetAddress addr = InetAddress.getByName(hostName);
                System.out.println("InetAddress is : " + addr);
                System.out.println("Is InetAddress instance of Inet6Address ? "
+ (addr instanceof Inet6Address));
                if (!addr.isReachable(10000)){
                        System.out.println(hostName + " is not reachable");
                }else {
                        throw new RuntimeException ("IPv6 address should not be reachable");
                }
    }
}
