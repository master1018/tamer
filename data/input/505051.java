public class PriviAction<T> implements PrivilegedAction<T> {
    private Object arg1;
    private Object arg2;
    private int action;
    private static final int GET_SYSTEM_PROPERTY = 1;
    private static final int GET_SECURITY_POLICY = 2;
    private static final int SET_ACCESSIBLE = 3;
    private static final int GET_SECURITY_PROPERTY = 4;
    public static PrivilegedAction<String> getSecurityProperty(String property) {
        return new PriviAction<String>(GET_SECURITY_PROPERTY, property);
    }
    private PriviAction(int action, Object arg) {
        this.action = action;
        this.arg1 = arg;
    }
    public PriviAction() {
        action = GET_SECURITY_POLICY;
    }
    public PriviAction(AccessibleObject object) {
        action = SET_ACCESSIBLE;
        arg1 = object;
    }
    public PriviAction(String property) {
        action = GET_SYSTEM_PROPERTY;
        arg1 = property;
    }
    public PriviAction(String property, String defaultAnswer) {
        action = GET_SYSTEM_PROPERTY;
        arg1 = property;
        arg2 = defaultAnswer;
    }
    @SuppressWarnings("unchecked")
    public T run() {
        switch (action) {
        case GET_SYSTEM_PROPERTY:
            return (T)System.getProperty((String) arg1, (String) arg2);
        case GET_SECURITY_PROPERTY:
            return (T)Security.getProperty((String) arg1);
        case GET_SECURITY_POLICY:
            return (T)Policy.getPolicy();
        case SET_ACCESSIBLE:
            ((AccessibleObject) arg1).setAccessible(true);
        }
        return null;
    }
}
