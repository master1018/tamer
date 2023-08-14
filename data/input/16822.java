final class LdapSearchEnumeration extends LdapNamingEnumeration {
    private Name startName;             
    private LdapCtx.SearchArgs searchArgs = null;
    LdapSearchEnumeration(LdapCtx homeCtx, LdapResult search_results,
        String starter, LdapCtx.SearchArgs args, Continuation cont)
        throws NamingException {
        super(homeCtx, search_results,
              args.name, 
              cont);
        startName = new LdapName(starter);
        searchArgs = args;
    }
    protected NameClassPair
    createItem(String dn, Attributes attrs, Vector respCtls)
        throws NamingException {
        Object obj = null;
        String relStart;         
        String relHome;          
        boolean relative = true; 
        try {
            Name parsed = new LdapName(dn);
            if (startName != null && parsed.startsWith(startName)) {
                relStart = parsed.getSuffix(startName.size()).toString();
                relHome = parsed.getSuffix(homeCtx.currentParsedDN.size()).toString();
            } else {
                relative = false;
                relHome = relStart =
                    LdapURL.toUrlString(homeCtx.hostname, homeCtx.port_number,
                    dn, homeCtx.hasLdapsScheme);
            }
        } catch (NamingException e) {
            relative = false;
            relHome = relStart =
                LdapURL.toUrlString(homeCtx.hostname, homeCtx.port_number,
                dn, homeCtx.hasLdapsScheme);
        }
        CompositeName cn = new CompositeName();
        if (!relStart.equals("")) {
            cn.add(relStart);
        }
        CompositeName rcn = new CompositeName();
        if (!relHome.equals("")) {
            rcn.add(relHome);
        }
        homeCtx.setParents(attrs, rcn);
        if (searchArgs.cons.getReturningObjFlag()) {
            if (attrs.get(Obj.JAVA_ATTRIBUTES[Obj.CLASSNAME]) != null) {
                obj = Obj.decodeObject(attrs);
            }
            if (obj == null) {
                obj = new LdapCtx(homeCtx, dn);
            }
            try {
                obj = DirectoryManager.getObjectInstance(
                    obj, rcn, (relative ? homeCtx : null),
                    homeCtx.envprops, attrs);
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                NamingException ne =
                    new NamingException(
                            "problem generating object using object factory");
                ne.setRootCause(e);
                throw ne;
            }
            String[] reqAttrs;
            if ((reqAttrs = searchArgs.reqAttrs) != null) {
                Attributes rattrs = new BasicAttributes(true); 
                for (int i = 0; i < reqAttrs.length; i++) {
                    rattrs.put(reqAttrs[i], null);
                }
                for (int i = 0; i < Obj.JAVA_ATTRIBUTES.length; i++) {
                    if (rattrs.get(Obj.JAVA_ATTRIBUTES[i]) == null) {
                        attrs.remove(Obj.JAVA_ATTRIBUTES[i]);
                    }
                }
            }
        }
        SearchResult sr;
        if (respCtls != null) {
            sr = new SearchResultWithControls(
                (relative ? cn.toString() : relStart), obj, attrs,
                relative, homeCtx.convertControls(respCtls));
        } else {
            sr = new SearchResult(
                (relative ? cn.toString() : relStart),
                obj, attrs, relative);
        }
        sr.setNameInNamespace(dn);
        return sr;
    }
    public void appendUnprocessedReferrals(LdapReferralException ex) {
        startName = null;
        super.appendUnprocessedReferrals(ex);
    }
    protected LdapNamingEnumeration
    getReferredResults(LdapReferralContext refCtx) throws NamingException {
        return (LdapSearchEnumeration)
            refCtx.search(searchArgs.name, searchArgs.filter, searchArgs.cons);
    }
    protected void update(LdapNamingEnumeration ne) {
        super.update(ne);
        LdapSearchEnumeration se = (LdapSearchEnumeration)ne;
        startName = se.startName;
    }
    void setStartName(Name nm) {
        startName = nm;
    }
}
