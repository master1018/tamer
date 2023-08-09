final class NotifierArgs {
    static final int ADDED_MASK = 0x1;
    static final int REMOVED_MASK = 0x2;
    static final int CHANGED_MASK = 0x4;
    static final int RENAMED_MASK = 0x8;
    String name;
    String filter;
    SearchControls controls;
    int mask;
    NotifierArgs(String name, int scope, NamingListener l) {
        this(name, "(objectclass=*)", null, l);
        if (scope != EventContext.ONELEVEL_SCOPE) {
            controls = new SearchControls();
            controls.setSearchScope(scope);
        }
    }
    NotifierArgs(String name, String filter, SearchControls ctls,
        NamingListener l) {
        this.name = name;
        this.filter = filter;
        this.controls = ctls;
        if (l instanceof NamespaceChangeListener) {
            mask |= ADDED_MASK|REMOVED_MASK|RENAMED_MASK;
        }
        if (l instanceof ObjectChangeListener) {
            mask |= CHANGED_MASK;
        }
    }
    public boolean equals(Object obj) {
        if (obj instanceof NotifierArgs) {
            NotifierArgs target = (NotifierArgs)obj;
            return mask == target.mask &&
                name.equals(target.name) && filter.equals(target.filter) &&
                checkControls(target.controls);
        }
        return false;
    }
    private boolean checkControls(SearchControls ctls) {
        if ((controls == null || ctls == null)) {
            return ctls == controls;
        }
        return (controls.getSearchScope() == ctls.getSearchScope()) &&
            (controls.getTimeLimit() == ctls.getTimeLimit()) &&
            (controls.getDerefLinkFlag() == ctls.getDerefLinkFlag()) &&
            (controls.getReturningObjFlag() == ctls.getReturningObjFlag()) &&
            (controls.getCountLimit() == ctls.getCountLimit()) &&
            checkStringArrays(controls.getReturningAttributes(),
                ctls.getReturningAttributes());
    }
    private static boolean checkStringArrays(String[] s1, String[] s2) {
        if ((s1 == null) || (s2 == null)) {
            return s1 == s2;
        }
        if (s1.length != s2.length) {
            return false;
        }
        for (int i = 0; i < s1.length; i++) {
            if (!s1[i].equals(s2[i])) {
                return false;
            }
        }
        return true;
    }
    private int sum = -1;
    public int hashCode() {
        if (sum == -1)
            sum = mask + name.hashCode() + filter.hashCode() + controlsCode();
        return sum;
    }
    private int controlsCode() {
        if (controls == null) return 0;
        int total = (int)controls.getTimeLimit() + (int)controls.getCountLimit() +
            (controls.getDerefLinkFlag() ? 1 : 0) +
            (controls.getReturningObjFlag() ? 1 : 0);
        String[] attrs = controls.getReturningAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.length; i++) {
                total += attrs[i].hashCode();
            }
        }
        return total;
    }
}
