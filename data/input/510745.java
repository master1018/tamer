class MockSecurityManager extends SecurityManager {
    String validHost = null;
    int validPort = -1;
    public boolean checkAcceptCalled = false;
    public boolean checkConnectCalled = false;
    MockSecurityManager() {
        super();
        this.validHost = null;
    }
    MockSecurityManager(String host) {
        super();
        this.validHost = host;
    }
    MockSecurityManager(int port) {
        super();
        this.validPort = port;
    }
    public void checkPermission(Permission perm) {
    }
    public void checkPermission(Permission perm, Object context) {
    }
    public void checkConnect(String host, int port) {
        checkConnectCalled = true;
        if (null != this.validHost) {
            if (!this.validHost.equals(host)) {
                throw new SecurityException();
            }
        }
        if ("127.0.0.1".equals(host)) {
            return;
        }
        super.checkConnect(host, port);
    }
    public void checkAccept(String host, int port) {
        checkAcceptCalled = true;
        if (null != this.validHost) {
            if (!this.validHost.equals(host)) {
                throw new SecurityException();
            }
        }
        if (-1 != this.validPort) {
            if (this.validPort != port) {
                throw new SecurityException();
            }
        }
        if ("127.0.0.1".equals(host)) {
            return;
        }
        super.checkAccept(host, port);
    }
}