public class      BeanContextServicesSupport extends BeanContextSupport
       implements BeanContextServices {
    private static final long serialVersionUID = -8494482757288719206L;
    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dTime, boolean visible) {
        super(peer, lcle, dTime, visible);
    }
    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dtime) {
        this (peer, lcle, dtime, true);
    }
    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle) {
        this (peer, lcle, false, true);
    }
    public BeanContextServicesSupport(BeanContextServices peer) {
        this (peer, null, false, true);
    }
    public BeanContextServicesSupport() {
        this (null, null, false, true);
    }
    public void initialize() {
        super.initialize();
        services     = new HashMap(serializable + 1);
        bcsListeners = new ArrayList(1);
    }
    public BeanContextServices getBeanContextServicesPeer() {
        return (BeanContextServices)getBeanContextChildPeer();
    }
    protected class BCSSChild extends BeanContextSupport.BCSChild  {
        private static final long serialVersionUID = -3263851306889194873L;
        class BCSSCServiceClassRef {
            BCSSCServiceClassRef(Class sc, BeanContextServiceProvider bcsp, boolean delegated) {
                super();
                serviceClass     = sc;
                if (delegated)
                    delegateProvider = bcsp;
                else
                    serviceProvider  = bcsp;
            }
            void addRequestor(Object requestor, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
                BeanContextServiceRevokedListener cbcsrl = (BeanContextServiceRevokedListener)requestors.get(requestor);
                if (cbcsrl != null && !cbcsrl.equals(bcsrl))
                    throw new TooManyListenersException();
                requestors.put(requestor, bcsrl);
            }
            void removeRequestor(Object requestor) {
                requestors.remove(requestor);
            }
            void verifyRequestor(Object requestor, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
                BeanContextServiceRevokedListener cbcsrl = (BeanContextServiceRevokedListener)requestors.get(requestor);
                if (cbcsrl != null && !cbcsrl.equals(bcsrl))
                    throw new TooManyListenersException();
            }
            void verifyAndMaybeSetProvider(BeanContextServiceProvider bcsp, boolean isDelegated) {
                BeanContextServiceProvider current;
                if (isDelegated) { 
                    current = delegateProvider;
                    if (current == null || bcsp == null) {
                        delegateProvider = bcsp;
                        return;
                    }
                } else { 
                    current = serviceProvider;
                    if (current == null || bcsp == null) {
                        serviceProvider = bcsp;
                        return;
                    }
                }
                if (!current.equals(bcsp))
                    throw new UnsupportedOperationException("existing service reference obtained from different BeanContextServiceProvider not supported");
            }
            Iterator cloneOfEntries() {
                return ((HashMap)requestors.clone()).entrySet().iterator();
            }
            Iterator entries() { return requestors.entrySet().iterator(); }
            boolean isEmpty() { return requestors.isEmpty(); }
            Class getServiceClass() { return serviceClass; }
            BeanContextServiceProvider getServiceProvider() {
                return serviceProvider;
            }
            BeanContextServiceProvider getDelegateProvider() {
                return delegateProvider;
            }
            boolean isDelegated() { return delegateProvider != null; }
            void addRef(boolean delegated) {
                if (delegated) {
                    delegateRefs++;
                } else {
                    serviceRefs++;
                }
            }
            void releaseRef(boolean delegated) {
                if (delegated) {
                    if (--delegateRefs == 0) {
                        delegateProvider = null;
                    }
                } else {
                    if (--serviceRefs  <= 0) {
                        serviceProvider = null;
                    }
                }
            }
            int getRefs() { return serviceRefs + delegateRefs; }
            int getDelegateRefs() { return delegateRefs; }
            int getServiceRefs() { return serviceRefs; }
            Class                               serviceClass;
            BeanContextServiceProvider          serviceProvider;
            int                                 serviceRefs;
            BeanContextServiceProvider          delegateProvider; 
            int                                 delegateRefs;
            HashMap                             requestors = new HashMap(1);
        }
        class BCSSCServiceRef {
            BCSSCServiceRef(BCSSCServiceClassRef scref, boolean isDelegated) {
                serviceClassRef = scref;
                delegated       = isDelegated;
            }
            void addRef()  { refCnt++;        }
            int  release() { return --refCnt; }
            BCSSCServiceClassRef getServiceClassRef() { return serviceClassRef; }
            boolean isDelegated() { return delegated; }
            BCSSCServiceClassRef serviceClassRef;
            int                  refCnt    = 1;
            boolean              delegated = false;
        }
        BCSSChild(Object bcc, Object peer) { super(bcc, peer); }
        synchronized void usingService(Object requestor, Object service, Class serviceClass, BeanContextServiceProvider bcsp, boolean isDelegated, BeanContextServiceRevokedListener bcsrl)  throws TooManyListenersException, UnsupportedOperationException {
            BCSSCServiceClassRef serviceClassRef = null;
            if (serviceClasses == null)
                serviceClasses = new HashMap(1);
            else
                serviceClassRef = (BCSSCServiceClassRef)serviceClasses.get(serviceClass);
            if (serviceClassRef == null) { 
                serviceClassRef = new BCSSCServiceClassRef(serviceClass, bcsp, isDelegated);
                serviceClasses.put(serviceClass, serviceClassRef);
            } else { 
                serviceClassRef.verifyAndMaybeSetProvider(bcsp, isDelegated); 
                serviceClassRef.verifyRequestor(requestor, bcsrl); 
            }
            serviceClassRef.addRequestor(requestor, bcsrl);
            serviceClassRef.addRef(isDelegated);
            BCSSCServiceRef serviceRef = null;
            Map             services   = null;
            if (serviceRequestors == null) {
                serviceRequestors = new HashMap(1);
            } else {
                services = (Map)serviceRequestors.get(requestor);
            }
            if (services == null) {
                services = new HashMap(1);
                serviceRequestors.put(requestor, services);
            } else
                serviceRef = (BCSSCServiceRef)services.get(service);
            if (serviceRef == null) {
                serviceRef = new BCSSCServiceRef(serviceClassRef, isDelegated);
                services.put(service, serviceRef);
            } else {
                serviceRef.addRef();
            }
        }
        synchronized void releaseService(Object requestor, Object service) {
            if (serviceRequestors == null) return;
            Map services = (Map)serviceRequestors.get(requestor);
            if (services == null) return; 
            BCSSCServiceRef serviceRef = (BCSSCServiceRef)services.get(service);
            if (serviceRef == null) return; 
            BCSSCServiceClassRef serviceClassRef = serviceRef.getServiceClassRef();
            boolean                    isDelegated = serviceRef.isDelegated();
            BeanContextServiceProvider bcsp        = isDelegated ? serviceClassRef.getDelegateProvider() : serviceClassRef.getServiceProvider();
            bcsp.releaseService(BeanContextServicesSupport.this.getBeanContextServicesPeer(), requestor, service);
            serviceClassRef.releaseRef(isDelegated);
            serviceClassRef.removeRequestor(requestor);
            if (serviceRef.release() == 0) {
                services.remove(service);
                if (services.isEmpty()) {
                    serviceRequestors.remove(requestor);
                    serviceClassRef.removeRequestor(requestor);
                }
                if (serviceRequestors.isEmpty()) {
                    serviceRequestors = null;
                }
                if (serviceClassRef.isEmpty()) {
                    serviceClasses.remove(serviceClassRef.getServiceClass());
                }
                if (serviceClasses.isEmpty())
                    serviceClasses = null;
            }
        }
        synchronized void revokeService(Class serviceClass, boolean isDelegated, boolean revokeNow) {
            if (serviceClasses == null) return;
            BCSSCServiceClassRef serviceClassRef = (BCSSCServiceClassRef)serviceClasses.get(serviceClass);
            if (serviceClassRef == null) return;
            Iterator i = serviceClassRef.cloneOfEntries();
            BeanContextServiceRevokedEvent bcsre       = new BeanContextServiceRevokedEvent(BeanContextServicesSupport.this.getBeanContextServicesPeer(), serviceClass, revokeNow);
            boolean                        noMoreRefs  = false;
            while (i.hasNext() && serviceRequestors != null) {
                Map.Entry                         entry    = (Map.Entry)i.next();
                BeanContextServiceRevokedListener listener = (BeanContextServiceRevokedListener)entry.getValue();
                if (revokeNow) {
                    Object  requestor = entry.getKey();
                    Map     services  = (Map)serviceRequestors.get(requestor);
                    if (services != null) {
                        Iterator i1 = services.entrySet().iterator();
                        while (i1.hasNext()) {
                            Map.Entry       tmp        = (Map.Entry)i1.next();
                            BCSSCServiceRef serviceRef = (BCSSCServiceRef)tmp.getValue();
                            if (serviceRef.getServiceClassRef().equals(serviceClassRef) && isDelegated == serviceRef.isDelegated()) {
                                i1.remove();
                            }
                        }
                        if (noMoreRefs = services.isEmpty()) {
                            serviceRequestors.remove(requestor);
                        }
                    }
                    if (noMoreRefs) serviceClassRef.removeRequestor(requestor);
                }
                listener.serviceRevoked(bcsre);
            }
            if (revokeNow && serviceClasses != null) {
                if (serviceClassRef.isEmpty())
                    serviceClasses.remove(serviceClass);
                if (serviceClasses.isEmpty())
                    serviceClasses = null;
            }
            if (serviceRequestors != null && serviceRequestors.isEmpty())
                serviceRequestors = null;
        }
        void cleanupReferences() {
            if (serviceRequestors == null) return;
            Iterator requestors = serviceRequestors.entrySet().iterator();
            while(requestors.hasNext()) {
                Map.Entry            tmp       = (Map.Entry)requestors.next();
                Object               requestor = tmp.getKey();
                Iterator             services  = ((Map)tmp.getValue()).entrySet().iterator();
                requestors.remove();
                while (services.hasNext()) {
                    Map.Entry       entry   = (Map.Entry)services.next();
                    Object          service = entry.getKey();
                    BCSSCServiceRef sref    = (BCSSCServiceRef)entry.getValue();
                    BCSSCServiceClassRef       scref = sref.getServiceClassRef();
                    BeanContextServiceProvider bcsp  = sref.isDelegated() ? scref.getDelegateProvider() : scref.getServiceProvider();
                    scref.removeRequestor(requestor);
                    services.remove();
                    while (sref.release() >= 0) {
                        bcsp.releaseService(BeanContextServicesSupport.this.getBeanContextServicesPeer(), requestor, service);
                    }
                }
            }
            serviceRequestors = null;
            serviceClasses    = null;
        }
        void revokeAllDelegatedServicesNow() {
            if (serviceClasses == null) return;
            Iterator serviceClassRefs  =
                new HashSet(serviceClasses.values()).iterator();
            while (serviceClassRefs.hasNext()) {
                BCSSCServiceClassRef serviceClassRef = (BCSSCServiceClassRef)serviceClassRefs.next();
                if (!serviceClassRef.isDelegated()) continue;
                Iterator i = serviceClassRef.cloneOfEntries();
                BeanContextServiceRevokedEvent bcsre       = new BeanContextServiceRevokedEvent(BeanContextServicesSupport.this.getBeanContextServicesPeer(), serviceClassRef.getServiceClass(), true);
                boolean                        noMoreRefs  = false;
                while (i.hasNext()) {
                    Map.Entry                         entry     = (Map.Entry)i.next();
                    BeanContextServiceRevokedListener listener  = (BeanContextServiceRevokedListener)entry.getValue();
                    Object                            requestor = entry.getKey();
                    Map                               services  = (Map)serviceRequestors.get(requestor);
                    if (services != null) {
                        Iterator i1 = services.entrySet().iterator();
                        while (i1.hasNext()) {
                            Map.Entry       tmp        = (Map.Entry)i1.next();
                            BCSSCServiceRef serviceRef = (BCSSCServiceRef)tmp.getValue();
                            if (serviceRef.getServiceClassRef().equals(serviceClassRef) && serviceRef.isDelegated()) {
                                i1.remove();
                            }
                        }
                        if (noMoreRefs = services.isEmpty()) {
                            serviceRequestors.remove(requestor);
                        }
                    }
                    if (noMoreRefs) serviceClassRef.removeRequestor(requestor);
                    listener.serviceRevoked(bcsre);
                    if (serviceClassRef.isEmpty())
                        serviceClasses.remove(serviceClassRef.getServiceClass());
                }
            }
            if (serviceClasses.isEmpty()) serviceClasses = null;
            if (serviceRequestors != null && serviceRequestors.isEmpty())
                serviceRequestors = null;
        }
        private transient HashMap       serviceClasses;
        private transient HashMap       serviceRequestors;
    }
    protected BCSChild createBCSChild(Object targetChild, Object peer) {
        return new BCSSChild(targetChild, peer);
    }
        protected static class BCSSServiceProvider implements Serializable {
            private static final long serialVersionUID = 861278251667444782L;
            BCSSServiceProvider(Class sc, BeanContextServiceProvider bcsp) {
                super();
                serviceProvider = bcsp;
            }
            protected BeanContextServiceProvider getServiceProvider() {
                return serviceProvider;
            }
            protected BeanContextServiceProvider serviceProvider;
        }
        protected BCSSServiceProvider createBCSSServiceProvider(Class sc, BeanContextServiceProvider bcsp) {
            return new BCSSServiceProvider(sc, bcsp);
        }
    public void addBeanContextServicesListener(BeanContextServicesListener bcsl) {
        if (bcsl == null) throw new NullPointerException("bcsl");
        synchronized(bcsListeners) {
            if (bcsListeners.contains(bcsl))
                return;
            else
                bcsListeners.add(bcsl);
        }
    }
    public void removeBeanContextServicesListener(BeanContextServicesListener bcsl) {
        if (bcsl == null) throw new NullPointerException("bcsl");
        synchronized(bcsListeners) {
            if (!bcsListeners.contains(bcsl))
                return;
            else
                bcsListeners.remove(bcsl);
        }
    }
    public boolean addService(Class serviceClass, BeanContextServiceProvider bcsp) {
        return addService(serviceClass, bcsp, true);
    }
    protected boolean addService(Class serviceClass, BeanContextServiceProvider bcsp, boolean fireEvent) {
        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (bcsp         == null) throw new NullPointerException("bcsp");
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(serviceClass))
                return false;
            else {
                services.put(serviceClass,  createBCSSServiceProvider(serviceClass, bcsp));
                if (bcsp instanceof Serializable) serializable++;
                if (!fireEvent) return true;
                BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(), serviceClass);
                fireServiceAdded(bcssae);
                synchronized(children) {
                    Iterator i = children.keySet().iterator();
                    while (i.hasNext()) {
                        Object c = i.next();
                        if (c instanceof BeanContextServices) {
                            ((BeanContextServicesListener)c).serviceAvailable(bcssae);
                        }
                    }
                }
                return true;
            }
        }
    }
    public void revokeService(Class serviceClass, BeanContextServiceProvider bcsp, boolean revokeCurrentServicesNow) {
        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (bcsp         == null) throw new NullPointerException("bcsp");
        synchronized(BeanContext.globalHierarchyLock) {
            if (!services.containsKey(serviceClass)) return;
            BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);
            if (!bcsssp.getServiceProvider().equals(bcsp))
                throw new IllegalArgumentException("service provider mismatch");
            services.remove(serviceClass);
            if (bcsp instanceof Serializable) serializable--;
            Iterator i = bcsChildren(); 
            while (i.hasNext()) {
                ((BCSSChild)i.next()).revokeService(serviceClass, false, revokeCurrentServicesNow);
            }
            fireServiceRevoked(serviceClass, revokeCurrentServicesNow);
        }
    }
    public synchronized boolean hasService(Class serviceClass) {
        if (serviceClass == null) throw new NullPointerException("serviceClass");
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(serviceClass)) return true;
            BeanContextServices bcs = null;
            try {
                bcs = (BeanContextServices)getBeanContext();
            } catch (ClassCastException cce) {
                return false;
            }
            return bcs == null ? false : bcs.hasService(serviceClass);
        }
    }
    protected class BCSSProxyServiceProvider implements BeanContextServiceProvider, BeanContextServiceRevokedListener {
        BCSSProxyServiceProvider(BeanContextServices bcs) {
            super();
            nestingCtxt = bcs;
        }
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object serviceSelector) {
            Object service = null;
            try {
                service = nestingCtxt.getService(bcs, requestor, serviceClass, serviceSelector, this);
            } catch (TooManyListenersException tmle) {
                return null;
            }
            return service;
        }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) {
            nestingCtxt.releaseService(bcs, requestor, service);
        }
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            return nestingCtxt.getCurrentServiceSelectors(serviceClass);
        }
        public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
            Iterator i = bcsChildren(); 
            while (i.hasNext()) {
                ((BCSSChild)i.next()).revokeService(bcsre.getServiceClass(), true, bcsre.isCurrentServiceInvalidNow());
            }
        }
        private BeanContextServices nestingCtxt;
    }
     public Object getService(BeanContextChild child, Object requestor, Class serviceClass, Object serviceSelector, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
        if (child        == null) throw new NullPointerException("child");
        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (requestor    == null) throw new NullPointerException("requestor");
        if (bcsrl        == null) throw new NullPointerException("bcsrl");
        Object              service = null;
        BCSSChild           bcsc;
        BeanContextServices bcssp   = getBeanContextServicesPeer();
        synchronized(BeanContext.globalHierarchyLock) {
            synchronized(children) { bcsc = (BCSSChild)children.get(child); }
            if (bcsc == null) throw new IllegalArgumentException("not a child of this context"); 
            BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);
            if (bcsssp != null) {
                BeanContextServiceProvider bcsp = bcsssp.getServiceProvider();
                service = bcsp.getService(bcssp, requestor, serviceClass, serviceSelector);
                if (service != null) { 
                    try {
                        bcsc.usingService(requestor, service, serviceClass, bcsp, false, bcsrl);
                    } catch (TooManyListenersException tmle) {
                        bcsp.releaseService(bcssp, requestor, service);
                        throw tmle;
                    } catch (UnsupportedOperationException uope) {
                        bcsp.releaseService(bcssp, requestor, service);
                        throw uope; 
                    }
                    return service;
                }
            }
            if (proxy != null) {
                service = proxy.getService(bcssp, requestor, serviceClass, serviceSelector);
                if (service != null) { 
                    try {
                        bcsc.usingService(requestor, service, serviceClass, proxy, true, bcsrl);
                    } catch (TooManyListenersException tmle) {
                        proxy.releaseService(bcssp, requestor, service);
                        throw tmle;
                    } catch (UnsupportedOperationException uope) {
                        proxy.releaseService(bcssp, requestor, service);
                        throw uope; 
                    }
                    return service;
                }
            }
        }
        return null;
    }
    public void releaseService(BeanContextChild child, Object requestor, Object service) {
        if (child     == null) throw new NullPointerException("child");
        if (requestor == null) throw new NullPointerException("requestor");
        if (service   == null) throw new NullPointerException("service");
        BCSSChild bcsc;
        synchronized(BeanContext.globalHierarchyLock) {
                synchronized(children) { bcsc = (BCSSChild)children.get(child); }
                if (bcsc != null)
                    bcsc.releaseService(requestor, service);
                else
                   throw new IllegalArgumentException("child actual is not a child of this BeanContext");
        }
    }
    public Iterator getCurrentServiceClasses() {
        return new BCSIterator(services.keySet().iterator());
    }
    public Iterator getCurrentServiceSelectors(Class serviceClass) {
        BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);
        return bcsssp != null ? new BCSIterator(bcsssp.getServiceProvider().getCurrentServiceSelectors(getBeanContextServicesPeer(), serviceClass)) : null;
    }
     public void serviceAvailable(BeanContextServiceAvailableEvent bcssae) {
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(bcssae.getServiceClass())) return;
            fireServiceAdded(bcssae);
            Iterator i;
            synchronized(children) {
                i = children.keySet().iterator();
            }
            while (i.hasNext()) {
                Object c = i.next();
                if (c instanceof BeanContextServices) {
                    ((BeanContextServicesListener)c).serviceAvailable(bcssae);
                }
            }
        }
     }
    public void serviceRevoked(BeanContextServiceRevokedEvent bcssre) {
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(bcssre.getServiceClass())) return;
            fireServiceRevoked(bcssre);
            Iterator i;
            synchronized(children) {
                i = children.keySet().iterator();
            }
            while (i.hasNext()) {
                Object c = i.next();
                if (c instanceof BeanContextServices) {
                    ((BeanContextServicesListener)c).serviceRevoked(bcssre);
                }
            }
        }
    }
    protected static final BeanContextServicesListener getChildBeanContextServicesListener(Object child) {
        try {
            return (BeanContextServicesListener)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }
    protected void childJustRemovedHook(Object child, BCSChild bcsc) {
        BCSSChild bcssc = (BCSSChild)bcsc;
        bcssc.cleanupReferences();
    }
    protected synchronized void releaseBeanContextResources() {
        Object[] bcssc;
        super.releaseBeanContextResources();
        synchronized(children) {
            if (children.isEmpty()) return;
            bcssc = children.values().toArray();
        }
        for (int i = 0; i < bcssc.length; i++) {
            ((BCSSChild)bcssc[i]).revokeAllDelegatedServicesNow();
        }
        proxy = null;
    }
    protected synchronized void initializeBeanContextResources() {
        super.initializeBeanContextResources();
        BeanContext nbc = getBeanContext();
        if (nbc == null) return;
        try {
            BeanContextServices bcs = (BeanContextServices)nbc;
            proxy = new BCSSProxyServiceProvider(bcs);
        } catch (ClassCastException cce) {
        }
    }
    protected final void fireServiceAdded(Class serviceClass) {
        BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(), serviceClass);
        fireServiceAdded(bcssae);
    }
    protected final void fireServiceAdded(BeanContextServiceAvailableEvent bcssae) {
        Object[]                         copy;
        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }
        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServicesListener)copy[i]).serviceAvailable(bcssae);
        }
    }
    protected final void fireServiceRevoked(BeanContextServiceRevokedEvent bcsre) {
        Object[]                         copy;
        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }
        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServiceRevokedListener)copy[i]).serviceRevoked(bcsre);
        }
    }
    protected final void fireServiceRevoked(Class serviceClass, boolean revokeNow) {
        Object[]                       copy;
        BeanContextServiceRevokedEvent bcsre = new BeanContextServiceRevokedEvent(getBeanContextServicesPeer(), serviceClass, revokeNow);
        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }
        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServicesListener)copy[i]).serviceRevoked(bcsre);
        }
   }
    protected synchronized void bcsPreSerializationHook(ObjectOutputStream oos) throws IOException {
        oos.writeInt(serializable);
        if (serializable <= 0) return;
        int count = 0;
        Iterator i = services.entrySet().iterator();
        while (i.hasNext() && count < serializable) {
            Map.Entry           entry = (Map.Entry)i.next();
            BCSSServiceProvider bcsp  = null;
             try {
                bcsp = (BCSSServiceProvider)entry.getValue();
             } catch (ClassCastException cce) {
                continue;
             }
             if (bcsp.getServiceProvider() instanceof Serializable) {
                oos.writeObject(entry.getKey());
                oos.writeObject(bcsp);
                count++;
             }
        }
        if (count != serializable)
            throw new IOException("wrote different number of service providers than expected");
    }
    protected synchronized void bcsPreDeserializationHook(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        serializable = ois.readInt();
        int count = serializable;
        while (count > 0) {
            services.put(ois.readObject(), ois.readObject());
            count--;
        }
    }
    private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        serialize(oos, (Collection)bcsListeners);
    }
    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        deserialize(ois, (Collection)bcsListeners);
    }
    protected transient HashMap                  services;
    protected transient int                      serializable = 0;
    protected transient BCSSProxyServiceProvider proxy;
    protected transient ArrayList                bcsListeners;
}
