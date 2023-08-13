public final class Security {
    private static Properties secprops = new Properties();
    static {
        AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
            public Void run() {
                boolean loaded = false;
                try {
                    InputStream configStream =
                        getClass().getResourceAsStream("security.properties"); 
                    InputStream input =
                        new BufferedInputStream(configStream, 8192);
                    secprops.load(input);
                    loaded = true;
                    configStream.close();
                } catch (Exception ex) {
                    Logger.global.log(Level.SEVERE,
                            "Could not load Security properties.", ex);
                }
                if (!loaded) {
                    registerDefaultProviders();
                }
                Engine.door = new SecurityDoor();
                return null;
            }
        });
    }
    private Security() {
    }
    private static void registerDefaultProviders() {
        secprops.put("security.provider.1", "org.apache.harmony.security.provider.cert.DRLCertFactory");  
        secprops.put("security.provider.2", "org.apache.harmony.security.provider.crypto.CryptoProvider");  
        secprops.put("security.provider.3", "org.apache.harmony.xnet.provider.jsse.JSSEProvider");  
        secprops.put("security.provider.4", "org.bouncycastle.jce.provider.BouncyCastleProvider");  
    }
    @Deprecated
    public static String getAlgorithmProperty(String algName, String propName) {
        if (algName == null || propName == null) {
            return null;
        }
        String prop = "Alg." + propName + "." + algName; 
        Provider[] providers = getProviders();
        for (int i = 0; i < providers.length; i++) {
            for (Enumeration e = providers[i].propertyNames(); e
                    .hasMoreElements();) {
                String pname = (String) e.nextElement();
                if (Util.equalsIgnoreCase(prop, pname)) {
                    return providers[i].getProperty(pname);
                }
            }
        }
        return null;
    }
    public static synchronized int insertProviderAt(Provider provider,
            int position) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("insertProvider." + provider.getName()); 
        }
        if (getProvider(provider.getName()) != null) {
            return -1;
        }
        int result = Services.insertProviderAt(provider, position);
        renumProviders();
        return result;
    }
    public static int addProvider(Provider provider) {
        return insertProviderAt(provider, 0);
    }
    public static synchronized void removeProvider(String name) {
        Provider p;
        if ((name == null) || (name.length() == 0)) {
            return;
        }
        p = getProvider(name);
        if (p == null) {
            return;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("removeProvider." + name); 
        }
        Services.removeProvider(p.getProviderNumber());
        renumProviders();
        p.setProviderNumber(-1);
    }
    public static synchronized Provider[] getProviders() {
        return Services.getProviders();
    }
    public static synchronized Provider getProvider(String name) {
        return Services.getProvider(name);
    }
    public static Provider[] getProviders(String filter) {
        if (filter == null) {
            throw new NullPointerException(Messages.getString("security.2A")); 
        }
        if (filter.length() == 0) {
            throw new InvalidParameterException(
                    Messages.getString("security.2B")); 
        }
        HashMap<String, String> hm = new HashMap<String, String>();
        int i = filter.indexOf(':');
        if ((i == filter.length() - 1) || (i == 0)) {
            throw new InvalidParameterException(
                    Messages.getString("security.2B")); 
        }
        if (i < 1) {
            hm.put(filter, ""); 
        } else {
            hm.put(filter.substring(0, i), filter.substring(i + 1));
        }
        return getProviders(hm);
    }
    public static synchronized Provider[] getProviders(Map<String,String> filter) {
        if (filter == null) {
            throw new NullPointerException(Messages.getString("security.2A")); 
        }
        if (filter.isEmpty()) {
            return null;
        }
        java.util.List<Provider> result = Services.getProvidersList();
        Set<Entry<String, String>> keys = filter.entrySet();
        Map.Entry<String, String> entry;
        for (Iterator<Entry<String, String>> it = keys.iterator(); it.hasNext();) {
            entry = it.next();
            String key = entry.getKey();
            String val = entry.getValue();
            String attribute = null;
            int i = key.indexOf(' ');
            int j = key.indexOf('.');
            if (j == -1) {
                throw new InvalidParameterException(
                        Messages.getString("security.2B")); 
            }
            if (i == -1) { 
                if (val.length() != 0) {
                    throw new InvalidParameterException(
                            Messages.getString("security.2B")); 
                }
            } else { 
                if (val.length() == 0) {
                    throw new InvalidParameterException(
                            Messages.getString("security.2B")); 
                }
                attribute = key.substring(i + 1);
                if (attribute.trim().length() == 0) {
                    throw new InvalidParameterException(
                            Messages.getString("security.2B")); 
                }
                key = key.substring(0, i);
            }
            String serv = key.substring(0, j);
            String alg = key.substring(j + 1);
            if (serv.length() == 0 || alg.length() == 0) {
                throw new InvalidParameterException(
                        Messages.getString("security.2B")); 
            }
            Provider p;
            for (int k = 0; k < result.size(); k++) {
                try {
                    p = result.get(k);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                if (!p.implementsAlg(serv, alg, attribute, val)) {
                    result.remove(p);
                    k--;
                }
            }
        }
        if (result.size() > 0) {
            return result.toArray(new Provider[result.size()]);
        }
        return null;
    }
    public static String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException(Messages.getString("security.2C")); 
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("getProperty." + key); 
        }
        String property = secprops.getProperty(key);
        if (property != null) {
            property = property.trim();
        }
        return property;
    }
    public static void setProperty(String key, String datnum) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setProperty." + key); 
        }
        secprops.put(key, datnum);
    }
    public static Set<String> getAlgorithms(String serviceName) {
        Set<String> result = new HashSet<String>();
        if (serviceName == null) {
            return result;
        }
        Provider[] p = getProviders();
        for (int i = 0; i < p.length; i++) {
            for (Iterator it = p[i].getServices().iterator(); it.hasNext();) {
                Provider.Service s = (Provider.Service) it.next();
                if (Util.equalsIgnoreCase(s.getType(),serviceName)) {
                    result.add(s.getAlgorithm());
                }
            }
        }
        return result;
    }
    private static void renumProviders() {
        Provider[] p = Services.getProviders();
        for (int i = 0; i < p.length; i++) {
            p[i].setProviderNumber(i + 1);
        }
    }
    private static class SecurityDoor implements SecurityAccess {
        public void renumProviders() {
            Security.renumProviders();
        }
        public Iterator<String> getAliases(Provider.Service s) {
            return s.getAliases();
        }
        public Provider.Service getService(Provider p, String type) {
            return p.getService(type);
        }
    }
}
