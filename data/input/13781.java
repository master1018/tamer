public class TcpTimeout {
    public static void main(String[] args)
            throws Exception {
        System.setProperty("sun.security.krb5.debug", "true");
        final int p1 = 10000 + new java.util.Random().nextInt(10000);
        final int p2 = 20000 + new java.util.Random().nextInt(10000);
        final int p3 = 30000 + new java.util.Random().nextInt(10000);
        KDC k = new KDC(OneKDC.REALM, OneKDC.KDCHOST, p3, true);
        k.addPrincipal(OneKDC.USER, OneKDC.PASS);
        k.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
        new Thread() {
            public void run() {
                try {
                    new ServerSocket(p1).accept();
                } catch (Exception e) {
                }}
        }.start();
        new Thread() {
            public void run() {
                try {
                    new ServerSocket(p2).accept();
                } catch (Exception e) {
                }}
        }.start();
        FileWriter fw = new FileWriter("alternative-krb5.conf");
        fw.write("[libdefaults]\n" +
                "udp_preference_limit = 1\n" +
                "max_retries = 2\n" +
                "default_realm = " + OneKDC.REALM + "\n" +
                "kdc_timeout = 5000\n");
        fw.write("[realms]\n" + OneKDC.REALM + " = {\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p1 + "\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p2 + "\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p3 + "\n" +
                "}\n");
        fw.close();
        System.setProperty("java.security.krb5.conf", "alternative-krb5.conf");
        Config.refresh();
        int count = 6;
        long start = System.nanoTime();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream oldout = System.out;
        System.setOut(new PrintStream(bo));
        Context c = Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        System.setOut(oldout);
        String[] lines = new String(bo.toByteArray()).split("\n");
        for (String line: lines) {
            if (line.startsWith(">>> KDCCommunication")) {
                System.out.println(line);
                count--;
            }
        }
        if (count != 0) {
            throw new Exception("Retry count is " + count + " less");
        }
        long end = System.nanoTime();
        if ((end - start)/1000000000L < 20) {
            throw new Exception("Too fast? " + (end - start)/1000000000L);
        }
    }
    private static KDC on(int p) throws Exception {
        KDC k = new KDC(OneKDC.REALM, OneKDC.KDCHOST, p, true);
        k.addPrincipal(OneKDC.USER, OneKDC.PASS);
        k.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
        return k;
    }
    private static void addFakeKDCs()
            throws Exception {
        BufferedReader fr = new BufferedReader(new FileReader(OneKDC.KRB5_CONF));
        FileWriter fw = new FileWriter("alternative-krb5.conf");
        while (true) {
            String s = fr.readLine();
            if (s == null) {
                break;
            }
            if (s.trim().startsWith("kdc = ")) {
                fw.write("    kdc = localhost:33333\n");
                fw.write("    kdc = localhost:22222\n");
            }
            fw.write(s + "\n");
        }
        fr.close();
        fw.close();
        sun.security.krb5.Config.refresh();
    }
}
