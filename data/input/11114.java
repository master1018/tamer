final class LdapBindingEnumeration extends LdapNamingEnumeration {
    LdapBindingEnumeration(LdapCtx homeCtx, LdapResult answer, Name remain,
        Continuation cont) throws NamingException
    {
        super(homeCtx, answer, remain, cont);
    }
    protected NameClassPair
      createItem(String dn, Attributes attrs, Vector respCtls)
        throws NamingException {
        Object obj = null;
        String atom = getAtom(dn);
        if (attrs.get(Obj.JAVA_ATTRIBUTES[Obj.CLASSNAME]) != null) {
            obj = Obj.decodeObject(attrs);
        }
        if (obj == null) {
            obj = new LdapCtx(homeCtx, dn);
        }
        CompositeName cn = new CompositeName();
        cn.add(atom);
        try {
            obj = DirectoryManager.getObjectInstance(obj, cn, homeCtx,
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
        Binding binding;
        if (respCtls != null) {
           binding = new BindingWithControls(cn.toString(), obj,
                                homeCtx.convertControls(respCtls));
        } else {
            binding = new Binding(cn.toString(), obj);
        }
        binding.setNameInNamespace(dn);
        return binding;
    }
    protected LdapNamingEnumeration
    getReferredResults(LdapReferralContext refCtx) throws NamingException{
        return (LdapNamingEnumeration) refCtx.listBindings(listArg);
    }
}
