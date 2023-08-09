public class EasyMockSupport {
    protected final List<IMocksControl> controls = new ArrayList<IMocksControl>(
            5);
    public <T> T createStrictMock(Class<T> toMock) {
        return createStrictControl().createMock(toMock);
    }
    public <T> T createStrictMock(String name, Class<T> toMock) {
        return createStrictControl().createMock(name, toMock);
    }
    public <T> T createMock(Class<T> toMock) {
        return createControl().createMock(toMock);
    }
    public <T> T createMock(String name, Class<T> toMock) {
        return createControl().createMock(name, toMock);
    }
    public <T> T createNiceMock(Class<T> toMock) {
        return createNiceControl().createMock(toMock);
    }
    public <T> T createNiceMock(String name, Class<T> toMock) {
        return createNiceControl().createMock(name, toMock);
    }
    public IMocksControl createStrictControl() {
        IMocksControl ctrl = EasyMock.createStrictControl();
        controls.add(ctrl);
        return ctrl;
    }
    public IMocksControl createControl() {
        IMocksControl ctrl = EasyMock.createControl();
        controls.add(ctrl);
        return ctrl;
    }
    public IMocksControl createNiceControl() {
        IMocksControl ctrl = EasyMock.createNiceControl();
        controls.add(ctrl);
        return ctrl;
    }
    public void replayAll() {
        for (IMocksControl c : controls) {
            c.replay();
        }
    }
    public void resetAll() {
        for (IMocksControl c : controls) {
            c.reset();
        }
    }
    public void verifyAll() {
        for (IMocksControl c : controls) {
            c.verify();
        }
    }
    public void resetAllToNice() {
        for (IMocksControl c : controls) {
            c.resetToNice();
        }
    }
    public void resetAllToDefault() {
        for (IMocksControl c : controls) {
            c.resetToDefault();
        }
    }
    public void resetAllToStrict() {
        for (IMocksControl c : controls) {
            c.resetToStrict();
        }
    }
}
