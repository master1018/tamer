public abstract class ComponentContext extends PartialCompositeContext {
    private static int debug = 0;
    protected ComponentContext() {
        _contextType = _COMPONENT;
    }
    protected abstract Object c_lookup(Name name, Continuation cont)
        throws NamingException;
    protected abstract Object c_lookupLink(Name name, Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration c_list(Name name,
        Continuation cont) throws NamingException;
    protected abstract NamingEnumeration c_listBindings(Name name,
        Continuation cont) throws NamingException;
    protected abstract void c_bind(Name name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void c_rebind(Name name, Object obj, Continuation cont)
        throws NamingException;
    protected abstract void c_unbind(Name name, Continuation cont)
        throws NamingException;
    protected abstract void c_destroySubcontext(Name name, Continuation cont)
        throws NamingException;
    protected abstract Context c_createSubcontext(Name name,
        Continuation cont) throws NamingException;
    protected abstract void c_rename(Name oldname, Name newname,
        Continuation cont) throws NamingException;
    protected abstract NameParser c_getNameParser(Name name, Continuation cont)
        throws NamingException;
    protected HeadTail p_parseComponent(Name name, Continuation cont)
        throws NamingException {
        int separator;
        if (name.isEmpty() ||  name.get(0).equals("")) {
            separator = 0;
        } else {
            separator = 1;
        }
        Name head, tail;
        if (name instanceof CompositeName) {
            head = name.getPrefix(separator);
            tail = name.getSuffix(separator);
        } else {
            head = new CompositeName().add(name.toString());
            tail = null;
        }
        if (debug > 2) {
            System.err.println("ORIG: " + name);
            System.err.println("PREFIX: " + name);
            System.err.println("SUFFIX: " + null);
        }
        return new HeadTail(head, tail);
    }
    protected Object c_resolveIntermediate_nns(Name name, Continuation cont)
        throws NamingException {
            try {
                final Object obj = c_lookup(name, cont);
                if (obj != null && getClass().isInstance(obj)) {
                    cont.setContinueNNS(obj, name, this);
                    return null;
                } else if (obj != null && !(obj instanceof Context)) {
                    RefAddr addr = new RefAddr("nns") {
                        public Object getContent() {
                            return obj;
                        }
                        private static final long serialVersionUID =
                            -8831204798861786362L;
                    };
                    Reference ref = new Reference("java.lang.Object", addr);
                    CompositeName resName = (CompositeName)name.clone();
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
    protected Object c_lookup_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected Object c_lookupLink_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration c_list_nns(Name name,
        Continuation cont) throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration c_listBindings_nns(Name name,
        Continuation cont) throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected void c_bind_nns(Name name, Object obj, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected void c_rebind_nns(Name name, Object obj, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected void c_unbind_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected Context c_createSubcontext_nns(Name name,
        Continuation cont) throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected void c_destroySubcontext_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected void c_rename_nns(Name oldname, Name newname, Continuation cont)
        throws NamingException {
            c_processJunction_nns(oldname, cont);
        }
    protected NameParser c_getNameParser_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected void c_processJunction_nns(Name name, Continuation cont)
            throws NamingException
    {
        if (name.isEmpty()) {
            RefAddr addr = new RefAddr("nns") {
                public Object getContent() {
                    return ComponentContext.this;
                }
                private static final long serialVersionUID =
                    -1389472957988053402L;
            };
            Reference ref = new Reference("java.lang.Object", addr);
            cont.setContinue(ref, _NNS_NAME, this);
            return;
        }
        try {
            Object target = c_lookup(name, cont);
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
    protected static final byte USE_CONTINUATION = 1;
    protected static final byte TERMINAL_COMPONENT = 2;
    protected static final byte TERMINAL_NNS_COMPONENT = 3;
    protected HeadTail p_resolveIntermediate(Name name, Continuation cont)
        throws NamingException {
        int ret = USE_CONTINUATION;
        cont.setSuccess();      
        HeadTail p = p_parseComponent(name, cont);
        Name tail = p.getTail();
        Name head = p.getHead();
        if (tail == null || tail.isEmpty()) {
            ret = TERMINAL_COMPONENT;
        } else if (!tail.get(0).equals("")) {
                try {
                    Object obj = c_resolveIntermediate_nns(head, cont);
                    if (obj != null)
                        cont.setContinue(obj, head, this, tail);
                    else if (cont.isContinue()) {
                        checkAndAdjustRemainingName(cont.getRemainingName());
                        cont.appendRemainingName(tail);
                    }
                } catch (NamingException e) {
                    checkAndAdjustRemainingName(e.getRemainingName());
                    e.appendRemainingName(tail);
                    throw e;
                }
        } else {
            if (tail.size() == 1) {
                ret = TERMINAL_NNS_COMPONENT;
            } else if (head.isEmpty() || isAllEmpty(tail)) {
                Name newTail = tail.getSuffix(1);
                try {
                    Object obj = c_lookup_nns(head, cont);
                    if (obj != null)
                        cont.setContinue(obj, head, this, newTail);
                    else if (cont.isContinue()) {
                        cont.appendRemainingName(newTail);
                    }
                } catch (NamingException e) {
                    e.appendRemainingName(newTail);
                    throw e;
                }
            } else {
                try {
                    Object obj = c_resolveIntermediate_nns(head, cont);
                    if (obj != null)
                        cont.setContinue(obj, head, this, tail);
                    else if (cont.isContinue()) {
                        checkAndAdjustRemainingName(cont.getRemainingName());
                        cont.appendRemainingName(tail);
                    }
                } catch (NamingException e) {
                    checkAndAdjustRemainingName(e.getRemainingName());
                    e.appendRemainingName(tail);
                    throw e;
                }
            }
        }
        p.setStatus(ret);
        return p;
    }
    void checkAndAdjustRemainingName(Name rname) throws InvalidNameException {
        int count;
        if (rname != null && (count=rname.size()) > 1 &&
            rname.get(count-1).equals("")) {
            rname.remove(count-1);
        }
    }
    protected boolean isAllEmpty(Name n) {
        int count = n.size();
        for (int i =0; i < count; i++ ) {
            if (!n.get(i).equals("")) {
                return false;
            }
        }
        return true;
    }
    protected ResolveResult p_resolveToClass(Name name,
                                             Class contextType,
                                             Continuation cont)
            throws NamingException {
        if (contextType.isInstance(this)) {
            cont.setSuccess();
            return (new ResolveResult(this, name));
        }
        ResolveResult ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
        case TERMINAL_NNS_COMPONENT:
            Object obj = p_lookup(name, cont);
            if (!cont.isContinue() && contextType.isInstance(obj)) {
                ret = new ResolveResult(obj, _EMPTY_NAME);
            }
            break;
        case TERMINAL_COMPONENT:
            cont.setSuccess();  
            break;
        default:
            break;
        }
        return ret;
    }
    protected Object p_lookup(Name name, Continuation cont) throws NamingException {
        Object ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                ret = c_lookup_nns(res.getHead(), cont);
                if (ret instanceof LinkRef) {
                    cont.setContinue(ret, res.getHead(), this);
                    ret = null;
                }
                break;
            case TERMINAL_COMPONENT:
                ret = c_lookup(res.getHead(), cont);
                if (ret instanceof LinkRef) {
                    cont.setContinue(ret, res.getHead(), this);
                    ret = null;
                }
                break;
            default:
                break;
        }
        return ret;
    }
    protected NamingEnumeration p_list(Name name, Continuation cont)
        throws NamingException {
        NamingEnumeration ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                if (debug > 0)
                    System.out.println("c_list_nns(" + res.getHead() + ")");
                ret = c_list_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                if (debug > 0)
                    System.out.println("c_list(" + res.getHead() + ")");
                ret = c_list(res.getHead(), cont);
                break;
            default:
                break;
        }
        return ret;
    }
    protected NamingEnumeration p_listBindings(Name name, Continuation cont) throws
        NamingException {
        NamingEnumeration ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                ret = c_listBindings_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                ret = c_listBindings(res.getHead(), cont);
                break;
            default:
                break;
        }
        return ret;
    }
    protected void p_bind(Name name, Object obj, Continuation cont) throws
        NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_bind_nns(res.getHead(), obj, cont);
                break;
            case TERMINAL_COMPONENT:
                c_bind(res.getHead(), obj, cont);
                break;
            default:
                break;
        }
    }
    protected void p_rebind(Name name, Object obj, Continuation cont) throws
        NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_rebind_nns(res.getHead(), obj, cont);
                break;
            case TERMINAL_COMPONENT:
                c_rebind(res.getHead(), obj, cont);
                break;
            default:
                break;
        }
    }
    protected void p_unbind(Name name, Continuation cont) throws
        NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_unbind_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                c_unbind(res.getHead(), cont);
                break;
            default:
                break;
        }
    }
    protected void p_destroySubcontext(Name name, Continuation cont) throws
        NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_destroySubcontext_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                c_destroySubcontext(res.getHead(), cont);
                break;
            default:
                break;
        }
    }
    protected Context p_createSubcontext(Name name, Continuation cont) throws
        NamingException {
            Context ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                ret = c_createSubcontext_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                ret = c_createSubcontext(res.getHead(), cont);
                break;
            default:
                break;
        }
        return ret;
    }
    protected void p_rename(Name oldName, Name newName, Continuation cont) throws
        NamingException {
        HeadTail res = p_resolveIntermediate(oldName, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_rename_nns(res.getHead(), newName, cont);
                break;
            case TERMINAL_COMPONENT:
                c_rename(res.getHead(), newName, cont);
                break;
            default:
                break;
        }
    }
    protected NameParser p_getNameParser(Name name, Continuation cont) throws
        NamingException {
        NameParser ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                ret = c_getNameParser_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                ret = c_getNameParser(res.getHead(), cont);
                break;
            default:
                break;
        }
        return ret;
    }
    protected Object p_lookupLink(Name name, Continuation cont)
        throws NamingException {
        Object ret = null;
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                ret = c_lookupLink_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                ret = c_lookupLink(res.getHead(), cont);
                break;
            default:
                break;
        }
        return ret;
    }
}
