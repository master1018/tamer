public class Lookup {
    public static void main(String args[]) throws UnknownHostException {
        if (args[0].equals("-q=PTR")) {
            InetAddress ia = InetAddress.getByName(args[1]);
            System.out.println(ia.getHostName());
            return;
        }
        String addr;
        if (args[0].equals("-q=A")) {
            addr = args[1];
        } else {
            addr = args[0];
        }
        InetAddress ia = InetAddress.getByName(args[1]);
        System.out.println(ia.getHostAddress());
    }
}
