public abstract class SdkTestCase extends TestCase {
    private Sdk mSdk;
    protected SdkTestCase() {
    }
    protected Sdk getSdk() {
        if (mSdk == null) {
            mSdk = loadSdk();
            assertNotNull(mSdk);
            validateSdk(mSdk);
        }
        return mSdk;
    }
    private Sdk loadSdk() {
        AdtPlugin adt = AdtPlugin.getDefault();
        if (adt == null) {
            return null;
        }
        Object sdkLock = Sdk.getLock();
        LoadStatus loadStatus = LoadStatus.LOADING;
        final int maxWait = 600;
        for (int i=0; i < maxWait && loadStatus == LoadStatus.LOADING; i++) {
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
            }
            synchronized(sdkLock) {
                loadStatus = adt.getSdkLoadStatus();
            }
        }
        Sdk sdk = null;
        synchronized(sdkLock) {
            assertEquals(LoadStatus.LOADED, loadStatus);
            sdk = Sdk.getCurrent();
        }
        assertNotNull(sdk);
        return sdk;
    }
    private void validateSdk(Sdk sdk) {
        assertTrue("sdk has no targets", sdk.getTargets().length > 0);
        for (IAndroidTarget target : sdk.getTargets()) {
            IStatus status = new AndroidTargetParser(target).run(new NullProgressMonitor());
            if (status.getCode() != IStatus.OK) {
                fail("Failed to parse targets data");
            }
        }
    }
}
