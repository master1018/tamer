public class MyGuard implements Guard, Serializable {
    private static final long serialVersionUID = 5767944725614823373L;
    final boolean enabled;
    public MyGuard(boolean state) {
        enabled = state;
    }
    public void checkGuard(Object object) {
        if (!enabled) {
            throw new SecurityException();
        }
    }
}
