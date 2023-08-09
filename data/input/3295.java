public class BadKdc {
    static final Pattern re = Pattern.compile(
            ">>> KDCCommunication: kdc=kdc.rabbit.hole UDP:(\\d)...., " +
            "timeout=(\\d)000,");
    public static void go(int[]... expected)
            throws Exception {
        try {
            go0(expected);
        } catch (BindException be) {
            System.out.println("The random port is used by another process");
        } catch (LoginException le) {
            Throwable cause = le.getCause();
            if (cause instanceof Asn1Exception) {
                System.out.println("Bad packet possibly from another process");
                return;
            }
            throw le;
        }
    }
    public static void go0(int[]... expected)
            throws Exception {
        System.setProperty("sun.security.krb5.debug", "true");
        int p1 = 10000 + new java.util.Random().nextInt(10000);
        int p2 = 20000 + new java.util.Random().nextInt(10000);
        int p3 = 30000 + new java.util.Random().nextInt(10000);
        FileWriter fw = new FileWriter("alternative-krb5.conf");
        fw.write("[libdefaults]\n" +
                "default_realm = " + OneKDC.REALM + "\n" +
                "kdc_timeout = 2000\n");
        fw.write("[realms]\n" + OneKDC.REALM + " = {\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p1 + "\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p2 + "\n" +
                "kdc = " + OneKDC.KDCHOST + ":" + p3 + "\n" +
                "}\n");
        fw.close();
        System.setProperty("java.security.krb5.conf", "alternative-krb5.conf");
        Config.refresh();
        KDC k3 = on(p3);
        test(expected[0]);
        test(expected[1]);
        Config.refresh();
        test(expected[2]);
        k3.terminate(); 
        on(p2);         
        test(expected[3]);
        on(p1);         
        test(expected[4]);
    }
    private static KDC on(int p) throws Exception {
        KDC k = new KDC(OneKDC.REALM, OneKDC.KDCHOST, p, true);
        k.addPrincipal(OneKDC.USER, OneKDC.PASS);
        k.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
        System.err.println("-------- IGNORE THIS ERROR MESSAGE --------");
        new DatagramSocket().send(
                new DatagramPacket("Hello".getBytes(), 5,
                        InetAddress.getByName(OneKDC.KDCHOST), p));
        return k;
    }
    private static void test(int... expected) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            test0(bo, expected);
        } catch (Exception e) {
            System.out.println("----------------- ERROR -----------------");
            System.out.println(new String(bo.toByteArray()));
            System.out.println("--------------- ERROR END ---------------");
            throw e;
        }
    }
    private static void test0(ByteArrayOutputStream bo, int... expected)
            throws Exception {
        PrintStream oldout = System.out;
        System.setOut(new PrintStream(bo));
        try {
            Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        } finally {
            System.setOut(oldout);
        }
        String[] lines = new String(bo.toByteArray()).split("\n");
        System.out.println("----------------- TEST -----------------");
        int count = 0;
        for (String line: lines) {
            Matcher m = re.matcher(line);
            if (m.find()) {
                System.out.println(line);
                if (Integer.parseInt(m.group(1)) != expected[count++] ||
                        Integer.parseInt(m.group(2)) != expected[count++]) {
                    throw new Exception("Fail here");
                }
            }
        }
        if (count != expected.length) {
            throw new Exception("Less rounds");
        }
    }
}
