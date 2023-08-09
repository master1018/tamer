public final class AuthPermission extends BasicPermission {
    private static final long serialVersionUID = 5806031445061587174L;
    private static final String CREATE_LOGIN_CONTEXT = "createLoginContext"; 
    private static final String CREATE_LOGIN_CONTEXT_ANY = "createLoginContext.*"; 
    private static String init(String name) {
        if (name == null) {
            throw new NullPointerException(Messages.getString("auth.13")); 
        }
        if (CREATE_LOGIN_CONTEXT.equals(name)) {
            return CREATE_LOGIN_CONTEXT_ANY;
        }
        return name;
    }
    public AuthPermission(String name) {
        super(init(name));
    }
    public AuthPermission(String name, String actions) {
        super(init(name), actions);
    }
}