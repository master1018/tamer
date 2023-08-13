public class MXBeanLookup {
    private MXBeanLookup(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    static MXBeanLookup lookupFor(MBeanServerConnection mbsc) {
        synchronized (mbscToLookup) {
            WeakReference<MXBeanLookup> weakLookup = mbscToLookup.get(mbsc);
            MXBeanLookup lookup = (weakLookup == null) ? null : weakLookup.get();
            if (lookup == null) {
                lookup = new MXBeanLookup(mbsc);
                mbscToLookup.put(mbsc, new WeakReference<MXBeanLookup>(lookup));
            }
            return lookup;
        }
    }
    synchronized <T> T objectNameToMXBean(ObjectName name, Class<T> type) {
        WeakReference<Object> wr = objectNameToProxy.get(name);
        if (wr != null) {
            Object proxy = wr.get();
            if (type.isInstance(proxy))
                return type.cast(proxy);
        }
        T proxy = JMX.newMXBeanProxy(mbsc, name, type);
        objectNameToProxy.put(name, new WeakReference<Object>(proxy));
        return proxy;
    }
    synchronized ObjectName mxbeanToObjectName(Object mxbean)
    throws OpenDataException {
        String wrong;
        if (mxbean instanceof Proxy) {
            InvocationHandler ih = Proxy.getInvocationHandler(mxbean);
            if (ih instanceof MBeanServerInvocationHandler) {
                MBeanServerInvocationHandler mbsih =
                        (MBeanServerInvocationHandler) ih;
                if (mbsih.getMBeanServerConnection().equals(mbsc))
                    return mbsih.getObjectName();
                else
                    wrong = "proxy for a different MBeanServer";
            } else
                wrong = "not a JMX proxy";
        } else {
            ObjectName name = mxbeanToObjectName.get(mxbean);
            if (name != null)
                return name;
            wrong = "not an MXBean registered in this MBeanServer";
        }
        String s = (mxbean == null) ?
            "null" : "object of type " + mxbean.getClass().getName();
        throw new OpenDataException(
                "Could not convert " + s + " to an ObjectName: " + wrong);
    }
    synchronized void addReference(ObjectName name, Object mxbean)
    throws InstanceAlreadyExistsException {
        ObjectName existing = mxbeanToObjectName.get(mxbean);
        if (existing != null) {
            String multiname = AccessController.doPrivileged(
                    new GetPropertyAction("jmx.mxbean.multiname"));
            if (!"true".equalsIgnoreCase(multiname)) {
                throw new InstanceAlreadyExistsException(
                        "MXBean already registered with name " + existing);
            }
        }
        mxbeanToObjectName.put(mxbean, name);
    }
    synchronized boolean removeReference(ObjectName name, Object mxbean) {
        if (name.equals(mxbeanToObjectName.get(mxbean))) {
            mxbeanToObjectName.remove(mxbean);
            return true;
        } else
            return false;
    }
    static MXBeanLookup getLookup() {
        return currentLookup.get();
    }
    static void setLookup(MXBeanLookup lookup) {
        currentLookup.set(lookup);
    }
    private static final ThreadLocal<MXBeanLookup> currentLookup =
            new ThreadLocal<MXBeanLookup>();
    private final MBeanServerConnection mbsc;
    private final WeakIdentityHashMap<Object, ObjectName>
        mxbeanToObjectName = WeakIdentityHashMap.make();
    private final Map<ObjectName, WeakReference<Object>>
        objectNameToProxy = newMap();
    private static final WeakIdentityHashMap<MBeanServerConnection,
                                             WeakReference<MXBeanLookup>>
        mbscToLookup = WeakIdentityHashMap.make();
}
