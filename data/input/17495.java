public final class DTraceProviderFactory extends ProviderFactory {
    public <T extends Provider> T createProvider(Class<T> cls) {
        DTraceProvider jsdt = new DTraceProvider(cls);
        T proxy = jsdt.newProxyInstance();
        jsdt.setProxy(proxy);
        jsdt.init();
        new Activation(jsdt.getModuleName(), new DTraceProvider[] { jsdt });
        return proxy;
    }
    public Map<Class<? extends Provider>,Provider> createProviders(
            Set<Class<? extends Provider>> providers, String moduleName) {
        HashMap<Class<? extends Provider>,Provider> map =
            new HashMap<Class<? extends Provider>,Provider>();
        HashSet<DTraceProvider> jsdts = new HashSet<DTraceProvider>();
        for (Class<? extends Provider> cls : providers) {
            DTraceProvider jsdt = new DTraceProvider(cls);
            jsdts.add(jsdt);
            map.put(cls, jsdt.newProxyInstance());
        }
        new Activation(moduleName, jsdts.toArray(new DTraceProvider[0]));
        return map;
    }
    public static boolean isSupported() {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                Permission perm = new RuntimePermission(
                        "com.sun.tracing.dtrace.createProvider");
                security.checkPermission(perm);
            }
            return JVM.isSupported();
        } catch (SecurityException e) {
            return false;
        }
    }
}
