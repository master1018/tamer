public class ResolverConfigurationImpl
    extends ResolverConfiguration
{
    private static Object lock = new Object();
    private static long lastRefresh = -1;
    private static final int TIMEOUT = 300000;
    private final Options opts;
    private LinkedList<String> resolvconf(String keyword,
                                          int maxperkeyword,
                                          int maxkeywords)
    {
        LinkedList<String> ll = new LinkedList<>();
        try {
            BufferedReader in =
                new BufferedReader(new FileReader("/etc/resolv.conf"));
            String line;
            while ((line = in.readLine()) != null) {
                int maxvalues = maxperkeyword;
                if (line.length() == 0)
                   continue;
                if (line.charAt(0) == '#' || line.charAt(0) == ';')
                    continue;
                if (!line.startsWith(keyword))
                    continue;
                String value = line.substring(keyword.length());
                if (value.length() == 0)
                    continue;
                if (value.charAt(0) != ' ' && value.charAt(0) != '\t')
                    continue;
                StringTokenizer st = new StringTokenizer(value, " \t");
                while (st.hasMoreTokens()) {
                    String val = st.nextToken();
                    if (val.charAt(0) == '#' || val.charAt(0) == ';') {
                        break;
                    }
                    ll.add(val);
                    if (--maxvalues == 0) {
                        break;
                    }
                }
                if (--maxkeywords == 0) {
                    break;
                }
            }
            in.close();
        } catch (IOException ioe) {
        }
        return ll;
    }
    private LinkedList<String> searchlist;
    private LinkedList<String> nameservers;
    private void loadConfig() {
        assert Thread.holdsLock(lock);
        if (lastRefresh >= 0) {
            long currTime = System.currentTimeMillis();
            if ((currTime - lastRefresh) < TIMEOUT) {
                return;
            }
        }
        nameservers =
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<LinkedList<String>>() {
                    public LinkedList<String> run() {
                        return resolvconf("nameserver", 1, 5);
                    } 
                });
        searchlist = getSearchList();
        lastRefresh = System.currentTimeMillis();
    }
    private LinkedList<String> getSearchList() {
        LinkedList<String> sl;
        sl = java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction<LinkedList<String>>() {
                    public LinkedList<String> run() {
                        LinkedList ll;
                        ll = resolvconf("search", 6, 1);
                        if (ll.size() > 0) {
                            return ll;
                        }
                        return null;
                    } 
                });
        if (sl != null) {
            return sl;
        }
        String localDomain = localDomain0();
        if (localDomain != null && localDomain.length() > 0) {
            sl = new LinkedList();
            sl.add(localDomain);
            return sl;
        }
        sl = java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction<LinkedList<String>>() {
                    public LinkedList<String> run() {
                        LinkedList<String> ll;
                        ll = resolvconf("domain", 1, 1);
                        if (ll.size() > 0) {
                            return ll;
                        }
                        return null;
                    } 
                });
        if (sl != null) {
            return sl;
        }
        sl = new LinkedList<>();
        String domain = fallbackDomain0();
        if (domain != null && domain.length() > 0) {
            sl.add(domain);
        }
        return sl;
    }
    ResolverConfigurationImpl() {
        opts = new OptionsImpl();
    }
    public List<String> searchlist() {
        synchronized (lock) {
            loadConfig();
            return (List)searchlist.clone();
        }
    }
    public List<String> nameservers() {
        synchronized (lock) {
            loadConfig();
            return (List)nameservers.clone();
         }
    }
    public Options options() {
        return opts;
    }
    static native String localDomain0();
    static native String fallbackDomain0();
    static {
        java.security.AccessController.doPrivileged(
            new sun.security.action.LoadLibraryAction("net"));
    }
}
class OptionsImpl extends ResolverConfiguration.Options {
}
