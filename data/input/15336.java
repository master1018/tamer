public class DnsFallback {
    public static void main(String[] args) throws Exception {
        check("true", "true", true);
        check("false", "true", false);
        check("true", "false", true);
        check("false", "false", false);
        check("true", null, true);
        check("false", null, false);
        check(null, "true", true);
        check(null, "false", false);
        check(null, null, true);
    }
    static void check(String realm, String fallback, boolean output) throws Exception {
        FileOutputStream fo = new FileOutputStream("dnsfallback.conf");
        StringBuffer sb = new StringBuffer();
        sb.append("[libdefaults]\n");
        if (realm != null) {
            sb.append("dns_lookup_realm=" + realm + "\n");
        }
        if (fallback != null) {
            sb.append("dns_fallback=" + fallback + "\n");
        }
        fo.write(sb.toString().getBytes());
        fo.close();
        System.setProperty("java.security.krb5.conf", "dnsfallback.conf");
        Config.refresh();
        System.out.println("Testing " + realm + ", " + fallback + ", " + output);
        if (Config.getInstance().useDNS_Realm() != output) {
            throw new Exception("Fail");
        }
    }
}
