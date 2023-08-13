final class LdapAttribute extends BasicAttribute {
    static final long serialVersionUID = -4288716561020779584L;
    private transient DirContext baseCtx = null;
    private Name rdn = new CompositeName();
    private String baseCtxURL;
    private Hashtable baseCtxEnv;
    public Object clone() {
        LdapAttribute attr = new LdapAttribute(this.attrID, baseCtx, rdn);
        attr.values = (Vector)values.clone();
        return attr;
    }
    public boolean add(Object attrVal) {
        values.addElement(attrVal);
        return true;
    }
    LdapAttribute(String id) {
        super(id);
    }
    private LdapAttribute(String id, DirContext baseCtx, Name rdn) {
        super(id);
        this.baseCtx = baseCtx;
        this.rdn = rdn;
    }
    void setParent(DirContext baseCtx, Name rdn) {
        this.baseCtx = baseCtx;
        this.rdn = rdn;
    }
    private DirContext getBaseCtx() throws NamingException {
        if(baseCtx == null) {
            if (baseCtxEnv == null) {
                baseCtxEnv = new Hashtable(3);
            }
            baseCtxEnv.put(Context.INITIAL_CONTEXT_FACTORY,
                             "com.sun.jndi.ldap.LdapCtxFactory");
            baseCtxEnv.put(Context.PROVIDER_URL,baseCtxURL);
            baseCtx = (new InitialDirContext(baseCtxEnv));
        }
        return baseCtx;
    }
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        this.setBaseCtxInfo();
        out.defaultWriteObject();
    }
    private void setBaseCtxInfo() {
        Hashtable realEnv = null;
        Hashtable secureEnv = null;
        if (baseCtx != null) {
            realEnv = ((LdapCtx)baseCtx).envprops;
            this.baseCtxURL = ((LdapCtx)baseCtx).getURL();
        }
        if(realEnv != null && realEnv.size() > 0 ) {
            Enumeration keys = realEnv.keys();
            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                if (key.indexOf("security") != -1 ) {
                    if(secureEnv == null) {
                        secureEnv = (Hashtable)realEnv.clone();
                    }
                    secureEnv.remove(key);
                }
            }
        }
        this.baseCtxEnv = (secureEnv == null ? realEnv : secureEnv);
    }
    public DirContext getAttributeSyntaxDefinition() throws NamingException {
        DirContext schema = getBaseCtx().getSchema(rdn);
        DirContext attrDef = (DirContext)schema.lookup(
            LdapSchemaParser.ATTRIBUTE_DEFINITION_NAME + "/" + getID());
        Attribute syntaxAttr = attrDef.getAttributes("").get("SYNTAX");
        if(syntaxAttr == null || syntaxAttr.size() == 0) {
            throw new NameNotFoundException(
                getID() + "does not have a syntax associated with it");
        }
        String syntaxName = (String)syntaxAttr.get();
        return (DirContext)schema.lookup(
            LdapSchemaParser.SYNTAX_DEFINITION_NAME + "/" + syntaxName);
    }
    public DirContext getAttributeDefinition() throws NamingException {
        DirContext schema = getBaseCtx().getSchema(rdn);
        return (DirContext)schema.lookup(
            LdapSchemaParser.ATTRIBUTE_DEFINITION_NAME + "/" + getID());
    }
}
