public abstract class ComponentDirContext extends PartialCompositeDirContext {
    protected ComponentDirContext () {
        _contextType = _COMPONENT;
    }
    protected abstract Attributes c_getAttributes(Name name,
                                                 String[] attrIds,
                                                 Continuation cont)
        throws NamingException;
    protected abstract void c_modifyAttributes(Name name, int mod_op,
                                            Attributes attrs,
                                            Continuation cont)
            throws NamingException;
    protected abstract void c_modifyAttributes(Name name,
                                            ModificationItem[] mods,
                                            Continuation cont)
        throws NamingException;
    protected abstract void c_bind(Name name, Object obj,
                                   Attributes attrs,
                                   Continuation cont)
        throws NamingException;
    protected abstract void c_rebind(Name name, Object obj,
                                     Attributes attrs,
                                     Continuation cont)
        throws NamingException;
    protected abstract DirContext c_createSubcontext(Name name,
                                                    Attributes attrs,
                                                    Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration c_search(Name name,
                                               Attributes matchingAttributes,
                                               String[] attributesToReturn,
                                               Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration c_search(Name name,
                                               String filter,
                                               SearchControls cons,
                                               Continuation cont)
        throws NamingException;
    protected abstract NamingEnumeration c_search(Name name,
                                                  String filterExpr,
                                                  Object[] filterArgs,
                                                  SearchControls cons,
                                                  Continuation cont)
        throws NamingException;
    protected abstract DirContext c_getSchema(Name name, Continuation cont)
        throws NamingException;
    protected abstract DirContext c_getSchemaClassDefinition(Name name,
                                                            Continuation cont)
        throws NamingException;
    protected Attributes c_getAttributes_nns(Name name,
                                            String[] attrIds,
                                            Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected void c_modifyAttributes_nns(Name name,
                                       int mod_op,
                                       Attributes attrs,
                                       Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected void c_modifyAttributes_nns(Name name,
                                       ModificationItem[] mods,
                                       Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
        }
    protected void c_bind_nns(Name name,
                              Object obj,
                              Attributes attrs,
                              Continuation cont)
        throws NamingException  {
            c_processJunction_nns(name, cont);
        }
    protected void c_rebind_nns(Name name,
                                Object obj,
                                Attributes attrs,
                                Continuation cont)
        throws NamingException  {
            c_processJunction_nns(name, cont);
        }
    protected DirContext c_createSubcontext_nns(Name name,
                                               Attributes attrs,
                                               Continuation cont)
        throws NamingException  {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                          Attributes matchingAttributes,
                                          String[] attributesToReturn,
                                          Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                          String filter,
                                          SearchControls cons,
                                          Continuation cont)
        throws NamingException  {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected NamingEnumeration c_search_nns(Name name,
                                             String filterExpr,
                                             Object[] filterArgs,
                                             SearchControls cons,
                                             Continuation cont)
        throws NamingException  {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected DirContext c_getSchema_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected DirContext c_getSchemaClassDefinition_nns(Name name, Continuation cont)
        throws NamingException {
            c_processJunction_nns(name, cont);
            return null;
        }
    protected Attributes p_getAttributes(Name name,
                                        String[] attrIds,
                                        Continuation cont)
        throws NamingException  {
        HeadTail res = p_resolveIntermediate(name, cont);
        Attributes answer = null;
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_getAttributes_nns(res.getHead(), attrIds, cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_getAttributes(res.getHead(), attrIds, cont);
                break;
            default:
                break;
            }
        return answer;
    }
    protected void p_modifyAttributes(Name name, int mod_op,
                                   Attributes attrs,
                                   Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_modifyAttributes_nns(res.getHead(), mod_op, attrs, cont);
                break;
            case TERMINAL_COMPONENT:
                c_modifyAttributes(res.getHead(), mod_op, attrs, cont);
                break;
            default:
                break;
            }
    }
    protected void p_modifyAttributes(Name name,
                                   ModificationItem[] mods,
                                   Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_modifyAttributes_nns(res.getHead(), mods, cont);
                break;
            case TERMINAL_COMPONENT:
                c_modifyAttributes(res.getHead(), mods, cont);
                break;
            default:
                break;
            }
    }
    protected void p_bind(Name name,
                          Object obj,
                          Attributes attrs,
                          Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_bind_nns(res.getHead(), obj, attrs, cont);
                break;
            case TERMINAL_COMPONENT:
                c_bind(res.getHead(), obj, attrs, cont);
                break;
            default:
                break;
            }
    }
    protected void p_rebind(Name name, Object obj,
                            Attributes attrs, Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                c_rebind_nns(res.getHead(), obj, attrs, cont);
                break;
            case TERMINAL_COMPONENT:
                c_rebind(res.getHead(), obj, attrs, cont);
                break;
            default:
                break;
            }
    }
    protected DirContext p_createSubcontext(Name name,
                                           Attributes attrs,
                                           Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        DirContext answer = null;
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_createSubcontext_nns(res.getHead(), attrs, cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_createSubcontext(res.getHead(), attrs, cont);
                break;
            default:
                break;
            }
        return answer;
    }
    protected NamingEnumeration p_search(Name name,
                                      Attributes matchingAttributes,
                                      String[] attributesToReturn,
                                      Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        NamingEnumeration answer = null;
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_search_nns(res.getHead(), matchingAttributes,
                                      attributesToReturn, cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_search(res.getHead(), matchingAttributes,
                                  attributesToReturn, cont);
                break;
            default:
                break;
            }
        return answer;
    }
    protected NamingEnumeration p_search(Name name,
                                      String filter,
                                      SearchControls cons, Continuation cont)
        throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        NamingEnumeration answer = null;
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_search_nns(res.getHead(), filter, cons, cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_search(res.getHead(), filter, cons, cont);
                break;
            default:
                break;
            }
        return answer;
    }
    protected NamingEnumeration p_search(Name name,
                                         String filterExpr,
                                         Object[] filterArgs,
                                         SearchControls cons,
                                         Continuation cont)
            throws NamingException {
        HeadTail res = p_resolveIntermediate(name, cont);
        NamingEnumeration answer = null;
        switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_search_nns(res.getHead(),
                                      filterExpr, filterArgs, cons, cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_search(res.getHead(), filterExpr, filterArgs, cons, cont);
                break;
            default:
                break;
            }
        return answer;
    }
    protected DirContext p_getSchema(Name name, Continuation cont)
        throws NamingException  {
            DirContext answer = null;
            HeadTail res = p_resolveIntermediate(name, cont);
            switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_getSchema_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_getSchema(res.getHead(), cont);
                break;
            default:
                break;
            }
            return answer;
        }
    protected DirContext p_getSchemaClassDefinition(Name name, Continuation cont)
        throws NamingException  {
            DirContext answer = null;
            HeadTail res = p_resolveIntermediate(name, cont);
            switch (res.getStatus()) {
            case TERMINAL_NNS_COMPONENT:
                answer = c_getSchemaClassDefinition_nns(res.getHead(), cont);
                break;
            case TERMINAL_COMPONENT:
                answer = c_getSchemaClassDefinition(res.getHead(), cont);
                break;
            default:
                break;
            }
            return answer;
        }
}
