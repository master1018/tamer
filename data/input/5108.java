final class LdapSchemaCtx extends HierMemDirCtx {
    static private final boolean debug = false;
    private static final int LEAF = 0;  
    private static final int SCHEMA_ROOT = 1;   
    static final int OBJECTCLASS_ROOT = 2;   
    static final int ATTRIBUTE_ROOT = 3;     
    static final int SYNTAX_ROOT = 4;        
    static final int MATCHRULE_ROOT = 5;     
    static final int OBJECTCLASS = 6;   
    static final int ATTRIBUTE = 7;     
    static final int SYNTAX = 8;        
    static final int MATCHRULE = 9;     
    private SchemaInfo info= null;
    private boolean setupMode = true;
    private int objectType;
    static DirContext createSchemaTree(Hashtable env, String subschemasubentry,
        LdapCtx schemaEntry, Attributes schemaAttrs, boolean netscapeBug)
        throws NamingException {
            try {
                LdapSchemaParser parser = new LdapSchemaParser(netscapeBug);
                SchemaInfo allinfo = new SchemaInfo(subschemasubentry,
                    schemaEntry, parser);
                LdapSchemaCtx root = new LdapSchemaCtx(SCHEMA_ROOT, env, allinfo);
                parser.LDAP2JNDISchema(schemaAttrs, root);
                return root;
            } catch (NamingException e) {
                schemaEntry.close(); 
                throw e;
            }
    }
    private LdapSchemaCtx(int objectType, Hashtable environment, SchemaInfo info) {
        super(environment, LdapClient.caseIgnore);
        this.objectType = objectType;
        this.info = info;
    }
    public void close() throws NamingException {
        info.close();
    }
    final public void bind(Name name, Object obj, Attributes attrs)
        throws NamingException {
        if (!setupMode) {
            if (obj != null) {
                throw new IllegalArgumentException("obj must be null");
            }
            addServerSchema(attrs);
        }
        LdapSchemaCtx newEntry =
            (LdapSchemaCtx)super.doCreateSubcontext(name, attrs);
    }
    final protected void doBind(Name name, Object obj, Attributes attrs,
        boolean useFactory) throws NamingException {
        if (!setupMode) {
            throw new SchemaViolationException(
                "Cannot bind arbitrary object; use createSubcontext()");
        } else {
            super.doBind(name, obj, attrs, false); 
        }
    }
    final public void rebind(Name name, Object obj, Attributes attrs)
        throws NamingException {
        try {
            doLookup(name, false);
            throw new SchemaViolationException(
                "Cannot replace existing schema object");
        } catch (NameNotFoundException e) {
            bind(name, obj, attrs);
        }
    }
    final protected void doRebind(Name name, Object obj, Attributes attrs,
        boolean useFactory) throws NamingException {
        if (!setupMode) {
            throw new SchemaViolationException(
                "Cannot bind arbitrary object; use createSubcontext()");
        } else {
            super.doRebind(name, obj, attrs, false); 
        }
    }
    final protected void doUnbind(Name name) throws NamingException {
        if (!setupMode) {
            try {
                LdapSchemaCtx target = (LdapSchemaCtx)doLookup(name, false);
                deleteServerSchema(target.attrs);
            } catch (NameNotFoundException e) {
                return;
            }
        }
        super.doUnbind(name);
    }
    final protected void doRename(Name oldname, Name newname)
        throws NamingException {
        if (!setupMode) {
            throw new SchemaViolationException("Cannot rename a schema object");
        } else {
            super.doRename(oldname, newname);
        }
    }
    final protected void doDestroySubcontext(Name name) throws NamingException {
        if (!setupMode) {
            try {
                LdapSchemaCtx target = (LdapSchemaCtx)doLookup(name, false);
                deleteServerSchema(target.attrs);
            } catch (NameNotFoundException e) {
                return;
            }
        }
        super.doDestroySubcontext(name);
     }
    final LdapSchemaCtx setup(int objectType, String name, Attributes attrs)
        throws NamingException{
            try {
                setupMode = true;
                LdapSchemaCtx answer =
                    (LdapSchemaCtx) super.doCreateSubcontext(
                        new CompositeName(name), attrs);
                answer.objectType = objectType;
                answer.setupMode = false;
                return answer;
            } finally {
                setupMode = false;
            }
    }
    final protected DirContext doCreateSubcontext(Name name, Attributes attrs)
        throws NamingException {
        if (attrs == null || attrs.size() == 0) {
            throw new SchemaViolationException(
                "Must supply attributes describing schema");
        }
        if (!setupMode) {
            addServerSchema(attrs);
        }
        LdapSchemaCtx newEntry =
            (LdapSchemaCtx) super.doCreateSubcontext(name, attrs);
        return newEntry;
    }
    final private static Attributes deepClone(Attributes orig)
        throws NamingException {
        BasicAttributes copy = new BasicAttributes(true);
        NamingEnumeration attrs = orig.getAll();
        while (attrs.hasMore()) {
            copy.put((Attribute)((Attribute)attrs.next()).clone());
        }
        return copy;
    }
    final protected void doModifyAttributes(ModificationItem[] mods)
        throws NamingException {
        if (setupMode) {
            super.doModifyAttributes(mods);
        } else {
            Attributes copy = deepClone(attrs);
            applyMods(mods, copy);
            modifyServerSchema(attrs, copy);
            attrs = copy;
        }
    }
    final protected HierMemDirCtx createNewCtx() {
        LdapSchemaCtx ctx = new LdapSchemaCtx(LEAF, myEnv, info);
        return ctx;
    }
    final private void addServerSchema(Attributes attrs)
        throws NamingException {
        Attribute schemaAttr;
        switch (objectType) {
        case OBJECTCLASS_ROOT:
            schemaAttr = info.parser.stringifyObjDesc(attrs);
            break;
        case ATTRIBUTE_ROOT:
            schemaAttr = info.parser.stringifyAttrDesc(attrs);
            break;
        case SYNTAX_ROOT:
            schemaAttr = info.parser.stringifySyntaxDesc(attrs);
            break;
        case MATCHRULE_ROOT:
            schemaAttr = info.parser.stringifyMatchRuleDesc(attrs);
            break;
        case SCHEMA_ROOT:
            throw new SchemaViolationException(
                "Cannot create new entry under schema root");
        default:
            throw new SchemaViolationException(
                "Cannot create child of schema object");
        }
        Attributes holder = new BasicAttributes(true);
        holder.put(schemaAttr);
        info.modifyAttributes(myEnv, DirContext.ADD_ATTRIBUTE, holder);
    }
    final private void deleteServerSchema(Attributes origAttrs)
        throws NamingException {
        Attribute origAttrVal;
        switch (objectType) {
        case OBJECTCLASS_ROOT:
            origAttrVal = info.parser.stringifyObjDesc(origAttrs);
            break;
        case ATTRIBUTE_ROOT:
            origAttrVal = info.parser.stringifyAttrDesc(origAttrs);
            break;
        case SYNTAX_ROOT:
            origAttrVal = info.parser.stringifySyntaxDesc(origAttrs);
            break;
        case MATCHRULE_ROOT:
            origAttrVal = info.parser.stringifyMatchRuleDesc(origAttrs);
            break;
        case SCHEMA_ROOT:
            throw new SchemaViolationException(
                "Cannot delete schema root");
        default:
            throw new SchemaViolationException(
                "Cannot delete child of schema object");
        }
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, origAttrVal);
        info.modifyAttributes(myEnv, mods);
    }
    final private void modifyServerSchema(Attributes origAttrs,
        Attributes newAttrs) throws NamingException {
        Attribute newAttrVal;
        Attribute origAttrVal;
        switch (objectType) {
        case OBJECTCLASS:
            origAttrVal = info.parser.stringifyObjDesc(origAttrs);
            newAttrVal = info.parser.stringifyObjDesc(newAttrs);
            break;
        case ATTRIBUTE:
            origAttrVal = info.parser.stringifyAttrDesc(origAttrs);
            newAttrVal = info.parser.stringifyAttrDesc(newAttrs);
            break;
        case SYNTAX:
            origAttrVal = info.parser.stringifySyntaxDesc(origAttrs);
            newAttrVal = info.parser.stringifySyntaxDesc(newAttrs);
            break;
        case MATCHRULE:
            origAttrVal = info.parser.stringifyMatchRuleDesc(origAttrs);
            newAttrVal = info.parser.stringifyMatchRuleDesc(newAttrs);
            break;
        default:
            throw new SchemaViolationException(
                "Cannot modify schema root");
        }
        ModificationItem[] mods = new ModificationItem[2];
        mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, origAttrVal);
        mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, newAttrVal);
        info.modifyAttributes(myEnv, mods);
    }
    final static private class SchemaInfo {
        private LdapCtx schemaEntry;
        private String schemaEntryName;
        LdapSchemaParser parser;
        private String host;
        private int port;
        private boolean hasLdapsScheme;
        SchemaInfo(String schemaEntryName, LdapCtx schemaEntry,
            LdapSchemaParser parser) {
            this.schemaEntryName = schemaEntryName;
            this.schemaEntry = schemaEntry;
            this.parser = parser;
            this.port = schemaEntry.port_number;
            this.host = schemaEntry.hostname;
            this.hasLdapsScheme = schemaEntry.hasLdapsScheme;
        }
        synchronized void close() throws NamingException {
            if (schemaEntry != null) {
                schemaEntry.close();
                schemaEntry = null;
            }
        }
        private LdapCtx reopenEntry(Hashtable env) throws NamingException {
            return new LdapCtx(schemaEntryName, host, port,
                                env, hasLdapsScheme);
        }
        synchronized void modifyAttributes(Hashtable env, ModificationItem[] mods)
            throws NamingException {
            if (schemaEntry == null) {
                schemaEntry = reopenEntry(env);
            }
            schemaEntry.modifyAttributes("", mods);
        }
        synchronized void modifyAttributes(Hashtable env, int mod,
            Attributes attrs) throws NamingException {
            if (schemaEntry == null) {
                schemaEntry = reopenEntry(env);
            }
            schemaEntry.modifyAttributes("", mod, attrs);
        }
    }
}
