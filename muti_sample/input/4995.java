public class IPv6 {
    public static void main(String[] args) throws Exception {
        String[][] kdcs = {
                {"simple.host", null},  
                {"simple.host", ""},
                {"simple.host", "8080"},
                {"0.0.0.1", null},
                {"0.0.0.1", ""},
                {"0.0.0.1", "8080"},
                {"1::1", null},
                {"[1::1]", null},
                {"[1::1]", ""},
                {"[1::1]", "8080"},
                {"[1::1", null},        
                {"[1::1]abc", null},
        };
        PrintStream out = new PrintStream(new FileOutputStream("ipv6.conf"));
        out.println("[libdefaults]");
        out.println("default_realm = V6");
        out.println("kdc_timeout = 1");
        out.println("[realms]");
        out.println("V6 = {");
        for (String[] hp: kdcs) {
            if (hp[1] != null) out.println("    kdc = "+hp[0]+":"+hp[1]);
            else out.println("    kdc = " + hp[0]);
        }
        out.println("}");
        out.close();
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.krb5.conf", "ipv6.conf");
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream po = new PrintStream(bo);
        PrintStream oldout = System.out;
        System.setOut(po);
        try {
            Subject subject = new Subject();
            Krb5LoginModule krb5 = new Krb5LoginModule();
            Map<String, String> map = new HashMap<>();
            Map<String, Object> shared = new HashMap<>();
            map.put("debug", "true");
            map.put("doNotPrompt", "true");
            map.put("useTicketCache", "false");
            map.put("useFirstPass", "true");
            shared.put("javax.security.auth.login.name", "any");
            shared.put("javax.security.auth.login.password", "any".toCharArray());
            krb5.initialize(subject, null, shared, map);
            krb5.login();
        } catch (Exception e) {
        }
        po.flush();
        System.setOut(oldout);
        BufferedReader br = new BufferedReader(new StringReader(
                new String(bo.toByteArray())));
        int cc = 0;
        Pattern r = Pattern.compile(".*KrbKdcReq send: kdc=(.*) UDP:(\\d+),.*");
        String line;
        while ((line = br.readLine()) != null) {
            Matcher m = r.matcher(line.subSequence(0, line.length()));
            if (m.matches()) {
                System.out.println("------------------");
                System.out.println(line);
                String h = m.group(1), p = m.group(2);
                String eh = kdcs[cc][0], ep = kdcs[cc][1];
                if (eh.charAt(0) == '[') {
                    eh = eh.substring(1, eh.length()-1);
                }
                System.out.println("Expected: " + eh + " : " + ep);
                System.out.println("Actual: " + h + " : " + p);
                if (!eh.equals(h) ||
                        (ep == null || ep.length() == 0) && !p.equals("88") ||
                        (ep != null && ep.length() > 0) && !p.equals(ep)) {
                    throw new Exception("Mismatch");
                }
                cc++;
            }
        }
        if (cc != kdcs.length - 2) {    
            throw new Exception("Not traversed");
        }
    }
}
