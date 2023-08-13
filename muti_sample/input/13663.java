public class FastCharsetProvider
    extends CharsetProvider
{
    private Map<String,String> classMap;
    private Map<String,String> aliasMap;
    private Map<String,Charset> cache;
    private String packagePrefix;
    protected FastCharsetProvider(String pp,
                                  Map<String,String> am,
                                  Map<String,String> cm,
                                  Map<String,Charset> c)
    {
        packagePrefix = pp;
        aliasMap = am;
        classMap = cm;
        cache = c;
    }
    private String canonicalize(String csn) {
        String acn = aliasMap.get(csn);
        return (acn != null) ? acn : csn;
    }
    private static String toLower(String s) {
        int n = s.length();
        boolean allLower = true;
        for (int i = 0; i < n; i++) {
            int c = s.charAt(i);
            if (((c - 'A') | ('Z' - c)) >= 0) {
                allLower = false;
                break;
            }
        }
        if (allLower)
            return s;
        char[] ca = new char[n];
        for (int i = 0; i < n; i++) {
            int c = s.charAt(i);
            if (((c - 'A') | ('Z' - c)) >= 0)
                ca[i] = (char)(c + 0x20);
            else
                ca[i] = (char)c;
        }
        return new String(ca);
    }
    private Charset lookup(String charsetName) {
        String csn = canonicalize(toLower(charsetName));
        Charset cs = cache.get(csn);
        if (cs != null)
            return cs;
        String cln = classMap.get(csn);
        if (cln == null)
            return null;
        if (cln.equals("US_ASCII")) {
            cs = new US_ASCII();
            cache.put(csn, cs);
            return cs;
        }
        try {
            Class c = Class.forName(packagePrefix + "." + cln,
                                    true,
                                    this.getClass().getClassLoader());
            cs = (Charset)c.newInstance();
            cache.put(csn, cs);
            return cs;
        } catch (ClassNotFoundException x) {
            return null;
        } catch (IllegalAccessException x) {
            return null;
        } catch (InstantiationException x) {
            return null;
        }
    }
    public final Charset charsetForName(String charsetName) {
        synchronized (this) {
            return lookup(canonicalize(charsetName));
        }
    }
    public final Iterator<Charset> charsets() {
        return new Iterator<Charset>() {
                Iterator<String> i = classMap.keySet().iterator();
                public boolean hasNext() {
                    return i.hasNext();
                }
                public Charset next() {
                    String csn = i.next();
                    return lookup(csn);
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
    }
}
