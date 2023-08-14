public class MaxRetries {
    public static void main(String[] args)
            throws Exception {
        System.setProperty("sun.security.krb5.debug", "true");
        new OneKDC(null).writeJAASConf();
        System.setProperty("java.security.krb5.conf", "alternative-krb5.conf");
        Security.setProperty("krb5.kdc.bad.policy", "trylast");
        rewriteMaxRetries(4);
        test1(4000, 6);         
        test1(4000, 2);         
        rewriteMaxRetries(1);
        test1(1000, 3);         
        test1(1000, 2);         
        rewriteMaxRetries(-1);
        test1(5000, 4);         
        test1(5000, 2);         
        Security.setProperty("krb5.kdc.bad.policy", "tryless");
        rewriteMaxRetries(4);
        test1(4000, 7);         
        test1(4000, 4);         
        rewriteMaxRetries(1);
        test1(1000, 4);         
        test1(1000, 4);         
        rewriteMaxRetries(-1);
        test1(5000, 5);         
        test1(5000, 4);         
        rewriteUdpPrefLimit(-1, -1);    
        test2("UDP");
        rewriteUdpPrefLimit(10, -1);    
        test2("TCP");
        rewriteUdpPrefLimit(10, 10000); 
        test2("UDP");
        rewriteUdpPrefLimit(10000, 10); 
        test2("TCP");
    }
    private static void test1(int timeout, int count) throws Exception {
        String timeoutTag = "timeout=" + timeout;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream oldout = System.out;
        System.setOut(new PrintStream(bo));
        Context c = Context.fromJAAS("client");
        System.setOut(oldout);
        String[] lines = new String(bo.toByteArray()).split("\n");
        System.out.println("----------------- TEST (" + timeout + "," +
                count + ") -----------------");
        for (String line: lines) {
            if (line.startsWith(">>> KDCCommunication")) {
                System.out.println(line);
                if (line.indexOf(timeoutTag) < 0) {
                    throw new Exception("Wrong timeout value");
                }
                count--;
            }
        }
        if (count != 0) {
            throw new Exception("Retry count is " + count + " less");
        }
    }
    private static void test2(String proto) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream oldout = System.out;
        System.setOut(new PrintStream(bo));
        Context c = Context.fromJAAS("client");
        System.setOut(oldout);
        int count = 2;
        String[] lines = new String(bo.toByteArray()).split("\n");
        System.out.println("----------------- TEST -----------------");
        for (String line: lines) {
            if (line.startsWith(">>> KDCCommunication")) {
                System.out.println(line);
                count--;
                if (line.indexOf(proto) < 0) {
                    throw new Exception("Wrong timeout value");
                }
            }
        }
        if (count != 0) {
            throw new Exception("Retry count is " + count + " less");
        }
    }
    private static void rewriteUdpPrefLimit(int global, int realm)
            throws Exception {
        BufferedReader fr = new BufferedReader(new FileReader(OneKDC.KRB5_CONF));
        FileWriter fw = new FileWriter("alternative-krb5.conf");
        while (true) {
            String s = fr.readLine();
            if (s == null) {
                break;
            }
            if (s.startsWith("[realms]")) {
                if (global != -1) {
                    fw.write("udp_preference_limit = " + global + "\n");
                }
            } else if (s.trim().startsWith("kdc = ")) {
                if (realm != -1) {
                    fw.write("    udp_preference_limit = " + realm + "\n");
                }
            }
            fw.write(s + "\n");
        }
        fr.close();
        fw.close();
        sun.security.krb5.Config.refresh();
    }
    private static void rewriteMaxRetries(int value) throws Exception {
        BufferedReader fr = new BufferedReader(new FileReader(OneKDC.KRB5_CONF));
        FileWriter fw = new FileWriter("alternative-krb5.conf");
        while (true) {
            String s = fr.readLine();
            if (s == null) {
                break;
            }
            if (s.startsWith("[realms]")) {
                fw.write("max_retries = 2\n");
                fw.write("kdc_timeout = 5000\n");
            } else if (s.trim().startsWith("kdc = ")) {
                if (value != -1) {
                    fw.write("    max_retries = " + value + "\n");
                    fw.write("    kdc_timeout = " + (value*1000) + "\n");
                }
                fw.write("    kdc = localhost:33333\n");
            }
            fw.write(s + "\n");
        }
        fr.close();
        fw.close();
        sun.security.krb5.Config.refresh();
    }
}
