public abstract class IdentityScope extends Identity {
    private static final long serialVersionUID = -2337346281189773310L;
    private static IdentityScope systemScope;
    protected IdentityScope() {
        super();
    }
    public IdentityScope(String name) {
        super(name);
    }
    public IdentityScope(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }
    public static IdentityScope getSystemScope() {
        if (systemScope == null) {
            String className = AccessController.doPrivileged(new PrivilegedAction<String>(){
                public String run() {
                    return Security.getProperty("system.scope"); 
                }
            });
            if(className != null){
                try {
                    systemScope = (IdentityScope) Class.forName(className).newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return systemScope;
    }
    protected static void setSystemScope(IdentityScope scope) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setSystemScope"); 
        }
        systemScope = scope;
    }
    public abstract int size();
    public abstract Identity getIdentity(String name);
    public Identity getIdentity(Principal principal) {
        return getIdentity(principal.getName());
    }
    public abstract Identity getIdentity(PublicKey key);
    public abstract void addIdentity(Identity identity)
            throws KeyManagementException;
    public abstract void removeIdentity(Identity identity)
            throws KeyManagementException;
    public abstract Enumeration<Identity> identities();
    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("[").append(size()).append("]").toString(); 
    }
}
