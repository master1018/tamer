public class DefaultMBeanServerInterceptor implements MBeanServerInterceptor {
    private final transient MBeanInstantiator instantiator;
    private transient MBeanServer server = null;
    private final transient MBeanServerDelegate delegate;
    private final transient Repository repository;
    private final transient
        WeakHashMap<ListenerWrapper, WeakReference<ListenerWrapper>>
            listenerWrappers =
                new WeakHashMap<ListenerWrapper,
                                WeakReference<ListenerWrapper>>();
    private final String domain;
    public DefaultMBeanServerInterceptor(MBeanServer         outer,
                                         MBeanServerDelegate delegate,
                                         MBeanInstantiator   instantiator,
                                         Repository          repository) {
        if (outer == null) throw new
            IllegalArgumentException("outer MBeanServer cannot be null");
        if (delegate == null) throw new
            IllegalArgumentException("MBeanServerDelegate cannot be null");
        if (instantiator == null) throw new
            IllegalArgumentException("MBeanInstantiator cannot be null");
        if (repository == null) throw new
            IllegalArgumentException("Repository cannot be null");
        this.server   = outer;
        this.delegate = delegate;
        this.instantiator = instantiator;
        this.repository   = repository;
        this.domain       = repository.getDefaultDomain();
    }
    public ObjectInstance createMBean(String className, ObjectName name)
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException {
        return createMBean(className, name, (Object[]) null, (String[]) null);
    }
    public ObjectInstance createMBean(String className, ObjectName name,
                                      ObjectName loaderName)
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException, InstanceNotFoundException {
        return createMBean(className, name, loaderName, (Object[]) null,
                           (String[]) null);
    }
    public ObjectInstance createMBean(String className, ObjectName name,
                                      Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException  {
        try {
            return createMBean(className, name, null, true,
                               params, signature);
        } catch (InstanceNotFoundException e) {
            throw EnvHelp.initCause(
                new IllegalArgumentException("Unexpected exception: " + e), e);
        }
    }
    public ObjectInstance createMBean(String className, ObjectName name,
                                      ObjectName loaderName,
                                      Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException, InstanceNotFoundException  {
        return createMBean(className, name, loaderName, false,
                           params, signature);
    }
    private ObjectInstance createMBean(String className, ObjectName name,
                                       ObjectName loaderName,
                                       boolean withDefaultLoaderRepository,
                                       Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException, InstanceNotFoundException {
        Class<?> theClass;
        if (className == null) {
            final RuntimeException wrapped =
                new IllegalArgumentException("The class name cannot be null");
            throw new RuntimeOperationsException(wrapped,
                      "Exception occurred during MBean creation");
        }
        if (name != null) {
            if (name.isPattern()) {
                final RuntimeException wrapped =
                    new IllegalArgumentException("Invalid name->" +
                                                 name.toString());
                final String msg = "Exception occurred during MBean creation";
                throw new RuntimeOperationsException(wrapped, msg);
            }
            name = nonDefaultDomain(name);
        }
        checkMBeanPermission(className, null, null, "instantiate");
        checkMBeanPermission(className, null, name, "registerMBean");
        if (withDefaultLoaderRepository) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "createMBean",
                        "ClassName = " + className + ", ObjectName = " + name);
            }
            theClass =
                instantiator.findClassWithDefaultLoaderRepository(className);
        } else if (loaderName == null) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "createMBean", "ClassName = " + className +
                        ", ObjectName = " + name + ", Loader name = null");
            }
            theClass = instantiator.findClass(className,
                                  server.getClass().getClassLoader());
        } else {
            loaderName = nonDefaultDomain(loaderName);
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "createMBean", "ClassName = " + className +
                        ", ObjectName = " + name +
                        ", Loader name = " + loaderName);
            }
            theClass = instantiator.findClass(className, loaderName);
        }
        checkMBeanTrustPermission(theClass);
        Introspector.testCreation(theClass);
        Introspector.checkCompliance(theClass);
        Object moi= instantiator.instantiate(theClass, params,  signature,
                                             server.getClass().getClassLoader());
        final String infoClassName = getNewMBeanClassName(moi);
        return registerObject(infoClassName, moi, name);
    }
    public ObjectInstance registerMBean(Object object, ObjectName name)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
        NotCompliantMBeanException  {
        Class<?> theClass = object.getClass();
        Introspector.checkCompliance(theClass);
        final String infoClassName = getNewMBeanClassName(object);
        checkMBeanPermission(infoClassName, null, name, "registerMBean");
        checkMBeanTrustPermission(theClass);
        return registerObject(infoClassName, object, name);
    }
    private static String getNewMBeanClassName(Object mbeanToRegister)
            throws NotCompliantMBeanException {
        if (mbeanToRegister instanceof DynamicMBean) {
            DynamicMBean mbean = (DynamicMBean) mbeanToRegister;
            final String name;
            try {
                name = mbean.getMBeanInfo().getClassName();
            } catch (Exception e) {
                NotCompliantMBeanException ncmbe =
                    new NotCompliantMBeanException("Bad getMBeanInfo()");
                ncmbe.initCause(e);
                throw ncmbe;
            }
            if (name == null) {
                final String msg = "MBeanInfo has null class name";
                throw new NotCompliantMBeanException(msg);
            }
            return name;
        } else
            return mbeanToRegister.getClass().getName();
    }
    private final Set<ObjectName> beingUnregistered =
        new HashSet<ObjectName>();
    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException  {
        if (name == null) {
            final RuntimeException wrapped =
                new IllegalArgumentException("Object name cannot be null");
            throw new RuntimeOperationsException(wrapped,
                      "Exception occurred trying to unregister the MBean");
        }
        name = nonDefaultDomain(name);
        synchronized (beingUnregistered) {
            while (beingUnregistered.contains(name)) {
                try {
                    beingUnregistered.wait();
                } catch (InterruptedException e) {
                    throw new MBeanRegistrationException(e, e.toString());
                }
            }
            beingUnregistered.add(name);
        }
        try {
            exclusiveUnregisterMBean(name);
        } finally {
            synchronized (beingUnregistered) {
                beingUnregistered.remove(name);
                beingUnregistered.notifyAll();
            }
        }
    }
    private void exclusiveUnregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException {
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, null, name, "unregisterMBean");
        if (instance instanceof MBeanRegistration)
            preDeregisterInvoke((MBeanRegistration) instance);
        final Object resource = getResource(instance);
        final ResourceContext context =
                unregisterFromRepository(resource, instance, name);
        try {
            if (instance instanceof MBeanRegistration)
                postDeregisterInvoke(name,(MBeanRegistration) instance);
        } finally {
            context.done();
        }
    }
    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException {
        name = nonDefaultDomain(name);
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, null, name, "getObjectInstance");
        final String className = getClassName(instance);
        return new ObjectInstance(name, className);
    }
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMBeanPermission((String) null, null, null, "queryMBeans");
            Set<ObjectInstance> list = queryMBeansImpl(name, null);
            Set<ObjectInstance> allowedList =
                new HashSet<ObjectInstance>(list.size());
            for (ObjectInstance oi : list) {
                try {
                    checkMBeanPermission(oi.getClassName(), null,
                                         oi.getObjectName(), "queryMBeans");
                    allowedList.add(oi);
                } catch (SecurityException e) {
                }
            }
            return filterListOfObjectInstances(allowedList, query);
        } else {
            return queryMBeansImpl(name, query);
        }
    }
    private Set<ObjectInstance> queryMBeansImpl(ObjectName name,
                                                QueryExp query) {
        Set<NamedObject> list = repository.query(name, query);
        return (objectInstancesFromFilteredNamedObjects(list, query));
    }
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        Set<ObjectName> queryList;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMBeanPermission((String) null, null, null, "queryNames");
            Set<ObjectInstance> list = queryMBeansImpl(name, null);
            Set<ObjectInstance> allowedList =
                new HashSet<ObjectInstance>(list.size());
            for (ObjectInstance oi : list) {
                try {
                    checkMBeanPermission(oi.getClassName(), null,
                                         oi.getObjectName(), "queryNames");
                    allowedList.add(oi);
                } catch (SecurityException e) {
                }
            }
            Set<ObjectInstance> queryObjectInstanceList =
                filterListOfObjectInstances(allowedList, query);
            queryList = new HashSet<ObjectName>(queryObjectInstanceList.size());
            for (ObjectInstance oi : queryObjectInstanceList) {
                queryList.add(oi.getObjectName());
            }
        } else {
            queryList = queryNamesImpl(name, query);
        }
        return queryList;
    }
    private Set<ObjectName> queryNamesImpl(ObjectName name, QueryExp query) {
        Set<NamedObject> list = repository.query(name, query);
        return (objectNamesFromFilteredNamedObjects(list, query));
    }
    public boolean isRegistered(ObjectName name) {
        if (name == null) {
            throw new RuntimeOperationsException(
                     new IllegalArgumentException("Object name cannot be null"),
                     "Object name cannot be null");
        }
        name = nonDefaultDomain(name);
        return (repository.contains(name));
    }
    public String[] getDomains()  {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMBeanPermission((String) null, null, null, "getDomains");
            String[] domains = repository.getDomains();
            List<String> result = new ArrayList<String>(domains.length);
            for (int i = 0; i < domains.length; i++) {
                try {
                    ObjectName dom = Util.newObjectName(domains[i] + ":x=x");
                    checkMBeanPermission((String) null, null, dom, "getDomains");
                    result.add(domains[i]);
                } catch (SecurityException e) {
                }
            }
            return result.toArray(new String[result.size()]);
        } else {
            return repository.getDomains();
        }
    }
    public Integer getMBeanCount() {
        return (repository.getCount());
    }
    public Object getAttribute(ObjectName name, String attribute)
        throws MBeanException, AttributeNotFoundException,
               InstanceNotFoundException, ReflectionException {
        if (name == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Object name cannot be null"),
                "Exception occurred trying to invoke the getter on the MBean");
        }
        if (attribute == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Attribute cannot be null"),
                "Exception occurred trying to invoke the getter on the MBean");
        }
        name = nonDefaultDomain(name);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "getAttribute",
                    "Attribute = " + attribute + ", ObjectName = " + name);
        }
        final DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, attribute, name, "getAttribute");
        try {
            return instance.getAttribute(attribute);
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            rethrowMaybeMBeanException(t);
            throw new AssertionError(); 
        }
    }
    public AttributeList getAttributes(ObjectName name, String[] attributes)
        throws InstanceNotFoundException, ReflectionException  {
        if (name == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("ObjectName name cannot be null"),
                "Exception occurred trying to invoke the getter on the MBean");
        }
        if (attributes == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Attributes cannot be null"),
                "Exception occurred trying to invoke the getter on the MBean");
        }
        name = nonDefaultDomain(name);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "getAttributes", "ObjectName = " + name);
        }
        final DynamicMBean instance = getMBean(name);
        final String[] allowedAttributes;
        final SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            allowedAttributes = attributes;
        else {
            final String classname = getClassName(instance);
            checkMBeanPermission(classname, null, name, "getAttribute");
            List<String> allowedList =
                new ArrayList<String>(attributes.length);
            for (String attr : attributes) {
                try {
                    checkMBeanPermission(classname, attr, name, "getAttribute");
                    allowedList.add(attr);
                } catch (SecurityException e) {
                }
            }
            allowedAttributes =
                    allowedList.toArray(new String[allowedList.size()]);
        }
        try {
            return instance.getAttributes(allowedAttributes);
        } catch (Throwable t) {
            rethrow(t);
            throw new AssertionError();
        }
    }
    public void setAttribute(ObjectName name, Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
               InvalidAttributeValueException, MBeanException,
               ReflectionException  {
        if (name == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("ObjectName name cannot be null"),
                "Exception occurred trying to invoke the setter on the MBean");
        }
        if (attribute == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Attribute cannot be null"),
                "Exception occurred trying to invoke the setter on the MBean");
        }
        name = nonDefaultDomain(name);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "setAttribute", "ObjectName = " + name +
                    ", Attribute = " + attribute.getName());
        }
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, attribute.getName(), name, "setAttribute");
        try {
            instance.setAttribute(attribute);
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (InvalidAttributeValueException e) {
            throw e;
        } catch (Throwable t) {
            rethrowMaybeMBeanException(t);
            throw new AssertionError();
        }
    }
    public AttributeList setAttributes(ObjectName name,
                                       AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException  {
        if (name == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("ObjectName name cannot be null"),
                "Exception occurred trying to invoke the setter on the MBean");
        }
        if (attributes == null) {
            throw new RuntimeOperationsException(new
            IllegalArgumentException("AttributeList  cannot be null"),
            "Exception occurred trying to invoke the setter on the MBean");
        }
        name = nonDefaultDomain(name);
        final DynamicMBean instance = getMBean(name);
        final AttributeList allowedAttributes;
        final SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            allowedAttributes = attributes;
        else {
            String classname = getClassName(instance);
            checkMBeanPermission(classname, null, name, "setAttribute");
            allowedAttributes = new AttributeList(attributes.size());
            for (Attribute attribute : attributes.asList()) {
                try {
                    checkMBeanPermission(classname, attribute.getName(),
                                         name, "setAttribute");
                    allowedAttributes.add(attribute);
                } catch (SecurityException e) {
                }
            }
        }
        try {
            return instance.setAttributes(allowedAttributes);
        } catch (Throwable t) {
            rethrow(t);
            throw new AssertionError();
        }
    }
    public Object invoke(ObjectName name, String operationName,
                         Object params[], String signature[])
            throws InstanceNotFoundException, MBeanException,
                   ReflectionException {
        name = nonDefaultDomain(name);
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, operationName, name, "invoke");
        try {
            return instance.invoke(operationName, params, signature);
        } catch (Throwable t) {
            rethrowMaybeMBeanException(t);
            throw new AssertionError();
        }
    }
    private static void rethrow(Throwable t)
            throws ReflectionException {
        try {
            throw t;
        } catch (ReflectionException e) {
            throw e;
        } catch (RuntimeOperationsException e) {
            throw e;
        } catch (RuntimeErrorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeMBeanException(e, e.toString());
        } catch (Error e) {
            throw new RuntimeErrorException(e, e.toString());
        } catch (Throwable t2) {
            throw new RuntimeException("Unexpected exception", t2);
        }
    }
    private static void rethrowMaybeMBeanException(Throwable t)
            throws ReflectionException, MBeanException {
        if (t instanceof MBeanException)
            throw (MBeanException) t;
        rethrow(t);
    }
    private ObjectInstance registerObject(String classname,
                                          Object object, ObjectName name)
        throws InstanceAlreadyExistsException,
               MBeanRegistrationException,
               NotCompliantMBeanException {
        if (object == null) {
            final RuntimeException wrapped =
                new IllegalArgumentException("Cannot add null object");
            throw new RuntimeOperationsException(wrapped,
                        "Exception occurred trying to register the MBean");
        }
        DynamicMBean mbean = Introspector.makeDynamicMBean(object);
        return registerDynamicMBean(classname, mbean, name);
    }
    private ObjectInstance registerDynamicMBean(String classname,
                                                DynamicMBean mbean,
                                                ObjectName name)
        throws InstanceAlreadyExistsException,
               MBeanRegistrationException,
               NotCompliantMBeanException {
        name = nonDefaultDomain(name);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "registerMBean", "ObjectName = " + name);
        }
        ObjectName logicalName = preRegister(mbean, server, name);
        boolean registered = false;
        boolean registerFailed = false;
        ResourceContext context = null;
        try {
            if (mbean instanceof DynamicMBean2) {
                try {
                    ((DynamicMBean2) mbean).preRegister2(server, logicalName);
                    registerFailed = true;  
                } catch (Exception e) {
                    if (e instanceof RuntimeException)
                        throw (RuntimeException) e;
                    if (e instanceof InstanceAlreadyExistsException)
                        throw (InstanceAlreadyExistsException) e;
                    throw new RuntimeException(e);
                }
            }
            if (logicalName != name && logicalName != null) {
                logicalName =
                        ObjectName.getInstance(nonDefaultDomain(logicalName));
            }
            checkMBeanPermission(classname, null, logicalName, "registerMBean");
            if (logicalName == null) {
                final RuntimeException wrapped =
                    new IllegalArgumentException("No object name specified");
                throw new RuntimeOperationsException(wrapped,
                            "Exception occurred trying to register the MBean");
            }
            final Object resource = getResource(mbean);
            context = registerWithRepository(resource, mbean, logicalName);
            registerFailed = false;
            registered = true;
        } finally {
            try {
                postRegister(logicalName, mbean, registered, registerFailed);
            } finally {
                if (registered && context!=null) context.done();
            }
        }
        return new ObjectInstance(logicalName, classname);
    }
    private static void throwMBeanRegistrationException(Throwable t, String where)
    throws MBeanRegistrationException {
        if (t instanceof RuntimeException) {
            throw new RuntimeMBeanException((RuntimeException)t,
                    "RuntimeException thrown " + where);
        } else if (t instanceof Error) {
            throw new RuntimeErrorException((Error)t,
                    "Error thrown " + where);
        } else if (t instanceof MBeanRegistrationException) {
            throw (MBeanRegistrationException)t;
        } else if (t instanceof Exception) {
            throw new MBeanRegistrationException((Exception)t,
                    "Exception thrown " + where);
        } else 
            throw new RuntimeException(t);
    }
    private static ObjectName preRegister(
            DynamicMBean mbean, MBeanServer mbs, ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        ObjectName newName = null;
        try {
            if (mbean instanceof MBeanRegistration)
                newName = ((MBeanRegistration) mbean).preRegister(mbs, name);
        } catch (Throwable t) {
            throwMBeanRegistrationException(t, "in preRegister method");
        }
        if (newName != null) return newName;
        else return name;
    }
    private static void postRegister(
            ObjectName logicalName, DynamicMBean mbean,
            boolean registrationDone, boolean registerFailed) {
        if (registerFailed && mbean instanceof DynamicMBean2)
            ((DynamicMBean2) mbean).registerFailed();
        try {
            if (mbean instanceof MBeanRegistration)
                ((MBeanRegistration) mbean).postRegister(registrationDone);
        } catch (RuntimeException e) {
            MBEANSERVER_LOGGER.fine("While registering MBean ["+logicalName+
                    "]: " + "Exception thrown by postRegister: " +
                    "rethrowing <"+e+">, but keeping the MBean registered");
            throw new RuntimeMBeanException(e,
                      "RuntimeException thrown in postRegister method: "+
                      "rethrowing <"+e+">, but keeping the MBean registered");
        } catch (Error er) {
            MBEANSERVER_LOGGER.fine("While registering MBean ["+logicalName+
                    "]: " + "Error thrown by postRegister: " +
                    "rethrowing <"+er+">, but keeping the MBean registered");
            throw new RuntimeErrorException(er,
                      "Error thrown in postRegister method: "+
                      "rethrowing <"+er+">, but keeping the MBean registered");
        }
    }
    private static void preDeregisterInvoke(MBeanRegistration moi)
            throws MBeanRegistrationException {
        try {
            moi.preDeregister();
        } catch (Throwable t) {
            throwMBeanRegistrationException(t, "in preDeregister method");
        }
    }
    private static void postDeregisterInvoke(ObjectName mbean,
            MBeanRegistration moi) {
        try {
            moi.postDeregister();
        } catch (RuntimeException e) {
            MBEANSERVER_LOGGER.fine("While unregistering MBean ["+mbean+
                    "]: " + "Exception thrown by postDeregister: " +
                    "rethrowing <"+e+">, although the MBean is succesfully " +
                    "unregistered");
            throw new RuntimeMBeanException(e,
                      "RuntimeException thrown in postDeregister method: "+
                      "rethrowing <"+e+
                      ">, although the MBean is sucessfully unregistered");
        } catch (Error er) {
            MBEANSERVER_LOGGER.fine("While unregistering MBean ["+mbean+
                    "]: " + "Error thrown by postDeregister: " +
                    "rethrowing <"+er+">, although the MBean is succesfully " +
                    "unregistered");
            throw new RuntimeErrorException(er,
                      "Error thrown in postDeregister method: "+
                      "rethrowing <"+er+
                      ">, although the MBean is sucessfully unregistered");
        }
    }
    private DynamicMBean getMBean(ObjectName name)
        throws InstanceNotFoundException {
        if (name == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Object name cannot be null"),
                               "Exception occurred trying to get an MBean");
        }
        DynamicMBean obj = repository.retrieve(name);
        if (obj == null) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "getMBean", name + " : Found no object");
            }
            throw new InstanceNotFoundException(name.toString());
        }
        return obj;
    }
    private static Object getResource(DynamicMBean mbean) {
        if (mbean instanceof DynamicMBean2)
            return ((DynamicMBean2) mbean).getResource();
        else
            return mbean;
    }
    private ObjectName nonDefaultDomain(ObjectName name) {
        if (name == null || name.getDomain().length() > 0)
            return name;
        final String completeName = domain + name;
        return Util.newObjectName(completeName);
    }
    public String getDefaultDomain()  {
        return domain;
    }
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException {
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "addNotificationListener", "ObjectName = " + name);
        }
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, null, name, "addNotificationListener");
        NotificationBroadcaster broadcaster =
                getNotificationBroadcaster(name, instance,
                                           NotificationBroadcaster.class);
        if (listener == null) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException("Null listener"),"Null listener");
        }
        NotificationListener listenerWrapper =
            getListenerWrapper(listener, name, instance, true);
        broadcaster.addNotificationListener(listenerWrapper, filter, handback);
    }
    public void addNotificationListener(ObjectName name,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException {
        DynamicMBean instance = getMBean(listener);
        Object resource = getResource(instance);
        if (!(resource instanceof NotificationListener)) {
            throw new RuntimeOperationsException(new
                IllegalArgumentException(listener.getCanonicalName()),
                "The MBean " + listener.getCanonicalName() +
                "does not implement the NotificationListener interface") ;
        }
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "addNotificationListener",
                    "ObjectName = " + name + ", Listener = " + listener);
        }
        server.addNotificationListener(name,(NotificationListener) resource,
                                       filter, handback) ;
    }
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        removeNotificationListener(name, listener, null, null, true);
    }
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        removeNotificationListener(name, listener, filter, handback, false);
    }
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        NotificationListener instance = getListener(listener);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "removeNotificationListener",
                    "ObjectName = " + name + ", Listener = " + listener);
        }
        server.removeNotificationListener(name, instance);
    }
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        NotificationListener instance = getListener(listener);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "removeNotificationListener",
                    "ObjectName = " + name + ", Listener = " + listener);
        }
        server.removeNotificationListener(name, instance, filter, handback);
    }
    private NotificationListener getListener(ObjectName listener)
        throws ListenerNotFoundException {
        DynamicMBean instance;
        try {
            instance = getMBean(listener);
        } catch (InstanceNotFoundException e) {
            throw EnvHelp.initCause(
                          new ListenerNotFoundException(e.getMessage()), e);
        }
        Object resource = getResource(instance);
        if (!(resource instanceof NotificationListener)) {
            final RuntimeException exc =
                new IllegalArgumentException(listener.getCanonicalName());
            final String msg =
                "MBean " + listener.getCanonicalName() + " does not " +
                "implement " + NotificationListener.class.getName();
            throw new RuntimeOperationsException(exc, msg);
        }
        return (NotificationListener) resource;
    }
    private void removeNotificationListener(ObjectName name,
                                            NotificationListener listener,
                                            NotificationFilter filter,
                                            Object handback,
                                            boolean removeAll)
            throws InstanceNotFoundException, ListenerNotFoundException {
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "removeNotificationListener", "ObjectName = " + name);
        }
        DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, null, name, "removeNotificationListener");
        Class<? extends NotificationBroadcaster> reqClass =
            removeAll ? NotificationBroadcaster.class : NotificationEmitter.class;
        NotificationBroadcaster broadcaster =
            getNotificationBroadcaster(name, instance, reqClass);
        NotificationListener listenerWrapper =
            getListenerWrapper(listener, name, instance, false);
        if (listenerWrapper == null)
            throw new ListenerNotFoundException("Unknown listener");
        if (removeAll)
            broadcaster.removeNotificationListener(listenerWrapper);
        else {
            NotificationEmitter emitter = (NotificationEmitter) broadcaster;
            emitter.removeNotificationListener(listenerWrapper,
                                               filter,
                                               handback);
        }
    }
    private static <T extends NotificationBroadcaster>
            T getNotificationBroadcaster(ObjectName name, Object instance,
                                         Class<T> reqClass) {
        if (reqClass.isInstance(instance))
            return reqClass.cast(instance);
        if (instance instanceof DynamicMBean2)
            instance = ((DynamicMBean2) instance).getResource();
        if (reqClass.isInstance(instance))
            return reqClass.cast(instance);
        final RuntimeException exc =
            new IllegalArgumentException(name.getCanonicalName());
        final String msg =
            "MBean " + name.getCanonicalName() + " does not " +
            "implement " + reqClass.getName();
        throw new RuntimeOperationsException(exc, msg);
    }
    public MBeanInfo getMBeanInfo(ObjectName name)
        throws InstanceNotFoundException, IntrospectionException,
               ReflectionException {
        DynamicMBean moi = getMBean(name);
        final MBeanInfo mbi;
        try {
            mbi = moi.getMBeanInfo();
        } catch (RuntimeMBeanException e) {
            throw e;
        } catch (RuntimeErrorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeMBeanException(e,
                    "getMBeanInfo threw RuntimeException");
        } catch (Error e) {
            throw new RuntimeErrorException(e, "getMBeanInfo threw Error");
        }
        if (mbi == null)
            throw new JMRuntimeException("MBean " + name +
                                         "has no MBeanInfo");
        checkMBeanPermission(mbi.getClassName(), null, name, "getMBeanInfo");
        return mbi;
    }
    public boolean isInstanceOf(ObjectName name, String className)
        throws InstanceNotFoundException {
        final DynamicMBean instance = getMBean(name);
        checkMBeanPermission(instance, null, name, "isInstanceOf");
        try {
            Object resource = getResource(instance);
            final String resourceClassName =
                    (resource instanceof DynamicMBean) ?
                        getClassName((DynamicMBean) resource) :
                        resource.getClass().getName();
            if (resourceClassName.equals(className))
                return true;
            final ClassLoader cl = resource.getClass().getClassLoader();
            final Class<?> classNameClass = Class.forName(className, false, cl);
            if (classNameClass.isInstance(resource))
                return true;
            final Class<?> resourceClass = Class.forName(resourceClassName, false, cl);
            return classNameClass.isAssignableFrom(resourceClass);
        } catch (Exception x) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINEST)) {
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "isInstanceOf", "Exception calling isInstanceOf", x);
            }
            return false;
        }
    }
    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
        throws InstanceNotFoundException {
        DynamicMBean instance = getMBean(mbeanName);
        checkMBeanPermission(instance, null, mbeanName, "getClassLoaderFor");
        return getResource(instance).getClass().getClassLoader();
    }
    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException {
        if (loaderName == null) {
            checkMBeanPermission((String) null, null, null, "getClassLoader");
            return server.getClass().getClassLoader();
        }
        DynamicMBean instance = getMBean(loaderName);
        checkMBeanPermission(instance, null, loaderName, "getClassLoader");
        Object resource = getResource(instance);
        if (!(resource instanceof ClassLoader))
            throw new InstanceNotFoundException(loaderName.toString() +
                                                " is not a classloader");
        return (ClassLoader) resource;
    }
    private void sendNotification(String NotifType, ObjectName name) {
        MBeanServerNotification notif = new MBeanServerNotification(
            NotifType,MBeanServerDelegate.DELEGATE_NAME,0,name);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "sendNotification", NotifType + " " + name);
        }
        delegate.sendNotification(notif);
    }
    private Set<ObjectName>
        objectNamesFromFilteredNamedObjects(Set<NamedObject> list,
                                            QueryExp query) {
        Set<ObjectName> result = new HashSet<ObjectName>();
        if (query == null) {
            for (NamedObject no : list) {
                result.add(no.getName());
            }
        } else {
            final MBeanServer oldServer = QueryEval.getMBeanServer();
            query.setMBeanServer(server);
            try {
                for (NamedObject no : list) {
                    boolean res;
                    try {
                        res = query.apply(no.getName());
                    } catch (Exception e) {
                        res = false;
                    }
                    if (res) {
                        result.add(no.getName());
                    }
                }
            } finally {
                query.setMBeanServer(oldServer);
            }
        }
        return result;
    }
    private Set<ObjectInstance>
        objectInstancesFromFilteredNamedObjects(Set<NamedObject> list,
                                                QueryExp query) {
        Set<ObjectInstance> result = new HashSet<ObjectInstance>();
        if (query == null) {
            for (NamedObject no : list) {
                final DynamicMBean obj = no.getObject();
                final String className = safeGetClassName(obj);
                result.add(new ObjectInstance(no.getName(), className));
            }
        } else {
            MBeanServer oldServer = QueryEval.getMBeanServer();
            query.setMBeanServer(server);
            try {
                for (NamedObject no : list) {
                    final DynamicMBean obj = no.getObject();
                    boolean res;
                    try {
                        res = query.apply(no.getName());
                    } catch (Exception e) {
                        res = false;
                    }
                    if (res) {
                        String className = safeGetClassName(obj);
                        result.add(new ObjectInstance(no.getName(), className));
                    }
                }
            } finally {
                query.setMBeanServer(oldServer);
            }
        }
        return result;
    }
    private static String safeGetClassName(DynamicMBean mbean) {
        try {
            return getClassName(mbean);
        } catch (Exception e) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINEST)) {
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        DefaultMBeanServerInterceptor.class.getName(),
                        "safeGetClassName",
                        "Exception getting MBean class name", e);
            }
            return null;
        }
    }
    private Set<ObjectInstance>
            filterListOfObjectInstances(Set<ObjectInstance> list,
                                        QueryExp query) {
        if (query == null) {
            return list;
        } else {
            Set<ObjectInstance> result = new HashSet<ObjectInstance>();
            for (ObjectInstance oi : list) {
                boolean res = false;
                MBeanServer oldServer = QueryEval.getMBeanServer();
                query.setMBeanServer(server);
                try {
                    res = query.apply(oi.getObjectName());
                } catch (Exception e) {
                    res = false;
                } finally {
                    query.setMBeanServer(oldServer);
                }
                if (res) {
                    result.add(oi);
                }
            }
            return result;
        }
    }
    private NotificationListener getListenerWrapper(NotificationListener l,
                                                    ObjectName name,
                                                    DynamicMBean mbean,
                                                    boolean create) {
        Object resource = getResource(mbean);
        ListenerWrapper wrapper = new ListenerWrapper(l, name, resource);
        synchronized (listenerWrappers) {
            WeakReference<ListenerWrapper> ref = listenerWrappers.get(wrapper);
            if (ref != null) {
                NotificationListener existing = ref.get();
                if (existing != null)
                    return existing;
            }
            if (create) {
                ref = new WeakReference<ListenerWrapper>(wrapper);
                listenerWrappers.put(wrapper, ref);
                return wrapper;
            } else
                return null;
        }
    }
    public Object instantiate(String className) throws ReflectionException,
                                                       MBeanException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException,
                                                                              MBeanException,
                                                                              InstanceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public Object instantiate(String className, Object[] params,
            String[] signature) throws ReflectionException, MBeanException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public Object instantiate(String className, ObjectName loaderName,
            Object[] params, String[] signature) throws ReflectionException,
                                                        MBeanException,
                                                        InstanceNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException,
                                                                              OperationsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException,
                                                                               ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ObjectInputStream deserialize(String className, ObjectName loaderName,
            byte[] data) throws InstanceNotFoundException, OperationsException,
                                ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public ClassLoaderRepository getClassLoaderRepository() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private static class ListenerWrapper implements NotificationListener {
        ListenerWrapper(NotificationListener l, ObjectName name,
                        Object mbean) {
            this.listener = l;
            this.name = name;
            this.mbean = mbean;
        }
        public void handleNotification(Notification notification,
                                       Object handback) {
            if (notification != null) {
                if (notification.getSource() == mbean)
                    notification.setSource(name);
            }
            listener.handleNotification(notification, handback);
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ListenerWrapper))
                return false;
            ListenerWrapper w = (ListenerWrapper) o;
            return (w.listener == listener && w.mbean == mbean
                    && w.name.equals(name));
        }
        @Override
        public int hashCode() {
            return (System.identityHashCode(listener) ^
                    System.identityHashCode(mbean));
        }
        private NotificationListener listener;
        private ObjectName name;
        private Object mbean;
    }
    private static String getClassName(DynamicMBean mbean) {
        if (mbean instanceof DynamicMBean2)
            return ((DynamicMBean2) mbean).getClassName();
        else
            return mbean.getMBeanInfo().getClassName();
    }
    private static void checkMBeanPermission(DynamicMBean mbean,
                                             String member,
                                             ObjectName objectName,
                                             String actions) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMBeanPermission(safeGetClassName(mbean),
                                 member,
                                 objectName,
                                 actions);
        }
    }
    private static void checkMBeanPermission(String classname,
                                             String member,
                                             ObjectName objectName,
                                             String actions) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Permission perm = new MBeanPermission(classname,
                                                  member,
                                                  objectName,
                                                  actions);
            sm.checkPermission(perm);
        }
    }
    private static void checkMBeanTrustPermission(final Class<?> theClass)
        throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Permission perm = new MBeanTrustPermission("register");
            PrivilegedAction<ProtectionDomain> act =
                new PrivilegedAction<ProtectionDomain>() {
                    public ProtectionDomain run() {
                        return theClass.getProtectionDomain();
                    }
                };
            ProtectionDomain pd = AccessController.doPrivileged(act);
            AccessControlContext acc =
                new AccessControlContext(new ProtectionDomain[] { pd });
            sm.checkPermission(perm, acc);
        }
    }
    private static interface ResourceContext extends RegistrationContext {
        public void done();
        public static final ResourceContext NONE = new ResourceContext() {
            public void done() {}
            public void registering() {}
            public void unregistered() {}
        };
    }
    private ResourceContext registerWithRepository(
            final Object resource,
            final DynamicMBean object,
            final ObjectName logicalName)
            throws InstanceAlreadyExistsException,
            MBeanRegistrationException {
        final ResourceContext context =
                makeResourceContextFor(resource, logicalName);
        repository.addMBean(object, logicalName, context);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "addObject", "Send create notification of object " +
                    logicalName.getCanonicalName());
        }
        sendNotification(
                MBeanServerNotification.REGISTRATION_NOTIFICATION,
                logicalName);
        return context;
    }
    private ResourceContext unregisterFromRepository(
            final Object resource,
            final DynamicMBean object,
            final ObjectName logicalName)
            throws InstanceNotFoundException {
        final ResourceContext context =
                makeResourceContextFor(resource, logicalName);
        repository.remove(logicalName, context);
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    DefaultMBeanServerInterceptor.class.getName(),
                    "unregisterMBean", "Send delete notification of object " +
                    logicalName.getCanonicalName());
        }
        sendNotification(MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
                logicalName);
        return context;
    }
    private void addClassLoader(ClassLoader loader,
            final ObjectName logicalName) {
        final ModifiableClassLoaderRepository clr =
                instantiator.getClassLoaderRepository();
        if (clr == null) {
            final RuntimeException wrapped =
                    new IllegalArgumentException(
                    "Dynamic addition of class loaders" +
                    " is not supported");
            throw new RuntimeOperationsException(wrapped,
                    "Exception occurred trying to register" +
                    " the MBean as a class loader");
        }
        clr.addClassLoader(logicalName, loader);
    }
    private void removeClassLoader(ClassLoader loader,
            final ObjectName logicalName) {
        if (loader != server.getClass().getClassLoader()) {
            final ModifiableClassLoaderRepository clr =
                    instantiator.getClassLoaderRepository();
            if (clr != null) {
                clr.removeClassLoader(logicalName);
            }
        }
    }
    private ResourceContext createClassLoaderContext(
            final ClassLoader loader,
            final ObjectName logicalName) {
        return new ResourceContext() {
            public void registering() {
                addClassLoader(loader, logicalName);
            }
            public void unregistered() {
                removeClassLoader(loader, logicalName);
            }
            public void done() {
            }
        };
    }
    private ResourceContext makeResourceContextFor(Object resource,
            ObjectName logicalName) {
        if (resource instanceof ClassLoader) {
            return createClassLoaderContext((ClassLoader) resource,
                    logicalName);
        }
        return ResourceContext.NONE;
    }
}
