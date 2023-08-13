public final class Subject implements Serializable {
    private static final long serialVersionUID = -8308522755600156056L;
    private static final AuthPermission _AS = new AuthPermission("doAs"); 
    private static final AuthPermission _AS_PRIVILEGED = new AuthPermission(
            "doAsPrivileged"); 
    private static final AuthPermission _SUBJECT = new AuthPermission(
            "getSubject"); 
    private static final AuthPermission _PRINCIPALS = new AuthPermission(
            "modifyPrincipals"); 
    private static final AuthPermission _PRIVATE_CREDENTIALS = new AuthPermission(
            "modifyPrivateCredentials"); 
    private static final AuthPermission _PUBLIC_CREDENTIALS = new AuthPermission(
            "modifyPublicCredentials"); 
    private static final AuthPermission _READ_ONLY = new AuthPermission(
            "setReadOnly"); 
    private final Set<Principal> principals;
    private boolean readOnly;
    private transient SecureSet<Object> privateCredentials;
    private transient SecureSet<Object> publicCredentials;
    public Subject() {
        super();
        principals = new SecureSet<Principal>(_PRINCIPALS);
        publicCredentials = new SecureSet<Object>(_PUBLIC_CREDENTIALS);
        privateCredentials = new SecureSet<Object>(_PRIVATE_CREDENTIALS);
        readOnly = false;
    }
    public Subject(boolean readOnly, Set<? extends Principal> subjPrincipals,
            Set<?> pubCredentials, Set<?> privCredentials) {
        if (subjPrincipals == null || pubCredentials == null || privCredentials == null) {
            throw new NullPointerException();
        }
        principals = new SecureSet<Principal>(_PRINCIPALS, subjPrincipals);
        publicCredentials = new SecureSet<Object>(_PUBLIC_CREDENTIALS, pubCredentials);
        privateCredentials = new SecureSet<Object>(_PRIVATE_CREDENTIALS, privCredentials);
        this.readOnly = readOnly;
    }
    @SuppressWarnings("unchecked")
    public static Object doAs(Subject subject, PrivilegedAction action) {
        checkPermission(_AS);
        return doAs_PrivilegedAction(subject, action, AccessController.getContext());
    }
    @SuppressWarnings("unchecked")
    public static Object doAsPrivileged(Subject subject, PrivilegedAction action,
            AccessControlContext context) {
        checkPermission(_AS_PRIVILEGED);
        if (context == null) {
            return doAs_PrivilegedAction(subject, action, new AccessControlContext(
                    new ProtectionDomain[0]));
        }
        return doAs_PrivilegedAction(subject, action, context);
    }
    @SuppressWarnings("unchecked")
    private static Object doAs_PrivilegedAction(Subject subject, PrivilegedAction action,
            final AccessControlContext context) {
        AccessControlContext newContext;
        final SubjectDomainCombiner combiner;
        if (subject == null) {
            combiner = null;
        } else {
            combiner = new SubjectDomainCombiner(subject);
        }
        PrivilegedAction dccAction = new PrivilegedAction() {
            public Object run() {
                return new AccessControlContext(context, combiner);
            }
        };
        newContext = (AccessControlContext) AccessController.doPrivileged(dccAction);
        return AccessController.doPrivileged(action, newContext);
    }
    @SuppressWarnings("unchecked")
    public static Object doAs(Subject subject, PrivilegedExceptionAction action)
            throws PrivilegedActionException {
        checkPermission(_AS);
        return doAs_PrivilegedExceptionAction(subject, action, AccessController.getContext());
    }
    @SuppressWarnings("unchecked")
    public static Object doAsPrivileged(Subject subject,
            PrivilegedExceptionAction action, AccessControlContext context)
            throws PrivilegedActionException {
        checkPermission(_AS_PRIVILEGED);
        if (context == null) {
            return doAs_PrivilegedExceptionAction(subject, action,
                    new AccessControlContext(new ProtectionDomain[0]));
        }
        return doAs_PrivilegedExceptionAction(subject, action, context);
    }
    @SuppressWarnings("unchecked")
    private static Object doAs_PrivilegedExceptionAction(Subject subject,
            PrivilegedExceptionAction action, final AccessControlContext context)
            throws PrivilegedActionException {
        AccessControlContext newContext;
        final SubjectDomainCombiner combiner;
        if (subject == null) {
            combiner = null;
        } else {
            combiner = new SubjectDomainCombiner(subject);
        }
        PrivilegedAction<AccessControlContext> dccAction = new PrivilegedAction<AccessControlContext>() {
            public AccessControlContext run() {
                return new AccessControlContext(context, combiner);
            }
        };
        newContext = AccessController.doPrivileged(dccAction);
        return AccessController.doPrivileged(action, newContext);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Subject that = (Subject) obj;
        if (principals.equals(that.principals)
                && publicCredentials.equals(that.publicCredentials)
                && privateCredentials.equals(that.privateCredentials)) {
            return true;
        }
        return false;
    }
    public Set<Principal> getPrincipals() {
        return principals;
    }
    public <T extends Principal> Set<T> getPrincipals(Class<T> c) {
        return ((SecureSet<Principal>) principals).get(c);
    }
    public Set<Object> getPrivateCredentials() {
        return privateCredentials;
    }
    public <T> Set<T> getPrivateCredentials(Class<T> c) {
        return privateCredentials.get(c);
    }
    public Set<Object> getPublicCredentials() {
        return publicCredentials;
    }
    public <T> Set<T> getPublicCredentials(Class<T> c) {
        return publicCredentials.get(c);
    }
    @Override
    public int hashCode() {
        return principals.hashCode() + privateCredentials.hashCode()
                + publicCredentials.hashCode();
    }
    public void setReadOnly() {
        checkPermission(_READ_ONLY);
        readOnly = true;
    }
    public boolean isReadOnly() {
        return readOnly;
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("Subject:\n"); 
        Iterator<?> it = principals.iterator();
        while (it.hasNext()) {
            buf.append("\tPrincipal: "); 
            buf.append(it.next());
            buf.append('\n');
        }
        it = publicCredentials.iterator();
        while (it.hasNext()) {
            buf.append("\tPublic Credential: "); 
            buf.append(it.next());
            buf.append('\n');
        }
        int offset = buf.length() - 1;
        it = privateCredentials.iterator();
        try {
            while (it.hasNext()) {
                buf.append("\tPrivate Credential: "); 
                buf.append(it.next());
                buf.append('\n');
            }
        } catch (SecurityException e) {
            buf.delete(offset, buf.length());
            buf.append("\tPrivate Credentials: no accessible information\n"); 
        }
        return buf.toString();
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        publicCredentials = new SecureSet<Object>(_PUBLIC_CREDENTIALS);
        privateCredentials = new SecureSet<Object>(_PRIVATE_CREDENTIALS);
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    public static Subject getSubject(final AccessControlContext context) {
        checkPermission(_SUBJECT);
        if (context == null) {
            throw new NullPointerException(Messages.getString("auth.09")); 
        }
        PrivilegedAction<DomainCombiner> action = new PrivilegedAction<DomainCombiner>() {
            public DomainCombiner run() {
                return context.getDomainCombiner();
            }
        };
        DomainCombiner combiner = AccessController.doPrivileged(action);
        if ((combiner == null) || !(combiner instanceof SubjectDomainCombiner)) {
            return null;
        }
        return ((SubjectDomainCombiner) combiner).getSubject();
    }
    private static void checkPermission(Permission p) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(p);
        }
    }
    private void checkState() {
        if (readOnly) {
            throw new IllegalStateException(Messages.getString("auth.0A")); 
        }
    }
    private final class SecureSet<SST> extends AbstractSet<SST> implements Serializable {
        private static final long serialVersionUID = 7911754171111800359L;
        private LinkedList<SST> elements;
        private int setType;
        private static final int SET_Principal = 0;
        private static final int SET_PrivCred = 1;
        private static final int SET_PubCred = 2;
        private transient AuthPermission permission;
        protected SecureSet(AuthPermission perm) {
            permission = perm;
            elements = new LinkedList<SST>();
        }
        protected SecureSet(AuthPermission perm, Collection<? extends SST> s) {
            this(perm);
            boolean trust = s.getClass().getClassLoader() == null; 
            Iterator<? extends SST> it = s.iterator();
            while (it.hasNext()) {
                SST o = it.next();
                verifyElement(o);
                if (trust || !elements.contains(o)) {
                    elements.add(o);
                }
            }
        }
        private void verifyElement(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (permission == _PRINCIPALS && !(Principal.class.isAssignableFrom(o.getClass()))) {
                throw new IllegalArgumentException(Messages.getString("auth.0B")); 
            }
        }
        @Override
        public boolean add(SST o) {
            verifyElement(o);
            checkState();
            checkPermission(permission);
            if (!elements.contains(o)) {
                elements.add(o);
                return true;
            }
            return false;
        }
        @Override
        public Iterator<SST> iterator() {
            if (permission == _PRIVATE_CREDENTIALS) {
                return new SecureIterator(elements.iterator()) {
                    @Override
                    public SST next() {
                        SST obj = iterator.next();
                        checkPermission(new PrivateCredentialPermission(obj
                                .getClass().getName(), principals));
                        return obj;
                    }
                };
            }
            return new SecureIterator(elements.iterator());
        }
        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            return super.retainAll(c);
        }
        @Override
        public int size() {
            return elements.size();
        }
        protected final <E> Set<E> get(final Class<E> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            AbstractSet<E> s = new AbstractSet<E>() {
                private LinkedList<E> elements = new LinkedList<E>();
                @Override
                public boolean add(E o) {
                    if (!c.isAssignableFrom(o.getClass())) {
                        throw new IllegalArgumentException(
                                Messages.getString("auth.0C", c.getName())); 
                    }
                    if (elements.contains(o)) {
                        return false;
                    }
                    elements.add(o);
                    return true;
                }
                @Override
                public Iterator<E> iterator() {
                    return elements.iterator();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    if (c == null) {
                        throw new NullPointerException();
                    }
                    return super.retainAll(c);
                }
                @Override
                public int size() {
                    return elements.size();
                }
            };
            for (Iterator<SST> it = iterator(); it.hasNext();) {
                SST o = it.next();
                if (c.isAssignableFrom(o.getClass())) {
                    s.add(c.cast(o));
                }
            }
            return s;
        }
        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            in.defaultReadObject();
            switch (setType) {
            case SET_Principal:
                permission = _PRINCIPALS;
                break;
            case SET_PrivCred:
                permission = _PRIVATE_CREDENTIALS;
                break;
            case SET_PubCred:
                permission = _PUBLIC_CREDENTIALS;
                break;
            default:
                throw new IllegalArgumentException();
            }
            Iterator<SST> it = elements.iterator();
            while (it.hasNext()) {
                verifyElement(it.next());
            }
        }
        private void writeObject(ObjectOutputStream out) throws IOException {
            if (permission == _PRIVATE_CREDENTIALS) {
                for (Iterator<SST> it = iterator(); it.hasNext();) {
                    it.next();
                }
                setType = SET_PrivCred;
            } else if (permission == _PRINCIPALS) {
                setType = SET_Principal;
            } else {
                setType = SET_PubCred;
            }
            out.defaultWriteObject();
        }
        private class SecureIterator implements Iterator<SST> {
            protected Iterator<SST> iterator;
            protected SecureIterator(Iterator<SST> iterator) {
                this.iterator = iterator;
            }
            public boolean hasNext() {
                return iterator.hasNext();
            }
            public SST next() {
                return iterator.next();
            }
            public void remove() {
                checkState();
                checkPermission(permission);
                iterator.remove();
            }
        }
    }
}