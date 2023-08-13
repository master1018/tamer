public abstract class BasicPermission extends Permission implements
    Serializable {
    private static final long serialVersionUID = 6279438298436773498L;
    public BasicPermission(String name) {
        super(name);
        checkName(name);
    }
    public BasicPermission(String name, String action) {
        super(name);
        checkName(name);
    }
    private final void checkName(String name) {
        if (name == null) {
            throw new NullPointerException(Messages.getString("security.28")); 
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("security.29")); 
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return this.getName().equals(((Permission)obj).getName());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
    @Override
    public String getActions() {
        return ""; 
    }
    @Override
    public boolean implies(Permission permission) {
        if (permission != null && permission.getClass() == this.getClass()) {
            return nameImplies(getName(), permission.getName());
        }
        return false;
    }
    static boolean nameImplies(String thisName, String thatName) {
        if (thisName == thatName) {
            return true;
        }
        int end = thisName.length();
        if (end > thatName.length()) {
            return false;
        }
        if (thisName.charAt(--end) == '*'
            && (end == 0 || thisName.charAt(end - 1) == '.')) {
            end--;
        } else if (end != (thatName.length()-1)) {
            return false;
        }
        for (int i = end; i >= 0; i--) {
            if (thisName.charAt(i) != thatName.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new BasicPermissionCollection();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();
        checkName(this.getName());
    }
}
