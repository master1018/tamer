public class InitialDirContext extends InitialContext implements DirContext {
    protected InitialDirContext(boolean lazy) throws NamingException {
        super(lazy);
    }
    public InitialDirContext() throws NamingException {
        super();
    }
    public InitialDirContext(Hashtable<?,?> environment)
        throws NamingException
    {
        super(environment);
    }
    private DirContext getURLOrDefaultInitDirCtx(String name)
            throws NamingException {
        Context answer = getURLOrDefaultInitCtx(name);
        if (!(answer instanceof DirContext)) {
            if (answer == null) {
                throw new NoInitialContextException();
            } else {
                throw new NotContextException(
                    "Not an instance of DirContext");
            }
        }
        return (DirContext)answer;
    }
    private DirContext getURLOrDefaultInitDirCtx(Name name)
            throws NamingException {
        Context answer = getURLOrDefaultInitCtx(name);
        if (!(answer instanceof DirContext)) {
            if (answer == null) {
                throw new NoInitialContextException();
            } else {
                throw new NotContextException(
                    "Not an instance of DirContext");
            }
        }
        return (DirContext)answer;
    }
    public Attributes getAttributes(String name)
            throws NamingException {
        return getAttributes(name, null);
    }
    public Attributes getAttributes(String name, String[] attrIds)
            throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getAttributes(name, attrIds);
    }
    public Attributes getAttributes(Name name)
            throws NamingException {
        return getAttributes(name, null);
    }
    public Attributes getAttributes(Name name, String[] attrIds)
            throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getAttributes(name, attrIds);
    }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
            throws NamingException {
        getURLOrDefaultInitDirCtx(name).modifyAttributes(name, mod_op, attrs);
    }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).modifyAttributes(name, mod_op, attrs);
    }
    public void modifyAttributes(String name, ModificationItem[] mods)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).modifyAttributes(name, mods);
    }
    public void modifyAttributes(Name name, ModificationItem[] mods)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).modifyAttributes(name, mods);
    }
    public void bind(String name, Object obj, Attributes attrs)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).bind(name, obj, attrs);
    }
    public void bind(Name name, Object obj, Attributes attrs)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).bind(name, obj, attrs);
    }
    public void rebind(String name, Object obj, Attributes attrs)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).rebind(name, obj, attrs);
    }
    public void rebind(Name name, Object obj, Attributes attrs)
            throws NamingException  {
        getURLOrDefaultInitDirCtx(name).rebind(name, obj, attrs);
    }
    public DirContext createSubcontext(String name, Attributes attrs)
            throws NamingException  {
        return getURLOrDefaultInitDirCtx(name).createSubcontext(name, attrs);
    }
    public DirContext createSubcontext(Name name, Attributes attrs)
            throws NamingException  {
        return getURLOrDefaultInitDirCtx(name).createSubcontext(name, attrs);
    }
    public DirContext getSchema(String name) throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getSchema(name);
    }
    public DirContext getSchema(Name name) throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getSchema(name);
    }
    public DirContext getSchemaClassDefinition(String name)
            throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getSchemaClassDefinition(name);
    }
    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException {
        return getURLOrDefaultInitDirCtx(name).getSchemaClassDefinition(name);
    }
    public NamingEnumeration<SearchResult>
        search(String name, Attributes matchingAttributes)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, matchingAttributes);
    }
    public NamingEnumeration<SearchResult>
        search(Name name, Attributes matchingAttributes)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, matchingAttributes);
    }
    public NamingEnumeration<SearchResult>
        search(String name,
               Attributes matchingAttributes,
               String[] attributesToReturn)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name,
                                                      matchingAttributes,
                                                      attributesToReturn);
    }
    public NamingEnumeration<SearchResult>
        search(Name name,
               Attributes matchingAttributes,
               String[] attributesToReturn)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name,
                                            matchingAttributes,
                                            attributesToReturn);
    }
    public NamingEnumeration<SearchResult>
        search(String name,
               String filter,
               SearchControls cons)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, filter, cons);
    }
    public NamingEnumeration<SearchResult>
        search(Name name,
               String filter,
               SearchControls cons)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, filter, cons);
    }
    public NamingEnumeration<SearchResult>
        search(String name,
               String filterExpr,
               Object[] filterArgs,
               SearchControls cons)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, filterExpr,
                                                      filterArgs, cons);
    }
    public NamingEnumeration<SearchResult>
        search(Name name,
               String filterExpr,
               Object[] filterArgs,
               SearchControls cons)
        throws NamingException
    {
        return getURLOrDefaultInitDirCtx(name).search(name, filterExpr,
                                                      filterArgs, cons);
    }
}
