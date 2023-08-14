public final class ProviderList {
    final static sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("jca", "ProviderList");
    private final static ProviderConfig[] PC0 = new ProviderConfig[0];
    private final static Provider[] P0 = new Provider[0];
    static final ProviderList EMPTY = new ProviderList(PC0, true);
    private static final Provider EMPTY_PROVIDER =
        new Provider("##Empty##", 1.0d, "initialization in progress") {
            public Service getService(String type, String algorithm) {
                return null;
            }
        };
    static ProviderList fromSecurityProperties() {
        return AccessController.doPrivileged(
                        new PrivilegedAction<ProviderList>() {
            public ProviderList run() {
                return new ProviderList();
            }
        });
    }
    public static ProviderList add(ProviderList providerList, Provider p) {
        return insertAt(providerList, p, -1);
    }
    public static ProviderList insertAt(ProviderList providerList, Provider p,
            int position) {
        if (providerList.getProvider(p.getName()) != null) {
            return providerList;
        }
        List<ProviderConfig> list = new ArrayList<>
                                    (Arrays.asList(providerList.configs));
        int n = list.size();
        if ((position < 0) || (position > n)) {
            position = n;
        }
        list.add(position, new ProviderConfig(p));
        return new ProviderList(list.toArray(PC0), true);
    }
    public static ProviderList remove(ProviderList providerList, String name) {
        if (providerList.getProvider(name) == null) {
            return providerList;
        }
        ProviderConfig[] configs = new ProviderConfig[providerList.size() - 1];
        int j = 0;
        for (ProviderConfig config : providerList.configs) {
            if (config.getProvider().getName().equals(name) == false) {
                configs[j++] = config;
            }
        }
        return new ProviderList(configs, true);
    }
    public static ProviderList newList(Provider ... providers) {
        ProviderConfig[] configs = new ProviderConfig[providers.length];
        for (int i = 0; i < providers.length; i++) {
            configs[i] = new ProviderConfig(providers[i]);
        }
        return new ProviderList(configs, true);
    }
    private final ProviderConfig[] configs;
    private volatile boolean allLoaded;
    private final List<Provider> userList = new AbstractList<Provider>() {
        public int size() {
            return configs.length;
        }
        public Provider get(int index) {
            return getProvider(index);
        }
    };
    private ProviderList(ProviderConfig[] configs, boolean allLoaded) {
        this.configs = configs;
        this.allLoaded = allLoaded;
    }
    private ProviderList() {
        List<ProviderConfig> configList = new ArrayList<>();
        for (int i = 1; true; i++) {
            String entry = Security.getProperty("security.provider." + i);
            if (entry == null) {
                break;
            }
            entry = entry.trim();
            if (entry.length() == 0) {
                System.err.println("invalid entry for " +
                                   "security.provider." + i);
                break;
            }
            int k = entry.indexOf(' ');
            ProviderConfig config;
            if (k == -1) {
                config = new ProviderConfig(entry);
            } else {
                String className = entry.substring(0, k);
                String argument = entry.substring(k + 1).trim();
                config = new ProviderConfig(className, argument);
            }
            if (configList.contains(config) == false) {
                configList.add(config);
            }
        }
        configs = configList.toArray(PC0);
        if (debug != null) {
            debug.println("provider configuration: " + configList);
        }
    }
    ProviderList getJarList(String[] jarClassNames) {
        List<ProviderConfig> newConfigs = new ArrayList<>();
        for (String className : jarClassNames) {
            ProviderConfig newConfig = new ProviderConfig(className);
            for (ProviderConfig config : configs) {
                if (config.equals(newConfig)) {
                    newConfig = config;
                    break;
                }
            }
            newConfigs.add(newConfig);
        }
        ProviderConfig[] configArray = newConfigs.toArray(PC0);
        return new ProviderList(configArray, false);
    }
    public int size() {
        return configs.length;
    }
    Provider getProvider(int index) {
        Provider p = configs[index].getProvider();
        return (p != null) ? p : EMPTY_PROVIDER;
    }
    public List<Provider> providers() {
        return userList;
    }
    private ProviderConfig getProviderConfig(String name) {
        int index = getIndex(name);
        return (index != -1) ? configs[index] : null;
    }
    public Provider getProvider(String name) {
        ProviderConfig config = getProviderConfig(name);
        return (config == null) ? null : config.getProvider();
    }
    public int getIndex(String name) {
        for (int i = 0; i < configs.length; i++) {
            Provider p = getProvider(i);
            if (p.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }
    private int loadAll() {
        if (allLoaded) {
            return configs.length;
        }
        if (debug != null) {
            debug.println("Loading all providers");
            new Exception("Call trace").printStackTrace();
        }
        int n = 0;
        for (int i = 0; i < configs.length; i++) {
            Provider p = configs[i].getProvider();
            if (p != null) {
                n++;
            }
        }
        if (n == configs.length) {
            allLoaded = true;
        }
        return n;
    }
    ProviderList removeInvalid() {
        int n = loadAll();
        if (n == configs.length) {
            return this;
        }
        ProviderConfig[] newConfigs = new ProviderConfig[n];
        for (int i = 0, j = 0; i < configs.length; i++) {
            ProviderConfig config = configs[i];
            if (config.isLoaded()) {
                newConfigs[j++] = config;
            }
        }
        return new ProviderList(newConfigs, true);
    }
    public Provider[] toArray() {
        return providers().toArray(P0);
    }
    public String toString() {
        return Arrays.asList(configs).toString();
    }
    public Service getService(String type, String name) {
        for (int i = 0; i < configs.length; i++) {
            Provider p = getProvider(i);
            Service s = p.getService(type, name);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
    public List<Service> getServices(String type, String algorithm) {
        return new ServiceList(type, algorithm);
    }
    @Deprecated
    public List<Service> getServices(String type, List<String> algorithms) {
        List<ServiceId> ids = new ArrayList<>();
        for (String alg : algorithms) {
            ids.add(new ServiceId(type, alg));
        }
        return getServices(ids);
    }
    public List<Service> getServices(List<ServiceId> ids) {
        return new ServiceList(ids);
    }
    private final class ServiceList extends AbstractList<Service> {
        private final String type;
        private final String algorithm;
        private final List<ServiceId> ids;
        private Service firstService;
        private List<Service> services;
        private int providerIndex;
        ServiceList(String type, String algorithm) {
            this.type = type;
            this.algorithm = algorithm;
            this.ids = null;
        }
        ServiceList(List<ServiceId> ids) {
            this.type = null;
            this.algorithm = null;
            this.ids = ids;
        }
        private void addService(Service s) {
            if (firstService == null) {
                firstService = s;
            } else {
                if (services == null) {
                    services = new ArrayList<Service>(4);
                    services.add(firstService);
                }
                services.add(s);
            }
        }
        private Service tryGet(int index) {
            while (true) {
                if ((index == 0) && (firstService != null)) {
                    return firstService;
                } else if ((services != null) && (services.size() > index)) {
                    return services.get(index);
                }
                if (providerIndex >= configs.length) {
                    return null;
                }
                Provider p = getProvider(providerIndex++);
                if (type != null) {
                    Service s = p.getService(type, algorithm);
                    if (s != null) {
                        addService(s);
                    }
                } else {
                    for (ServiceId id : ids) {
                        Service s = p.getService(id.type, id.algorithm);
                        if (s != null) {
                            addService(s);
                        }
                    }
                }
            }
        }
        public Service get(int index) {
            Service s = tryGet(index);
            if (s == null) {
                throw new IndexOutOfBoundsException();
            }
            return s;
        }
        public int size() {
            int n;
            if (services != null) {
                n = services.size();
            } else {
                n = (firstService != null) ? 1 : 0;
            }
            while (tryGet(n) != null) {
                n++;
            }
            return n;
        }
        public boolean isEmpty() {
            return (tryGet(0) == null);
        }
        public Iterator<Service> iterator() {
            return new Iterator<Service>() {
                int index;
                public boolean hasNext() {
                    return tryGet(index) != null;
                }
                public Service next() {
                    Service s = tryGet(index);
                    if (s == null) {
                        throw new NoSuchElementException();
                    }
                    index++;
                    return s;
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
