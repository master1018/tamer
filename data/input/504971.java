public final class PrivateCredentialPermission extends Permission {
    private static final long serialVersionUID = 5284372143517237068L;
    private static final String READ = "read"; 
    private String credentialClass;
    private transient int offset;
    private transient CredOwner[] set;
    public PrivateCredentialPermission(String name, String action) {
        super(name);
        if (READ.equalsIgnoreCase(action)) {
            initTargetName(name);
        } else {
            throw new IllegalArgumentException(Messages.getString("auth.11")); 
        }
    }
    PrivateCredentialPermission(String credentialClass, Set<Principal> principals) {
        super(credentialClass);
        this.credentialClass = credentialClass;
        set = new CredOwner[principals.size()];
        for (Principal p : principals) {
            CredOwner element = new CredOwner(p.getClass().getName(), p.getName());
            boolean found = false;
            for (int ii = 0; ii < offset; ii++) {
                if (set[ii].equals(element)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                set[offset++] = element;
            }
        }
    }
    private void initTargetName(String name) {
        if (name == null) {
            throw new NullPointerException(Messages.getString("auth.0E")); 
        }
        name = name.trim();
        if (name.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("auth.0F")); 
        }
        int beg = name.indexOf(' ');
        if (beg == -1) {
            throw new IllegalArgumentException(Messages.getString("auth.10")); 
        }
        credentialClass = name.substring(0, beg);
        beg++;
        int count = 0;
        int nameLength = name.length();
        for (int i, j = 0; beg < nameLength; beg = j + 2, count++) {
            i = name.indexOf(' ', beg);
            j = name.indexOf('"', i + 2);
            if (i == -1 || j == -1 || name.charAt(i + 1) != '"') {
                throw new IllegalArgumentException(Messages.getString("auth.10")); 
            }
        }
        if (count < 1) {
            throw new IllegalArgumentException(Messages.getString("auth.10")); 
        }
        beg = name.indexOf(' ');
        beg++;
        String principalClass;
        String principalName;
        set = new CredOwner[count];
        for (int index = 0, i, j; index < count; beg = j + 2, index++) {
            i = name.indexOf(' ', beg);
            j = name.indexOf('"', i + 2);
            principalClass = name.substring(beg, i);
            principalName = name.substring(i + 2, j);
            CredOwner element = new CredOwner(principalClass, principalName);
            boolean found = false;
            for (int ii = 0; ii < offset; ii++) {
                if (set[ii].equals(element)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                set[offset++] = element;
            }
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        initTargetName(getName());
    }
    public String[][] getPrincipals() {
        String[][] s = new String[offset][2];
        for (int i = 0; i < s.length; i++) {
            s[i][0] = set[i].principalClass;
            s[i][1] = set[i].principalName;
        }
        return s;
    }
    @Override
    public String getActions() {
        return READ;
    }
    public String getCredentialClass() {
        return credentialClass;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < offset; i++) {
            hash = hash + set[i].hashCode();
        }
        return getCredentialClass().hashCode() + hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        PrivateCredentialPermission that = (PrivateCredentialPermission) obj;
        return credentialClass.equals(that.credentialClass) && (offset == that.offset)
                && sameMembers(set, that.set, offset);
    }
    @Override
    public boolean implies(Permission permission) {
        if (permission == null || this.getClass() != permission.getClass()) {
            return false;
        }
        PrivateCredentialPermission that = (PrivateCredentialPermission) permission;
        if (!("*".equals(credentialClass) || credentialClass 
                .equals(that.getCredentialClass()))) {
            return false;
        }
        if (that.offset == 0) {
            return true;
        }
        CredOwner[] thisCo = set;
        CredOwner[] thatCo = that.set;
        int thisPrincipalsSize = offset;
        int thatPrincipalsSize = that.offset;
        for (int i = 0, j; i < thisPrincipalsSize; i++) {
            for (j = 0; j < thatPrincipalsSize; j++) {
                if (thisCo[i].implies(thatCo[j])) {
                    break;
                }
            }
            if (j == thatCo.length) {
                return false;
            }
        }
        return true;
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return null;
    }
    private boolean sameMembers(Object[] ar1, Object[] ar2, int length) {
        if (ar1 == null && ar2 == null) {
            return true;
        }
        if (ar1 == null || ar2 == null) {
            return false;
        }
        boolean found;
        for (int i = 0; i < length; i++) {
            found = false;
            for (int j = 0; j < length; j++) {
                if (ar1[i].equals(ar2[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    private static final class CredOwner implements Serializable {
        private static final long serialVersionUID = -5607449830436408266L;
        String principalClass;
        String principalName;
        private transient boolean isClassWildcard;
        private transient boolean isPNameWildcard;
        CredOwner(String principalClass, String principalName) {
            super();
            if ("*".equals(principalClass)) { 
                isClassWildcard = true;
            }
            if ("*".equals(principalName)) { 
                isPNameWildcard = true;
            }
            if (isClassWildcard && !isPNameWildcard) {
                throw new IllegalArgumentException(Messages.getString("auth.12")); 
            }
            this.principalClass = principalClass;
            this.principalName = principalName;
        }
        boolean implies(Object obj) {
            if (obj == this) {
                return true;
            }
            CredOwner co = (CredOwner) obj;
            if (isClassWildcard || principalClass.equals(co.principalClass)) {
                if (isPNameWildcard || principalName.equals(co.principalName)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean equals(Object obj) {
            return principalClass.equals(((CredOwner) obj).principalClass)
                    && principalName.equals(((CredOwner) obj).principalName);
        }
        @Override
        public int hashCode() {
            return principalClass.hashCode() + principalName.hashCode();
        }
    }
}
