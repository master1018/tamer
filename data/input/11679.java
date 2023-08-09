abstract public class GenericURLContext implements Context {
    protected Hashtable myEnv = null;
    public GenericURLContext(Hashtable env) {
        myEnv = env;  
    }
    public void close() throws NamingException {
        myEnv = null;
    }
    public String getNameInNamespace() throws NamingException {
        return ""; 
    }
    abstract protected ResolveResult getRootURLContext(String url,
        Hashtable env) throws NamingException;
    protected Name getURLSuffix(String prefix, String url) throws NamingException {
        String suffix = url.substring(prefix.length());
        if (suffix.length() == 0) {
            return new CompositeName();
        }
        if (suffix.charAt(0) == '/') {
            suffix = suffix.substring(1); 
        }
        try {
            return new CompositeName().add(UrlUtil.decode(suffix));
        } catch (MalformedURLException e) {
            throw new InvalidNameException(e.getMessage());
        }
    }
    protected String getURLPrefix(String url) throws NamingException {
        int start = url.indexOf(":");
        if (start < 0) {
            throw new OperationNotSupportedException("Invalid URL: " + url);
        }
        ++start; 
        if (url.startsWith("
            start += 2;  
            int posn = url.indexOf("/", start);
            if (posn >= 0) {
                start = posn;
            } else {
                start = url.length();  
            }
        }
        return url.substring(0, start);
    }
    protected boolean urlEquals(String url1, String url2) {
        return url1.equals(url2);
    }
    protected Context getContinuationContext(Name n) throws NamingException {
        Object obj = lookup(n.get(0));
        CannotProceedException cpe = new CannotProceedException();
        cpe.setResolvedObj(obj);
        cpe.setEnvironment(myEnv);
        return NamingManager.getContinuationContext(cpe);
    }
    public Object lookup(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.lookup(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public Object lookup(Name name) throws NamingException {
        if (name.size() == 1) {
            return lookup(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.lookup(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public void bind(String name, Object obj) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            ctx.bind(res.getRemainingName(), obj);
        } finally {
            ctx.close();
        }
    }
    public void bind(Name name, Object obj) throws NamingException {
        if (name.size() == 1) {
            bind(name.get(0), obj);
        } else {
            Context ctx = getContinuationContext(name);
            try {
                ctx.bind(name.getSuffix(1), obj);
            } finally {
                ctx.close();
            }
        }
    }
    public void rebind(String name, Object obj) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            ctx.rebind(res.getRemainingName(), obj);
        } finally {
            ctx.close();
        }
    }
    public void rebind(Name name, Object obj) throws NamingException {
        if (name.size() == 1) {
            rebind(name.get(0), obj);
        } else {
            Context ctx = getContinuationContext(name);
            try {
                ctx.rebind(name.getSuffix(1), obj);
            } finally {
                ctx.close();
            }
        }
    }
    public void unbind(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            ctx.unbind(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public void unbind(Name name) throws NamingException {
        if (name.size() == 1) {
            unbind(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                ctx.unbind(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public void rename(String oldName, String newName) throws NamingException {
        String oldPrefix = getURLPrefix(oldName);
        String newPrefix = getURLPrefix(newName);
        if (!urlEquals(oldPrefix, newPrefix)) {
            throw new OperationNotSupportedException(
                "Renaming using different URL prefixes not supported : " +
                oldName + " " + newName);
        }
        ResolveResult res = getRootURLContext(oldName, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            ctx.rename(res.getRemainingName(), getURLSuffix(newPrefix, newName));
        } finally {
            ctx.close();
        }
    }
    public void rename(Name name, Name newName) throws NamingException {
        if (name.size() == 1) {
            if (newName.size() != 1) {
                throw new OperationNotSupportedException(
            "Renaming to a Name with more components not supported: " + newName);
            }
            rename(name.get(0), newName.get(0));
        } else {
            if (!urlEquals(name.get(0), newName.get(0))) {
                throw new OperationNotSupportedException(
                    "Renaming using different URLs as first components not supported: " +
                    name + " " + newName);
            }
            Context ctx = getContinuationContext(name);
            try {
                ctx.rename(name.getSuffix(1), newName.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public NamingEnumeration<NameClassPair> list(String name)   throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.list(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        if (name.size() == 1) {
            return list(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.list(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public NamingEnumeration<Binding> listBindings(String name)
        throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.listBindings(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        if (name.size() == 1) {
            return listBindings(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.listBindings(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public void destroySubcontext(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            ctx.destroySubcontext(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public void destroySubcontext(Name name) throws NamingException {
        if (name.size() == 1) {
            destroySubcontext(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                ctx.destroySubcontext(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public Context createSubcontext(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.createSubcontext(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public Context createSubcontext(Name name) throws NamingException {
        if (name.size() == 1) {
            return createSubcontext(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.createSubcontext(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public Object lookupLink(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.lookupLink(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public Object lookupLink(Name name) throws NamingException {
        if (name.size() == 1) {
            return lookupLink(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.lookupLink(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public NameParser getNameParser(String name) throws NamingException {
        ResolveResult res = getRootURLContext(name, myEnv);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.getNameParser(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    public NameParser getNameParser(Name name) throws NamingException {
        if (name.size() == 1) {
            return getNameParser(name.get(0));
        } else {
            Context ctx = getContinuationContext(name);
            try {
                return ctx.getNameParser(name.getSuffix(1));
            } finally {
                ctx.close();
            }
        }
    }
    public String composeName(String name, String prefix)
        throws NamingException {
            if (prefix.equals("")) {
                return name;
            } else if (name.equals("")) {
                return prefix;
            } else {
                return (prefix + "/" + name);
            }
    }
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name)prefix.clone();
        result.addAll(name);
        return result;
    }
    public Object removeFromEnvironment(String propName)
        throws NamingException {
            if (myEnv == null) {
                return null;
            }
            myEnv = (Hashtable)myEnv.clone();
            return myEnv.remove(propName);
    }
    public Object addToEnvironment(String propName, Object propVal)
        throws NamingException {
            myEnv = (myEnv == null) ?
                new Hashtable(11, 0.75f) : (Hashtable)myEnv.clone();
            return myEnv.put(propName, propVal);
    }
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            return new Hashtable(5, 0.75f);
        } else {
            return (Hashtable)myEnv.clone();
        }
    }
}
