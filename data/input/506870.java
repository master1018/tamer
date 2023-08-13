public class ServiceManagerPermissionTests extends TestCase {
    @SmallTest
	public void testAddService() {
        try {
            Binder binder = new Binder();
            ServiceManager.addService("activity", binder);
            fail("ServiceManager.addService did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
	}
    @SmallTest
    public void testSetPermissionController() {
        try {
            IPermissionController pc = new IPermissionController.Stub() {
                public boolean checkPermission(java.lang.String permission, int pid, int uid) {
                    return true;
                }
            };
            ServiceManagerNative.asInterface(BinderInternal.getContextObject())
                    .setPermissionController(pc);
            fail("IServiceManager.setPermissionController did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
}
