class LdapNamingEnumeration implements NamingEnumeration, ReferralEnumeration {
    protected Name listArg;
    private boolean cleaned = false;
    private LdapResult res;
    private LdapClient enumClnt;
    private Continuation cont;  
    private Vector entries = null;
    private int limit = 0;
    private int posn = 0;
    protected LdapCtx homeCtx;
    private LdapReferralException refEx = null;
    private NamingException errEx = null;
    private static final String defaultClassName = DirContext.class.getName();
    LdapNamingEnumeration(LdapCtx homeCtx, LdapResult answer, Name listArg,
        Continuation cont) throws NamingException {
            if ((answer.status != LdapClient.LDAP_SUCCESS) &&
                (answer.status != LdapClient.LDAP_SIZE_LIMIT_EXCEEDED) &&
                (answer.status != LdapClient.LDAP_TIME_LIMIT_EXCEEDED) &&
                (answer.status != LdapClient.LDAP_ADMIN_LIMIT_EXCEEDED) &&
                (answer.status != LdapClient.LDAP_REFERRAL) &&
                (answer.status != LdapClient.LDAP_PARTIAL_RESULTS)) {
                NamingException e = new NamingException(
                                    LdapClient.getErrorMessage(
                                    answer.status, answer.errorMessage));
                throw cont.fillInException(e);
            }
            res = answer;
            entries = answer.entries;
            limit = (entries == null) ? 0 : entries.size(); 
            this.listArg = listArg;
            this.cont = cont;
            if (answer.refEx != null) {
                refEx = answer.refEx;
            }
            this.homeCtx = homeCtx;
            homeCtx.incEnumCount();
            enumClnt = homeCtx.clnt; 
    }
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            cleanup();
            return null;
        }
    }
    public boolean hasMoreElements() {
        try {
            return hasMore();
        } catch (NamingException e) {
            cleanup();
            return false;
        }
    }
    private void getNextBatch() throws NamingException {
        res = homeCtx.getSearchReply(enumClnt, res);
        if (res == null) {
            limit = posn = 0;
            return;
        }
        entries = res.entries;
        limit = (entries == null) ? 0 : entries.size(); 
        posn = 0; 
        if ((res.status != LdapClient.LDAP_SUCCESS) ||
            ((res.status == LdapClient.LDAP_SUCCESS) &&
                (res.referrals != null))) {
            try {
                homeCtx.processReturnCode(res, listArg);
            } catch (LimitExceededException e) {
                setNamingException(e);
            } catch (PartialResultException e) {
                setNamingException(e);
            }
        }
        if (res.refEx != null) {
            if (refEx == null) {
                refEx = res.refEx;
            } else {
                refEx = refEx.appendUnprocessedReferrals(res.refEx);
            }
            res.refEx = null; 
        }
        if (res.resControls != null) {
            homeCtx.respCtls = res.resControls;
        }
    }
    private boolean more = true;  
    private boolean hasMoreCalled = false;
    public boolean hasMore() throws NamingException {
        if (hasMoreCalled) {
            return more;
        }
        hasMoreCalled = true;
        if (!more) {
            return false;
        } else {
            return (more = hasMoreImpl());
        }
    }
    public Object next() throws NamingException {
        if (!hasMoreCalled) {
            hasMore();
        }
        hasMoreCalled = false;
        return nextImpl();
    }
    private boolean hasMoreImpl() throws NamingException {
        if (posn == limit) {
            getNextBatch();
        }
        if (posn < limit) {
            return true;
        } else {
            try {
                return hasMoreReferrals();
            } catch (LdapReferralException e) {
                cleanup();
                throw e;
            } catch (LimitExceededException e) {
                cleanup();
                throw e;
            } catch (PartialResultException e) {
                cleanup();
                throw e;
            } catch (NamingException e) {
                cleanup();
                PartialResultException pre = new PartialResultException();
                pre.setRootCause(e);
                throw pre;
            }
        }
    }
    private Object nextImpl() throws NamingException {
        try {
            return nextAux();
        } catch (NamingException e) {
            cleanup();
            throw cont.fillInException(e);
        }
    }
    private Object nextAux() throws NamingException {
        if (posn == limit) {
            getNextBatch();  
        }
        if (posn >= limit) {
            cleanup();
            throw new NoSuchElementException("invalid enumeration handle");
        }
        LdapEntry result = (LdapEntry)entries.elementAt(posn++);
        return createItem(result.DN, result.attributes, result.respCtls);
    }
    protected String getAtom(String dn) {
        String atom;
        try {
            Name parsed = new LdapName(dn);
            return parsed.get(parsed.size() - 1);
        } catch (NamingException e) {
            return dn;
        }
    }
    protected NameClassPair createItem(String dn, Attributes attrs,
        Vector respCtls) throws NamingException {
        Attribute attr;
        String className = null;
        if ((attr = attrs.get(Obj.JAVA_ATTRIBUTES[Obj.CLASSNAME])) != null) {
            className = (String)attr.get();
        } else {
            className = defaultClassName;
        }
        CompositeName cn = new CompositeName();
        cn.add(getAtom(dn));
        NameClassPair ncp;
        if (respCtls != null) {
            ncp = new NameClassPairWithControls(
                        cn.toString(), className,
                        homeCtx.convertControls(respCtls));
        } else {
            ncp = new NameClassPair(cn.toString(), className);
        }
        ncp.setNameInNamespace(dn);
        return ncp;
    }
    public void appendUnprocessedReferrals(LdapReferralException ex) {
        if (refEx != null) {
            refEx = refEx.appendUnprocessedReferrals(ex);
        } else {
            refEx = ex.appendUnprocessedReferrals(refEx);
        }
    }
    void setNamingException(NamingException e) {
        errEx = e;
    }
    protected LdapNamingEnumeration
    getReferredResults(LdapReferralContext refCtx) throws NamingException {
        return (LdapNamingEnumeration)refCtx.list(listArg);
    }
    protected boolean hasMoreReferrals() throws NamingException {
        if ((refEx != null) &&
            (refEx.hasMoreReferrals() ||
             refEx.hasMoreReferralExceptions())) {
            if (homeCtx.handleReferrals == LdapClient.LDAP_REF_THROW) {
                throw (NamingException)(refEx.fillInStackTrace());
            }
            while (true) {
                LdapReferralContext refCtx =
                    (LdapReferralContext)refEx.getReferralContext(
                    homeCtx.envprops, homeCtx.reqCtls);
                try {
                    update(getReferredResults(refCtx));
                    break;
                } catch (LdapReferralException re) {
                    if (errEx == null) {
                        errEx = re.getNamingException();
                    }
                    refEx = re;
                    continue;
                } finally {
                    refCtx.close();
                }
            }
            return hasMoreImpl();
        } else {
            cleanup();
            if (errEx != null) {
                throw errEx;
            }
            return (false);
        }
    }
    protected void update(LdapNamingEnumeration ne) {
        homeCtx.decEnumCount();
        homeCtx = ne.homeCtx;
        enumClnt = ne.enumClnt;
        ne.homeCtx = null;
        posn = ne.posn;
        limit = ne.limit;
        res = ne.res;
        entries = ne.entries;
        refEx = ne.refEx;
        listArg = ne.listArg;
    }
    protected void finalize() {
        cleanup();
    }
    protected void cleanup() {
        if (cleaned) return; 
        if(enumClnt != null) {
            enumClnt.clearSearchReply(res, homeCtx.reqCtls);
        }
        enumClnt = null;
        cleaned = true;
        if (homeCtx != null) {
            homeCtx.decEnumCount();
            homeCtx = null;
        }
    }
    public void close() {
        cleanup();
    }
}
