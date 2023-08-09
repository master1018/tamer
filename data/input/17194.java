public final class DNSNameService implements NameService {
    private LinkedList<String> domainList = null;
    private String nameProviderUrl = null;
    private static ThreadLocal<SoftReference<ThreadContext>> contextRef =
            new ThreadLocal<>();
    private static class ThreadContext {
        private DirContext dirCtxt;
        private List<String> nsList;
        public ThreadContext(DirContext dirCtxt, List<String> nsList) {
            this.dirCtxt = dirCtxt;
            this.nsList = nsList;
        }
        public DirContext dirContext() {
            return dirCtxt;
        }
        public List<String> nameservers() {
            return nsList;
        }
    }
    private DirContext getTemporaryContext() throws NamingException {
        SoftReference<ThreadContext> ref = contextRef.get();
        ThreadContext thrCtxt = null;
        List<String> nsList = null;
        if (nameProviderUrl == null)
            nsList = ResolverConfiguration.open().nameservers();
        if ((ref != null) && ((thrCtxt = ref.get()) != null)) {
            if (nameProviderUrl == null) {
                if (!thrCtxt.nameservers().equals(nsList)) {
                    thrCtxt = null;
                }
            }
        }
        if (thrCtxt == null) {
            final Hashtable<String,Object> env = new Hashtable<>();
            env.put("java.naming.factory.initial",
                    "com.sun.jndi.dns.DnsContextFactory");
            String provUrl = nameProviderUrl;
            if (provUrl == null) {
                provUrl = createProviderURL(nsList);
                if (provUrl.length() == 0) {
                    throw new RuntimeException("bad nameserver configuration");
                }
            }
            env.put("java.naming.provider.url", provUrl);
            DirContext dirCtxt;
            try {
                dirCtxt = java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedExceptionAction<DirContext>() {
                            public DirContext run() throws NamingException {
                                Context ctx = NamingManager.getInitialContext(env);
                                if (!(ctx instanceof DirContext)) {
                                    return null; 
                                }
                                return (DirContext)ctx;
                            }
                    });
            } catch (java.security.PrivilegedActionException pae) {
                throw (NamingException)pae.getException();
            }
            thrCtxt = new ThreadContext(dirCtxt, nsList);
            contextRef.set(new SoftReference(thrCtxt));
        }
        return thrCtxt.dirContext();
    }
    private ArrayList<String> resolve(final DirContext ctx, final String name,
                                      final String[] ids, int depth)
            throws UnknownHostException
    {
        ArrayList<String> results = new ArrayList<>();
        Attributes attrs;
        try {
            attrs = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<Attributes>() {
                        public Attributes run() throws NamingException {
                            return ctx.getAttributes(name, ids);
                        }
                });
        } catch (java.security.PrivilegedActionException pae) {
            throw new UnknownHostException(pae.getException().getMessage());
        }
        NamingEnumeration<? extends Attribute> ne = attrs.getAll();
        if (!ne.hasMoreElements()) {
            throw new UnknownHostException("DNS record not found");
        }
        UnknownHostException uhe = null;
        try {
            while (ne.hasMoreElements()) {
                Attribute attr = ne.next();
                String attrID = attr.getID();
                for (NamingEnumeration e = attr.getAll(); e.hasMoreElements();) {
                    String addr = (String)e.next();
                    if (attrID.equals("CNAME")) {
                        if (depth > 4) {
                            throw new UnknownHostException(name + ": possible CNAME loop");
                        }
                        try {
                            results.addAll(resolve(ctx, addr, ids, depth+1));
                        } catch (UnknownHostException x) {
                            if (uhe == null)
                                uhe = x;
                        }
                    } else {
                        results.add(addr);
                    }
                }
            }
        } catch (NamingException nx) {
            throw new UnknownHostException(nx.getMessage());
        }
        if (results.isEmpty() && uhe != null) {
            throw uhe;
        }
        return results;
    }
    public DNSNameService() throws Exception {
        String domain = AccessController.doPrivileged(
            new GetPropertyAction("sun.net.spi.nameservice.domain"));
        if (domain != null && domain.length() > 0) {
            domainList = new LinkedList();
            domainList.add(domain);
        }
        String nameservers = AccessController.doPrivileged(
            new GetPropertyAction("sun.net.spi.nameservice.nameservers"));
        if (nameservers != null && nameservers.length() > 0) {
            nameProviderUrl = createProviderURL(nameservers);
            if (nameProviderUrl.length() == 0) {
                throw new RuntimeException("malformed nameservers property");
            }
        } else {
            List<String> nsList = ResolverConfiguration.open().nameservers();
            if (nsList.isEmpty()) {
                throw new RuntimeException("no nameservers provided");
            }
            boolean found = false;
            for (String addr: nsList) {
                if (IPAddressUtil.isIPv4LiteralAddress(addr) ||
                    IPAddressUtil.isIPv6LiteralAddress(addr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("bad nameserver configuration");
            }
        }
    }
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        String[] ids = {"A", "AAAA", "CNAME"};
        DirContext ctx;
        try {
            ctx = getTemporaryContext();
        } catch (NamingException nx) {
            throw new Error(nx);
        }
        ArrayList results = null;
        UnknownHostException uhe = null;
        if (host.indexOf('.') >= 0) {
            try {
                results = resolve(ctx, host, ids, 0);
            } catch (UnknownHostException x) {
                uhe = x;
            }
        }
        if (results == null) {
            List<String> searchList = null;
            Iterator<String> i;
            boolean usingSearchList = false;
            if (domainList != null) {
                i = domainList.iterator();
            } else {
                searchList = ResolverConfiguration.open().searchlist();
                if (searchList.size() > 1) {
                    usingSearchList = true;
                }
                i = searchList.iterator();
            }
            while (i.hasNext()) {
                String parentDomain = i.next();
                int start = 0;
                while ((start = parentDomain.indexOf(".")) != -1
                       && start < parentDomain.length() -1) {
                    try {
                        results = resolve(ctx, host+"."+parentDomain, ids, 0);
                        break;
                    } catch (UnknownHostException x) {
                        uhe = x;
                        if (usingSearchList) {
                            break;
                        }
                        parentDomain = parentDomain.substring(start+1);
                    }
                }
                if (results != null) {
                    break;
                }
            }
        }
        if (results == null && (host.indexOf('.') < 0)) {
            results = resolve(ctx, host, ids, 0);
        }
        if (results == null) {
            assert uhe != null;
            throw uhe;
        }
        assert results.size() > 0;
        InetAddress[] addrs = new InetAddress[results.size()];
        int count = 0;
        for (int i=0; i<results.size(); i++) {
            String addrString = (String)results.get(i);
            byte addr[] = IPAddressUtil.textToNumericFormatV4(addrString);
            if (addr == null) {
                addr = IPAddressUtil.textToNumericFormatV6(addrString);
            }
            if (addr != null) {
                addrs[count++] = InetAddress.getByAddress(host, addr);
            }
        }
        if (count == 0) {
            throw new UnknownHostException(host + ": no valid DNS records");
        }
        if (count < results.size()) {
            InetAddress[] tmp = new InetAddress[count];
            for (int i=0; i<count; i++) {
                tmp[i] = addrs[i];
            }
            addrs = tmp;
        }
        return addrs;
    }
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        String host = null;
        try {
            String literalip = "";
            String[] ids = { "PTR" };
            DirContext ctx;
            ArrayList<String> results = null;
            try {
                ctx = getTemporaryContext();
            } catch (NamingException nx) {
                throw new Error(nx);
            }
            if (addr.length == 4) { 
                for (int i = addr.length-1; i >= 0; i--) {
                    literalip += (addr[i] & 0xff) +".";
                }
                literalip += "IN-ADDR.ARPA.";
                results = resolve(ctx, literalip, ids, 0);
                host = results.get(0);
            } else if (addr.length == 16) { 
                for (int i = addr.length-1; i >= 0; i--) {
                    literalip += Integer.toHexString((addr[i] & 0x0f)) +"."
                        +Integer.toHexString((addr[i] & 0xf0) >> 4) +".";
                }
                String ip6lit = literalip + "IP6.ARPA.";
                try {
                    results = resolve(ctx, ip6lit, ids, 0);
                    host = results.get(0);
                } catch (UnknownHostException e) {
                    host = null;
                }
                if (host == null) {
                    ip6lit = literalip + "IP6.INT.";
                    results = resolve(ctx, ip6lit, ids, 0);
                    host = results.get(0);
                }
            }
        } catch (Exception e) {
            throw new UnknownHostException(e.getMessage());
        }
        if (host == null)
            throw new UnknownHostException();
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }
    private static void appendIfLiteralAddress(String addr, StringBuffer sb) {
        if (IPAddressUtil.isIPv4LiteralAddress(addr)) {
            sb.append("dns:
        } else {
            if (IPAddressUtil.isIPv6LiteralAddress(addr)) {
                sb.append("dns:
            }
        }
    }
    private static String createProviderURL(List<String> nsList) {
        StringBuffer sb = new StringBuffer();
        for (String s: nsList) {
            appendIfLiteralAddress(s, sb);
        }
        return sb.toString();
    }
    private static String createProviderURL(String str) {
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens()) {
            appendIfLiteralAddress(st.nextToken(), sb);
        }
        return sb.toString();
    }
}
