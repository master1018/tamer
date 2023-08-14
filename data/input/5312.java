abstract public class GenericURLDirContext extends GenericURLContext
implements DirContext {
    protected GenericURLDirContext(Hashtable env) {
        super(env);
    }
    protected DirContext getContinuationDirContext(Name n) throws NamingException {
        Object obj = lookup(n.get(0));
        CannotProceedException cpe = new CannotProceedException();
        cpe.setResolvedObj(obj);
        cpe.setEnvironment(myEnv);
        return DirectoryManager.getContinuationDirContext(cpe);
    }
    public Attributes getAttributes(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        DirContext ctx = (DirContext)res.getResolvedObj();
        try {
            return ctx.getAttributes(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public Attributes getAttributes(Name name) throws NamingException  {
        if (name.size() == 1) {
            return getAttributes(name.get(0));
        } else {
            DirContext ctx = getContinuationDirContext(name);
            try {
                return ctx.getAttributes(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public Attributes getAttributes(String name, String[] attrIds)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.getAttributes(res.getRemainingName(), attrIds);
            } finally {
                ctx.close();
            }
    }
    public Attributes getAttributes(Name name, String[] attrIds)
        throws NamingException {
            if (name.size() == 1) {
                return getAttributes(name.get(0), attrIds);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.getAttributes(name.getSuffix(1), attrIds);
                } finally {
                    ctx.close();
                }
            }
    }
    public void modifyAttributes(String name, int mod_op, Attributes attrs)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                ctx.modifyAttributes(res.getRemainingName(), mod_op, attrs);
            } finally {
                ctx.close();
            }
    }
    public void modifyAttributes(Name name, int mod_op, Attributes attrs)
        throws NamingException {
            if (name.size() == 1) {
                modifyAttributes(name.get(0), mod_op, attrs);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    ctx.modifyAttributes(name.getSuffix(1), mod_op, attrs);
                } finally {
                    ctx.close();
                }
            }
    }
    public void modifyAttributes(String name, ModificationItem[] mods)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                ctx.modifyAttributes(res.getRemainingName(), mods);
            } finally {
                ctx.close();
            }
    }
    public void modifyAttributes(Name name, ModificationItem[] mods)
        throws NamingException  {
            if (name.size() == 1) {
                modifyAttributes(name.get(0), mods);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    ctx.modifyAttributes(name.getSuffix(1), mods);
                } finally {
                    ctx.close();
                }
            }
    }
    public void bind(String name, Object obj, Attributes attrs)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                ctx.bind(res.getRemainingName(), obj, attrs);
            } finally {
                ctx.close();
            }
    }
    public void bind(Name name, Object obj, Attributes attrs)
        throws NamingException {
            if (name.size() == 1) {
                bind(name.get(0), obj, attrs);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    ctx.bind(name.getSuffix(1), obj, attrs);
                } finally {
                    ctx.close();
                }
            }
    }
    public void rebind(String name, Object obj, Attributes attrs)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                ctx.rebind(res.getRemainingName(), obj, attrs);
            } finally {
                ctx.close();
            }
    }
    public void rebind(Name name, Object obj, Attributes attrs)
        throws NamingException {
            if (name.size() == 1) {
                rebind(name.get(0), obj, attrs);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    ctx.rebind(name.getSuffix(1), obj, attrs);
                } finally {
                    ctx.close();
                }
            }
    }
    public DirContext createSubcontext(String name, Attributes attrs)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.createSubcontext(res.getRemainingName(), attrs);
            } finally {
                ctx.close();
            }
    }
    public DirContext createSubcontext(Name name, Attributes attrs)
        throws NamingException {
            if (name.size() == 1) {
                return createSubcontext(name.get(0), attrs);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.createSubcontext(name.getSuffix(1), attrs);
                } finally {
                    ctx.close();
                }
            }
    }
    public DirContext getSchema(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        DirContext ctx = (DirContext)res.getResolvedObj();
        return ctx.getSchema(res.getRemainingName());
    }
    public DirContext getSchema(Name name) throws NamingException {
        if (name.size() == 1) {
            return getSchema(name.get(0));
        } else {
            DirContext ctx = getContinuationDirContext(name);
            try {
                return ctx.getSchema(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public DirContext getSchemaClassDefinition(String name)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.getSchemaClassDefinition(res.getRemainingName());
            } finally {
                ctx.close();
            }
    }
    public DirContext getSchemaClassDefinition(Name name)
        throws NamingException {
            if (name.size() == 1) {
                return getSchemaClassDefinition(name.get(0));
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.getSchemaClassDefinition(name.getSuffix(1));
                } finally {
                    ctx.close();
                }
            }
    }
    public NamingEnumeration<SearchResult> search(String name,
        Attributes matchingAttributes)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.search(res.getRemainingName(), matchingAttributes);
            } finally {
                ctx.close();
            }
    }
    public NamingEnumeration<SearchResult> search(Name name,
        Attributes matchingAttributes)
        throws NamingException {
            if (name.size() == 1) {
                return search(name.get(0), matchingAttributes);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.search(name.getSuffix(1), matchingAttributes);
                } finally {
                    ctx.close();
                }
            }
    }
    public NamingEnumeration<SearchResult> search(String name,
        Attributes matchingAttributes,
        String[] attributesToReturn)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.search(res.getRemainingName(),
                    matchingAttributes, attributesToReturn);
            } finally {
                ctx.close();
            }
    }
    public NamingEnumeration<SearchResult> search(Name name,
        Attributes matchingAttributes,
        String[] attributesToReturn)
        throws NamingException {
            if (name.size() == 1) {
                return search(name.get(0), matchingAttributes,
                    attributesToReturn);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.search(name.getSuffix(1),
                        matchingAttributes, attributesToReturn);
                } finally {
                    ctx.close();
                }
            }
    }
    public NamingEnumeration<SearchResult> search(String name,
        String filter,
        SearchControls cons)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return ctx.search(res.getRemainingName(), filter, cons);
            } finally {
                ctx.close();
            }
    }
    public NamingEnumeration<SearchResult> search(Name name,
        String filter,
        SearchControls cons)
        throws NamingException {
            if (name.size() == 1) {
                return search(name.get(0), filter, cons);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                    return ctx.search(name.getSuffix(1), filter, cons);
                } finally {
                    ctx.close();
                }
            }
    }
    public NamingEnumeration<SearchResult> search(String name,
        String filterExpr,
        Object[] filterArgs,
        SearchControls cons)
        throws NamingException {
            ResolveResult res = getRootURLContext(name, myEnv);
            DirContext ctx = (DirContext)res.getResolvedObj();
            try {
                return
                    ctx.search(res.getRemainingName(), filterExpr, filterArgs, cons);
            } finally {
                ctx.close();
            }
    }
    public NamingEnumeration<SearchResult> search(Name name,
        String filterExpr,
        Object[] filterArgs,
        SearchControls cons)
        throws NamingException {
            if (name.size() == 1) {
                return search(name.get(0), filterExpr, filterArgs, cons);
            } else {
                DirContext ctx = getContinuationDirContext(name);
                try {
                return ctx.search(name.getSuffix(1), filterExpr, filterArgs, cons);
                } finally {
                    ctx.close();
                }
            }
    }
}
