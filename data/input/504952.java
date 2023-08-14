public class ActivityManagerPermissionTests extends TestCase {
    IActivityManager mAm;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAm = ActivityManagerNative.getDefault();
    }
    @SmallTest
	public void testREORDER_TASKS() {
        try {
            mAm.moveTaskToFront(-1);
            fail("IActivityManager.moveTaskToFront did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mAm.moveTaskToBack(-1);
            fail("IActivityManager.moveTaskToBack did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mAm.moveTaskBackwards(-1);
            fail("IActivityManager.moveTaskToFront did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
	}
    @SmallTest
    public void testCHANGE_CONFIGURATION() {
        try {
            mAm.updateConfiguration(new Configuration());
            fail("IActivityManager.updateConfiguration did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSET_DEBUG_APP() {
        try {
            mAm.setDebugApp(null, false, false);
            fail("IActivityManager.setDebugApp did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSET_PROCESS_LIMIT() {
        try {
            mAm.setProcessLimit(10);
            fail("IActivityManager.setProcessLimit did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testALWAYS_FINISH() {
        try {
            mAm.setAlwaysFinish(false);
            fail("IActivityManager.setAlwaysFinish did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSIGNAL_PERSISTENT_PROCESSES() {
        try {
            mAm.signalPersistentProcesses(-1);
            fail("IActivityManager.signalPersistentProcesses did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testFORCE_BACK() {
        try {
            mAm.unhandledBack();
            fail("IActivityManager.unhandledBack did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSET_ACTIVITY_WATCHER() {
        try {
            mAm.setActivityController(null);
            fail("IActivityManager.setActivityController did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSHUTDOWN() {
        try {
            mAm.shutdown(0);
            fail("IActivityManager.shutdown did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSTOP_APP_SWITCHES() {
        try {
            mAm.stopAppSwitches();
            fail("IActivityManager.stopAppSwitches did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mAm.resumeAppSwitches();
            fail("IActivityManager.resumeAppSwitches did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
}
