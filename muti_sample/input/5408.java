final class LdapReferralContext implements DirContext, LdapContext {
    private DirContext refCtx = null;
    private Name urlName = null;   
    private String urlAttrs = null;  
    private String urlScope = null;  
    private String urlFilter = null; 
    private LdapReferralException refEx = null;
    private boolean skipThisReferral = false;
    private int hopCount = 1;
    private NamingException previousEx = null;
    LdapReferralContext(LdapReferralException ex, Hashtable env,
        Control[] connCtls,
        Control[] reqCtls,
        String nextName,
        boolean skipThisReferral,
        int handleReferrals) throws NamingException {
        refEx = ex;
        if (this.skipThisReferral = skipThisReferral) {
            return; 
        }
        String referral;
        if (env != null) {
            env = (Hashtable) env.clone();
            if (connCtls == null) {
                env.remove(LdapCtx.BIND_CONTROLS);
            }
        } else if (connCtls != null) {
            env = new Hashtable(5);
        }
        if (connCtls != null) {
            Control[] copiedCtls = new Control[connCtls.length];
            System.arraycopy(connCtls, 0, copiedCtls, 0, connCtls.length);
            env.put(LdapCtx.BIND_CONTROLS, copiedCtls);
        }
        while (true) {
            try {
                referral = refEx.getNextReferral();
                if (referral == null) {
                    throw (NamingException)(previousEx.fillInStackTrace());
                }
            } catch (LdapReferralException e) {
                if (handleReferrals == LdapClient.LDAP_REF_THROW) {
                    throw e;
                } else {
                    refEx = e;
                    continue;
                }
            }
            Reference ref = new Reference("javax.naming.directory.DirContext",
                                          new StringRefAddr("URL", referral));
            Object obj;
            try {
                obj = NamingManager.getObjectInstance(ref, null, null, env);
            } catch (NamingException e) {
                if (handleReferrals == LdapClient.LDAP_REF_THROW) {
                    throw e;
                }
                previousEx = e;
                continue;
            } catch (Exception e) {
                NamingException e2 =
                    new NamingException(
                        "problem generating object using object factory");
                e2.setRootCause(e);
                throw e2;
            }
            if (obj instanceof DirContext) {
                refCtx = (DirContext)obj;
                if (refCtx instanceof LdapContext && reqCtls != null) {
                    ((LdapContext)refCtx).setRequestControls(reqCtls);
                }
                initDefaults(referral, nextName);
                break;
            } else {
                NamingException ne = new NotContextException(
                    "Cannot create context for: " + referral);
                ne.setRemainingName((new CompositeName()).add(nextName));
                throw ne;
            }
        }
    }
    private void initDefaults(String referral, String nextName)
        throws NamingException {
        String urlString;
        try {
            LdapURL url = new LdapURL(referral);
            urlString = url.getDN();
            urlAttrs = url.getAttributes();
            urlScope = url.getScope();
            urlFilter = url.getFilter();
        } catch (NamingException e) {
            urlString = referral;
            urlAttrs = urlScope = urlFilter = null;
        }
        if (urlString == null) {
            urlString = nextName;
        } else {
            urlString = "";
        }
        if (urlString == null) {
            urlName = null;
        } else {
            urlName = urlString.equals("") ? new CompositeName() :
                new CompositeName().add(urlString);
        }
    }
    public void close() throws NamingException {
        if (refCtx != null) {
            refCtx.close();
            refCtx = null;
        }
        refEx = null;
    }
    void setHopCount(int hopCount) {
        this.hopCount = hopCount;
        if ((refCtx != null) && (refCtx instanceof LdapCtx)) {
            ((LdapCtx)refCtx).setHopCount(hopCount);
        }
    }
    public Object lookup(String name) throws NamingException {
        return lookup(toName(name));
    }
    public Object lookup(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.lookup(overrideName(name));
    }
    public void bind(String name, Object obj) throws NamingException {
        bind(toName(name), obj);
    }
    public void bind(Name name, Object obj) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.bind(overrideName(name), obj);
    }
    public void rebind(String name, Object obj) throws NamingException {
        rebind(toName(name), obj);
    }
    public void rebind(Name name, Object obj) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.rebind(overrideName(name), obj);
    }
    public void unbind(String name) throws NamingException {
        unbind(toName(name));
    }
    public void unbind(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.unbind(overrideName(name));
    }
    public void rename(String oldName, String newName) throws NamingException {
        rename(toName(oldName), toName(newName));
    }
    public void rename(Name oldName, Name newName) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.rename(overrideName(oldName), toName(refEx.getNewRdn()));
    }
    public NamingEnumeration list(String name) throws NamingException {
        return list(toName(name));
    }
    public NamingEnumeration list(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        try {
            NamingEnumeration ne = null;
            if (urlScope != null && urlScope.equals("base")) {
                SearchControls cons = new SearchControls();
                cons.setReturningObjFlag(true);
                cons.setSearchScope(SearchControls.OBJECT_SCOPE);
                ne = refCtx.search(overrideName(name), "(objectclass=*)", cons);
            } else {
                ne = refCtx.list(overrideName(name));
            }
            refEx.setNameResolved(true);
            ((ReferralEnumeration)ne).appendUnprocessedReferrals(refEx);
            return (ne);
        } catch (LdapReferralException e) {
            e.appendUnprocessedReferrals(refEx);
            throw (NamingException)(e.fillInStackTrace());
        } catch (NamingException e) {
            if ((refEx != null) && (! refEx.hasMoreReferrals())) {
                refEx.setNamingException(e);
            }
            if ((refEx != null) &&
                (refEx.hasMoreReferrals() ||
                 refEx.hasMoreReferralExceptions())) {
                throw (NamingException)
                    ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
            } else {
                throw e;
            }
        }
    }
    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(toName(name));
    }
    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        try {
            NamingEnumeration be = null;
            if (urlScope != null && urlScope.equals("base")) {
                SearchControls cons = new SearchControls();
                cons.setReturningObjFlag(true);
                cons.setSearchScope(SearchControls.OBJECT_SCOPE);
                be = refCtx.search(overrideName(name), "(objectclass=*)", cons);
            } else {
                be = refCtx.listBindings(overrideName(name));
            }
            refEx.setNameResolved(true);
            ((ReferralEnumeration)be).appendUnprocessedReferrals(refEx);
            return (be);
        } catch (LdapReferralException e) {
            e.appendUnprocessedReferrals(refEx);
            throw (NamingException)(e.fillInStackTrace());
        } catch (NamingException e) {
            if ((refEx != null) && (! refEx.hasMoreReferrals())) {
                refEx.setNamingException(e);
            }
            if ((refEx != null) &&
                (refEx.hasMoreReferrals() ||
                 refEx.hasMoreReferralExceptions())) {
                throw (NamingException)
                    ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
            } else {
                throw e;
            }
        }
    }
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(toName(name));
    }
    public void destroySubcontext(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.destroySubcontext(overrideName(name));
    }
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(toName(name));
    }
    public Context createSubcontext(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.createSubcontext(overrideName(name));
    }
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(toName(name));
    }
    public Object lookupLink(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.lookupLink(overrideName(name));
    }
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(toName(name));
    }
    public NameParser getNameParser(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.getNameParser(overrideName(name));
    }
    public String composeName(String name, String prefix)
            throws NamingException {
                return composeName(toName(name), toName(prefix)).toString();
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.composeName(name, prefix);
    }
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.addToEnvironment(propName, propVal);
    }
    public Object removeFromEnvironment(String propName)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.removeFromEnvironment(propName);
    }
    public Hashtable getEnvironment() throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.getEnvironment();
    }
    public Attributes getAttributes(String name) throws NamingException {
        return getAttributes(toName(name));
    }
    public Attributes getAttributes(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.getAttributes(overrideName(name));
    }
    public Attributes getAttributes(String name, String[] attrIds)
            throws NamingException {
        return getAttributes(toName(name), attrIds);
    }
    public Attributes getAttributes(Name name, String[] attrIds)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.getAttributes(overrideName(name), attrIds);
    }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
            throws NamingException {
        modifyAttributes(toName(name), mod_op, attrs);
    }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.modifyAttributes(overrideName(name), mod_op, attrs);
    }
    public void modifyAttributes(String name, ModificationItem[] mods)
            throws NamingException {
        modifyAttributes(toName(name), mods);
    }
    public void modifyAttributes(Name name, ModificationItem[] mods)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.modifyAttributes(overrideName(name), mods);
    }
    public void bind(String name, Object obj, Attributes attrs)
            throws NamingException {
        bind(toName(name), obj, attrs);
    }
    public void bind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.bind(overrideName(name), obj, attrs);
    }
    public void rebind(String name, Object obj, Attributes attrs)
            throws NamingException {
        rebind(toName(name), obj, attrs);
    }
    public void rebind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        refCtx.rebind(overrideName(name), obj, attrs);
    }
    public DirContext createSubcontext(String name, Attributes attrs)
            throws NamingException {
        return createSubcontext(toName(name), attrs);
    }
    public DirContext createSubcontext(Name name, Attributes attrs)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.createSubcontext(overrideName(name), attrs);
    }
    public DirContext getSchema(String name) throws NamingException {
        return getSchema(toName(name));
    }
    public DirContext getSchema(Name name) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return refCtx.getSchema(overrideName(name));
    }
    public DirContext getSchemaClassDefinition(String name)
            throws NamingException {
        return getSchemaClassDefinition(toName(name));
    }
    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
      return refCtx.getSchemaClassDefinition(overrideName(name));
    }
    public NamingEnumeration search(String name,
                                    Attributes matchingAttributes)
            throws NamingException {
        return search(toName(name), SearchFilter.format(matchingAttributes),
            new SearchControls());
    }
    public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes)
            throws NamingException {
        return search(name, SearchFilter.format(matchingAttributes),
            new SearchControls());
    }
    public NamingEnumeration search(String name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
            throws NamingException {
        SearchControls cons = new SearchControls();
        cons.setReturningAttributes(attributesToReturn);
        return search(toName(name), SearchFilter.format(matchingAttributes),
            cons);
    }
    public NamingEnumeration search(Name name,
                                    Attributes matchingAttributes,
                                    String[] attributesToReturn)
            throws NamingException {
        SearchControls cons = new SearchControls();
        cons.setReturningAttributes(attributesToReturn);
        return search(name, SearchFilter.format(matchingAttributes), cons);
    }
    public NamingEnumeration search(String name,
                                    String filter,
                                    SearchControls cons)
            throws NamingException {
        return search(toName(name), filter, cons);
    }
    public NamingEnumeration search(Name name,
                                    String filter,
        SearchControls cons) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        try {
            NamingEnumeration se = refCtx.search(overrideName(name),
                overrideFilter(filter), overrideAttributesAndScope(cons));
            refEx.setNameResolved(true);
            ((ReferralEnumeration)se).appendUnprocessedReferrals(refEx);
            return (se);
        } catch (LdapReferralException e) {
            e.appendUnprocessedReferrals(refEx);
            throw (NamingException)(e.fillInStackTrace());
        } catch (NamingException e) {
            if ((refEx != null) && (! refEx.hasMoreReferrals())) {
                refEx.setNamingException(e);
            }
            if ((refEx != null) &&
                (refEx.hasMoreReferrals() ||
                 refEx.hasMoreReferralExceptions())) {
                throw (NamingException)
                    ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
            } else {
                throw e;
            }
        }
    }
    public NamingEnumeration search(String name,
                                    String filterExpr,
                                    Object[] filterArgs,
                                    SearchControls cons)
            throws NamingException {
        return search(toName(name), filterExpr, filterArgs, cons);
    }
    public NamingEnumeration search(Name name,
        String filterExpr,
        Object[] filterArgs,
        SearchControls cons) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        try {
            NamingEnumeration se;
            if (urlFilter != null) {
                se = refCtx.search(overrideName(name), urlFilter,
                overrideAttributesAndScope(cons));
            } else {
                se = refCtx.search(overrideName(name), filterExpr,
                filterArgs, overrideAttributesAndScope(cons));
            }
            refEx.setNameResolved(true);
            ((ReferralEnumeration)se).appendUnprocessedReferrals(refEx);
            return (se);
        } catch (LdapReferralException e) {
            e.appendUnprocessedReferrals(refEx);
            throw (NamingException)(e.fillInStackTrace());
        } catch (NamingException e) {
            if ((refEx != null) && (! refEx.hasMoreReferrals())) {
                refEx.setNamingException(e);
            }
            if ((refEx != null) &&
                (refEx.hasMoreReferrals() ||
                 refEx.hasMoreReferralExceptions())) {
                throw (NamingException)
                    ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
            } else {
                throw e;
            }
        }
    }
    public String getNameInNamespace() throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        return urlName != null && !urlName.isEmpty() ? urlName.get(0) : "";
    }
    public ExtendedResponse extendedOperation(ExtendedRequest request)
        throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        return ((LdapContext)refCtx).extendedOperation(request);
    }
    public LdapContext newInstance(Control[] requestControls)
        throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        return ((LdapContext)refCtx).newInstance(requestControls);
    }
    public void reconnect(Control[] connCtls) throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        ((LdapContext)refCtx).reconnect(connCtls);
    }
    public Control[] getConnectControls() throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        return ((LdapContext)refCtx).getConnectControls();
    }
    public void setRequestControls(Control[] requestControls)
        throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        ((LdapContext)refCtx).setRequestControls(requestControls);
    }
    public Control[] getRequestControls() throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        return ((LdapContext)refCtx).getRequestControls();
    }
    public Control[] getResponseControls() throws NamingException {
        if (skipThisReferral) {
            throw (NamingException)
                ((refEx.appendUnprocessedReferrals(null)).fillInStackTrace());
        }
        if (!(refCtx instanceof LdapContext)) {
            throw new NotContextException(
                "Referral context not an instance of LdapContext");
        }
        return ((LdapContext)refCtx).getResponseControls();
    }
    private Name toName(String name) throws InvalidNameException {
        return name.equals("") ? new CompositeName() :
            new CompositeName().add(name);
    }
    private Name overrideName(Name name) throws InvalidNameException {
        return (urlName == null ? name : urlName);
    }
    private SearchControls overrideAttributesAndScope(SearchControls cons) {
        SearchControls urlCons;
        if ((urlScope != null) || (urlAttrs != null)) {
            urlCons = new SearchControls(cons.getSearchScope(),
                                        cons.getCountLimit(),
                                        cons.getTimeLimit(),
                                        cons.getReturningAttributes(),
                                        cons.getReturningObjFlag(),
                                        cons.getDerefLinkFlag());
            if (urlScope != null) {
                if (urlScope.equals("base")) {
                    urlCons.setSearchScope(SearchControls.OBJECT_SCOPE);
                } else if (urlScope.equals("one")) {
                    urlCons.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                } else if (urlScope.equals("sub")) {
                    urlCons.setSearchScope(SearchControls.SUBTREE_SCOPE);
                }
            }
            if (urlAttrs != null) {
                StringTokenizer tokens = new StringTokenizer(urlAttrs, ",");
                int count = tokens.countTokens();
                String[] attrs = new String[count];
                for (int i = 0; i < count; i ++) {
                    attrs[i] = tokens.nextToken();
                }
                urlCons.setReturningAttributes(attrs);
            }
            return urlCons;
        } else {
            return cons;
        }
    }
    private String overrideFilter(String filter) {
        return (urlFilter == null ? filter : urlFilter);
    }
}
