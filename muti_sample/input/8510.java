class SubjectCodeSource extends CodeSource implements java.io.Serializable {
    private static final long serialVersionUID = 6039418085604715275L;
    private static final java.util.ResourceBundle rb =
        java.security.AccessController.doPrivileged
        (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
            public java.util.ResourceBundle run() {
                return (java.util.ResourceBundle.getBundle
                        ("sun.security.util.AuthResources"));
            }
        });
    private Subject subject;
    private LinkedList<PolicyParser.PrincipalEntry> principals;
    private static final Class[] PARAMS = { String.class };
    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("auth", "\t[Auth Access]");
    private ClassLoader sysClassLoader;
    SubjectCodeSource(Subject subject,
        LinkedList<PolicyParser.PrincipalEntry> principals,
        URL url, Certificate[] certs) {
        super(url, certs);
        this.subject = subject;
        this.principals = (principals == null ?
                new LinkedList<PolicyParser.PrincipalEntry>() :
                new LinkedList<PolicyParser.PrincipalEntry>(principals));
        sysClassLoader = java.security.AccessController.doPrivileged
        (new java.security.PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                    return ClassLoader.getSystemClassLoader();
            }
        });
    }
    LinkedList<PolicyParser.PrincipalEntry> getPrincipals() {
        return principals;
    }
    Subject getSubject() {
        return subject;
    }
    public boolean implies(CodeSource codesource) {
        LinkedList<PolicyParser.PrincipalEntry> subjectList = null;
        if (codesource == null ||
            !(codesource instanceof SubjectCodeSource) ||
            !(super.implies(codesource))) {
            if (debug != null)
                debug.println("\tSubjectCodeSource.implies: FAILURE 1");
            return false;
        }
        SubjectCodeSource that = (SubjectCodeSource)codesource;
        if (this.principals == null) {
            if (debug != null)
                debug.println("\tSubjectCodeSource.implies: PASS 1");
            return true;
        }
        if (that.getSubject() == null ||
            that.getSubject().getPrincipals().size() == 0) {
            if (debug != null)
                debug.println("\tSubjectCodeSource.implies: FAILURE 2");
            return false;
        }
        ListIterator<PolicyParser.PrincipalEntry> li =
                this.principals.listIterator(0);
        while (li.hasNext()) {
            PolicyParser.PrincipalEntry pppe = li.next();
            try {
                Class principalComparator = Class.forName(pppe.principalClass,
                                                        true,
                                                        sysClassLoader);
                Constructor c = principalComparator.getConstructor(PARAMS);
                PrincipalComparator pc =
                        (PrincipalComparator)c.newInstance
                        (new Object[] { pppe.principalName });
                if (!pc.implies(that.getSubject())) {
                    if (debug != null)
                        debug.println("\tSubjectCodeSource.implies: FAILURE 3");
                    return false;
                } else {
                    if (debug != null)
                        debug.println("\tSubjectCodeSource.implies: PASS 2");
                    return true;
                }
            } catch (Exception e) {
                if (subjectList == null) {
                    if (that.getSubject() == null) {
                        if (debug != null)
                            debug.println("\tSubjectCodeSource.implies: " +
                                        "FAILURE 4");
                        return false;
                    }
                    Iterator<Principal> i =
                                that.getSubject().getPrincipals().iterator();
                    subjectList = new LinkedList<PolicyParser.PrincipalEntry>();
                    while (i.hasNext()) {
                        Principal p = i.next();
                        PolicyParser.PrincipalEntry spppe =
                                new PolicyParser.PrincipalEntry
                                (p.getClass().getName(), p.getName());
                        subjectList.add(spppe);
                    }
                }
                if (!subjectListImpliesPrincipalEntry(subjectList, pppe)) {
                    if (debug != null)
                        debug.println("\tSubjectCodeSource.implies: FAILURE 5");
                    return false;
                }
            }
        }
        if (debug != null)
            debug.println("\tSubjectCodeSource.implies: PASS 3");
        return true;
    }
    private boolean subjectListImpliesPrincipalEntry(
                LinkedList<PolicyParser.PrincipalEntry> subjectList,
                PolicyParser.PrincipalEntry pppe) {
        ListIterator<PolicyParser.PrincipalEntry> li =
                                        subjectList.listIterator(0);
        while (li.hasNext()) {
            PolicyParser.PrincipalEntry listPppe = li.next();
            if (pppe.principalClass.equals
                        (PolicyParser.PrincipalEntry.WILDCARD_CLASS) ||
                pppe.principalClass.equals
                        (listPppe.principalClass)) {
                if (pppe.principalName.equals
                        (PolicyParser.PrincipalEntry.WILDCARD_NAME) ||
                    pppe.principalName.equals
                        (listPppe.principalName))
                    return true;
            }
        }
        return false;
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (super.equals(obj) == false)
            return false;
        if (!(obj instanceof SubjectCodeSource))
            return false;
        SubjectCodeSource that = (SubjectCodeSource)obj;
        try {
            if (this.getSubject() != that.getSubject())
                return false;
        } catch (SecurityException se) {
            return false;
        }
        if ((this.principals == null && that.principals != null) ||
            (this.principals != null && that.principals == null))
            return false;
        if (this.principals != null && that.principals != null) {
            if (!this.principals.containsAll(that.principals) ||
                !that.principals.containsAll(this.principals))
                return false;
        }
        return true;
    }
    public int hashCode() {
        return super.hashCode();
    }
    public String toString() {
        String returnMe = super.toString();
        if (getSubject() != null) {
            if (debug != null) {
                final Subject finalSubject = getSubject();
                returnMe = returnMe + "\n" +
                        java.security.AccessController.doPrivileged
                                (new java.security.PrivilegedAction<String>() {
                                public String run() {
                                    return finalSubject.toString();
                                }
                        });
            } else {
                returnMe = returnMe + "\n" + getSubject().toString();
            }
        }
        if (principals != null) {
            ListIterator<PolicyParser.PrincipalEntry> li =
                                        principals.listIterator();
            while (li.hasNext()) {
                PolicyParser.PrincipalEntry pppe = li.next();
                returnMe = returnMe + rb.getString("NEWLINE") +
                        pppe.principalClass + " " +
                        pppe.principalName;
            }
        }
        return returnMe;
    }
}
