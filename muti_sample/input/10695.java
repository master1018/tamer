public class TestSecurityManager extends SecurityManager {
    public TestSecurityManager() {
    }
    public void checkListen(int port) {
        System.exit(1);
    }
    public void checkExit(int status) {
    }
}
