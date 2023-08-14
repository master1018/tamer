public class Services {
    private static final Map<String, Provider.Service> services = new HashMap<String, Provider.Service>(600);
    private static boolean needRefresh; 
    static int refreshNumber = 1;
    private static final List<Provider> providers = new ArrayList<Provider>(20);
    private static final Map<String, Provider> providersNames = new HashMap<String, Provider>(20);
    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                loadProviders();
                return null;
            }
        });
    }
    private static void loadProviders() {
        String providerClassName = null;
        int i = 1;
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Provider p;
        while ((providerClassName = Security.getProperty("security.provider." 
                + i++)) != null) {
            try {
                p = (Provider) Class
                        .forName(providerClassName.trim(), true, cl)
                        .newInstance();
                providers.add(p);
                providersNames.put(p.getName(), p);
                initServiceInfo(p);
            } catch (ClassNotFoundException e) { 
            } catch (IllegalAccessException e) {
			} catch (InstantiationException e) {
			}
        }
        Engine.door.renumProviders();
    }
    public static Provider[] getProviders() {
        return providers.toArray(new Provider[providers.size()]);
    }
    public static List<Provider> getProvidersList() {
        return new ArrayList<Provider>(providers);
    }
    public static Provider getProvider(String name) {
        if (name == null) {
            return null;
        }
        return providersNames.get(name);
    }
    public static int insertProviderAt(Provider provider, int position) {
        int size = providers.size();
        if ((position < 1) || (position > size)) {
            position = size + 1;
        }
        providers.add(position - 1, provider);
        providersNames.put(provider.getName(), provider);
        setNeedRefresh();
        return position;
    }
    public static void removeProvider(int providerNumber) {
        Provider p = providers.remove(providerNumber - 1);
        providersNames.remove(p.getName());
        setNeedRefresh();
    }
    public static void initServiceInfo(Provider p) {
        Provider.Service serv;
        String key;
        String type;
        String alias;
        StringBuilder sb = new StringBuilder(128);
        for (Iterator<Provider.Service> it1 = p.getServices().iterator(); it1.hasNext();) {
            serv = it1.next();
            type = serv.getType();
            sb.delete(0, sb.length());
            key = sb.append(type).append(".").append( 
                    Util.toUpperCase(serv.getAlgorithm())).toString();
            if (!services.containsKey(key)) {
                services.put(key, serv);
            }
            for (Iterator<String> it2 = Engine.door.getAliases(serv); it2.hasNext();) {
                alias = it2.next();
                sb.delete(0, sb.length());
                key = sb.append(type).append(".").append(Util.toUpperCase(alias)) 
                        .toString();
                if (!services.containsKey(key)) {
                    services.put(key, serv);
                }
            }
        }
    }
    public static void updateServiceInfo() {
        services.clear();
        for (Iterator<Provider> it = providers.iterator(); it.hasNext();) {
            initServiceInfo(it.next());
        }
        needRefresh = false;
    }
    public static boolean isEmpty() {
        return services.isEmpty();
    }
    public static Provider.Service getService(String key) {
        return services.get(key);
    }
    public static void printServices() {
        refresh();
        Set<String> s = services.keySet();
        for (Iterator<String> i = s.iterator(); i.hasNext();) {
            String key = i.next();
            System.out.println(key + "=" + services.get(key)); 
        }
    }
    public static void setNeedRefresh() {
        needRefresh = true;
    }
    public static void refresh() {
        if (needRefresh) {
            refreshNumber++;
            updateServiceInfo();
        }
    }
}