public class CaseSensitiveServices extends Provider {
    CaseSensitiveServices() {
        super("Foo", 1.0d, null);
        put("MessageDigest.Foo", "com.Foo");
        put("mESSAGEdIGEST.fOO xYz", "aBc");
        put("ALg.aliaS.MESSAGEdigest.Fu", "FoO");
        put("messageDigest.Bar", "com.Bar");
        put("MESSAGEDIGEST.BAZ", "com.Baz");
    }
    public static void main(String[] args) throws Exception {
        Provider p = new CaseSensitiveServices();
        System.out.println(p.getServices());
        if (p.getServices().size() != 3) {
            throw new Exception("services.size() should be 3");
        }
        Service s = testService(p, "MessageDigest", "fOO");
        String val = s.getAttribute("Xyz");
        if ("aBc".equals(val) == false) {
            throw new Exception("Wrong value: " + val);
        }
        testService(p, "MessageDigest", "fU");
        testService(p, "MessageDigest", "BAR");
        testService(p, "MessageDigest", "baz");
        System.out.println("OK");
    }
    private static Service testService(Provider p, String type, String alg) throws Exception {
        System.out.println("Getting " + type + "." + alg + "...");
        Service s = p.getService(type, alg);
        System.out.println(s);
        if (s == null) {
            throw new Exception("Lookup failed for: " + type + "." + alg);
        }
        return s;
    }
}
