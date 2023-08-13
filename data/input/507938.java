public class SecurityChecker extends SecurityManager {
    public boolean enableAccess;
    public Permission checkTarget;
    public boolean checkAsserted;
    public SecurityChecker(Permission target, boolean enable) {
        checkAsserted = false;
        checkTarget = target;
        enableAccess = enable;
    }
    public void checkPermission(Permission p) {
        if (checkTarget.equals(p)) {
            checkAsserted = true;
            if (!enableAccess) {
                throw new SecurityException();
            }
        }
    }
    public SecurityChecker reset() {
        checkAsserted = false;
        return this;
    }
}