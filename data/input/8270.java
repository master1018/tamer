public class HashSpread {
    static Random r = new Random();
    static InetAddress randomIPv6Adress() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<8; i++) {
            if (i > 0)
                sb.append(":");
            for (int j=0; j<4; j++) {
                int v = r.nextInt(16);
                if (v < 10) {
                    sb.append(Integer.toString(v));
                } else {
                    char c = (char) ('A' + v - 10);
                    sb.append(c);
                }
            }
        }
        try {
            return InetAddress.getByName(sb.toString());
        } catch (UnknownHostException x) {
            throw new Error("Internal error in test");
        }
    }
    public static void main(String args[]) throws Exception {
        int iterations = 10000;
        if (args.length > 0) {
            iterations = Integer.parseInt(args[0]);
        }
        int MIN_SHORT = (int)Short.MIN_VALUE;
        int MAX_SHORT = (int)Short.MAX_VALUE;
        int narrow = 0;
        for (int i=0; i<iterations; i++) {
            int hc = randomIPv6Adress().hashCode();
            if (hc >= MIN_SHORT && hc <= MAX_SHORT) {
                narrow++;
            }
        }
        double percent = (double)narrow / (double)iterations * 100.0;
        if (percent > 85.0) {
            throw new RuntimeException(percent + " of hash codes were in " +
                MIN_SHORT + " to " + MAX_SHORT  + " range.");
        }
    }
}
