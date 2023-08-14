public class KeywordMap {
    public static void main(String[] args) throws Exception {
        X500Principal p = null;
        Map<String, String> m = null;
        try {
            p = new X500Principal("CN=user", null);
            throw new Exception
                ("expected NullPointerException for null keywordMap");
        } catch (NullPointerException npe) {}
        m = Collections.singletonMap("FOO", "FOO");
        try {
            p = new X500Principal("FOO=user", m);
            throw new Exception
                ("expected IllegalArgumentException for bad OID");
        } catch (IllegalArgumentException iae) {}
        m = Collections.singletonMap("?*&", "FOO");
        p = new X500Principal("CN=user", m);
        m = Collections.singletonMap("BAR", "1.2.3");
        try {
            p = new X500Principal("FOO=user", m);
            throw new Exception
                ("expected IllegalArgumentExc for keyword with no mapping");
        } catch (IllegalArgumentException iae) {}
        m = Collections.singletonMap("foo", "1.2.3");
        try {
            p = new X500Principal("FOO=user", m);
            throw new Exception
                ("expected IllegalArgumentExc for wrong-case keyword mapping");
        } catch (IllegalArgumentException iae) {}
        m = new HashMap<String, String>();
        m.put("FOO", "1.2.3");
        m.put("BAR", "1.2.3");
        p = new X500Principal("BAR=user", m);
        m = Collections.singletonMap("CN", "1.2.3");
        p = new X500Principal("CN=user", m);
        if (!p.getName().startsWith("1.2.3")) {
            throw new Exception("mapping did not override builtin keyword");
        }
        m = Collections.singletonMap("FOO", "2.5.4.3");
        p = new X500Principal("FOO=sean", m);
        if (!p.getName().startsWith("CN")) {
            throw new Exception("mapping did not override builtin OID");
        }
    }
}
