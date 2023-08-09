public class NoBroadcastPackageRemovedPermissionTest extends AndroidTestCase {
    private static final String TEST_RECEIVER_PERMISSION = "receiverPermission";
    @SmallTest
    public void testSendOrRemoveStickyBroadcast() {
        try {
            mContext.sendStickyBroadcast(createIntent(Intent.ACTION_WALLPAPER_CHANGED));
            fail("Context.sendStickyBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            mContext.removeStickyBroadcast(createIntent(Intent.ACTION_WALLPAPER_CHANGED));
            fail("Context.removeStickyBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSendBroadcast() {
        try {
            mContext.sendBroadcast(createIntent(Intent.ACTION_PACKAGE_REMOVED));
            fail("Context.sendBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            mContext.sendBroadcast(createIntent(Intent.ACTION_PACKAGE_REMOVED),
                    TEST_RECEIVER_PERMISSION);
            fail("Context.sendBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            mContext.sendOrderedBroadcast(createIntent(Intent.ACTION_PACKAGE_REMOVED),
                    TEST_RECEIVER_PERMISSION, null, null, 0, "initialData", Bundle.EMPTY);
            fail("Context.sendOrderedBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            mContext.sendOrderedBroadcast(createIntent(Intent.ACTION_PACKAGE_REMOVED),
                    TEST_RECEIVER_PERMISSION);
            fail("Context.sendOrderedBroadcast did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    private Intent createIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        return intent;
    }
}
