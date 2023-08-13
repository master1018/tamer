public class CNCtx implements javax.naming.Context {
    private final static boolean debug = false;
    ORB _orb;                   
    public NamingContext _nc;   
    private NameComponent[] _name = null;
    Hashtable _env; 
    static final CNNameParser parser = new CNNameParser();
    private static final String FED_PROP = "com.sun.jndi.cosnaming.federation";
    boolean federation = false;
    OrbReuseTracker orbTracker = null;
    int enumCount;
    boolean isCloseCalled = false;
    CNCtx(Hashtable env) throws NamingException {
        if (env != null) {
            env = (Hashtable) env.clone();
        }
        _env = env;
        federation = "true".equals(env != null ? env.get(FED_PROP) : null);
        initOrbAndRootContext(env);
    }
    private CNCtx() {
    }
    public static ResolveResult createUsingURL(String url, Hashtable env)
    throws NamingException {
        CNCtx ctx = new CNCtx();
        if (env != null) {
            env = (Hashtable) env.clone();
        }
        ctx._env = env;
        String rest = ctx.initUsingUrl(
            env != null ?
                (org.omg.CORBA.ORB) env.get("java.naming.corba.orb")
                : null,
            url, env);
        return new ResolveResult(ctx, parser.parse(rest));
    }
    CNCtx(ORB orb, OrbReuseTracker tracker, NamingContext nctx, Hashtable env,
                        NameComponent[]name)
        throws NamingException {
            if (orb == null || nctx == null)
                throw new ConfigurationException(
                    "Must supply ORB or NamingContext");
            _orb = orb;
            orbTracker = tracker;
            if (orbTracker != null) {
                orbTracker.incRefCount();
            }
            _nc = nctx;
            _env = env;
            _name = name;
            federation = "true".equals(env != null ? env.get(FED_PROP) : null);
    }
    NameComponent[] makeFullName(NameComponent[] child) {
        if (_name == null || _name.length == 0) {
            return child;
        }
        NameComponent[] answer = new NameComponent[_name.length+child.length];
        System.arraycopy(_name, 0, answer, 0, _name.length);
        System.arraycopy(child, 0, answer, _name.length, child.length);
        return answer;
    }
    public String getNameInNamespace() throws NamingException {
        if (_name == null || _name.length == 0) {
            return "";
        }
        return CNNameParser.cosNameToInsString(_name);
    }
    private static boolean isCorbaUrl(String url) {
        return url.startsWith("iiop:
            || url.startsWith("iiopname:
            || url.startsWith("corbaname:")
            ;
    }
    private void initOrbAndRootContext(Hashtable env) throws NamingException {
        org.omg.CORBA.ORB inOrb = null;
        String ncIor = null;
        if (env != null) {
            inOrb = (org.omg.CORBA.ORB) env.get("java.naming.corba.orb");
        }
        String provUrl = null;
        if (env != null) {
            provUrl = (String)env.get(javax.naming.Context.PROVIDER_URL);
        }
        if (provUrl != null && !isCorbaUrl(provUrl)) {
            ncIor = getStringifiedIor(provUrl);
            if (inOrb == null) {
                inOrb = CorbaUtils.getOrb(null, -1, env);
                orbTracker = new OrbReuseTracker(inOrb);
            }
            setOrbAndRootContext(inOrb, ncIor);
        } else if (provUrl != null) {
            String insName = initUsingUrl(inOrb, provUrl, env);
            if (insName.length() > 0) {
                _name = parser.nameToCosName(parser.parse(insName));
                try {
                    org.omg.CORBA.Object obj = _nc.resolve(_name);
                    _nc = NamingContextHelper.narrow(obj);
                    if (_nc == null) {
                        throw new ConfigurationException(insName +
                            " does not name a NamingContext");
                    }
                } catch (org.omg.CORBA.BAD_PARAM e) {
                    throw new ConfigurationException(insName +
                        " does not name a NamingContext");
                } catch (Exception e) {
                    throw ExceptionMapper.mapException(e, this, _name);
                }
            }
        } else {
            if (inOrb == null) {
                inOrb = CorbaUtils.getOrb(null, -1, env);
                orbTracker = new OrbReuseTracker(inOrb);
                if (debug) {
                    System.err.println("Getting default ORB: " + inOrb + env);
                }
            }
            setOrbAndRootContext(inOrb, (String)null);
        }
    }
    private String initUsingUrl(ORB orb, String url, Hashtable env)
        throws NamingException {
        if (url.startsWith("iiop:
            return initUsingIiopUrl(orb, url, env);
        } else {
            return initUsingCorbanameUrl(orb, url, env);
        }
    }
    private String initUsingIiopUrl(ORB defOrb, String url, Hashtable env)
        throws NamingException {
        try {
            IiopUrl parsedUrl = new IiopUrl(url);
            Vector addrs = parsedUrl.getAddresses();
            IiopUrl.Address addr;
            NamingException savedException = null;
            for (int i = 0; i < addrs.size(); i++) {
                addr = (IiopUrl.Address)addrs.elementAt(i);
                try {
                    if (defOrb != null) {
                        try {
                            String tmpUrl = "corbaloc:iiop:" + addr.host
                                + ":" + addr.port + "/NameService";
                            if (debug) {
                                System.err.println("Using url: " + tmpUrl);
                            }
                            org.omg.CORBA.Object rootCtx =
                                defOrb.string_to_object(tmpUrl);
                            setOrbAndRootContext(defOrb, rootCtx);
                            return parsedUrl.getStringName();
                        } catch (Exception e) {} 
                    }
                    if (debug) {
                        System.err.println("Getting ORB for " + addr.host
                            + " and port " + addr.port);
                    }
                    ORB orb = CorbaUtils.getOrb(addr.host, addr.port, env);
                    orbTracker = new OrbReuseTracker(orb);
                    setOrbAndRootContext(orb, (String)null);
                    return parsedUrl.getStringName();
                } catch (NamingException ne) {
                    savedException = ne;
                }
            }
            if (savedException != null) {
                throw savedException;
            } else {
                throw new ConfigurationException("Problem with URL: " + url);
            }
        } catch (MalformedURLException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
    private String initUsingCorbanameUrl(ORB orb, String url, Hashtable env)
        throws NamingException {
        try {
            CorbanameUrl parsedUrl = new CorbanameUrl(url);
            String corbaloc = parsedUrl.getLocation();
            String cosName = parsedUrl.getStringName();
            if (orb == null) {
                orb = CorbaUtils.getOrb(null, -1, env);
                orbTracker = new OrbReuseTracker(orb);
            }
            setOrbAndRootContext(orb, corbaloc);
            return parsedUrl.getStringName();
        } catch (MalformedURLException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
    private void setOrbAndRootContext(ORB orb, String ncIor)
        throws NamingException {
        _orb = orb;
        try {
            org.omg.CORBA.Object ncRef;
            if (ncIor != null) {
                if (debug) {
                    System.err.println("Passing to string_to_object: " + ncIor);
                }
                ncRef = _orb.string_to_object(ncIor);
            } else {
                ncRef = _orb.resolve_initial_references("NameService");
            }
            if (debug) {
                System.err.println("Naming Context Ref: " + ncRef);
            }
            _nc = NamingContextHelper.narrow(ncRef);
            if (_nc == null) {
                if (ncIor != null) {
                    throw new ConfigurationException(
                        "Cannot convert IOR to a NamingContext: " + ncIor);
                } else {
                    throw new ConfigurationException(
"ORB.resolve_initial_references(\"NameService\") does not return a NamingContext");
                }
            }
        } catch (org.omg.CORBA.ORBPackage.InvalidName in) {
            NamingException ne =
                new ConfigurationException(
"COS Name Service not registered with ORB under the name 'NameService'");
            ne.setRootCause(in);
            throw ne;
        } catch (org.omg.CORBA.COMM_FAILURE e) {
            NamingException ne =
                new CommunicationException("Cannot connect to ORB");
            ne.setRootCause(e);
            throw ne;
        } catch (org.omg.CORBA.BAD_PARAM e) {
            NamingException ne = new ConfigurationException(
                "Invalid URL or IOR: " + ncIor);
            ne.setRootCause(e);
            throw ne;
        } catch (org.omg.CORBA.INV_OBJREF e) {
            NamingException ne = new ConfigurationException(
                "Invalid object reference: " + ncIor);
            ne.setRootCause(e);
            throw ne;
        }
    }
    private void setOrbAndRootContext(ORB orb, org.omg.CORBA.Object ncRef)
        throws NamingException {
        _orb = orb;
        try {
            _nc = NamingContextHelper.narrow(ncRef);
            if (_nc == null) {
                throw new ConfigurationException(
                    "Cannot convert object reference to NamingContext: " + ncRef);
            }
        } catch (org.omg.CORBA.COMM_FAILURE e) {
            NamingException ne =
                new CommunicationException("Cannot connect to ORB");
            ne.setRootCause(e);
            throw ne;
        }
    }
    private String getStringifiedIor(String url) throws NamingException {
        if (url.startsWith("IOR:") || url.startsWith("corbaloc:")) {
            return url;
        } else {
            InputStream in = null;
            try {
                URL u = new URL(url);
                in = u.openStream();
                if (in != null) {
                    BufferedReader bufin =
                        new BufferedReader(new InputStreamReader(in, "8859_1"));
                    String str;
                    while ((str = bufin.readLine()) != null) {
                        if (str.startsWith("IOR:")) {
                            return str;
                        }
                    }
                }
            } catch (IOException e) {
                NamingException ne =
                    new ConfigurationException("Invalid URL: " + url);
                ne.setRootCause(e);
                throw ne;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    NamingException ne =
                        new ConfigurationException("Invalid URL: " + url);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            throw new ConfigurationException(url + " does not contain an IOR");
        }
    }
    java.lang.Object callResolve(NameComponent[] path)
        throws NamingException {
            try {
                org.omg.CORBA.Object obj = _nc.resolve(path);
                try {
                    NamingContext nc =
                        NamingContextHelper.narrow(obj);
                    if (nc != null) {
                        return new CNCtx(_orb, orbTracker, nc, _env,
                                        makeFullName(path));
                    } else {
                        return obj;
                    }
                } catch (org.omg.CORBA.SystemException e) {
                    return obj;
                }
            } catch (Exception e) {
                throw ExceptionMapper.mapException(e, this, path);
            }
    }
    public java.lang.Object lookup(String name) throws NamingException {
        if (debug) {
            System.out.println("Looking up: " + name);
        }
        return lookup(new CompositeName(name));
    }
    public java.lang.Object lookup(Name name)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            if (name.size() == 0 )
                return this; 
            NameComponent[] path = CNNameParser.nameToCosName(name);
            try {
                java.lang.Object answer = callResolve(path);
                try {
                    return NamingManager.getObjectInstance(answer, name, this, _env);
                } catch (NamingException e) {
                    throw e;
                } catch (Exception e) {
                    NamingException ne = new NamingException(
                        "problem generating object using object factory");
                    ne.setRootCause(e);
                    throw ne;
                }
            } catch (CannotProceedException cpe) {
                javax.naming.Context cctx = getContinuationContext(cpe);
                return cctx.lookup(cpe.getRemainingName());
            }
    }
    private void callBindOrRebind(NameComponent[] pth, Name name,
        java.lang.Object obj, boolean rebind) throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            try {
                obj = NamingManager.getStateToBind(obj, name, this, _env);
                if (obj instanceof CNCtx) {
                    obj = ((CNCtx)obj)._nc;
                }
                if ( obj instanceof org.omg.CosNaming.NamingContext) {
                    NamingContext nobj =
                        NamingContextHelper.narrow((org.omg.CORBA.Object)obj);
                    if (rebind)
                        _nc.rebind_context(pth,nobj);
                    else
                        _nc.bind_context(pth,nobj);
                } else if (obj instanceof org.omg.CORBA.Object) {
                    if (rebind)
                        _nc.rebind(pth,(org.omg.CORBA.Object)obj);
                    else
                        _nc.bind(pth,(org.omg.CORBA.Object)obj);
                }
                else
                    throw new IllegalArgumentException(
                "Only instances of org.omg.CORBA.Object can be bound");
            } catch (BAD_PARAM e) {
                NamingException ne = new NotContextException(name.toString());
                ne.setRootCause(e);
                throw ne;
            } catch (Exception e) {
                throw ExceptionMapper.mapException(e, this, pth);
            }
    }
    public  void bind(Name name, java.lang.Object obj)
        throws NamingException {
            if (name.size() == 0 ) {
                throw new InvalidNameException("Name is empty");
            }
            if (debug) {
                System.out.println("Bind: " + name);
            }
            NameComponent[] path = CNNameParser.nameToCosName(name);
            try {
                callBindOrRebind(path, name, obj, false);
            } catch (CannotProceedException e) {
                javax.naming.Context cctx = getContinuationContext(e);
                cctx.bind(e.getRemainingName(), obj);
            }
    }
    static private javax.naming.Context
        getContinuationContext(CannotProceedException cpe)
        throws NamingException {
        try {
            return NamingManager.getContinuationContext(cpe);
        } catch (CannotProceedException e) {
            java.lang.Object resObj = e.getResolvedObj();
            if (resObj instanceof Reference) {
                Reference ref = (Reference)resObj;
                RefAddr addr = ref.get("nns");
                if (addr.getContent() instanceof javax.naming.Context) {
                    NamingException ne = new NameNotFoundException(
                        "No object reference bound for specified name");
                    ne.setRootCause(cpe.getRootCause());
                    ne.setRemainingName(cpe.getRemainingName());
                    throw ne;
                }
            }
            throw e;
        }
    }
    public void bind(String name, java.lang.Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }
    public  void rebind(Name name, java.lang.Object obj)
        throws NamingException {
            if (name.size() == 0 ) {
                throw new InvalidNameException("Name is empty");
            }
            NameComponent[] path = CNNameParser.nameToCosName(name);
            try {
                callBindOrRebind(path, name, obj, true);
            } catch (CannotProceedException e) {
                javax.naming.Context cctx = getContinuationContext(e);
                cctx.rebind(e.getRemainingName(), obj);
            }
    }
    public  void rebind(String name, java.lang.Object obj)
        throws NamingException {
            rebind(new CompositeName(name), obj);
    }
    private void callUnbind(NameComponent[] path) throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            try {
                _nc.unbind(path);
            } catch (NotFound e) {
                if (leafNotFound(e, path[path.length-1])) {
                    ; 
                } else {
                    throw ExceptionMapper.mapException(e, this, path);
                }
            } catch (Exception e) {
                throw ExceptionMapper.mapException(e, this, path);
            }
    }
    private boolean leafNotFound(NotFound e, NameComponent leaf) {
        NameComponent rest;
        return e.why.value() == NotFoundReason._missing_node &&
            e.rest_of_name.length == 1 &&
            (rest=e.rest_of_name[0]).id.equals(leaf.id) &&
            (rest.kind == leaf.kind ||
             (rest.kind != null && rest.kind.equals(leaf.kind)));
    }
    public  void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }
    public  void unbind(Name name)
        throws NamingException {
            if (name.size() == 0 )
                throw new InvalidNameException("Name is empty");
            NameComponent[] path = CNNameParser.nameToCosName(name);
            try {
                callUnbind(path);
            } catch (CannotProceedException e) {
                javax.naming.Context cctx = getContinuationContext(e);
                cctx.unbind(e.getRemainingName());
            }
    }
    public  void rename(String oldName,String newName)
        throws NamingException {
            rename(new CompositeName(oldName), new CompositeName(newName));
    }
    public  void rename(Name oldName,Name newName)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            if (oldName.size() == 0 || newName.size() == 0)
                throw new InvalidNameException("One or both names empty");
            java.lang.Object obj = lookup(oldName);
            bind(newName,obj);
            unbind(oldName);
    }
    public  NamingEnumeration list(String name) throws NamingException {
            return list(new CompositeName(name));
    }
    public  NamingEnumeration list(Name name)
        throws NamingException {
            return listBindings(name);
    }
    public  NamingEnumeration listBindings(String name)
        throws NamingException {
            return listBindings(new CompositeName(name));
    }
    public  NamingEnumeration listBindings(Name name)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            if (name.size() > 0) {
                try {
                    java.lang.Object obj = lookup(name);
                    if (obj instanceof CNCtx) {
                        return new CNBindingEnumeration(
                                        (CNCtx) obj, true, _env);
                    } else {
                        throw new NotContextException(name.toString());
                    }
                } catch (NamingException ne) {
                    throw ne;
                } catch (BAD_PARAM e) {
                    NamingException ne =
                        new NotContextException(name.toString());
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            return new CNBindingEnumeration(this, false, _env);
    }
    private void callDestroy(NamingContext nc)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            try {
                nc.destroy();
            } catch (Exception e) {
                throw ExceptionMapper.mapException(e, this, null);
            }
    }
    public  void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }
    public  void destroySubcontext(Name name)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            NamingContext the_nc = _nc;
            NameComponent[] path = CNNameParser.nameToCosName(name);
            if ( name.size() > 0) {
                try {
                    javax.naming.Context ctx =
                        (javax.naming.Context) callResolve(path);
                    CNCtx cnc = (CNCtx)ctx;
                    the_nc = cnc._nc;
                    cnc.close(); 
                } catch (ClassCastException e) {
                    throw new NotContextException(name.toString());
                } catch (CannotProceedException e) {
                    javax.naming.Context cctx = getContinuationContext(e);
                    cctx.destroySubcontext(e.getRemainingName());
                    return;
                } catch (NameNotFoundException e) {
                    if (e.getRootCause() instanceof NotFound &&
                        leafNotFound((NotFound)e.getRootCause(),
                            path[path.length-1])) {
                        return; 
                    }
                    throw e;
                } catch (NamingException e) {
                    throw e;
                }
            }
            callDestroy(the_nc);
            callUnbind(path);
    }
    private javax.naming.Context callBindNewContext(NameComponent[] path)
        throws NamingException {
            if (_nc == null)
                throw new ConfigurationException(
                    "Context does not have a corresponding NamingContext");
            try {
                NamingContext nctx = _nc.bind_new_context(path);
                return new CNCtx(_orb, orbTracker, nctx, _env,
                                        makeFullName(path));
            } catch (Exception e) {
                throw ExceptionMapper.mapException(e, this, path);
            }
    }
    public  javax.naming.Context createSubcontext(String name)
        throws NamingException {
            return createSubcontext(new CompositeName(name));
    }
    public  javax.naming.Context createSubcontext(Name name)
        throws NamingException {
            if (name.size() == 0 )
                throw new InvalidNameException("Name is empty");
            NameComponent[] path = CNNameParser.nameToCosName(name);
            try {
                return callBindNewContext(path);
            } catch (CannotProceedException e) {
                javax.naming.Context cctx = getContinuationContext(e);
                return cctx.createSubcontext(e.getRemainingName());
            }
    }
    public  java.lang.Object lookupLink(String name) throws NamingException {
            return lookupLink(new CompositeName(name));
    }
    public  java.lang.Object lookupLink(Name name) throws NamingException {
            return lookup(name);
    }
    public  NameParser getNameParser(String name) throws NamingException {
        return parser;
    }
    public  NameParser getNameParser(Name name) throws NamingException {
        return parser;
    }
    public  Hashtable getEnvironment() throws NamingException {
        if (_env == null) {
            return new Hashtable(5, 0.75f);
        } else {
            return (Hashtable)_env.clone();
        }
    }
    public String composeName(String name, String prefix) throws NamingException {
        return composeName(new CompositeName(name),
            new CompositeName(prefix)).toString();
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name)prefix.clone();
        return result.addAll(name);
    }
    public java.lang.Object addToEnvironment(String propName,
        java.lang.Object propValue)
        throws NamingException {
            if (_env == null) {
                _env = new Hashtable(7, 0.75f);
            } else {
                _env = (Hashtable)_env.clone();
            }
            return _env.put(propName, propValue);
    }
    public java.lang.Object removeFromEnvironment(String propName)
        throws NamingException {
            if (_env != null  && _env.get(propName) != null) {
                _env = (Hashtable)_env.clone();
                return _env.remove(propName);
            }
            return null;
    }
    synchronized public void incEnumCount() {
        if (orbTracker == null) {
            return;
        }
        enumCount++;
        if (debug) {
            System.out.println("incEnumCount, new count:" + enumCount);
        }
    }
    synchronized public void decEnumCount()
            throws NamingException {
        if (orbTracker == null) {
            return;
        }
        enumCount--;
        if (debug) {
            System.out.println("decEnumCount, new count:" + enumCount +
                        "    isCloseCalled:" + isCloseCalled);
        }
        if ((enumCount == 0) && isCloseCalled) {
            close();
        }
    }
    synchronized public void close() throws NamingException {
        if (orbTracker == null) {
            return;
        }
        if (enumCount > 0) {
            isCloseCalled = true;
            return;
        }
        orbTracker.decRefCount();
    }
    protected void finalize() {
        try {
            close();
        } catch (NamingException e) {
        }
    }
}
