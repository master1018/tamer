public class DnsContext extends ComponentDirContext {
    DnsName domain;             
    Hashtable environment;
    private boolean envShared;  
    private boolean parentIsDns;        
    private String[] servers;
    private Resolver resolver;
    private boolean authoritative;      
    private boolean recursion;          
    private int timeout;                
    private int retries;                
    static final NameParser nameParser = new DnsNameParser();
    private static final int DEFAULT_INIT_TIMEOUT = 1000;
    private static final int DEFAULT_RETRIES = 4;
    private static final String INIT_TIMEOUT =
                                          "com.sun.jndi.dns.timeout.initial";
    private static final String RETRIES = "com.sun.jndi.dns.timeout.retries";
    private CT lookupCT;
    private static final String LOOKUP_ATTR = "com.sun.jndi.dns.lookup.attr";
    private static final String RECURSION = "com.sun.jndi.dns.recursion";
    private static final int ANY = ResourceRecord.QTYPE_STAR;
    private static final ZoneNode zoneTree = new ZoneNode(null);
    public DnsContext(String domain, String[] servers, Hashtable environment)
            throws NamingException {
        this.domain = new DnsName(domain.endsWith(".")
                                  ? domain
                                  : domain + ".");
        this.servers = servers;
        this.environment = (Hashtable) environment.clone();
        envShared = false;
        parentIsDns = false;
        resolver = null;
        initFromEnvironment();
    }
    DnsContext(DnsContext ctx, DnsName domain) {
        this(ctx);
        this.domain = domain;
        parentIsDns = true;
    }
    private DnsContext(DnsContext ctx) {
        environment = ctx.environment;
        envShared = ctx.envShared = true;
        parentIsDns = ctx.parentIsDns;
        domain = ctx.domain;
        servers = ctx.servers;
        resolver = ctx.resolver;
        authoritative = ctx.authoritative;
        recursion = ctx.recursion;
        timeout = ctx.timeout;
        retries = ctx.retries;
        lookupCT = ctx.lookupCT;
    }
    public void close() {
        if (resolver != null) {
            resolver.close();
            resolver = null;
        }
    }
    protected Hashtable p_getEnvironment() {
        return environment;
    }
    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) environment.clone();
    }
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (propName.equals(LOOKUP_ATTR)) {
            lookupCT = getLookupCT((String) propVal);
        } else if (propName.equals(Context.AUTHORITATIVE)) {
            authoritative = "true".equalsIgnoreCase((String) propVal);
        } else if (propName.equals(RECURSION)) {
            recursion = "true".equalsIgnoreCase((String) propVal);
        } else if (propName.equals(INIT_TIMEOUT)) {
            int val = Integer.parseInt((String) propVal);
            if (timeout != val) {
                timeout = val;
                resolver = null;
            }
        } else if (propName.equals(RETRIES)) {
            int val = Integer.parseInt((String) propVal);
            if (retries != val) {
                retries = val;
                resolver = null;
            }
        }
        if (!envShared) {
            return environment.put(propName, propVal);
        } else if (environment.get(propName) != propVal) {
            environment = (Hashtable) environment.clone();
            envShared = false;
            return environment.put(propName, propVal);
        } else {
            return propVal;
        }
    }
    public Object removeFromEnvironment(String propName)
            throws NamingException {
        if (propName.equals(LOOKUP_ATTR)) {
            lookupCT = getLookupCT(null);
        } else if (propName.equals(Context.AUTHORITATIVE)) {
            authoritative = false;
        } else if (propName.equals(RECURSION)) {
            recursion = true;
        } else if (propName.equals(INIT_TIMEOUT)) {
            if (timeout != DEFAULT_INIT_TIMEOUT) {
                timeout = DEFAULT_INIT_TIMEOUT;
                resolver = null;
            }
        } else if (propName.equals(RETRIES)) {
            if (retries != DEFAULT_RETRIES) {
                retries = DEFAULT_RETRIES;
                resolver = null;
            }
        }
        if (!envShared) {
            return environment.remove(propName);
        } else if (environment.get(propName) != null) {
            environment = (Hashtable) environment.clone();
            envShared = false;
            return environment.remove(propName);
        } else {
            return null;
        }
    }
    void setProviderUrl(String url) {
        environment.put(Context.PROVIDER_URL, url);
    }
    private void initFromEnvironment()
            throws InvalidAttributeIdentifierException {
        lookupCT = getLookupCT((String) environment.get(LOOKUP_ATTR));
        authoritative = "true".equalsIgnoreCase((String)
                                       environment.get(Context.AUTHORITATIVE));
        String val = (String) environment.get(RECURSION);
        recursion = ((val == null) ||
                     "true".equalsIgnoreCase(val));
        val = (String) environment.get(INIT_TIMEOUT);
        timeout = (val == null)
            ? DEFAULT_INIT_TIMEOUT
            : Integer.parseInt(val);
        val = (String) environment.get(RETRIES);
        retries = (val == null)
            ? DEFAULT_RETRIES
            : Integer.parseInt(val);
    }
    private CT getLookupCT(String attrId)
            throws InvalidAttributeIdentifierException {
        return (attrId == null)
            ? new CT(ResourceRecord.CLASS_INTERNET, ResourceRecord.TYPE_TXT)
            : fromAttrId(attrId);
    }
    public Object c_lookup(Name name, Continuation cont)
            throws NamingException {
        cont.setSuccess();
        if (name.isEmpty()) {
            DnsContext ctx = new DnsContext(this);
            ctx.resolver = new Resolver(servers, timeout, retries);
            return ctx;
        }
        try {
            DnsName fqdn = fullyQualify(name);
            ResourceRecords rrs =
                getResolver().query(fqdn, lookupCT.rrclass, lookupCT.rrtype,
                                    recursion, authoritative);
            Attributes attrs = rrsToAttrs(rrs, null);
            DnsContext ctx = new DnsContext(this, fqdn);
            return DirectoryManager.getObjectInstance(ctx, name, this,
                                                      environment, attrs);
        } catch (NamingException e) {
            cont.setError(this, name);
            throw cont.fillInException(e);
        } catch (Exception e) {
            cont.setError(this, name);
            NamingException ne = new NamingException(
                    "Problem generating object using object factory");
            ne.setRootCause(e);
            throw cont.fillInException(ne);
        }
    }
    public Object c_lookupLink(Name name, Continuation cont)
            throws NamingException {
        return c_lookup(name, cont);
    }
    public NamingEnumeration c_list(Name name, Continuation cont)
            throws NamingException {
        cont.setSuccess();
        try {
            DnsName fqdn = fullyQualify(name);
            NameNode nnode = getNameNode(fqdn);
            DnsContext ctx = new DnsContext(this, fqdn);
            return new NameClassPairEnumeration(ctx, nnode.getChildren());
        } catch (NamingException e) {
            cont.setError(this, name);
            throw cont.fillInException(e);
        }
    }
    public NamingEnumeration c_listBindings(Name name, Continuation cont)
            throws NamingException {
        cont.setSuccess();
        try {
            DnsName fqdn = fullyQualify(name);
            NameNode nnode = getNameNode(fqdn);
            DnsContext ctx = new DnsContext(this, fqdn);
            return new BindingEnumeration(ctx, nnode.getChildren());
        } catch (NamingException e) {
            cont.setError(this, name);
            throw cont.fillInException(e);
        }
    }
    public void c_bind(Name name, Object obj, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_rebind(Name name, Object obj, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_unbind(Name name, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_rename(Name oldname, Name newname, Continuation cont)
            throws NamingException {
        cont.setError(this, oldname);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public Context c_createSubcontext(Name name, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_destroySubcontext(Name name, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public NameParser c_getNameParser(Name name, Continuation cont)
            throws NamingException {
        cont.setSuccess();
        return nameParser;
    }
    public void c_bind(Name name,
                       Object obj,
                       Attributes attrs,
                       Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_rebind(Name name,
                         Object obj,
                         Attributes attrs,
                         Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public DirContext c_createSubcontext(Name name,
                                         Attributes attrs,
                                         Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public Attributes c_getAttributes(Name name,
                                      String[] attrIds,
                                      Continuation cont)
            throws NamingException {
        cont.setSuccess();
        try {
            DnsName fqdn = fullyQualify(name);
            CT[] cts = attrIdsToClassesAndTypes(attrIds);
            CT ct = getClassAndTypeToQuery(cts);
            ResourceRecords rrs =
                getResolver().query(fqdn, ct.rrclass, ct.rrtype,
                                    recursion, authoritative);
            return rrsToAttrs(rrs, cts);
        } catch (NamingException e) {
            cont.setError(this, name);
            throw cont.fillInException(e);
        }
    }
    public void c_modifyAttributes(Name name,
                                   int mod_op,
                                   Attributes attrs,
                                   Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public void c_modifyAttributes(Name name,
                                   ModificationItem[] mods,
                                   Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public NamingEnumeration c_search(Name name,
                                      Attributes matchingAttributes,
                                      String[] attributesToReturn,
                                      Continuation cont)
            throws NamingException {
        throw new OperationNotSupportedException();
    }
    public NamingEnumeration c_search(Name name,
                                      String filter,
                                      SearchControls cons,
                                      Continuation cont)
            throws NamingException {
        throw new OperationNotSupportedException();
    }
    public NamingEnumeration c_search(Name name,
                                      String filterExpr,
                                      Object[] filterArgs,
                                      SearchControls cons,
                                      Continuation cont)
            throws NamingException {
        throw new OperationNotSupportedException();
    }
    public DirContext c_getSchema(Name name, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public DirContext c_getSchemaClassDefinition(Name name, Continuation cont)
            throws NamingException {
        cont.setError(this, name);
        throw cont.fillInException(
                new OperationNotSupportedException());
    }
    public String getNameInNamespace() {
        return domain.toString();
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result;
        if (!(prefix instanceof DnsName || prefix instanceof CompositeName)) {
            prefix = (new DnsName()).addAll(prefix);
        }
        if (!(name instanceof DnsName || name instanceof CompositeName)) {
            name = (new DnsName()).addAll(name);
        }
        if ((prefix instanceof DnsName) && (name instanceof DnsName)) {
            result = (DnsName) (prefix.clone());
            result.addAll(name);
            return new CompositeName().add(result.toString());
        }
        Name prefixC = (prefix instanceof CompositeName)
            ? prefix
            : new CompositeName().add(prefix.toString());
        Name nameC = (name instanceof CompositeName)
            ? name
            : new CompositeName().add(name.toString());
        int prefixLast = prefixC.size() - 1;
        if (nameC.isEmpty() || nameC.get(0).equals("") ||
                prefixC.isEmpty() || prefixC.get(prefixLast).equals("")) {
            return super.composeName(nameC, prefixC);
        }
        result = (prefix == prefixC)
            ? (CompositeName) prefixC.clone()
            : prefixC;                  
        result.addAll(nameC);
        if (parentIsDns) {
            DnsName dnsComp = (prefix instanceof DnsName)
                           ? (DnsName) prefix.clone()
                           : new DnsName(prefixC.get(prefixLast));
            dnsComp.addAll((name instanceof DnsName)
                           ? name
                           : new DnsName(nameC.get(0)));
            result.remove(prefixLast + 1);
            result.remove(prefixLast);
            result.add(prefixLast, dnsComp.toString());
        }
        return result;
    }
    private synchronized Resolver getResolver() throws NamingException {
        if (resolver == null) {
            resolver = new Resolver(servers, timeout, retries);
        }
        return resolver;
    }
    DnsName fullyQualify(Name name) throws NamingException {
        if (name.isEmpty()) {
            return domain;
        }
        DnsName dnsName = (name instanceof CompositeName)
            ? new DnsName(name.get(0))                  
            : (DnsName) (new DnsName()).addAll(name);   
        if (dnsName.hasRootLabel()) {
            if (domain.size() == 1) {
                return dnsName;
            } else {
                throw new InvalidNameException(
                       "DNS name " + dnsName + " not relative to " + domain);
            }
        }
        return (DnsName) dnsName.addAll(0, domain);
    }
    private static Attributes rrsToAttrs(ResourceRecords rrs, CT[] cts) {
        BasicAttributes attrs = new BasicAttributes(true);
        for (int i = 0; i < rrs.answer.size(); i++) {
            ResourceRecord rr = (ResourceRecord) rrs.answer.elementAt(i);
            int rrtype  = rr.getType();
            int rrclass = rr.getRrclass();
            if (!classAndTypeMatch(rrclass, rrtype, cts)) {
                continue;
            }
            String attrId = toAttrId(rrclass, rrtype);
            Attribute attr = attrs.get(attrId);
            if (attr == null) {
                attr = new BasicAttribute(attrId);
                attrs.put(attr);
            }
            attr.add(rr.getRdata());
        }
        return attrs;
    }
    private static boolean classAndTypeMatch(int rrclass, int rrtype,
                                             CT[] cts) {
        if (cts == null) {
            return true;
        }
        for (int i = 0; i < cts.length; i++) {
            CT ct = cts[i];
            boolean classMatch = (ct.rrclass == ANY) ||
                                 (ct.rrclass == rrclass);
            boolean typeMatch  = (ct.rrtype == ANY) ||
                                 (ct.rrtype == rrtype);
            if (classMatch && typeMatch) {
                return true;
            }
        }
        return false;
    }
    private static String toAttrId(int rrclass, int rrtype) {
        String attrId = ResourceRecord.getTypeName(rrtype);
        if (rrclass != ResourceRecord.CLASS_INTERNET) {
            attrId = ResourceRecord.getRrclassName(rrclass) + " " + attrId;
        }
        return attrId;
    }
    private static CT fromAttrId(String attrId)
            throws InvalidAttributeIdentifierException {
        if (attrId.equals("")) {
            throw new InvalidAttributeIdentifierException(
                    "Attribute ID cannot be empty");
        }
        int rrclass;
        int rrtype;
        int space = attrId.indexOf(' ');
        if (space < 0) {
            rrclass = ResourceRecord.CLASS_INTERNET;
        } else {
            String className = attrId.substring(0, space);
            rrclass = ResourceRecord.getRrclass(className);
            if (rrclass < 0) {
                throw new InvalidAttributeIdentifierException(
                        "Unknown resource record class '" + className + '\'');
            }
        }
        String typeName = attrId.substring(space + 1);
        rrtype = ResourceRecord.getType(typeName);
        if (rrtype < 0) {
            throw new InvalidAttributeIdentifierException(
                    "Unknown resource record type '" + typeName + '\'');
        }
        return new CT(rrclass, rrtype);
    }
    private static CT[] attrIdsToClassesAndTypes(String[] attrIds)
            throws InvalidAttributeIdentifierException {
        if (attrIds == null) {
            return null;
        }
        CT[] cts = new CT[attrIds.length];
        for (int i = 0; i < attrIds.length; i++) {
            cts[i] = fromAttrId(attrIds[i]);
        }
        return cts;
    }
    private static CT getClassAndTypeToQuery(CT[] cts) {
        int rrclass;
        int rrtype;
        if (cts == null) {
            rrclass = ANY;
            rrtype  = ANY;
        } else if (cts.length == 0) {
            rrclass = ResourceRecord.CLASS_INTERNET;
            rrtype  = ANY;
        } else {
            rrclass = cts[0].rrclass;
            rrtype  = cts[0].rrtype;
            for (int i = 1; i < cts.length; i++) {
                if (rrclass != cts[i].rrclass) {
                    rrclass = ANY;
                }
                if (rrtype != cts[i].rrtype) {
                    rrtype = ANY;
                }
            }
        }
        return new CT(rrclass, rrtype);
    }
    private NameNode getNameNode(DnsName fqdn) throws NamingException {
        dprint("getNameNode(" + fqdn + ")");
        ZoneNode znode;
        DnsName zone;
        synchronized (zoneTree) {
            znode = zoneTree.getDeepestPopulated(fqdn);
        }
        dprint("Deepest related zone in zone tree: " +
               ((znode != null) ? znode.getLabel() : "[none]"));
        NameNode topOfZone;
        NameNode nnode;
        if (znode != null) {
            synchronized (znode) {
                topOfZone = znode.getContents();
            }
            if (topOfZone != null) {
                nnode = topOfZone.get(fqdn, znode.depth() + 1); 
                if ((nnode != null) && !nnode.isZoneCut()) {
                    dprint("Found node " + fqdn + " in zone tree");
                    zone = (DnsName)
                        fqdn.getPrefix(znode.depth() + 1);      
                    boolean current = isZoneCurrent(znode, zone);
                    boolean restart = false;
                    synchronized (znode) {
                        if (topOfZone != znode.getContents()) {
                            restart = true;
                        } else if (!current) {
                            znode.depopulate();
                        } else {
                            return nnode;                       
                        }
                    }
                    dprint("Zone not current; discarding node");
                    if (restart) {
                        return getNameNode(fqdn);
                    }
                }
            }
        }
        dprint("Adding node " + fqdn + " to zone tree");
        zone = getResolver().findZoneName(fqdn, ResourceRecord.CLASS_INTERNET,
                                          recursion);
        dprint("Node's zone is " + zone);
        synchronized (zoneTree) {
            znode = (ZoneNode) zoneTree.add(zone, 1);   
        }
        synchronized (znode) {
            topOfZone = znode.isPopulated()
                ? znode.getContents()
                : populateZone(znode, zone);
        }
        nnode = topOfZone.get(fqdn, zone.size());
        if (nnode == null) {
            throw new ConfigurationException(
                    "DNS error: node not found in its own zone");
        }
        dprint("Found node in newly-populated zone");
        return nnode;
    }
    private NameNode populateZone(ZoneNode znode, DnsName zone)
            throws NamingException {
        dprint("Populating zone " + zone);
        ResourceRecords rrs =
            getResolver().queryZone(zone,
                                    ResourceRecord.CLASS_INTERNET, recursion);
        dprint("zone xfer complete: " + rrs.answer.size() + " records");
        return znode.populate(zone, rrs);
    }
    private boolean isZoneCurrent(ZoneNode znode, DnsName zone)
            throws NamingException {
        if (!znode.isPopulated()) {
            return false;
        }
        ResourceRecord soa =
            getResolver().findSoa(zone, ResourceRecord.CLASS_INTERNET,
                                  recursion);
        synchronized (znode) {
            if (soa == null) {
                znode.depopulate();
            }
            return (znode.isPopulated() &&
                    znode.compareSerialNumberTo(soa) >= 0);
        }
    }
    private static final boolean debug = false;
    private static final void dprint(String msg) {
        if (debug) {
            System.err.println("** " + msg);
        }
    }
}
class CT {
    int rrclass;
    int rrtype;
    CT(int rrclass, int rrtype) {
        this.rrclass = rrclass;
        this.rrtype = rrtype;
    }
}
class NameClassPairEnumeration implements NamingEnumeration {
    protected Enumeration nodes;    
    protected DnsContext ctx;       
    NameClassPairEnumeration(DnsContext ctx, Hashtable nodes) {
        this.ctx = ctx;
        this.nodes = (nodes != null)
            ? nodes.elements()
            : null;
    }
    public void close() {
        nodes = null;
        ctx = null;
    }
    public boolean hasMore() {
        boolean more = ((nodes != null) && nodes.hasMoreElements());
        if (!more) {
            close();
        }
        return more;
    }
    public Object next() throws NamingException {
        if (!hasMore()) {
            throw new java.util.NoSuchElementException();
        }
        NameNode nnode = (NameNode) nodes.nextElement();
        String className = (nnode.isZoneCut() ||
                            (nnode.getChildren() != null))
            ? "javax.naming.directory.DirContext"
            : "java.lang.Object";
        String label = nnode.getLabel();
        Name compName = (new DnsName()).add(label);
        Name cname = (new CompositeName()).add(compName.toString());
        NameClassPair ncp = new NameClassPair(cname.toString(), className);
        ncp.setNameInNamespace(ctx.fullyQualify(cname).toString());
        return ncp;
    }
    public boolean hasMoreElements() {
        return hasMore();
    }
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw (new java.util.NoSuchElementException(
                    "javax.naming.NamingException was thrown: " +
                    e.getMessage()));
        }
    }
}
class BindingEnumeration extends NameClassPairEnumeration {
    BindingEnumeration(DnsContext ctx, Hashtable nodes) {
        super(ctx, nodes);
    }
    public Object next() throws NamingException {
        if (!hasMore()) {
            throw (new java.util.NoSuchElementException());
        }
        NameNode nnode = (NameNode) nodes.nextElement();
        String label = nnode.getLabel();
        Name compName = (new DnsName()).add(label);
        String compNameStr = compName.toString();
        Name cname = (new CompositeName()).add(compNameStr);
        String cnameStr = cname.toString();
        DnsName fqdn = ctx.fullyQualify(compName);
        DnsContext child = new DnsContext(ctx, fqdn);
        try {
            Object obj = DirectoryManager.getObjectInstance(
                    child, cname, ctx, child.environment, null);
            Binding binding = new Binding(cnameStr, obj);
            binding.setNameInNamespace(ctx.fullyQualify(cname).toString());
            return binding;
        } catch (Exception e) {
            NamingException ne = new NamingException(
                    "Problem generating object using object factory");
            ne.setRootCause(e);
            throw ne;
        }
    }
}
