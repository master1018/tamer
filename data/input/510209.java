public class HostSideOnlyPackage extends TestPackage {
    public HostSideOnlyPackage(final String testPkgBinaryName, final String version,
            final String androidVersion, final String jarPath,
            final String appPackageName) {
        super(null, testPkgBinaryName, null, null, version,
                androidVersion, jarPath, null, appPackageName);
    }
    @Override
    public boolean isHostSideOnly() {
        return true;
    }
    @Override
    protected void runImpl(final String javaPkgName)
            throws IOException, DeviceDisconnectedException, ADBServerNeedRestartException {
        try {
            if (!mTestStop) {
                Log.d("run in individual mode...");
                runInIndividualMode(javaPkgName);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
    @Override
    protected void runTestImpl(final Test test) throws DeviceDisconnectedException,
                ADBServerNeedRestartException {
        try {
            if (!mTestStop) {
                mCurrentTestSuite = test.getTestSuite();
                mCurrentTestSuite.run(mDevice, test);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
}
