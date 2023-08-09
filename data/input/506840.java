public final class AWTPermission extends BasicPermission {
    private static final long serialVersionUID = 8890392402588814465L;
    public AWTPermission(String name, String actions) {
        super(name, actions);
    }
    public AWTPermission(String name) {
        super(name);
    }
}
