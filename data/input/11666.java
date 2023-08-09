public class StandardMBean implements DynamicMBean, MBeanRegistration {
    private final static DescriptorCache descriptors =
        DescriptorCache.getInstance(JMX.proof);
    private volatile MBeanSupport<?> mbean;
    private volatile MBeanInfo cachedMBeanInfo;
    private <T> void construct(T implementation, Class<T> mbeanInterface,
                               boolean nullImplementationAllowed,
                               boolean isMXBean)
                               throws NotCompliantMBeanException {
        if (implementation == null) {
            if (nullImplementationAllowed)
                implementation = Util.<T>cast(this);
            else throw new IllegalArgumentException("implementation is null");
        }
        if (isMXBean) {
            if (mbeanInterface == null) {
                mbeanInterface = Util.cast(Introspector.getMXBeanInterface(
                        implementation.getClass()));
            }
            this.mbean = new MXBeanSupport(implementation, mbeanInterface);
        } else {
            if (mbeanInterface == null) {
                mbeanInterface = Util.cast(Introspector.getStandardMBeanInterface(
                        implementation.getClass()));
            }
            this.mbean =
                    new StandardMBeanSupport(implementation, mbeanInterface);
        }
    }
    public <T> StandardMBean(T implementation, Class<T> mbeanInterface)
        throws NotCompliantMBeanException {
        construct(implementation, mbeanInterface, false, false);
    }
    protected StandardMBean(Class<?> mbeanInterface)
        throws NotCompliantMBeanException {
        construct(null, mbeanInterface, true, false);
    }
    public <T> StandardMBean(T implementation, Class<T> mbeanInterface,
                             boolean isMXBean) {
        try {
            construct(implementation, mbeanInterface, false, isMXBean);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException(e);
        }
    }
    protected StandardMBean(Class<?> mbeanInterface, boolean isMXBean) {
        try {
            construct(null, mbeanInterface, true, isMXBean);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public void setImplementation(Object implementation)
        throws NotCompliantMBeanException {
        if (implementation == null)
            throw new IllegalArgumentException("implementation is null");
        if (isMXBean()) {
            this.mbean = new MXBeanSupport(implementation,
                    Util.<Class<Object>>cast(getMBeanInterface()));
        } else {
            this.mbean = new StandardMBeanSupport(implementation,
                    Util.<Class<Object>>cast(getMBeanInterface()));
        }
    }
    public Object getImplementation() {
        return mbean.getResource();
    }
    public final Class<?> getMBeanInterface() {
        return mbean.getMBeanInterface();
    }
    public Class<?> getImplementationClass() {
        return mbean.getResource().getClass();
    }
    public Object getAttribute(String attribute)
        throws AttributeNotFoundException,
               MBeanException,
               ReflectionException {
        return mbean.getAttribute(attribute);
    }
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException,
               InvalidAttributeValueException,
               MBeanException,
               ReflectionException {
        mbean.setAttribute(attribute);
    }
    public AttributeList getAttributes(String[] attributes) {
        return mbean.getAttributes(attributes);
    }
    public AttributeList setAttributes(AttributeList attributes) {
        return mbean.setAttributes(attributes);
    }
    public Object invoke(String actionName, Object params[], String signature[])
            throws MBeanException, ReflectionException {
        return mbean.invoke(actionName, params, signature);
    }
    public MBeanInfo getMBeanInfo() {
        try {
            final MBeanInfo cached = getCachedMBeanInfo();
            if (cached != null) return cached;
        } catch (RuntimeException x) {
            if (MISC_LOGGER.isLoggable(Level.FINEST)) {
                MISC_LOGGER.logp(Level.FINEST,
                        MBeanServerFactory.class.getName(), "getMBeanInfo",
                        "Failed to get cached MBeanInfo", x);
            }
        }
        if (MISC_LOGGER.isLoggable(Level.FINER)) {
            MISC_LOGGER.logp(Level.FINER,
                    MBeanServerFactory.class.getName(), "getMBeanInfo",
                    "Building MBeanInfo for " +
                    getImplementationClass().getName());
        }
        MBeanSupport<?> msupport = mbean;
        final MBeanInfo bi = msupport.getMBeanInfo();
        final Object impl = msupport.getResource();
        final boolean immutableInfo = immutableInfo(this.getClass());
        final String                  cname = getClassName(bi);
        final String                  text  = getDescription(bi);
        final MBeanConstructorInfo[]  ctors = getConstructors(bi,impl);
        final MBeanAttributeInfo[]    attrs = getAttributes(bi);
        final MBeanOperationInfo[]    ops   = getOperations(bi);
        final MBeanNotificationInfo[] ntfs  = getNotifications(bi);
        final Descriptor              desc  = getDescriptor(bi, immutableInfo);
        final MBeanInfo nmbi = new MBeanInfo(
                cname, text, attrs, ctors, ops, ntfs, desc);
        try {
            cacheMBeanInfo(nmbi);
        } catch (RuntimeException x) {
            if (MISC_LOGGER.isLoggable(Level.FINEST)) {
                MISC_LOGGER.logp(Level.FINEST,
                        MBeanServerFactory.class.getName(), "getMBeanInfo",
                        "Failed to cache MBeanInfo", x);
            }
        }
        return nmbi;
    }
    protected String getClassName(MBeanInfo info) {
        if (info == null) return getImplementationClass().getName();
        return info.getClassName();
    }
    protected String getDescription(MBeanInfo info) {
        if (info == null) return null;
        return info.getDescription();
    }
    protected String getDescription(MBeanFeatureInfo info) {
        if (info == null) return null;
        return info.getDescription();
    }
    protected String getDescription(MBeanAttributeInfo info) {
        return getDescription((MBeanFeatureInfo)info);
    }
    protected String getDescription(MBeanConstructorInfo info) {
        return getDescription((MBeanFeatureInfo)info);
    }
    protected String getDescription(MBeanConstructorInfo ctor,
                                    MBeanParameterInfo   param,
                                    int sequence) {
        if (param == null) return null;
        return param.getDescription();
    }
    protected String getParameterName(MBeanConstructorInfo ctor,
                                      MBeanParameterInfo param,
                                      int sequence) {
        if (param == null) return null;
        return param.getName();
    }
    protected String getDescription(MBeanOperationInfo info) {
        return getDescription((MBeanFeatureInfo)info);
    }
    protected int getImpact(MBeanOperationInfo info) {
        if (info == null) return MBeanOperationInfo.UNKNOWN;
        return info.getImpact();
    }
    protected String getParameterName(MBeanOperationInfo op,
                                      MBeanParameterInfo param,
                                      int sequence) {
        if (param == null) return null;
        return param.getName();
    }
    protected String getDescription(MBeanOperationInfo op,
                                    MBeanParameterInfo param,
                                    int sequence) {
        if (param == null) return null;
        return param.getDescription();
    }
    protected MBeanConstructorInfo[]
        getConstructors(MBeanConstructorInfo[] ctors, Object impl) {
            if (ctors == null) return null;
            if (impl != null && impl != this) return null;
            return ctors;
    }
    MBeanNotificationInfo[] getNotifications(MBeanInfo info) {
        return null;
    }
    Descriptor getDescriptor(MBeanInfo info, boolean immutableInfo) {
        ImmutableDescriptor desc;
        if (info == null ||
            info.getDescriptor() == null ||
            info.getDescriptor().getFieldNames().length == 0) {
            final String interfaceClassNameS =
                "interfaceClassName=" + getMBeanInterface().getName();
            final String immutableInfoS =
                "immutableInfo=" + immutableInfo;
            desc = new ImmutableDescriptor(interfaceClassNameS, immutableInfoS);
            desc = descriptors.get(desc);
        } else {
            Descriptor d = info.getDescriptor();
            Map<String,Object> fields = new HashMap<String,Object>();
            for (String fieldName : d.getFieldNames()) {
                if (fieldName.equals("immutableInfo")) {
                    fields.put(fieldName, Boolean.toString(immutableInfo));
                } else {
                    fields.put(fieldName, d.getFieldValue(fieldName));
                }
            }
            desc = new ImmutableDescriptor(fields);
        }
        return desc;
    }
    protected MBeanInfo getCachedMBeanInfo() {
        return cachedMBeanInfo;
    }
    protected void cacheMBeanInfo(MBeanInfo info) {
        cachedMBeanInfo = info;
    }
    private boolean isMXBean() {
        return mbean.isMXBean();
    }
    private static <T> boolean identicalArrays(T[] a, T[] b) {
        if (a == b)
            return true;
        if (a == null || b == null || a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }
    private static <T> boolean equal(T a, T b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        return a.equals(b);
    }
    private static MBeanParameterInfo
            customize(MBeanParameterInfo pi,
                      String name,
                      String description) {
        if (equal(name, pi.getName()) &&
                equal(description, pi.getDescription()))
            return pi;
        else if (pi instanceof OpenMBeanParameterInfo) {
            OpenMBeanParameterInfo opi = (OpenMBeanParameterInfo) pi;
            return new OpenMBeanParameterInfoSupport(name,
                                                     description,
                                                     opi.getOpenType(),
                                                     pi.getDescriptor());
        } else {
            return new MBeanParameterInfo(name,
                                          pi.getType(),
                                          description,
                                          pi.getDescriptor());
        }
    }
    private static MBeanConstructorInfo
            customize(MBeanConstructorInfo ci,
                      String description,
                      MBeanParameterInfo[] signature) {
        if (equal(description, ci.getDescription()) &&
                identicalArrays(signature, ci.getSignature()))
            return ci;
        if (ci instanceof OpenMBeanConstructorInfo) {
            OpenMBeanParameterInfo[] oparams =
                paramsToOpenParams(signature);
            return new OpenMBeanConstructorInfoSupport(ci.getName(),
                                                       description,
                                                       oparams,
                                                       ci.getDescriptor());
        } else {
            return new MBeanConstructorInfo(ci.getName(),
                                            description,
                                            signature,
                                            ci.getDescriptor());
        }
    }
    private static MBeanOperationInfo
            customize(MBeanOperationInfo oi,
                      String description,
                      MBeanParameterInfo[] signature,
                      int impact) {
        if (equal(description, oi.getDescription()) &&
                identicalArrays(signature, oi.getSignature()) &&
                impact == oi.getImpact())
            return oi;
        if (oi instanceof OpenMBeanOperationInfo) {
            OpenMBeanOperationInfo ooi = (OpenMBeanOperationInfo) oi;
            OpenMBeanParameterInfo[] oparams =
                paramsToOpenParams(signature);
            return new OpenMBeanOperationInfoSupport(oi.getName(),
                                                     description,
                                                     oparams,
                                                     ooi.getReturnOpenType(),
                                                     impact,
                                                     oi.getDescriptor());
        } else {
            return new MBeanOperationInfo(oi.getName(),
                                          description,
                                          signature,
                                          oi.getReturnType(),
                                          impact,
                                          oi.getDescriptor());
        }
    }
    private static MBeanAttributeInfo
            customize(MBeanAttributeInfo ai,
                      String description) {
        if (equal(description, ai.getDescription()))
            return ai;
        if (ai instanceof OpenMBeanAttributeInfo) {
            OpenMBeanAttributeInfo oai = (OpenMBeanAttributeInfo) ai;
            return new OpenMBeanAttributeInfoSupport(ai.getName(),
                                                     description,
                                                     oai.getOpenType(),
                                                     ai.isReadable(),
                                                     ai.isWritable(),
                                                     ai.isIs(),
                                                     ai.getDescriptor());
        } else {
            return new MBeanAttributeInfo(ai.getName(),
                                          ai.getType(),
                                          description,
                                          ai.isReadable(),
                                          ai.isWritable(),
                                          ai.isIs(),
                                          ai.getDescriptor());
        }
    }
    private static OpenMBeanParameterInfo[]
            paramsToOpenParams(MBeanParameterInfo[] params) {
        if (params instanceof OpenMBeanParameterInfo[])
            return (OpenMBeanParameterInfo[]) params;
        OpenMBeanParameterInfo[] oparams =
            new OpenMBeanParameterInfoSupport[params.length];
        System.arraycopy(params, 0, oparams, 0, params.length);
        return oparams;
    }
    private MBeanConstructorInfo[]
            getConstructors(MBeanInfo info, Object impl) {
        final MBeanConstructorInfo[] ctors =
            getConstructors(info.getConstructors(), impl);
        if (ctors == null)
            return null;
        final int ctorlen = ctors.length;
        final MBeanConstructorInfo[] nctors = new MBeanConstructorInfo[ctorlen];
        for (int i=0; i<ctorlen; i++) {
            final MBeanConstructorInfo c = ctors[i];
            final MBeanParameterInfo[] params = c.getSignature();
            final MBeanParameterInfo[] nps;
            if (params != null) {
                final int plen = params.length;
                nps = new MBeanParameterInfo[plen];
                for (int ii=0;ii<plen;ii++) {
                    MBeanParameterInfo p = params[ii];
                    nps[ii] = customize(p,
                                        getParameterName(c,p,ii),
                                        getDescription(c,p,ii));
                }
            } else {
                nps = null;
            }
            nctors[i] =
                customize(c, getDescription(c), nps);
        }
        return nctors;
    }
    private MBeanOperationInfo[] getOperations(MBeanInfo info) {
        final MBeanOperationInfo[] ops = info.getOperations();
        if (ops == null)
            return null;
        final int oplen = ops.length;
        final MBeanOperationInfo[] nops = new MBeanOperationInfo[oplen];
        for (int i=0; i<oplen; i++) {
            final MBeanOperationInfo o = ops[i];
            final MBeanParameterInfo[] params = o.getSignature();
            final MBeanParameterInfo[] nps;
            if (params != null) {
                final int plen = params.length;
                nps = new MBeanParameterInfo[plen];
                for (int ii=0;ii<plen;ii++) {
                    MBeanParameterInfo p = params[ii];
                    nps[ii] = customize(p,
                                        getParameterName(o,p,ii),
                                        getDescription(o,p,ii));
                }
            } else {
                nps = null;
            }
            nops[i] = customize(o, getDescription(o), nps, getImpact(o));
        }
        return nops;
    }
    private MBeanAttributeInfo[] getAttributes(MBeanInfo info) {
        final MBeanAttributeInfo[] atts = info.getAttributes();
        if (atts == null)
            return null; 
        final MBeanAttributeInfo[] natts;
        final int attlen = atts.length;
        natts = new MBeanAttributeInfo[attlen];
        for (int i=0; i<attlen; i++) {
            final MBeanAttributeInfo a = atts[i];
            natts[i] = customize(a, getDescription(a));
        }
        return natts;
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        mbean.register(server, name);
        return name;
    }
    public void postRegister(Boolean registrationDone) {
        if (!registrationDone)
            mbean.unregister();
    }
    public void preDeregister() throws Exception {
    }
    public void postDeregister() {
        mbean.unregister();
    }
    private static final Map<Class<?>, Boolean> mbeanInfoSafeMap =
        new WeakHashMap<Class<?>, Boolean>();
    static boolean immutableInfo(Class<? extends StandardMBean> subclass) {
        if (subclass == StandardMBean.class ||
            subclass == StandardEmitterMBean.class)
            return true;
        synchronized (mbeanInfoSafeMap) {
            Boolean safe = mbeanInfoSafeMap.get(subclass);
            if (safe == null) {
                try {
                    MBeanInfoSafeAction action =
                        new MBeanInfoSafeAction(subclass);
                    safe = AccessController.doPrivileged(action);
                } catch (Exception e) { 
                    safe = false;
                }
                mbeanInfoSafeMap.put(subclass, safe);
            }
            return safe;
        }
    }
    static boolean overrides(Class<?> subclass, Class<?> superclass,
                             String name, Class<?>... params) {
        for (Class<?> c = subclass; c != superclass; c = c.getSuperclass()) {
            try {
                c.getDeclaredMethod(name, params);
                return true;
            } catch (NoSuchMethodException e) {
            }
        }
        return false;
    }
    private static class MBeanInfoSafeAction
            implements PrivilegedAction<Boolean> {
        private final Class<?> subclass;
        MBeanInfoSafeAction(Class<?> subclass) {
            this.subclass = subclass;
        }
        public Boolean run() {
            if (overrides(subclass, StandardMBean.class,
                          "cacheMBeanInfo", MBeanInfo.class))
                return false;
            if (overrides(subclass, StandardMBean.class,
                          "getCachedMBeanInfo", (Class<?>[]) null))
                return false;
            if (overrides(subclass, StandardMBean.class,
                          "getMBeanInfo", (Class<?>[]) null))
                return false;
            if (StandardEmitterMBean.class.isAssignableFrom(subclass))
                if (overrides(subclass, StandardEmitterMBean.class,
                              "getNotificationInfo", (Class<?>[]) null))
                    return false;
            return true;
        }
    }
}
