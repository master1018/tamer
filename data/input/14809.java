public abstract class AtomicContext extends ComponentContext {
    private static int debug = 0;
    protected AtomicContext () {
        _contextType = _ATOMIC;
    }
    protected abstract Object a_lookup(String name, Continuation cont)
        throws NamingException;
    protected abstract Object a_lookupLink(String name, Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration a_list(
        Continuation cont) throws NamingException;
    protected abstract NamingEnumeration a_listBindings(
        Continuation cont) throws NamingException;
    protected abstract void a_bind(String name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void a_rebind(String name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void a_unbind(String name, Continuation cont)
        throws NamingException;
    protected abstract void a_destroySubcontext(String name, Continuation cont)
        throws NamingException;
    protected abstract Context a_createSubcontext(String name,
        Continuation cont) throws NamingException;
    protected abstract void a_rename(String oldname, Name newname,
        Continuation cont) throws NamingException;
    protected abstract NameParser a_getNameParser(Continuation cont)
        throws NamingException;
    protected abstract StringHeadTail c_parseComponent(String inputName,
        Continuation cont) throws NamingException;
    protected Object a_resolveIntermediate_nns(String name, Continuation cont)
        throws NamingException {
            try {
                final Object obj = a_lookup(name, cont);
                if (obj != null && getClass().isInstance(obj)) {
                    cont.setContinueNNS(obj, name, this);
                    return null;
                } else if (obj != null && !(obj instanceof Context)) {
                    RefAddr addr = new RefAddr("nns") {
                        public Object getContent() {
                            return obj;
                        }
                        private static final long serialVersionUID =
                            -3399518522645918499L;
                    };
                    Reference ref = new Reference("java.lang.Object", addr);
                    CompositeName resName = new CompositeName();
                    resName.add(name);
                    resName.add(""); 
                    cont.setContinue(ref, resName, this);
                    return null;
                } else {
                    return obj;
                }
            } catch (NamingException e) {
                e.appendRemainingComponent(""); 
                throw e;
            }
        }
    protected Object a_lookup_nns(String name, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected Object a_lookupLink_nns(String name, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration a_list_nns(Continuation cont)
        throws NamingException {
            a_processJunction_nns(cont);
            return null;
        }
    protected NamingEnumeration a_listBindings_nns(Continuation cont)
        throws NamingException {
            a_processJunction_nns(cont);
            return null;
        }
    protected void a_bind_nns(String name, Object obj, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected void a_rebind_nns(String name, Object obj, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected void a_unbind_nns(String name, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected Context a_createSubcontext_nns(String name, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
            return null;
        }
    protected void a_destroySubcontext_nns(String name, Continuation cont)
        throws NamingException {
            a_processJunction_nns(name, cont);
        }
    protected void a_rename_nns(String oldname, Name newname, Continuation cont)
        throws NamingException {
            a_processJunction_nns(oldname, cont);
        }
    protected NameParser a_getNameParser_nns(Continuation cont)
        throws NamingException {
            a_processJunction_nns(cont);
            return null;
        }
    protected boolean isEmpty(String name) {
        return name == null || name.equals("");
    }
    protected Object c_lookup(Name name, Continuation cont)
        throws NamingException {
            Object ret = null;
            if (resolve_to_penultimate_context(name, cont)) {
                ret = a_lookup(name.toString(), cont);
                if (ret != null && ret instanceof LinkRef) {
                    cont.setContinue(ret, name, this);
                    ret = null;
                }
            }
            return ret;
        }
    protected Object c_lookupLink(Name name, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont)) {
                return a_lookupLink(name.toString(), cont);
            }
            return null;
        }
    protected NamingEnumeration c_list(Name name,
        Continuation cont) throws NamingException {
            if (resolve_to_context(name, cont)) {
                return a_list(cont);
            }
            return null;
        }
    protected NamingEnumeration c_listBindings(Name name,
        Continuation cont) throws NamingException {
            if (resolve_to_context(name, cont)) {
                return a_listBindings(cont);
            }
            return null;
        }
    protected void c_bind(Name name, Object obj, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_bind(name.toString(), obj, cont);
        }
    protected void c_rebind(Name name, Object obj, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_rebind(name.toString(), obj, cont);
        }
    protected void c_unbind(Name name, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_unbind(name.toString(), cont);
        }
    protected void c_destroySubcontext(Name name, Continuation cont)
        throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                a_destroySubcontext(name.toString(), cont);
        }
    protected Context c_createSubcontext(Name name,
        Continuation cont) throws NamingException {
            if (resolve_to_penultimate_context(name, cont))
                return a_createSubcontext(name.toString(), cont);
            else
                return null;
        }
    protected void c_rename(Name oldname, Name newname,
        Continuation cont) throws NamingException {
            if (resolve_to_penultimate_context(oldname, cont))
                 a_rename(oldname.toString(), newname, cont);
        }
    protected NameParser c_getNameParser(Name name,
        Continuation cont) throws NamingException {
            if (resolve_to_context(name, cont))
                return a_getNameParser(cont);
            return null;
        }
    protected Object c_resolveIntermediate_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                Object ret = null;
                if (resolve_to_penultimate_context_nns(name, cont)) {
                    ret = a_resolveIntermediate_nns(name.toString(), cont);
                    if (ret != null && ret instanceof LinkRef) {
                        cont.setContinue(ret, name, this);
                        ret = null;
                    }
                }
                return ret;
            } else {
                return super.c_resolveIntermediate_nns(name, cont);
            }
        }
    protected Object c_lookup_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                Object ret = null;
                if (resolve_to_penultimate_context_nns(name, cont)) {
                    ret = a_lookup_nns(name.toString(), cont);
                    if (ret != null && ret instanceof LinkRef) {
                        cont.setContinue(ret, name, this);
                        ret = null;
                    }
                }
                return ret;
            } else {
                return super.c_lookup_nns(name, cont);
            }
        }
    protected Object c_lookupLink_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                resolve_to_nns_and_continue(name, cont);
                return null;
            } else {
                return super.c_lookupLink_nns(name, cont);
            }
        }
    protected NamingEnumeration c_list_nns(Name name,
        Continuation cont) throws NamingException {
            if (_contextType == _ATOMIC) {
                resolve_to_nns_and_continue(name, cont);
                return null;
            } else {
                return super.c_list_nns(name, cont);
            }
        }
    protected NamingEnumeration c_listBindings_nns(Name name,
        Continuation cont) throws NamingException {
            if (_contextType == _ATOMIC) {
                resolve_to_nns_and_continue(name, cont);
                return null;
            } else {
                return super.c_list_nns(name, cont);
            }
        }
    protected void c_bind_nns(Name name, Object obj, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(name, cont))
                    a_bind_nns(name.toString(), obj, cont);
            } else {
                super.c_bind_nns(name, obj, cont);
            }
        }
    protected void c_rebind_nns(Name name, Object obj, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(name, cont))
                    a_rebind_nns(name.toString(), obj, cont);
            } else {
                super.c_rebind_nns(name, obj, cont);
            }
        }
    protected void c_unbind_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(name, cont))
                    a_unbind_nns(name.toString(), cont);
            } else {
                super.c_unbind_nns(name, cont);
            }
        }
    protected Context c_createSubcontext_nns(Name name,
        Continuation cont) throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(name, cont))
                    return a_createSubcontext_nns(name.toString(), cont);
                else
                    return null;
            } else {
                return super.c_createSubcontext_nns(name, cont);
            }
        }
    protected void c_destroySubcontext_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(name, cont))
                    a_destroySubcontext_nns(name.toString(), cont);
            } else {
                super.c_destroySubcontext_nns(name, cont);
            }
        }
    protected void c_rename_nns(Name oldname, Name newname, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                if (resolve_to_penultimate_context_nns(oldname, cont))
                    a_rename_nns(oldname.toString(), newname, cont);
            } else {
                super.c_rename_nns(oldname, newname, cont);
            }
        }
    protected NameParser c_getNameParser_nns(Name name, Continuation cont)
        throws NamingException {
            if (_contextType == _ATOMIC) {
                resolve_to_nns_and_continue(name, cont);
                return null;
            } else {
                return super.c_getNameParser_nns(name, cont);
            }
        }
    protected void a_processJunction_nns(String name, Continuation cont)
        throws NamingException {
            if (name.equals("")) {
                NameNotFoundException e = new NameNotFoundException();
                cont.setErrorNNS(this, name);
                throw cont.fillInException(e);
            }
            try {
                Object target = a_lookup(name, cont);
                if (cont.isContinue())
                    cont.appendRemainingComponent("");  
                else {
                    cont.setContinueNNS(target, name, this);
                }
            } catch (NamingException e) {
                e.appendRemainingComponent(""); 
                throw e;
            }
        }
    protected void a_processJunction_nns(Continuation cont) throws NamingException {
        RefAddr addr = new RefAddr("nns") {
            public Object getContent() {
                return AtomicContext.this;
            }
            private static final long serialVersionUID = 3449785852664978312L;
        };
        Reference ref = new Reference("java.lang.Object", addr);
        cont.setContinue(ref, _NNS_NAME, this);
    }
    protected boolean resolve_to_context(Name name, Continuation cont)
    throws NamingException {
        String target = name.toString();
        StringHeadTail ht = c_parseComponent(target, cont);
        String tail = ht.getTail();
        String head = ht.getHead();
        if (debug > 0)
            System.out.println("RESOLVE TO CONTEXT(" + target + ") = {" +
                               head + ", " + tail + "}");
        if (head == null) {
            InvalidNameException e = new InvalidNameException();
            throw cont.fillInException(e);
        }
        if (!isEmpty(head)) {
            try {
                Object headCtx = a_lookup(head, cont);
                if (headCtx != null)
                    cont.setContinue(headCtx, head, this, (tail == null ? "" : tail));
                else if (cont.isContinue())
                    cont.appendRemainingComponent(tail);
            } catch (NamingException e) {
                e.appendRemainingComponent(tail);
                throw e;
            }
        } else {
            cont.setSuccess();  
            return true;
        }
        return false;
    }
    protected boolean resolve_to_penultimate_context(Name name, Continuation cont)
    throws NamingException {
        String target = name.toString();
        if (debug > 0)
            System.out.println("RESOLVE TO PENULTIMATE" + target);
        StringHeadTail ht = c_parseComponent(target, cont);
        String tail = ht.getTail();
        String head = ht.getHead();
        if (head == null) {
            InvalidNameException e = new InvalidNameException();
            throw cont.fillInException(e);
        }
        if (!isEmpty(tail)) {
            try {
                Object headCtx = a_lookup(head, cont);
                if (headCtx != null)
                    cont.setContinue(headCtx, head, this, tail);
                else if (cont.isContinue())
                    cont.appendRemainingComponent(tail);
            } catch (NamingException e) {
                e.appendRemainingComponent(tail);
                throw e;
            }
        } else {
            cont.setSuccess();  
            return true;
        }
        return false;
    }
    protected boolean resolve_to_penultimate_context_nns(Name name,
                                                         Continuation cont)
        throws NamingException {
            try {
        if (debug > 0)
            System.out.println("RESOLVE TO PENULTIMATE NNS" + name.toString());
                boolean answer = resolve_to_penultimate_context(name, cont);
                if (cont.isContinue())
                    cont.appendRemainingComponent("");
                return answer;
            } catch (NamingException e) {
                e.appendRemainingComponent("");
                throw e;
            }
        }
    protected void resolve_to_nns_and_continue(Name name, Continuation cont)
        throws NamingException {
        if (debug > 0)
            System.out.println("RESOLVE TO NNS AND CONTINUE" + name.toString());
        if (resolve_to_penultimate_context_nns(name, cont)) {
            Object nns = a_lookup_nns(name.toString(), cont);
            if (nns != null)
                cont.setContinue(nns, name, this);
        }
    }
}
