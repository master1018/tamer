public class WhiteSpaceHostTest {
    public static void main(String args[]) throws Exception {
        String hosts = "        localhost;localhost; localhost;localhost1; localhost1; bogus.mil;\u0010localhost";
        StringTokenizer tokenizer = new StringTokenizer(hosts, ";");
        while (tokenizer.hasMoreTokens()) {
            String hostname = tokenizer.nextToken();
            InetAddress ia;
            try {
                ia = InetAddress.getByName(hostname);
            } catch (UnknownHostException e) {
                continue;
            }
            if (ia.isAnyLocalAddress()) {
                throw new Exception("Bogus hostname lookup returned any local address");
            }
        }
    }
}
