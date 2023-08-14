public class Providers {
    private static final ThreadLocal<ProviderList> threadLists =
        new InheritableThreadLocal<>();
    private static volatile int threadListsUsed;
    private static volatile ProviderList providerList;
    static {
        providerList = ProviderList.EMPTY;
        providerList = ProviderList.fromSecurityProperties();
    }
    private Providers() {
    }
    private static final String BACKUP_PROVIDER_CLASSNAME =
        "sun.security.provider.VerificationProvider";
    private static final String[] jarVerificationProviders = {
        "sun.security.provider.Sun",
        "sun.security.rsa.SunRsaSign",
        "sun.security.ec.SunEC",
        BACKUP_PROVIDER_CLASSNAME,
    };
    public static Provider getSunProvider() {
        try {
            Class clazz = Class.forName(jarVerificationProviders[0]);
            return (Provider)clazz.newInstance();
        } catch (Exception e) {
            try {
                Class clazz = Class.forName(BACKUP_PROVIDER_CLASSNAME);
                return (Provider)clazz.newInstance();
            } catch (Exception ee) {
                throw new RuntimeException("Sun provider not found", e);
            }
        }
    }
    public static Object startJarVerification() {
        ProviderList currentList = getProviderList();
        ProviderList jarList = currentList.getJarList(jarVerificationProviders);
        return beginThreadProviderList(jarList);
    }
    public static void stopJarVerification(Object obj) {
        endThreadProviderList((ProviderList)obj);
    }
    public static ProviderList getProviderList() {
        ProviderList list = getThreadProviderList();
        if (list == null) {
            list = getSystemProviderList();
        }
        return list;
    }
    public static void setProviderList(ProviderList newList) {
        if (getThreadProviderList() == null) {
            setSystemProviderList(newList);
        } else {
            changeThreadProviderList(newList);
        }
    }
    public static ProviderList getFullProviderList() {
        ProviderList list;
        synchronized (Providers.class) {
            list = getThreadProviderList();
            if (list != null) {
                ProviderList newList = list.removeInvalid();
                if (newList != list) {
                    changeThreadProviderList(newList);
                    list = newList;
                }
                return list;
            }
        }
        list = getSystemProviderList();
        ProviderList newList = list.removeInvalid();
        if (newList != list) {
            setSystemProviderList(newList);
            list = newList;
        }
        return list;
    }
    private static ProviderList getSystemProviderList() {
        return providerList;
    }
    private static void setSystemProviderList(ProviderList list) {
        providerList = list;
    }
    public static ProviderList getThreadProviderList() {
        if (threadListsUsed == 0) {
            return null;
        }
        return threadLists.get();
    }
    private static void changeThreadProviderList(ProviderList list) {
        threadLists.set(list);
    }
    public static synchronized ProviderList beginThreadProviderList(ProviderList list) {
        if (ProviderList.debug != null) {
            ProviderList.debug.println("ThreadLocal providers: " + list);
        }
        ProviderList oldList = threadLists.get();
        threadListsUsed++;
        threadLists.set(list);
        return oldList;
    }
    public static synchronized void endThreadProviderList(ProviderList list) {
        if (list == null) {
            if (ProviderList.debug != null) {
                ProviderList.debug.println("Disabling ThreadLocal providers");
            }
            threadLists.remove();
        } else {
            if (ProviderList.debug != null) {
                ProviderList.debug.println
                    ("Restoring previous ThreadLocal providers: " + list);
            }
            threadLists.set(list);
        }
        threadListsUsed--;
    }
}
